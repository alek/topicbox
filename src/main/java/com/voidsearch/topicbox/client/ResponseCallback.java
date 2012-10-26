package com.voidsearch.topicbox.client;

import java.util.Iterator;
import java.util.concurrent.*;

public class ResponseCallback<T> implements Iterator<T> {
    
    private static final int DEFAULT_TIMEOUT = 1;

    private boolean hasNext = true;
    BlockingQueue<T>queue = new LinkedBlockingQueue<T>();

    public void addData(T data) {
        queue.add(data);
    }

    public void close() {
        hasNext = false;
    }
    
    public boolean hasNext() {
        return hasNext || queue.size() > 0;
    }

    public T next() {
        try {
            return queue.poll(DEFAULT_TIMEOUT, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void remove() {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
