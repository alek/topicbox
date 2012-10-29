package com.voidsearch.topicbox.lda;

/**
 * Topic model generator thread
 */

public class TopicModelGenerator extends Thread {

    TopicModel model = new TopicModel();
    TextCorpus corpus;

    public void setCorpus(TextCorpus corpus) {
        this.corpus = corpus;
    }

    public void run() {
        try {
            model.update(corpus);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean modelComplete() {
        return model.hasTopics();
    }

    public TopicModel getModel() {
        return model;
    }

    public TextCorpus getCorpus() {
        return corpus;
    }

}
