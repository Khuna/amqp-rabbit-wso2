package org.apache.axis2.transport.amqp.builders;

import static org.junit.Assert.assertEquals;

import org.apache.axis2.transport.amqp.TestConsts;
import org.junit.Test;

public class ParsedUriTest implements TestConsts {
    @Test
    public void testSimple() throws Exception {
        ParsedUri uri = new ParsedUri("http://login:password@localhost/part1/part2?param=value");
        assertEquals("login", uri.getUserName());
        assertEquals("password", uri.getPassword());
        assertEquals("localhost", uri.getHost());
        assertEquals("part1", uri.getSegmentSafe(0));
        assertEquals("part2", uri.getSegmentSafe(1));
        assertEquals("value", uri.getParameter("param"));
    }
    
    @Test
    public void testFull() throws Exception {
        ParsedUri uri = new ParsedUri(urlStr);
        assertEquals(login, uri.getUserName());
        assertEquals(password, uri.getPassword());
        assertEquals(queue, uri.getSegmentSafe(0));
        assertEquals(exchange, uri.getSegmentSafe(1));
        assertEquals(virtualHost, uri.getSegmentSafe(2));
        assertEquals(queue, uri.getSegmentSafe(3));
    }
}
