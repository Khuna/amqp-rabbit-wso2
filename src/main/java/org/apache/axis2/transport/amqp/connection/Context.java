package org.apache.axis2.transport.amqp.connection;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.axis2.transport.amqp.common.Utils;
import org.apache.axis2.transport.amqp.data.Configuration;
import org.apache.axis2.transport.amqp.data.RabbitUrl;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class Context {
    private static final Logger logger = LoggerFactory.getLogger(Context.class);
    
    private final RabbitUrl url;
    private final Configuration configuration;
    
    private ConnectionFactory factory;
    private ExecutorService executor;
    
    private volatile Connection connection;

    public Context(RabbitUrl url, Configuration configuration) {
        this.url = url;
        this.configuration = configuration;
    }
    
    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        close();
    }
    
    public static void close(Context ctx) {
        if (ctx != null) {
            ctx.close();
        }
    }
    
    public void close() {
        Utils.closeQuietly(connection);
        connection = null;
        factory = null;
        executor = null;
    }
    
    private synchronized ChannelHolder reconnectIfNeeded(ChannelHolder oldChannel) throws IOException {
        ChannelHolder newChannel = oldChannel;
        if (connection == null) {
            Utils.closeQuietly(oldChannel);
            ExecutorService executor = getExecutor();
            ConnectionFactory connectionFactory = getConnectionFactory();
            logger.info("creating new connection");
            Connection connection = connectionFactory.newConnection(executor);
            newChannel = new ChannelHolder(connection);
            declareObjects(newChannel);
            this.connection = connection;
        }
        if (newChannel == null) {
            newChannel = new ChannelHolder(connection);
        }
        return newChannel;
    }
    
    private void declareObjects(ChannelHolder channel) throws IOException {
        String exchangeName = url.getExchange();
        String queueName = url.getQueue();
        String routingKey = url.getRoutingKey();
        if (!exchangeExists(channel, exchangeName)) {
            logger.warn("exchange " + exchangeName + " does not exist. creating...");
            channel.get().exchangeDeclare(exchangeName, configuration.getExchangeType(), configuration.getDurable(), configuration.getAutoDelete(), null);
        }
        if (!queueExists(channel, queueName)) {
            logger.warn("queue " + queueName + " does not exist. creating...");
            channel.get().queueDeclare(queueName, configuration.getDurable(), configuration.getExclusive(), configuration.getAutoDelete(), null);
        }
        channel.get().queueBind(queueName, exchangeName, routingKey);
    }
    
    private boolean queueExists(ChannelHolder channel, String name) {
        try {
            channel.get().queueDeclarePassive(name);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
    
    private boolean exchangeExists(ChannelHolder channel, String name) {
        try {
            channel.get().exchangeDeclarePassive(name);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
    
    private ConnectionFactory getConnectionFactory() {
        if (factory == null) {
            factory = createConnectionFactory(url);
        }
        return factory;
    }
    
    private ExecutorService getExecutor() {
        if (executor == null) {
            executor = Executors.newFixedThreadPool(configuration.getConnectionThreadPool());
        }
        return executor;
    }

    private static ConnectionFactory createConnectionFactory(RabbitUrl url) {
        ConnectionFactory result = new ConnectionFactory();
        result.setHost(url.getHost());
        if (url.getPort() != null) {
            result.setPort(url.getPort());
        }
        if (url.getVirtualHost() != null) {
            result.setVirtualHost(url.getVirtualHost());
        }
        if (url.getUserName() != null) {
            result.setUsername(url.getUserName());
        }
        if (url.getPassword() != null) {
            result.setPassword(url.getPassword());
        }
        return result;
    }
    
    public ChannelHolder getChannel(ChannelHolder channel) throws IOException {
        channel = reconnectIfNeeded(channel);
        return channel;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public RabbitUrl getUrl() {
        return url;
    }

    public Context copy() {
        return new Context(url, configuration);
    }
    
    @Override
    public String toString() {
        ToStringBuilder res = new ToStringBuilder(this);
        res.append("url", url);
        res.append("conf", configuration);
        return res.toString();
    }
}
