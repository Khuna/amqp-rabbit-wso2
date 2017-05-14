package org.apache.axis2.transport.amqp.builders;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.gitblit.utils.ArrayUtils;
import org.apache.axiom.om.OMOutputFormat;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.MessageFormatter;
import org.apache.axis2.transport.amqp.common.Constants;
import org.apache.axis2.transport.amqp.data.Message;
import org.apache.axis2.transport.base.BaseUtils;
import org.apache.axis2.util.MessageProcessorSelector;
import org.apache.commons.io.output.ByteArrayOutputStream;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.QueueingConsumer;
import org.apache.poi.util.ArrayUtil;

public class MessageBuilder {
    public static Message build(QueueingConsumer.Delivery delivery) {
        Message result = new Message();
        BasicProperties properties = delivery.getProperties();
        Map<String, Object> headers = properties.getHeaders();
        result.setBody(delivery.getBody());
        result.setDeliveryTag(delivery.getEnvelope().getDeliveryTag());
        result.setReplyTo(properties.getReplyTo());
        result.setMessageId(properties.getMessageId());
        result.setContentType(properties.getContentType());
        result.setContentEncoding(properties.getContentEncoding());
        result.setCorrelationId(properties.getCorrelationId());
        if (headers != null) {
            result.setHeaders(headers);
            Object soapAction = headers.get(Constants.SOAP_ACTION);
            String soapActionStr = null;
            if (soapAction != null) {
                soapActionStr = soapAction.toString();
            } else {
                soapActionStr = Constants.DEFAULT_SOAP_ACTION;
            }
            result.setSoapAction(soapActionStr);
        }
        return result;
    }
    
    public static Message build(MessageContext ctx, String contentType) throws AxisFault {
        Message result = new Message();
        result.setSoapAction(ctx.getSoapAction());
        result.setMessageId(ctx.getMessageID());
        if (contentType != null) {
            result.setContentType(contentType);
        } else {
            result.setContentType((String) ctx.getProperty(Constants.CONTENT_TYPE));
        }
        result.setCorrelationId((String) ctx.getProperty(Constants.CORRELATION_ID));
        result.setContentEncoding((String) ctx.getProperty(Constants.CONTENT_ENCODING));
        @SuppressWarnings("unchecked")
        Map<String, Object> headers = (Map<String, Object>) ctx.getProperty(MessageContext.TRANSPORT_HEADERS); 
        result.setHeaders(headers);
        byte[] body = formatBody(ctx);
        result.setBody(body);
        return result;
    }
    
    private static byte[] formatBody(MessageContext ctx) throws AxisFault {
        OMOutputFormat format = BaseUtils.getOMOutputFormat(ctx);
        MessageFormatter messageFormatter = MessageProcessorSelector.getMessageFormatter(ctx);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            messageFormatter.writeTo(ctx, format, bos, false);
        } finally {
            try {
                bos.close();
            } catch (IOException e) {
                throw new AxisFault(e.getMessage(), e);
            }
        }
        byte[] body = bos.toByteArray();
        return body;
    }

    public static BasicProperties.Builder buildBasicProperties(Message message) {
        BasicProperties.Builder builder = new BasicProperties().builder();
        builder.messageId(message.getMessageId());
        builder.contentType(message.getContentType());
        builder.replyTo(message.getReplyTo());
        builder.correlationId(message.getCorrelationId());
        builder.contentEncoding(message.getContentEncoding());
        Map<String, Object> headers = new HashMap<String,Object>();
        if (message.getHeaders() != null) {
            headers.putAll(message.getHeaders());
        }
        String soapAction = message.getSoapAction();
        if (soapAction != null) {
            headers.put(Constants.SOAP_ACTION, soapAction);
        }
        if (!headers.isEmpty()) {
            builder.headers(headers);
        }
        return builder;
    }
    
}
