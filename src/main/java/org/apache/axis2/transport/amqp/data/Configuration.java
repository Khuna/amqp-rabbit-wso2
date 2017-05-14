package org.apache.axis2.transport.amqp.data;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

public class Configuration {
    private String name;
    private Integer connectionThreadPool;
    private Integer listenerThreadPool;
    private Boolean autoAck;
    private String exchangeType;
    private Boolean durable;
    private Boolean exclusive;
    private Boolean autoDelete;
    private Long waitMessageTimeoutMillis;
    private Long reconnectIntervalMillis;
    private Long finishTimeoutMillis;
    private Long channelReferenceTimeoutMillis;
    private Integer deliveryMode;
    private String listenerUrl;
    private String replyToUri;
    private String faultUri;
    private String senderUri;
    private Boolean receiverReplyToEnabled;
    private Boolean syncResponseEnabled;
    private Boolean asyncResponseEnabled;
    
    public Configuration() {
        
    }
    
    public Integer getConnectionThreadPool() {
        return connectionThreadPool;
    }
    
    public void setConnectionThreadPool(Integer connectionThreadPool) {
        this.connectionThreadPool = connectionThreadPool;
    }

    public Integer getListenerThreadPool() {
        return listenerThreadPool;
    }

    public void setListenerThreadPool(Integer listenerThreadPool) {
        this.listenerThreadPool = listenerThreadPool;
    }

    public Boolean getAutoAck() {
        return autoAck;
    }

    public void setAutoAck(Boolean autoAck) {
        this.autoAck = autoAck;
    }

    public String getExchangeType() {
        return exchangeType;
    }

    public void setExchangeType(String exchangeType) {
        this.exchangeType = exchangeType;
    }

    public Boolean getDurable() {
        return durable;
    }

    public void setDurable(Boolean durable) {
        this.durable = durable;
    }

    public Boolean getExclusive() {
        return exclusive;
    }

    public void setExclusive(Boolean exclusive) {
        this.exclusive = exclusive;
    }

    public Boolean getAutoDelete() {
        return autoDelete;
    }

