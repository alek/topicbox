package com.voidsearch.topicbox.source;

import org.testng.annotations.Test;

public class TwitterStreamingSourceTest {

    @Test
    public void nullTest() {
        try {
            TwitterStreamingSource source = new TwitterStreamingSource("http://stream.twitter.com/1/statuses/sample.json?delimited=length");
            while (source.hasNext()) {
                System.out.println(source.next());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
