package com.voidsearch.topicbox.lda;

import cc.mallet.topics.ParallelTopicModel;

import java.io.IOException;

/**
 * local override of parallel topic model generator
 * providing alternative implementation & support for monitoring of estimation process
 */

public class LocalParallelTopicModel extends ParallelTopicModel {

    public LocalParallelTopicModel(int numberOfTopics, double alphaSum, double beta) {
        super(numberOfTopics, alphaSum, beta);
    }

    @Override
    public void estimate () throws IOException {
        super.estimate();
        // TBD - re-implement mallet's estimate + provide facilities for process monitoring
    }

}
