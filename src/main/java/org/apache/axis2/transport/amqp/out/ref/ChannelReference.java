package org.apache.axis2.transport.amqp.out.ref;

import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.Channel;

public class ChannelReference {
    private static final Logger logger = LoggerFactory.getLogger(ChannelReference.class);
    final CountDownLatch closedEvent = new CountDownLatch(1);
    private Channel channel;
    private ChannelReferenceStore parent;
    
    
    ChannelReference(Channel channel, ChannelReferenceStore parent) {
        parent.add(this);
        this.channel = channel;
        this.parent = parent;
        logger.info("constructor " + this);
    }

    protected void finalize() throws Throwable {
        super.finalize();
        logger.info("finalize");
    };
    
    public Channel get() {
        return channel;
    }
    
    public void close() {
        if (channel != null) {
            closedEvent.countDown();
            parent.remove(this);
            channel = null;
            parent = null;
        }
    }
}
