package org.apache.axis2.transport.amqp.out.ref;

import java.util.List;

import org.apache.axis2.transport.amqp.common.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChannelReferenceStore {
    private static final Logger logger = LoggerFactory.getLogger(ChannelReferenceStore.class);
    private WeakList<ChannelReference> list = new WeakList<ChannelReference>();
    private volatile boolean removeOnly = false;
    
    public void stop(long timeout) {
        List<ChannelReference> arr = startStopping();
        waitForAll(arr, timeout);
        closeAll();
    }
    
    private void closeAll() {
        for (ChannelReference ref : list.list()) {
            ref.close();
        }
    }

    private boolean waitForAll(List<ChannelReference> list, long timeout) {
        long startTime = System.currentTimeMillis();
        long waitForTime = startTime + timeout;
        long currentTime = startTime;
        boolean timeoutOccurred = false;
        for (ChannelReference ref : list) {
            long currentTimeout = waitForTime - currentTime;
            if (currentTimeout <= 0) {
                timeoutOccurred = true;
                break;
            }
            if (!Utils.waitQuietly(ref.closedEvent, currentTimeout)) {
                timeoutOccurred = true;
            }
            currentTime = System.currentTimeMillis();
        }
        boolean result = !timeoutOccurred;
        if (!result) {
            logger.info("not all channel references are closed. expect exception");
        }
        return result;
    }

    synchronized void add(ChannelReference r) {
        if (removeOnly) {
            throw new IllegalStateException("already stopped");
        }
        list.add(r);
    }
    
    public synchronized void remove(ChannelReference r) {
        list.remove(r);
        logger.info("store after remove " + this);
    }
    
    private synchronized List<ChannelReference> startStopping() {
        removeOnly = true;
        return list.list();
    }
    
    @Override
    public String toString() {
        return list.toString();
    }
}
