package org.apache.axis2.transport.amqp.connection;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

public class ChannelHolder {
    private static final Logger logger = LoggerFactory.getLogger(ChannelHolder.class);

    private final Connection connection;
    private Channel channel;

    public ChannelHolder(Connection connection) {
        this.connection = connection;
    }
    
    public Channel get() throws IOException {
        if (channel == null || !channel.isOpen()) {
            logger.info("creating channel");
            channel = connection.createChannel();
        }
        return channel;
    }
    
    public void close() throws IOException {
        if (channel != null && channel.isOpen()) {
            logger.info("closing channel");
            channel.close();
        }
        channel = null;
    }
    
}
