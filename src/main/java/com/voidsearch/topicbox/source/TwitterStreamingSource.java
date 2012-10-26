package com.voidsearch.topicbox.source;

import com.voidsearch.topicbox.client.ResponseCallback;
import com.voidsearch.topicbox.client.TopicboxClient;

import java.net.URI;
import java.util.Iterator;

public class TwitterStreamingSource implements Iterator {

    TopicboxClient client;
    ResponseCallback<String> callback;
    
    public TwitterStreamingSource(String url) throws Exception {
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

    public String next() {
        return callback.next();
    }
}
