package org.apache.axis2.transport.amqp.builders;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.apache.axis2.AxisFault;
import org.apache.axis2.transport.amqp.common.Constants;
import org.apache.axis2.transport.amqp.common.Utils;
import org.apache.axis2.transport.amqp.data.RabbitUrl;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;

public class RabbitUrlParser {
    public static void parseFinished(RabbitUrl url, String transport, String serviceName) throws AxisFault {
        if (url.getQueue() == null) {
            url.setQueue(serviceName);
        }
        final String urlStr;
        try {
            urlStr = buildUrl(url, transport, false);
        } catch (UnsupportedEncodingException e) {
            throw new AxisFault(e.getMessage(), e);
        } catch (URISyntaxException e) {
            throw new AxisFault(e.getMessage(), e);
        }
        url.setUrl(urlStr);
        
        validate(url);
    }
    
    public static RabbitUrl parseSingle(String uri, String transport) throws AxisFault {
        if (uri == null) {
            return null;
        }
        ParsedUri parsedUri = ParsedUri.create(uri);
        RabbitUrl result = new RabbitUrl();
        result.setHost(parsedUri.getHost());
        result.setPort(parsedUri.getPort());
        String scheme = parsedUri.getScheme();
        if (scheme != null && !transport.equals(scheme)) {
            throw new IllegalArgumentException("Unexpected scheme=" + parsedUri.getScheme() + " for " + parsedUri + ". expected " + transport);
        }
        result.setUserName(parsedUri.getUserName());
        result.setPassword(parsedUri.getPassword());
        if (parsedUri.getSegmentsCount() > 4) {
            throw new IllegalArgumentException("Unexpected format " + uri);
        }

        String queue = parsedUri.getSegmentSafe(0);
        result.setQueue(queue);
        
        result.setExchange(parsedUri.getSegmentSafe(1));

        String virtualHost = parsedUri.getSegmentSafe(2);
        if (virtualHost != null && virtualHost.isEmpty()) {
            virtualHost = null;
        }
        result.setVirtualHost(virtualHost);
        
        String routingKey = parsedUri.getSegmentSafe(3);
        if (routingKey == null) {
            routingKey = queue;
        }
        result.setRoutingKey(routingKey);
        
        for (Entry<String, String> paramEntry : parsedUri.getParameters().entrySet()) {
            String name = paramEntry.getKey();
            String value = paramEntry.getValue();
            if (Constants.CONFIGURATION_NAME.equals(name)) {
                String configurationName = value;
                configurationName = Utils.removeEmpty(configurationName);
                if (configurationName != null) {
                    configurationName = configurationName.toLowerCase();
                }
                result.setConfigurationName(configurationName);
            } else {
                throw new IllegalArgumentException("Unexpected parameter " + name + " in " + uri);
            }
        }
        
        return result;
    }
    
    static String buildUrl(RabbitUrl url, String transport, boolean revealPassword) throws URISyntaxException, UnsupportedEncodingException {
        StringBuilder sb = new StringBuilder();
        sb.append(transport);
        sb.append("://");
        String userName = url.getUserName();
        if (userName != null) {
            String userNameEncoded = Utils.urlEncode(userName, true);
            sb.append(userNameEncoded);
            String password = url.getPassword();
            if (password != null) {
                if (!revealPassword) {
                    password = "***";
                }
                String passwordEncoded = Utils.urlEncode(password, true);
                sb.append(":");
                sb.append(passwordEncoded);
            }
            sb.append("@");
        }
        sb.append(url.getHost());
        Integer port = url.getPort();
        if (port != null) {
            sb.append(":");
            sb.append(port);
        }
        appendPath(url.getQueue(), sb);
        appendPath(url.getExchange(), sb);
        appendPath(url.getVirtualHost(), sb);
        appendPath(url.getRoutingKey(), sb);

        List<BasicNameValuePair> parameters = new ArrayList<BasicNameValuePair>();
        if (url.getConfigurationName() != null) {
            parameters.add(new BasicNameValuePair(Constants.CONFIGURATION_NAME, url.getConfigurationName()));
        }
        final String query = URLEncodedUtils.format(parameters, "UTF-8");
        if (query != null && !query.isEmpty()) {
            sb.append("?");
            sb.append(query);
        }
        
        final String result = sb.toString();
        return result;
    }
    
    private static final void appendPath(String val, StringBuilder result) throws UnsupportedEncodingException {
        result.append("/");
        if (val != null) {
            String encodedVal = Utils.urlEncode(val);
            result.append(encodedVal);
        }
    }
    
    private static void validate(RabbitUrl url) {
        if (url.getHost() == null) {
            throw new IllegalArgumentException("host not found for " + url);
        }
        if (url.getExchange() == null) {
            throw new IllegalArgumentException("exchange not found for " + url);
        }
        if (url.getQueue() == null) {
            throw new IllegalArgumentException("queue not found for " + url);
        }
    }
}
