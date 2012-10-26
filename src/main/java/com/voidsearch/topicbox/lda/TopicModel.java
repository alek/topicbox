package com.voidsearch.topicbox.lda;

import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.types.InstanceList;

public class TopicModel {

    /**
     * update model from given text corpus
     *
     * @param corpus
     * @throws Exception
     */
    public void update(TextCorpus corpus) throws Exception {

        // get mallet training instances
        InstanceList trainingData = corpus.getInstances();
        
        //int numTopics = MalletConfig.numTopics.value;
        int numTopics = 50;

        // create new topic model
        ParallelTopicModel topicModel = new ParallelTopicModel (numTopics, MalletConfig.alpha.value, MalletConfig.beta.value);

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

}
