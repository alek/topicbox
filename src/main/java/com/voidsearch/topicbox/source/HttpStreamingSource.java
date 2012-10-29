package com.voidsearch.topicbox.source;

import com.voidsearch.topicbox.client.ResponseCallback;
import com.voidsearch.topicbox.client.TopicboxClient;

import java.net.URI;
import java.util.Iterator;

public class HttpStreamingSource<T> implements Iterator<T> {

    TopicboxClient client;
    ResponseCallback<String> callback;

    public HttpStreamingSource(String url) throws Exception {
        client = (new TopicboxClient(new URI(url)));
        callback = client.getCallback();
        client.run();
    }

    public boolean hasNext() {
        return callback.hasNext();
    }

    public void remove() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public T next() {
        return (T) callback.next();
    }
}
