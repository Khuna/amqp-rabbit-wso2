package org.apache.axis2.transport.amqp.builders;

import static org.junit.Assert.assertEquals;

import org.apache.axis2.transport.amqp.TestConsts;
import org.apache.axis2.transport.amqp.data.RabbitUrl;
import org.junit.Test;

public class RabbitUrlParserTest implements TestConsts {
    @Test
    public void test1() throws Exception {
        RabbitUrl url = RabbitUrlParser.parseSingle(urlStr, transport);
        assertEquals(login, url.getUserName());
        assertEquals(password, url.getPassword());
        assertEquals(virtualHost, url.getVirtualHost());
        assertEquals(queue, url.getQueue());
        assertEquals(exchange, url.getExchange());
        assertEquals(queue, url.getRoutingKey());
        assertEquals(configurationName, url.getConfigurationName());
        String val = RabbitUrlParser.buildUrl(url, transport, true);
        assertEquals(urlStr, val);
    }
}
