package org.apache.axis2.transport.amqp.out.ref;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class WeakList<T> {
    private List<WeakReference<T>> list = new LinkedList<WeakReference<T>>();
    
    public boolean add(T obj) {
        removeIncluding(null);
        return list.add(new WeakReference<T>(obj));
    }
    
    public boolean remove(T obj) {
        return removeIncluding(obj);
    }
    
    private boolean removeIncluding(T val) {
        boolean removed = false;
        for (Iterator<WeakReference<T>> it = list.iterator(); it.hasNext(); ) {
            WeakReference<T> ref = it.next();
            T existing = ref.get();
            if (existing == null || existing.equals(val)) {
                it.remove();
                removed = true;
            }
        }
        return removed;
    }
    
    public List<T> list() {
        ArrayList<T> res = new ArrayList<T>(list.size());
        for (WeakReference<T> ref : list) {
            T obj = ref.get();
            if (obj != null) {
                res.add(obj);
            }
        }
        return res;
    }
    
    @Override
    public String toString() {
        return list.toString();
    }
}
