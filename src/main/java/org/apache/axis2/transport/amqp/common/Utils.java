package org.apache.axis2.transport.amqp.common;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.axis2.transport.amqp.connection.ChannelHolder;
import org.apache.axis2.transport.amqp.data.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.Connection;

public class Utils {
    private static final Logger logger = LoggerFactory.getLogger(Utils.class);
    
    public static void closeQuietly(Connection connection) {
        if (connection != null && connection.isOpen()) {
            try {
                connection.close();
            } catch (Exception e) {
                logger.warn(e.getMessage(), e);
            }
        }
    }

    public static void closeQuietly(ChannelHolder channel) {
        if (channel != null) {
            try {
                channel.close();
            } catch (Exception e) {
                logger.warn(e.getMessage(), e);
            }
        }
    }


    public static boolean waitQuietly(CountDownLatch event, long millis) {
        try {
            boolean result = event.await(millis, TimeUnit.MILLISECONDS);
            return result;
        } catch (InterruptedException e) {
            return false;
        }
    }
    
    public static void applyDefaults(Configuration c) {
        if (c.getConnectionThreadPool() == null) {
            c.setConnectionThreadPool(Constants.DEFAULT_CONNECTION_THREAD_POOL);
        }
        if (c.getListenerThreadPool() == null) {
            c.setListenerThreadPool(Constants.DEFAULT_LISTENER_THREAD_POOL);
        }
        if (c.getAutoAck() == null) {
            c.setAutoAck(Constants.DEFAULT_AUTO_ACK);
        }
        if (c.getExchangeType() == null) {
            c.setExchangeType(Constants.DEFAULT_EXCHANGE_TYPE);
        }
        if (c.getExclusive() == null) {
            c.setExclusive(Constants.DEFAULT_EXCLUSIVE);
        }
        if (c.getDurable() == null) {
            c.setDurable(Constants.DEFAULT_DURABLE);
        }
        if (c.getAutoDelete() == null) {
            c.setAutoDelete(Constants.DEFAULT_AUTO_DELETE);
        }
        if (c.getReconnectIntervalMillis() == null) {
            c.setReconnectIntervalMillis(Constants.DEFAULT_RECONNECT_INTERVAL_MILLIS);
        }
        if (c.getWaitMessageTimeoutMillis() == null) {
            c.setWaitMessageTimeoutMillis(Constants.DEFAULT_WAIT_MESSAGE_TIMEOUT_MILLIS);
        }
        if (c.getFinishTimeoutMillis() == null) {
            c.setFinishTimeoutMillis(Constants.DEFAULT_FINISH_TIMEOUT_MILLIS);
        }
        if (c.getChannelReferenceTimeoutMillis() == null) {
            c.setChannelReferenceTimeoutMillis(Constants.DEFAULT_CHANNEL_REFERENCE_TIMEOUT_MILLIS);
        }
        if (c.getReceiverReplyToEnabled() == null) {
            c.setReceiverReplyToEnabled(Constants.DEFAULT_RECEIVER_REPLY_TO_ENABLED);
        }
        if (c.getSyncResponseEnabled() == null) {
            c.setSyncResponseEnabled(Constants.DEFAULT_SYNC_RESPONSE_ENABLED);
        }
        if (c.getAsyncResponseEnabled() == null) {
            c.setAsyncResponseEnabled(Constants.DEFAULT_ASYNC_RESPONSE_ENABLED);
        }
    }

    public static Configuration getConfiguration(final String name, Map<String, Configuration> configurations) {
        String effectiveName = name;
        if (effectiveName == null) {
            effectiveName = Constants.DEFAULT_CONFIGURATION_NAME;
        }
        Configuration result = configurations.get(effectiveName);
        return result;
    }
    
    public static String removeEmpty(final String val) {
        String id = val;
        if (id != null) {
            id = id.trim();
            if (id.isEmpty()) {
                id = null;
            }
        }
        return id;
    }
    
    public static String urlDecode(String val) throws UnsupportedEncodingException {
        return URLDecoder.decode(val, "UTF-8");
    }
    
    public static String urlEncode(String val) throws UnsupportedEncodingException {
        if (val == null) {
            return null;
        } else {
            return urlEncode(val, false);
        }
    }
    
    public static String urlEncode(String val, boolean replacePlus) throws UnsupportedEncodingException {
        if (val == null) {
            return null;
        }
        String result = URLEncoder.encode(val, "UTF-8");
//        if (replacePlus) {
//            result = result.replace("+", "%20");
//        }
        return result;
    }
    
    public static List<String> split(String val, char ch) {
        if (val == null) {
            return Collections.emptyList();
        }
        List<String> result = new LinkedList<String>();
        int currentIndex = 0;
        while (currentIndex <= val.length()) {
            int splitIndex = val.indexOf(ch, currentIndex);
            if (splitIndex == -1) {
                String str = val.substring(currentIndex);
                result.add(str);
                break;
            } else {
                String str = val.substring(currentIndex, splitIndex);
                result.add(str);
                currentIndex = splitIndex + 1;
            }
        }
        return result;
    }
    
    public static List<String> parseUrlPath(String path) throws UnsupportedEncodingException {
        List<String> segmentsArray = split(path, '/');
        List<String> result = new ArrayList<String>(segmentsArray.size());
        for (String segment : segmentsArray) {
            String val = segment;
            if (val != null) {
                val = urlDecode(val);
                result.add(val);
            }
        }
        if (!result.isEmpty()) {
            result.remove(0);
        }
        return result;
    }
}
