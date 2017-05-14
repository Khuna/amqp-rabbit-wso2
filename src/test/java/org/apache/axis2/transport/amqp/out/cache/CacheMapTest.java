package org.apache.axis2.transport.amqp.out.cache;

import static org.junit.Assert.*;

import org.apache.axis2.transport.amqp.TestUtils;
import org.junit.Test;

public class CacheMapTest {

    @Test
    public void test() throws Exception {
        CacheMap<String, long[]> map = new CacheMap<String, long[]>(1, 1);
        final String key = "key";
        RecalcCallback<String, long[]> callback = new RecalcCallback<String, long[]>() {
            public long[] recalc(String key) {
                return new long[102400];
            }
        };
        assertEquals("{}", map.toString());
        map.get(key, callback);
        assertEquals("{key=soft=false,strong=true}", map.toString());
        Thread.sleep(2);
        map.recalcIfNeeded();
        assertEquals("{key=soft=true,strong=false}", map.toString());
        System.gc();
        assertEquals("{key=soft=true,strong=false}", map.toString());
        TestUtils.forceReleaseSoft();
        assertEquals("{key=soft=false,strong=false}", map.toString());
        map.recalcIfNeeded();
        assertEquals("{}", map.toString());
    }
}
