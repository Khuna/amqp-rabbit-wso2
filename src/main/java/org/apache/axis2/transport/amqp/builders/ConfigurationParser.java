package org.apache.axis2.transport.amqp.builders;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.ParameterIncludeImpl;
import org.apache.axis2.transport.amqp.common.Constants;
import org.apache.axis2.transport.amqp.common.Utils;
import org.apache.axis2.transport.amqp.data.Configuration;

public class ConfigurationParser {
    public static Configuration parse(Iterable<Parameter> parameters, boolean ignoreUnknown) {
        Configuration result = new Configuration();
        for (Parameter parameter : parameters) {
            String name = parameter.getName();
            Object obj = parameter.getValue();
            if (obj != null) {
                if (obj instanceof String) {
                    String value = (String) obj;
                    value = value.trim();
                    if (!value.isEmpty()) {
                        setField(result, name, value, ignoreUnknown);
                    }
                }
            }
        }
        return result;
    }
    
    public static Configuration parse(Parameter parameter, boolean ignoreUnknown) throws AxisFault {
        ParameterIncludeImpl pi = new ParameterIncludeImpl();
        String configurationName = parameter.getName();
        pi.deserializeParameters((OMElement) parameter.getValue());
        ArrayList<Parameter> parameters = pi.getParameters();
        Configuration result = parse(parameters, ignoreUnknown);
        result.setName(configurationName);
        return result;
    }
    
    public static Map<String, Configuration> parseList(List<Parameter> parameters, boolean ignoreUnknown) throws AxisFault {
        Map<String, Configuration> result = new HashMap<String, Configuration>(parameters.size());
        for (Parameter parameter : parameters) {
            Configuration configuration = parse(parameter, ignoreUnknown);
            Utils.applyDefaults(configuration);
            String configurationName = configuration.getName();
            configurationName = configurationName.toLowerCase();
            result.put(configurationName, configuration);
        }
        return result;
    }
    
    private static void setField(Configuration c, String name, String value, boolean ignoreUnknown) {
        if (Constants.CONNECTION_THREAD_POOL.equals(name)) {
            c.setConnectionThreadPool(Integer.parseInt(value));
        } else if (Constants.LISTENER_THREAD_POOL.equals(name)) {
            c.setListenerThreadPool(Integer.parseInt(value));
        } else if (Constants.QUEUE_AUTO_ACK.equals(name)) {
            c.setAutoAck(Boolean.parseBoolean(value));
        } else if (Constants.EXCHANGE_TYPE.equals(name)) {
            c.setExchangeType(value);
        } else if (Constants.QUEUE_EXCLUSIVE.equals(name)) {
            c.setExclusive(Boolean.parseBoolean(value));
        } else if (Constants.QUEUE_DURABLE.equals(name)) {
            c.setDurable(Boolean.parseBoolean(value));
        } else if (Constants.QUEUE_AUTO_DELETE.equals(name)) {
            c.setAutoDelete(Boolean.parseBoolean(value));
        } else if (Constants.RECONNECT_INTERVAL_MILLIS.equals(name)) {
            c.setReconnectIntervalMillis(Long.parseLong(value));
        } else if (Constants.WAIT_MESSAGE_TIMEOUT_MILLIS.equals(name)) {
            c.setWaitMessageTimeoutMillis(Long.parseLong(value));
        } else if (Constants.FINISH_TIMEOUT_MILLIS.equals(name)) {
            c.setFinishTimeoutMillis(Long.parseLong(value));
        } else if (Constants.CHANNEL_REFERENCE_TIMEOUT_MILLIS.equals(name)) {
            c.setFinishTimeoutMillis(Long.parseLong(value));
        } else if (Constants.QUEUE_DELIVERY_MODE.equals(name)) {
            c.setDeliveryMode(Integer.parseInt(value));
        } else if (Constants.LISTENER_URI.equals(name)) {
            c.setListenerUrl(value);
        } else if (Constants.REPLY_TO_URI.equals(name)) {
            c.setReplyToUri(value);
        } else if (Constants.FAULT_URI.equals(name)) {
            c.setFaultUri(value);
        } else if (Constants.SENDER_URI.equals(name)) {
            c.setSenderUri(value);
        } else if (Constants.RECEIVER_REPLY_TO_ENABLED.equals(name)) {
            c.setReceiverReplyToEnabled(Boolean.parseBoolean(value));
        } else if (Constants.SYNC_RESPONSE_ENABLED.equals(name)) {
            c.setSyncResponseEnabled(Boolean.parseBoolean(value));
        } else if (Constants.ASYNC_RESPONSE_ENABLED.equals(name)) {
            c.setAsyncResponseEnabled(Boolean.parseBoolean(value));
        } else if (!ignoreUnknown) {
            throw new IllegalArgumentException("unknown parameter " + name);
        }
    }
    
}
