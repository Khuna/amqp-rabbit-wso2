package org.apache.axis2.transport.amqp.out.cache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.axis2.AxisFault;

public class CacheMap<K, T> {
//    private static final Logger logger = LoggerFactory.getLogger(CacheMap.class);
    /**
     * 2 min
     */
    private static final long DEFAULT_RECALC_INTERVAL = 120000;
    private static final long DEFAULT_TIMEOUT = 120000;
    private Map<K, CacheValue<T>> map = new HashMap<K, CacheValue<T>>();
    private final long recalcInterval;
    private final long timeout;
    private long lastRecalc = 0;
    
    public CacheMap() {
        this(DEFAULT_RECALC_INTERVAL, DEFAULT_TIMEOUT);
    }
    
    public CacheMap(long recalcInterval, long timeout) {
        this.recalcInterval = recalcInterval;
        this.timeout = timeout;
    }

    public synchronized T get(K key, RecalcCallback<K, T> callback) throws AxisFault {
//        logger.info("get.map=" + map + ",key=" + key);
        T result = null;
        CacheValue<T> cacheValue = map.get(key);
        if (cacheValue != null) {
            result = cacheValue.get();
//            logger.info("hit.result=" + result);
        } else {
            cacheValue = new CacheValue<T>(null);
            map.put(key, cacheValue);
        }
        if (result == null) {
            result = callback.recalc(key);
            cacheValue.set(result);
        }
        recalcIfNeeded();
        return result;
    }
    
    void recalcIfNeeded() {
//        logger.info("before recalc " + map);
        long now = System.currentTimeMillis();
        if (now - lastRecalc > recalcInterval) {
            List<K> keysToRemove = new ArrayList<K>(map.size());
            for (Entry<K, CacheValue<T>> entry : map.entrySet()) {
                CacheValue<T> value = entry.getValue();
                value.recalc(now, timeout);
                if (value.isEmpty()) {
                    keysToRemove.add(entry.getKey());
                }
            }
            for (K key : keysToRemove) {
                map.remove(key);
            }
            lastRecalc = now;
        }
//        logger.info("after recalc " + map);
    }
    
    public synchronized void visitAndClear(ValueVisitor<T> visitor) {
        for (CacheValue<T> cacheValue : map.values()) {
            if (cacheValue != null) {
                cacheValue.visit(visitor);
            }
        }
        map.clear();
    }
    
    @Override
    public String toString() {
        return map.toString();
    }
    
}

