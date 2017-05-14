package org.apache.axis2.transport.amqp.out;

import org.apache.axis2.AxisFault;
import org.apache.axis2.transport.amqp.connection.Context;
import org.apache.axis2.transport.amqp.data.ContextKey;

public interface CalcSenderConfigurationCallback {
    Context calc(ContextKey key) throws AxisFault;
}
