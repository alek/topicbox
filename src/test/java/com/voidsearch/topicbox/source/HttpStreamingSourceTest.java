package com.voidsearch.topicbox.source;

import org.testng.annotations.Test;

public class HttpStreamingSourceTest {

    @Test
    public void nullTest() {
        try {
            //HttpStreamingSource source = new HttpStreamingSource("http://stream.twitter.com/1/statuses/sample.json?delimited=length");
            HttpStreamingSource source = new HttpStreamingSource("http://localhost/foo");
            while (source.hasNext()) {
                System.out.println(source.next());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
