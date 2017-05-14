package org.apache.axis2.transport.amqp.out;

import java.util.UUID;

import org.apache.axis2.AxisFault;
import org.apache.axis2.transport.amqp.builders.MessageBuilder;
import org.apache.axis2.transport.amqp.common.Utils;
import org.apache.axis2.transport.amqp.connection.ChannelHolder;
import org.apache.axis2.transport.amqp.connection.Context;
import org.apache.axis2.transport.amqp.data.Message;
import org.apache.axis2.transport.base.MetricsCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.AMQP.BasicProperties;

public class ContextSender {
    private static Logger logger = LoggerFactory.getLogger(ContextSender.class);
    
    public static void send(Message message, Context context, MetricsCollector metricsCollector) throws AxisFault {
        try {
            BasicProperties.Builder builder = MessageBuilder.buildBasicProperties(message);
            Integer deliveryMode = context.getConfiguration().getDeliveryMode();
            if (deliveryMode != null) {
                builder.deliveryMode(deliveryMode);
            }
            BasicProperties basicProperties = builder.build();
            
            String routeKey = context.getUrl().getRoutingKey();
            String exchangeType = context.getConfiguration().getExchangeType();
            if (exchangeType != null && exchangeType.equals("x-consistent-hash")) {
                routeKey = UUID.randomUUID().toString();
            }
            
            byte[] body = message.getBody();
            String exchangeName = context.getUrl().getExchange();
            
            //consider applying pool of channels to minimize synchronization
            final ChannelHolder channel = context.getChannel(null);
            try {
                logger.info("publish.exchange=" + exchangeName + ",routeKey=" + routeKey);
                logger.info("data=" + new String(body));
                logger.info("props=" + basicProperties);
                channel.get().basicPublish(exchangeName, routeKey, basicProperties, body);
                if (metricsCollector != null) {
                    metricsCollector.notifySentMessageSize(body.length);
                    metricsCollector.incrementBytesSent(body.length);
                    metricsCollector.incrementMessagesSent();
                }
            } finally {
                Utils.closeQuietly(channel);
            }
        } catch (Exception e) {
            if (metricsCollector != null) {
                metricsCollector.incrementFaultsSending();
            }
//            context.close();
            throw new AxisFault(e.getMessage(), e);
        }
    }
    
}
