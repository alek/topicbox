package com.voidsearch.topicbox.lda;

import com.voidsearch.topicbox.source.LocalFileSource;
import com.voidsearch.topicbox.source.TwitterStreamingSource;
import org.testng.annotations.Test;

import java.io.File;

public class TopicModelTest {

    @Test
    public void nullTest() {

        try {

            TextCorpus corpus = new TextCorpus();

//            corpus.add("foo bar baz classify me test");
//            corpus.add("foo baz baz2 classify me test");
//            corpus.add("somefoo baz baz2 classify me test");
//
//            corpus.add(new LocalFileSource<String>(new File("/var/log/launchd-shutdown.log")));

            corpus.add(new TwitterStreamingSource("http://localhost/foo"));

            TopicModel model = new TopicModel();
            model.update(corpus);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
