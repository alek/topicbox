package com.voidsearch.topicbox.lda;

import com.voidsearch.topicbox.source.LocalFileSource;
import org.testng.annotations.Test;

import java.io.File;

public class TopicModelTest {

    @Test
    public void nullTest() {

        try {

            TextCorpus corpus = new TextCorpus();

            corpus.add("foo bar baz classify me test");
            corpus.add("foo baz baz2 classify me test");
            corpus.add("somefoo baz baz2 classify me test waz maz zaz");

//            corpus.add(new LocalFileSource<String>(new File("/var/log/launchd-shutdown.log")));
//            corpus.add(new HttpStreamingSource("http://localhost/foo"));

            TopicModel model = new TopicModel("foo");
            model.update(corpus);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
