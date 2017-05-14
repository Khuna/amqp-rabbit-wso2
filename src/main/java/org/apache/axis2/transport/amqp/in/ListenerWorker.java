package org.apache.axis2.transport.amqp.in;

import java.util.concurrent.CountDownLatch;

import org.apache.axis2.transport.amqp.builders.MessageBuilder;
import org.apache.axis2.transport.amqp.common.Constants;
import org.apache.axis2.transport.amqp.common.Utils;
import org.apache.axis2.transport.amqp.connection.ChannelHolder;
import org.apache.axis2.transport.amqp.connection.Context;
import org.apache.axis2.transport.amqp.data.Configuration;
import org.apache.axis2.transport.amqp.data.Message;
import org.apache.axis2.transport.amqp.data.RabbitUrl;
import org.apache.axis2.transport.amqp.out.ContextSender;
import org.apache.axis2.transport.amqp.out.TransportInfo;
import org.apache.axis2.transport.amqp.out.ref.AckReference;
import org.apache.axis2.transport.amqp.out.ref.ChannelReferenceStore;
import org.apache.axis2.transport.base.MetricsCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.QueueingConsumer.Delivery;

public class ListenerWorker implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(ListenerWorker.class);
    private final Endpoint parent;
    private volatile boolean stopRequested = false;
    private volatile boolean stopped = false;
    private CountDownLatch waitEvent;
    private CountDownLatch finishedEvent;
    private volatile CountDownLatch pausedEvent = null;
    private final Context context;
    private final Context replyToContext;
    private final Context faultContext;
    
    public ListenerWorker(Endpoint parent, Context context, Context replyToContext, Context faultContext) {
        this.parent = parent;
        this.context = context;
        this.replyToContext = replyToContext;
        this.faultContext = faultContext;
    }
    
    public void markStopping() {
        stopRequested = true;
    }
    
    public boolean stop() {
        if (stopped) {
            return true;
        } else {
            stopRequested = true;
            signalWaitFinished();
            waitFinishQuietly();
            return stopped;
        }
    }
    
    public void starting() {
        stopRequested = false;
        stopped = false;
        waitEvent = new CountDownLatch(1);
        finishedEvent = new CountDownLatch(1);
    }
    
    public void pauseUntil(CountDownLatch pausedEvent) {
        this.pausedEvent = pausedEvent;
    }
    
    public void run() {
        logger.info("started");
        final MetricsCollector metricsCollector = parent.getListener().getMetricsCollector();
        final Configuration configuration = context.getConfiguration();
        final RabbitUrl url = context.getUrl();
        final boolean autoAck = configuration.getAutoAck();
        final String queueName = url.getQueue();
        final ListenerProxy proxy = parent.getProxy();
        final boolean replyToEnabled = configuration.getReceiverReplyToEnabled();
        final ChannelReferenceStore channelReferenceStore = parent.getChannelReferenceStore();
        final Context replyToContext = replyToEnabled ? this.replyToContext : null;
        int failCount = 0;
        ChannelHolder channelHolder = null;
        try {
            while (!stopRequested) {
                boolean stoppedReportingException = false;
                try {
                    channelHolder = context.getChannel(channelHolder);
                    Channel channel = channelHolder.get();
                    if (stoppedReportingException) {
                        logger.info("connection restored");
                        stoppedReportingException = false;
                    }
                    QueueingConsumer consumer = new QueueingConsumer(channel);
                    channel.basicConsume(queueName, autoAck, Constants.CONSUMER_TAG, consumer);
                    while (!stopRequested) {
                        Delivery delivery = consumer.nextDelivery(getWaitMessageTimeout());
                        if (delivery != null) {
                            byte[] body = delivery.getBody();
                            if (body != null) {
                                int bodyLength = body.length;
                                metricsCollector.notifyReceivedMessageSize(bodyLength);
                                metricsCollector.incrementBytesReceived(bodyLength);
                            }
                            Message message = MessageBuilder.build(delivery);
                            final AckReference ackReference;
                            if (!autoAck) {
                                ackReference = new AckReference(channel, channelReferenceStore, delivery.getEnvelope().getDeliveryTag());
                                logger.info("store after add " + channelReferenceStore);
                            } else {
                                ackReference = null;
                            }
                            final TransportInfo transportInfo;
                            transportInfo = new TransportInfo(replyToContext, faultContext, ackReference, parent);

                            try {
                                proxy.messageReceived(message, transportInfo);
                            } catch (Exception e) {
                                logger.error(e.getMessage(), e);
                                metricsCollector.incrementFaultsReceiving();
                                if (faultContext != null) {
                                    try {
                                        ContextSender.send(message, faultContext, metricsCollector);
                                    } catch (Exception ex) {
                                        logger.error("unable to send to fault queue " + ex.getMessage(), ex);
                                    }
                                } else {
                                    logger.warn("fault context not configured, purging message");
                                }
                                if (!autoAck) {
                                    channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                                }
                            }
                            metricsCollector.incrementMessagesReceived();
                        }
                        checkPaused();
                    }
                } catch (Exception e) {
                    metricsCollector.incrementFaultsReceiving();
                    failCount++;
                    if (failCount == 1) {
                        logger.error(e.getMessage(), e);
                    } else if (failCount > 5) {
                        parent.restart();
                    } else if (failCount < 5) {
                        logger.warn(e.getMessage());
                    } else if (failCount == 5) {
                        logger.warn("Stopped reporting further connection problems until first reconnect");
                        stoppedReportingException = true;
                    }
                    handleRabbitException();
                }
                checkPaused();
            }
        } finally {
            Utils.closeQuietly(channelHolder);
        }
        stopped = true;
        signalThreadFinished();
        logger.info("stopped");    }

    private void checkPaused() {
        CountDownLatch pauseEvent = this.pausedEvent;
        if (pauseEvent != null) {
            Utils.waitQuietly(pauseEvent, 0);
        }
    }
    
    private void handleRabbitException() {
        waitReconnectQuietly();
    }
    
    private void signalThreadFinished() {
        finishedEvent.countDown();
    }
    
    private void signalWaitFinished() {
        waitEvent.countDown();
    }

    private void waitFinishQuietly() {
        Utils.waitQuietly(finishedEvent, getFinishTimeout());
    }

    private Long getFinishTimeout() {
        return context.getConfiguration().getFinishTimeoutMillis();
    }

    private Long getReconnectInterval() {
        return context.getConfiguration().getReconnectIntervalMillis();
    }
    
    private Long getWaitMessageTimeout() {
        return context.getConfiguration().getWaitMessageTimeoutMillis();
    }
    
    
    private void waitReconnectQuietly() {
        Utils.waitQuietly(waitEvent, getReconnectInterval());
    }
    
}
