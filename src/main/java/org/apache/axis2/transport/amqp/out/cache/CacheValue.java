package org.apache.axis2.transport.amqp.out.cache;

import java.lang.ref.SoftReference;

public class CacheValue<T> {
    /**
     * 2 min timeout
     */
    private SoftReference<T> soft;
    private T strong;
    private long lastAccessed;
    
    public CacheValue(T value) {
        set(value);
        strong = value;
    }
    
    public void set(T value) {
        strong = value;
        if (soft != null) {
            soft.clear();
        }
        lastAccessed = System.currentTimeMillis();
    }
    
    public T get() {
        if (strong != null) {
            lastAccessed = System.currentTimeMillis();
            return strong;
        } else if (soft != null) {
            T result = soft.get();
            if (result != null) {
                lastAccessed = System.currentTimeMillis();
                strong = result;
                soft.clear();
            }
            return result;
        } else {
            return null;
        }
    }
    
    public void visit(ValueVisitor<T> visitor) {
        if (strong != null) {
            visitor.visit(strong);
        } else if (soft != null) {
            T value = soft.get();
            if (value != null) {
                visitor.visit(value);
            }
        }
    }
    
    public void recalc(long now, long timeout) {
        if (strong != null && now - lastAccessed > timeout) {
            soft = new SoftReference<T>(strong);
            strong = null;
        }
    }
    
    public boolean isEmpty() {
        return strong == null && (soft == null || soft.get() == null);
    }
    
    
    @Override
    public String toString() {
        boolean softSet = soft != null && soft.get() != null;
        boolean strongSet = strong != null;
        return "soft=" + softSet + ",strong=" + strongSet;
    }

}
