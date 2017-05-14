package org.apache.axis2.transport.amqp.out.cache;

import org.apache.axis2.AxisFault;
import org.apache.axis2.transport.amqp.connection.Context;
import org.apache.axis2.transport.amqp.data.ContextKey;
import org.apache.axis2.transport.amqp.out.CalcSenderConfigurationCallback;

public class SenderConfigurationCache {
    private static final ValueVisitor<Context> clearVisitor = new ValueVisitor<Context>() {
        public void visit(Context value) {
            Context.close(value);
        }
    };
    private final CacheMap<ContextKey, Context> map = new CacheMap<ContextKey, Context>();
    private final Callback internalCallback = new Callback();
    private final CalcSenderConfigurationCallback externalCallback;
    
    public SenderConfigurationCache(CalcSenderConfigurationCallback externalCallback) {
        this.externalCallback = externalCallback;
    }
    
    public Context get(ContextKey key) throws AxisFault {
        Context result = map.get(key, this.internalCallback);
        return result;
    }
    
    private class Callback implements RecalcCallback<ContextKey, Context> {
        public Context recalc(ContextKey key) throws AxisFault {
            Context result = externalCallback.calc(key);
            return result;
        }
    }
    
    public void clear() {
        map.visitAndClear(clearVisitor);
    }
}
