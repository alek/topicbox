package com.voidsearch.topicbox.lda;

import org.testng.annotations.Test;

public class TextCorpusTest {

    @Test
    public void nullTest() {
        TextCorpus corpus = new TextCorpus();
        corpus.add("foo bar baz classify me test");
        corpus.add("foo baz baz2 classify me test");
        corpus.add("somefoo baz baz2 classify me test");
    }
}
