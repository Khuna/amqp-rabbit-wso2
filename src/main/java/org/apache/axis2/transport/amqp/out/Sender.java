package org.apache.axis2.transport.amqp.out;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.transport.OutTransportInfo;
import org.apache.axis2.transport.TransportUtils;
import org.apache.axis2.transport.amqp.builders.ConfigurationParser;
import org.apache.axis2.transport.amqp.builders.MessageBuilder;
import org.apache.axis2.transport.amqp.builders.RabbitUrlParser;
import org.apache.axis2.transport.amqp.common.Utils;
import org.apache.axis2.transport.amqp.connection.Context;
import org.apache.axis2.transport.amqp.data.Configuration;
import org.apache.axis2.transport.amqp.data.ContextKey;
import org.apache.axis2.transport.amqp.data.Message;
import org.apache.axis2.transport.amqp.data.RabbitUrl;
import org.apache.axis2.transport.amqp.out.cache.SenderConfigurationCache;
import org.apache.axis2.transport.amqp.out.ref.AckReference;
import org.apache.axis2.transport.base.AbstractTransportSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Sender extends AbstractTransportSender implements CalcSenderConfigurationCallback {
    private static final Logger logger = LoggerFactory.getLogger(Sender.class);
    private Map<String, Configuration> configurations;
    private SenderConfigurationCache configurationCache = new SenderConfigurationCache(this);
    private String transportName;
    
    @Override
    public void init(ConfigurationContext cfgCtx, TransportOutDescription transportOut) throws AxisFault {
        super.init(cfgCtx, transportOut);
        this.configurations = ConfigurationParser.parseList(transportOut.getParameters(), false);
        this.transportName = getTransportName();
        logger.info("init.transportName=" + transportName + ",conf=" + configurations);
    }
    
    @Override
    public void stop() {
        super.stop();
        configurationCache.clear();
    }
    
    @Override
    public void sendMessage(MessageContext ctx, String url, OutTransportInfo outTransportInfo) throws AxisFault {
        boolean sync = waitForSynchronousResponse(ctx);
        logger.info("sendMessage.url=" + url + ",outTransportInfo=" + outTransportInfo + ",sync=" + sync);
        final Context context;
        final Context faultContext;
        final String contentType;
        String proxyName;
        final boolean sendReply;
        AckReference ackReference;
        if (url != null) {
            ContextKey key = calcConfiguration(ctx, url);
            context = configurationCache.get(key);
            faultContext = null;
            logger.info("url=" + context.getUrl());
            contentType = null;
            sendReply = true;
            ackReference = null;
            proxyName = "";
        } else if (outTransportInfo != null) {
            TransportInfo transportInfo = (TransportInfo) outTransportInfo;
            ackReference = transportInfo.getAckReference();
            context = transportInfo.getcontext();
            faultContext = transportInfo.getFaultContext();
            contentType = transportInfo.getContentType();
            sendReply = false;
            proxyName = transportInfo.getEndpoint().getProxyName();
            logger.info("context=" + context + ",contentType=" + contentType);
        } else {
            throw new AxisFault("neither url nor outTransportInfo specified");
        }

        int faultCount = 0;
        while (true) {
            try {
                checkResult(context, faultContext, ackReference, ctx, proxyName, contentType);
                if (context != null) {
                    if (context.getConfiguration().getResponseEnabled(sync) && sendReply) {
                        reportAccepted(ctx);
                    }
                }
                break;
            } catch (AxisFault axisFault) {
                faultCount++;
                if (faultCount > 5) {
                    break;
                }
            }
        }
    }

    private void checkResult(Context context, Context faultContext, AckReference ackReference, MessageContext ctx, String proxyName, String contentType) throws AxisFault {
        Boolean isFault = isFaultMessage(ctx);
        Integer customHeader = Integer.parseInt(getHeader(ctx, "X-Rabbit-Response"));
        Boolean autoAck;
        if (ackReference == null) {
            autoAck = true;
        } else {
            autoAck = false;
        }

        if (isFault) {
            Utils.waitQuietly(new CountDownLatch(1), 2000);
        }

        if (autoAck == true) {
            if (customHeader == 200) {
            }else if (customHeader == 308) {
                Message message = MessageBuilder.build(ctx, contentType);
                if (context != null) {
                    doSendMessage(context, message, proxyName);
                }
                if (faultContext != null) {
                    doSendMessage(faultContext, message, proxyName);
                }
            }else if (customHeader == 406) {
                Message message = MessageBuilder.build(ctx, contentType);
                if (faultContext != null) {
                    doSendMessage(faultContext, message, proxyName);
                }
            }else if (customHeader == 503) {
            }
        }else {
            if (customHeader == 200) {
                doAck(ackReference, proxyName);
            }else if (customHeader == 308) {
                doAck(ackReference, proxyName);
                Message message = MessageBuilder.build(ctx, contentType);
                if (context != null) {
                    doSendMessage(context, message, proxyName);
                }
                if (faultContext != null) {
                    doSendMessage(faultContext, message, proxyName);
                }
            }else if (customHeader == 406) {
                doAck(ackReference, proxyName);
                Message message = MessageBuilder.build(ctx, contentType);
                if (faultContext != null) {
                    doSendMessage(faultContext, message, proxyName);
                }
            }else if (customHeader == 503) {
                doNoAck(ackReference, proxyName);
            }
        }
    }

    private Boolean isFaultMessage(MessageContext ctx) {
        Boolean isFaultMessage = false;
        if (ctx.getProperty("FAULT_MESSAGE") != null) {
            isFaultMessage = Boolean.parseBoolean((String) ctx.getProperty("FAULT_MESSAGE"));
        }

        return isFaultMessage;
    }

    private String getHeader(MessageContext ctx, String key) {
        try {
            String result = (String)((Map)ctx.getProperty("TRANSPORT_HEADERS")).get(key);
            if (result != null) {
                return result;
            }else {
                return "200";
            }
        } catch (Exception e ) {
            return "200";
        }
    }

    private void doAck(AckReference ackReference, String proxyName) throws AxisFault{
        try {
            ackReference.ack();
            logger.info("message acknowledge, tag: " + ackReference.getTag() + ", proxy name: " + proxyName);
        } catch (IOException e) {
            throw new AxisFault("exception while acknowleging, source message " + e.getMessage(), e);
        }
    }

    private void doNoAck(AckReference ackReference, String proxyName) throws AxisFault{
        try {
            ackReference.nack();;
            logger.info("message not acknowledge, tag: " + ackReference.getTag() + ", proxy name: " + proxyName);
        } catch (IOException e) {
            throw new AxisFault("exception while not acknowleging, source message " + e.getMessage(), e);
        }
    }

    private void doSendMessage(Context context, Message message, String proxyName) throws AxisFault{
        try {
            ContextSender.send(message, context, metrics);
        }catch (Exception e) {
            logger.info("Exception while sending message, proxy name: " + proxyName);
        }
    }

    private void reportAccepted(MessageContext requestContext) throws AxisFault {
        MessageContext responseContext = createResponseMessageContext(requestContext);
        logger.info("sending  reply=" + responseContext);
        SOAPEnvelope responseEnvelope = TransportUtils.createSOAPEnvelope(null);
        responseContext.setEnvelope(responseEnvelope);
        handleIncomingMessage(responseContext, Collections.emptyMap(), "envelope", "application/xml");
        logger.info("done");
    }
    
    private ContextKey calcConfiguration(MessageContext ctx, String url) throws AxisFault {
        String serviceName = ctx.getServiceContext().getName();
        String transportName = this.transportName;
        RabbitUrl rabbitUrl = RabbitUrlParser.parseSingle(url, transportName);
        
        String configurationName = rabbitUrl.getConfigurationName();
        Configuration configuration = Utils.getConfiguration(configurationName, configurations);
        
        String senderUriStr = configuration.getSenderUri();
        RabbitUrl senderUri = RabbitUrlParser.parseSingle(senderUriStr, transportName);
        rabbitUrl.mergeFrom(senderUri, true);
        RabbitUrlParser.parseFinished(rabbitUrl, transportName, serviceName);

        configurationName = rabbitUrl.getConfigurationName();
        configuration = Utils.getConfiguration(configurationName, configurations);
        
        ContextKey result = new ContextKey(rabbitUrl, configuration);
        return result;
    }

    public Context calc(ContextKey key) throws AxisFault {
        logger.info("creating new context");
        return new Context(key.getUrl(), key.getConfiguration());
    }

}
