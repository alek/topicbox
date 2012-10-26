package com.voidsearch.topicbox.lda;

import cc.mallet.classify.tui.Csv2Vectors;
import cc.mallet.pipe.*;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

/**
 * Corpus of active documents
 */

public class TextCorpus extends Csv2Vectors {

    private AtomicInteger docCounter = new AtomicInteger();

    Pipe defaultPipe;
    InstanceList instances;

    public TextCorpus() {

        // list of pipes that will be applied when adding individual documents

        List<Pipe> pipeList = new ArrayList<Pipe>();

        pipeList.add(new CharSequence2TokenSequence(Pattern.compile(MalletConfig.tokenRegex.value())));

        pipeList.add(new TokenSequenceLowercase());
        pipeList.add(new TokenSequenceRemoveNonAlpha(true));

        TokenSequenceRemoveStopwords stopwordFilter =
                new TokenSequenceRemoveStopwords(false, MalletConfig.keepSequenceBigrams.value);

        pipeList.add(stopwordFilter);

        pipeList.add(new TokenSequence2FeatureSequence());
        defaultPipe = new SerialPipes(pipeList);
        instances = new InstanceList (defaultPipe);

    }

    public void add(Iterator<String> source) {
        while (source.hasNext()) {
            add(source.next());
        }
    }

    /**
     * add text snippet to existing corpus
     *
     * @param text
     */
    public void add(String text) {
        instances.add(defaultPipe.instanceFrom(new Instance(text, "foo", docCounter.getAndIncrement(), null)));
    }

    /**
     * get instance list
     *
     * @return
     */
    InstanceList getInstances() {
        return instances;
    }


    /**
     * get number of documents in the corpus
     *
     * @return
     */
    public int size() {
        return instances.size();
    }
    
}
