package org.apache.axis2.transport.amqp.data;

import org.apache.commons.lang.ObjectUtils;



public class RabbitUrl {
    private String host;
    private Integer port;
    private String virtualHost;
    private String userName;
    private String password;
    private String exchange;
    private String queue;
    private String routingKey;
    private String configurationName;
    private String url;
    
    public String getHost() {
        return host;
    }
    
    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getVirtualHost() {
        return virtualHost;
    }

    public void setVirtualHost(String virtualHost) {
        this.virtualHost = virtualHost;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getExchange() {
        return exchange;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

    public String getQueue() {
        return queue;
    }

    public void setQueue(String queue) {
        this.queue = queue;
    }

    public String getRoutingKey() {
        return routingKey;
    }

    public void setRoutingKey(String routingKey) {
        this.routingKey = routingKey;
    }
    
    public String getUrl() {
        return url;
    }
    
    public void setUrl(String url) {
        this.url = url;
    }
    
    public String getConfigurationName() {
        return configurationName;
    }

    public void setConfigurationName(String configurationName) {
        this.configurationName = configurationName;
    }
    
    public boolean mergeFrom(RabbitUrl that, boolean sameDirection) {
        if (that == null) {
            return false;
        }
        boolean changed = false;
        if (this.host == null && that.host != null) {
            this.host = that.host;
            changed = true;
        }
        if (this.port == null && that.port != null) {
            this.port = that.port;
            changed = true;
        }
        if (this.virtualHost == null && that.virtualHost != null) {
            this.virtualHost = that.virtualHost;
            changed = true;
        }
        if (this.userName == null && that.userName != null) {
            this.userName = that.userName;
            changed = true;
        }
        if (this.password == null && that.password != null) {
            this.password = that.password;
            changed = true;
        }
        if (this.exchange == null && that.exchange != null) {
            this.exchange = that.exchange;
            changed = true;
        }
        if (this.queue == null && that.queue != null) {
            this.queue = that.queue;
            changed = true;
        }
        if (this.routingKey == null && that.routingKey != null) {
            this.routingKey = that.routingKey;
            changed = true;
        }
        if (sameDirection) {
            if (this.configurationName == null && that.configurationName != null) {
                this.configurationName = that.configurationName;
                changed = true;
            }
        }
        if (changed) {
            url = null;
        }
        return changed;
    }
    
    @Override
    public String toString() {
        return url;
    }
    
    @Override
    public int hashCode() {
        return url.hashCode();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        } else if (obj == this) {
            return true;
        } else if (obj.getClass() != this.getClass()) {
            return false;
        } else {
            RabbitUrl that = (RabbitUrl) obj;
            boolean result = ObjectUtils.equals(this.url, that.url);
            return result;
        }
    }

}