    public void setAutoDelete(Boolean autoDelete) {
        this.autoDelete = autoDelete;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public Long getWaitMessageTimeoutMillis() {
        return waitMessageTimeoutMillis;
    }

    public void setWaitMessageTimeoutMillis(Long waitMessageTimeoutMillis) {
        this.waitMessageTimeoutMillis = waitMessageTimeoutMillis;
    }

    public Long getReconnectIntervalMillis() {
        return reconnectIntervalMillis;
    }

    public void setReconnectIntervalMillis(Long reconnectIntervalMillis) {
        this.reconnectIntervalMillis = reconnectIntervalMillis;
    }

    public Long getFinishTimeoutMillis() {
        return finishTimeoutMillis;
    }

    public void setFinishTimeoutMillis(Long finishTimeoutMillis) {
        this.finishTimeoutMillis = finishTimeoutMillis;
    }
    
    public Long getChannelReferenceTimeoutMillis() {
        return channelReferenceTimeoutMillis;
    }
    
    public void setChannelReferenceTimeoutMillis(Long channelReferenceTimeoutMillis) {
        this.channelReferenceTimeoutMillis = channelReferenceTimeoutMillis;
    }
    
    public Integer getDeliveryMode() {
        return deliveryMode;
    }

    public void setDeliveryMode(Integer deliveryMode) {
        this.deliveryMode = deliveryMode;
    }
    
    public String getFaultUri() {
        return faultUri;
    }

    public void setFaultUri(String faultUri) {
        this.faultUri = faultUri;
    }
    
    public String getListenerUrl() {
        return listenerUrl;
    }

    public void setListenerUrl(String listenerUrl) {
        this.listenerUrl = listenerUrl;
    }

    public String getReplyToUri() {
        return replyToUri;
    }

    public void setReplyToUri(String replyToUri) {
        this.replyToUri = replyToUri;
    }

    public String getSenderUri() {
        return senderUri;
    }

    public void setSenderUri(String senderUri) {
        this.senderUri = senderUri;
    }

    public Boolean getReceiverReplyToEnabled() {
        return receiverReplyToEnabled;
    }

    public void setReceiverReplyToEnabled(Boolean receiverReplyToEnabled) {
        this.receiverReplyToEnabled = receiverReplyToEnabled;
    }

    public Boolean getSyncResponseEnabled() {
        return syncResponseEnabled;
    }

    public void setSyncResponseEnabled(Boolean syncResponseEnabled) {
        this.syncResponseEnabled = syncResponseEnabled;
    }

    public Boolean getAsyncResponseEnabled() {
        return asyncResponseEnabled;
    }

    public void setAsyncResponseEnabled(Boolean asyncResponseEnabled) {
        this.asyncResponseEnabled = asyncResponseEnabled;
    }
    
    public Boolean getResponseEnabled(boolean sync) {
        if (sync) {
            return getSyncResponseEnabled();
        } else {
            return getAsyncResponseEnabled();
        }
    }
    
    public static Configuration merge(Configuration absolute, Configuration relative) {
        Configuration result = new Configuration();
        result.fillFrom(relative);
        result.fillFrom(absolute);
        return result;
    }
    
    public Configuration merge(Configuration conf) {
        return merge(conf, this);
    }
    

    public void fillFrom(Configuration that) {
        if (that != null) {
            if (this.name == null && that.name != null) {
                this.name = that.name;
            }
            if (this.connectionThreadPool == null && that.connectionThreadPool != null) {
                this.connectionThreadPool = that.connectionThreadPool;
            }
            if (this.listenerThreadPool == null && that.listenerThreadPool != null) {
                this.listenerThreadPool = that.listenerThreadPool;
            }
            if (this.autoAck == null && that.autoAck != null) {
                this.autoAck = that.autoAck;
            }
            if (this.exchangeType == null && that.exchangeType != null) {
                this.exchangeType = that.exchangeType;
            }
            if (this.durable == null && that.durable != null) {
                this.durable = that.durable;
            }
            if (this.exclusive == null && that.exclusive != null) {
                this.exclusive = that.exclusive;
            }
            if (this.autoDelete == null && that.autoDelete != null) {
                this.autoDelete = that.autoDelete;
            }
            if (this.waitMessageTimeoutMillis == null && that.waitMessageTimeoutMillis != null) {
                this.waitMessageTimeoutMillis = that.waitMessageTimeoutMillis;
            }
            if (this.reconnectIntervalMillis == null && that.reconnectIntervalMillis != null) {
                this.reconnectIntervalMillis = that.reconnectIntervalMillis;
            }
            if (this.finishTimeoutMillis == null && that.finishTimeoutMillis != null) {
                this.finishTimeoutMillis = that.finishTimeoutMillis;
            }
            if (this.channelReferenceTimeoutMillis == null && that.channelReferenceTimeoutMillis != null) {
                this.channelReferenceTimeoutMillis = that.channelReferenceTimeoutMillis;
            }
            if (this.deliveryMode == null && that.deliveryMode != null) {
                this.deliveryMode = that.deliveryMode;
            }
            if (this.faultUri == null && that.faultUri != null) {
                this.faultUri = that.faultUri;
            }
            if (this.listenerUrl == null && that.listenerUrl != null) {
                this.listenerUrl = that.listenerUrl;
            }
            if (this.replyToUri == null && that.replyToUri != null) {
                this.replyToUri = that.replyToUri;
            }
            if (this.senderUri == null && that.senderUri != null) {
                this.senderUri = that.senderUri;
            }
            if (this.receiverReplyToEnabled == null && that.receiverReplyToEnabled != null) {
                this.receiverReplyToEnabled = that.receiverReplyToEnabled;
            }
            if (this.syncResponseEnabled == null && that.syncResponseEnabled != null) {
                this.syncResponseEnabled = that.syncResponseEnabled;
            }
            if (this.asyncResponseEnabled == null && that.asyncResponseEnabled != null) {
                this.asyncResponseEnabled = that.asyncResponseEnabled;
            }
        }
    }
    
    @Override
    public String toString() {
        ToStringBuilder res = new ToStringBuilder(this);
        res.append("name", name);
        res.append("autoDelete", autoDelete);
        res.append("durable", durable);
        res.append("exchangeType", exchangeType);
        res.append("exclusive", exclusive);
        res.append("deliveryMode", deliveryMode);
        return res.toString();
    }
    
    @Override
    public int hashCode() {
        if (name != null) {
            return name.hashCode();
        } else {
            return 0;
        }
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
            Configuration that = (Configuration) obj;
            EqualsBuilder eb = new EqualsBuilder();
            eb.append(this.name, that.name);
            
            eb.append(this.connectionThreadPool, that.connectionThreadPool);
            eb.append(this.listenerThreadPool, that.listenerThreadPool);
            eb.append(this.autoAck, that.autoAck);
            eb.append(this.exchangeType, that.exchangeType);
            eb.append(this.durable, that.durable);
            eb.append(this.exclusive, that.exclusive);
            eb.append(this.autoDelete, that.autoDelete);
            eb.append(this.waitMessageTimeoutMillis, that.waitMessageTimeoutMillis);
            eb.append(this.reconnectIntervalMillis, that.reconnectIntervalMillis);
            eb.append(this.finishTimeoutMillis, that.finishTimeoutMillis);
            eb.append(this.channelReferenceTimeoutMillis, that.channelReferenceTimeoutMillis);
            eb.append(this.deliveryMode, that.deliveryMode);
            eb.append(this.listenerUrl, that.listenerUrl);
            eb.append(this.replyToUri, that.replyToUri);
            eb.append(this.faultUri, that.faultUri);
            eb.append(this.senderUri, that.senderUri);
            eb.append(this.receiverReplyToEnabled, that.receiverReplyToEnabled);
            eb.append(this.syncResponseEnabled, that.syncResponseEnabled);
            eb.append(this.asyncResponseEnabled, that.asyncResponseEnabled);

            boolean result = eb.isEquals();
            return result;
            
        }
    }
}
