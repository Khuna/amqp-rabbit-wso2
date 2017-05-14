package org.apache.axis2.transport.amqp;

import org.apache.axis2.AxisFault;
import org.apache.axis2.transport.amqp.builders.RabbitUrlParser;
import org.apache.axis2.transport.amqp.data.RabbitUrl;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

public class TestUtils {
    private static final int BUF_SIZE = 1024 * 1024;

    public static void forceReleaseSoft() {
        try {
            final List<long[]> memhog = new LinkedList<long[]>();
            while(true) {
                memhog.add(new long[BUF_SIZE]);
            }
        }
        catch(final OutOfMemoryError e) {
        }


    }
}
