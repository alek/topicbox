package com.voidsearch.topicbox.lda;

import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.types.InstanceList;

public class TopicModel {

    private static final int MAX_TOPICS = 10;       // max latent topics
    private static final int MAX_KEYWORDS = 25;     // max top keywords per topic
    
    private LocalParallelTopicModel topicModel;

    /**
     * update model from given text corpus
     * TODO : pass custom configuration
     *
     * @param corpus
     * @throws Exception
     */
    public void update(TextCorpus corpus) throws Exception {

        // get mallet training instances
        InstanceList trainingData = corpus.getInstances();
        
        //int numTopics = MalletConfig.numTopics.value;
        int numTopics = MAX_TOPICS;

        // create new topic model
        topicModel = new LocalParallelTopicModel(numTopics, MalletConfig.alpha.value, MalletConfig.beta.value);

        topicModel.setRandomSeed(MalletConfig.randomSeed.value);

        topicModel.addInstances(trainingData);

        topicModel.setTopicDisplay(MalletConfig.showTopicsInterval.value, MalletConfig.topWords.value);
        topicModel.setNumIterations(MalletConfig.numIterations.value);
        topicModel.setOptimizeInterval(MalletConfig.optimizeInterval.value);
        topicModel.setBurninPeriod(MalletConfig.optimizeBurnIn.value);
        topicModel.setSymmetricAlpha(MalletConfig.useSymmetricAlpha.value);

        topicModel.setNumThreads(MalletConfig.numThreads.value);
        
        topicModel.estimate();
        
    }

    public boolean hasTopics() {
        return (topicModel != null) && (topicModel.numTopics > 0);
    }

    public Object[][] getModelTopKeywords() {
        return topicModel.getTopWords(MAX_KEYWORDS);
    }

}
