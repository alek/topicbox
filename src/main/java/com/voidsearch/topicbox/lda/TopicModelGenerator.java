package com.voidsearch.topicbox.lda;

/**
 * Topic model generator thread
 */

public class TopicModelGenerator extends Thread {

    private TopicModel model = new TopicModel();
    private TextCorpus corpus;

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

    public boolean running() {
        return model.estimationStarted();
    }

    public boolean modelComplete() {
        return model.modelComplete();
    }

    public TopicModel getModel() {
        return model;
    }

    public TextCorpus getCorpus() {
        return corpus;
    }

}
