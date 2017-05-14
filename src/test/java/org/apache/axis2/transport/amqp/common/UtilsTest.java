package org.apache.axis2.transport.amqp.common;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.axis2.transport.amqp.TestConsts;
import org.junit.Test;

public class UtilsTest implements TestConsts {
    @Test
    public void testEncoding() throws Exception {
        String passwordVal = Utils.urlEncode(password, true);
        assertEquals(passwordEncoded, passwordVal);
        String loginVal = Utils.urlEncode(login, true);
        assertEquals(loginEncoded, loginVal);
        String virtualHostVal = Utils.urlEncode(virtualHost);
        assertEquals(virtualHostEncoded, virtualHostVal);
        String queueVal = Utils.urlEncode(queue);
        assertEquals(queueEncoded, queueVal);
        String exchangeVal = Utils.urlEncode(exchange);
        assertEquals(exchangeEncoded, exchangeVal);
    }

    @Test
    public void testDecoding() throws Exception {
        String passwordVal = Utils.urlDecode(passwordEncoded);
        assertEquals(password, passwordVal);
        String loginVal = Utils.urlDecode(loginEncoded);
        assertEquals(login, loginVal);
        String virtualHostVal = Utils.urlDecode(virtualHostEncoded);
        assertEquals(virtualHost, virtualHostVal);
        String queueVal = Utils.urlDecode(queueEncoded);
        assertEquals(queue, queueVal);
        String exchangeVal = Utils.urlDecode(exchangeEncoded);
        assertEquals(exchange, exchangeVal);
    }
    
    @Test
    public void testParsePath() throws Exception {
        List<String> list1 = Utils.parseUrlPath("");
        List<String> expectedList1 = Collections.emptyList();
        assertEquals(expectedList1, list1);
        
        List<String> list2 = Utils.parseUrlPath("/");
        List<String> expectedList2 = Arrays.asList("");
        assertEquals(expectedList2, list2);

        List<String> list3 = Utils.parseUrlPath("/name/");
        List<String> expectedList3 = Arrays.asList("name", "");
        assertEquals(expectedList3, list3);
    }
}
