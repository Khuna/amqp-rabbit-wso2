package org.apache.axis2.transport.amqp.data;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

public class ContextKey {
    private final RabbitUrl url;
    private final Configuration configuration;

    public ContextKey(RabbitUrl url, Configuration configuration) {
        this.url = url;
        this.configuration = configuration;
    }

    public RabbitUrl getUrl() {
        return url;
    }

    public Configuration getConfiguration() {
        return configuration;
    }
    
    @Override
    public String toString() {
        ToStringBuilder res = new ToStringBuilder(this);
        res.append("url", url);
        res.append("conf", configuration);
        return res.toString();
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
            ContextKey that = (ContextKey) obj;
            EqualsBuilder eb = new EqualsBuilder();
            eb.append(this.url, that.url);
            eb.append(this.configuration, that.configuration);
            boolean result = eb.isEquals();
            return result;
        }
    }
    
}
