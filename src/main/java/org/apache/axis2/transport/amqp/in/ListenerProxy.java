package org.apache.axis2.transport.amqp.in;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.mail.internet.ContentType;
import javax.mail.internet.ParseException;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.AxisFault;
import org.apache.axis2.builder.ApplicationXMLBuilder;
import org.apache.axis2.builder.Builder;
import org.apache.axis2.builder.BuilderUtil;
import org.apache.axis2.builder.SOAPBuilder;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.format.PlainTextBuilder;
import org.apache.axis2.transport.TransportUtils;
import org.apache.axis2.transport.amqp.common.Constants;
import org.apache.axis2.transport.amqp.data.Message;
import org.apache.axis2.transport.amqp.out.TransportInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ListenerProxy {
    private static final Logger logger = LoggerFactory.getLogger(ListenerProxy.class);
    private static final Builder soapBuilder = new SOAPBuilder();
    private static final Builder xmlBuilder = new ApplicationXMLBuilder();
    private static final Builder plainTextBuilder = new PlainTextBuilder();
    
    private final Endpoint endpoint;

    public ListenerProxy(Endpoint endpoint) {
        this.endpoint = endpoint;
    }

    public void messageReceived(Message message, TransportInfo transportInfo) throws AxisFault {
        MessageContext ctx = endpoint.createMessageContext();
        
        final String messageId = message.getMessageId();
        ctx.setMessageID(messageId);
        
        String correlationId = message.getCorrelationId();
        if (correlationId == null || correlationId.isEmpty()) {
            correlationId = messageId; 
        }
        ctx.setProperty(Constants.CORRELATION_ID, correlationId);
        
        String contentType = message.getContentType();
        if (contentType == null || contentType.isEmpty()) {
            contentType = Constants.DEFAULT_CONTENT_TYPE;
            logger.error("content type not set for message " + messageId + " assuming " + contentType);
        }
        ctx.setProperty(Constants.CONTENT_TYPE, contentType);
        
        String contentEncoding = message.getContentEncoding();
        if (contentEncoding != null) {
            ctx.setProperty(Constants.CONTENT_ENCODING, contentEncoding);
        }
        
        if (transportInfo != null) {
            ctx.setProperty(Constants.OUT_TRANSPORT_INFO, transportInfo);
        }
        if (message.getReplyTo() != null) {
            logger.error("Unsupported replyTo. This implementation uses fixed destination for replies");
        }
        
        final ContentType typedContentType;
        try {
            typedContentType = new ContentType(contentType);
        } catch (ParseException e) {
            throw new AxisFault(e.getMessage(), e);
        }
        
        String charset = typedContentType.getParameter("charset");
        ctx.setProperty(Constants.CHARACTER_SET_ENCODING, charset);

        String type = typedContentType.getBaseType();
        SOAPEnvelope envelope = pack(message.getBody(), type, ctx, contentType);
        ctx.setEnvelope(envelope);
        
        String soapAction = message.getSoapAction();
        Map<String, Object> headers = getTransportHeaders(message);
        if (transportInfo.getAckReference() != null) {
            logger.info("message receiced, tag: " + transportInfo.getAckReference().getTag() + ", proxy name: " + transportInfo.getEndpoint().getProxyName());
        } else {
            logger.info("message receiced, autoack, proxy name: " + transportInfo.getEndpoint().getProxyName());
        }
        endpoint.getListener().handleIncomingMessage(ctx, headers, soapAction, contentType);
    }
    
    private static final Map<String, Object> getTransportHeaders(Message message) {
        Map<String, Object> map = new HashMap<String, Object>();

        if (message.getCorrelationId() != null) {
            map.put(Constants.CORRELATION_ID, message.getCorrelationId());
        }

        if (message.getMessageId() != null) {
            map.put(Constants.MESSAGE_ID, message.getMessageId());
        }

        if (message.getReplyTo() != null) {
            map.put(Constants.REPLY_TO, message.getReplyTo());
        }

        Map<String, Object> headers = message.getHeaders();
        if (headers != null && !headers.isEmpty()) {
            map.putAll(headers);
        }

        return map;
    }

    private static final SOAPEnvelope pack(byte[] data, String type, MessageContext ctx, String contentType) throws AxisFault {
        Builder axisBuilder = BuilderUtil.getBuilderFromSelector(type, ctx);
        if (axisBuilder == null) {
            logger.warn("unable to find Builder by contentType=" + type + ". It's strongly recommended to add appropriate messageBuilder to axis2.xml");
        }
        SOAPEnvelope result = pack(data, ctx, contentType, axisBuilder, xmlBuilder, soapBuilder, plainTextBuilder);
        return result;
    }
    
    private static final SOAPEnvelope pack(byte[] data, MessageContext ctx, String contentType, Builder... builders) throws AxisFault {
        Exception lastException = null;
        for (Builder builder : builders) {
            if (builder != null) {
                try {
                    SOAPEnvelope result = pack(data, ctx, contentType, builder);
                    return result;
                } catch (Exception e) {
                    if (logger.isDebugEnabled()) {
                        logger.debug(e.getMessage(), e);
                    } else {
                        logger.warn("Parse failed for " + contentType + " by builder " + builder.getClass());
                    }
                    lastException = e;
                }
            }
        }
        throw new AxisFault(lastException.getMessage(), lastException);
    }

    private static final SOAPEnvelope pack(byte[] data, MessageContext ctx, String contentType, Builder builder) throws AxisFault {
        ByteArrayInputStream bis = new ByteArrayInputStream(data);
        try {
            final OMElement documentElement = builder.processDocument(bis, contentType, ctx);
            logger.info("doc=" + documentElement);
            SOAPEnvelope result = TransportUtils.createSOAPEnvelope(documentElement);
            logger.info("env=" + result);
            return result;
        } finally {
            try {
                bis.close();
            } catch (IOException e) {
                throw new AxisFault(e.getMessage(), e);
            }
        }
    }
}
