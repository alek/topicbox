package com.voidsearch.topicbox.lda;

import com.voidsearch.topicbox.source.LocalFileSource;
import org.testng.annotations.Test;

import java.io.File;

public class TopicInferenceTest {

    @Test
    public void nullTest() throws Exception {
        
        TextCorpus corpus = new TextCorpus();
        corpus.add("foo bar baz classify me test");
        corpus.add("foo baz baz2 classify me test");
        corpus.add("somefoo baz baz2 classify me test waz maz zaz");

        TopicModel model = new TopicModel();
        model.update(corpus);

        for (String doc : corpus.getDocs()) {
            int topic = model.getMaxLikelihoodTopic(doc);
        }

    }

}
