package org.apache.axis2.transport.amqp.out.ref;

import java.io.IOException;

import com.rabbitmq.client.Channel;

public class AckReference extends ChannelReference {
    private final long tag;

    public AckReference(Channel channel, ChannelReferenceStore parent, long tag) {
        super(channel, parent);
        this.tag = tag;
    }
    
    public void ack() throws IOException {
        try {
            Channel channel = get();
            channel.basicAck(tag, false);
        } finally {
            close();
        }
    }

    public void nack() throws IOException {
        try {
            Channel channel = get();
            channel.basicNack(tag, false, true);
        } finally {
            close();
        }
    }

    public long getTag() {
        return tag;
    }
}
