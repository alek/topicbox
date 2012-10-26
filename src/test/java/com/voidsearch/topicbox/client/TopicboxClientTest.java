package com.voidsearch.topicbox.client;

import org.testng.annotations.Test;

import java.net.URI;

public class TopicboxClientTest {

    @Test
    public void nullTest() {
        try {
            TopicboxClient client = new TopicboxClient(new URI("http://localhost:80/foo.html"));
            ResponseCallback callback = client.getCallback();
            client.run();
            while (callback.hasNext()) {
                System.out.println(callback.next());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
