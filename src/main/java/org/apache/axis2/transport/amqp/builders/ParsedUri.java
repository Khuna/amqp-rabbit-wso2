package org.apache.axis2.transport.amqp.builders;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.axis2.transport.amqp.common.Utils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

public class ParsedUri {
    private final URI uri;
    private final String userName;
    private final String password;
    private final Map<String, String> parameters;
    private final List<String> segments;
    
    public static ParsedUri create(String uri) {
        try {
            ParsedUri result = new ParsedUri(uri);
            return result;
        } catch (Exception e) {
            throw new IllegalArgumentException("error while parsing uri " + uri + " " + e.getMessage(), e);
        }
    }
    
    public ParsedUri(String uri) throws UnsupportedEncodingException, URISyntaxException {
        this(new URI(uri));
    }

    public ParsedUri(URI uri) throws UnsupportedEncodingException {
        this.uri = uri;
        String userInfo = uri.getRawUserInfo();
        if (userInfo != null && !userInfo.isEmpty()) {
            int splitIndex = userInfo.indexOf(':');
            final String passwordEncoded;
            final String userNameEncoded;
            if (splitIndex > 0) {
                userNameEncoded = userInfo.substring(0, splitIndex);
                passwordEncoded = userInfo.substring(splitIndex + 1);
            } else {
                passwordEncoded = null;
                userNameEncoded = userInfo;
            }
            this.userName = Utils.urlDecode(userNameEncoded);
            this.password = Utils.urlDecode(passwordEncoded);
        } else {
            this.userName = null;
            this.password = null;
        }
        List<NameValuePair> params = URLEncodedUtils.parse(uri, "UTF-8");
        Map<String, String> parameters = new HashMap<String, String>(params.size());
        for (NameValuePair pair : params) {
            parameters.put(pair.getName(), pair.getValue());
        }
        parameters = Collections.unmodifiableMap(parameters);
        this.parameters = parameters;
        String path = uri.getRawPath();
        this.segments = Utils.parseUrlPath(path);
    }
    
    public String getScheme() {
        return uri.getScheme();
    }

    public String getHost() {
        return uri.getHost();
    }
    
    public Integer getPort() {
        int port = uri.getPort();
        return port == -1 ? null : port;
    }
    
    public String getUserName() {
        return userName;
    }
    
    public String getPassword() {
        return password;
    }
    
    public String getParameter(String name) {
        return parameters.get(name);
    }
    
    public Map<String, String> getParameters() {
        return parameters;
    }

    public String getSegmentSafe(int index) {
        if (index >= segments.size()) {
            return null;
        } else {
            return segments.get(index);
        }
    }
    
    public int getSegmentsCount() {
        return segments.size();
    }

    @Override
    public String toString() {
        return uri.toString();
    }
}
