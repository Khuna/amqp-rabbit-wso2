package org.apache.axis2.transport.amqp.out.cache;

import org.apache.axis2.AxisFault;

public interface RecalcCallback<K, T> {
    T recalc(K k) throws AxisFault;
}
