package com.voidsearch.topicbox.lda;

import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.topics.TopicInferencer;
import cc.mallet.topics.WorkerRunnable;
import cc.mallet.types.IDSorter;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import cc.mallet.util.Randoms;
import com.sun.org.apache.xalan.internal.xsltc.compiler.util.ObjectType;
import com.sun.org.apache.xml.internal.utils.ObjectPool;
import com.voidsearch.topicbox.util.TopicboxUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

public class TopicModel {

    private static final int MAX_TOPICS = 10;       // max latent topics
    private static final int MAX_KEYWORDS = 25;     // max top keywords per topic
    private static final int INFERENCER_ITERATIONS = 5;

    private int numTopics;
    private TextCorpus trainingCorpus;
    private SimpleTopicModel topicModel;
    private TopicInferencer inferencer;

    /**
     * update model from given text corpus
     * TODO : pass custom configuration
     *
     * @param corpus
     * @throws Exception
     */
    public void update(TextCorpus corpus) throws Exception {

        this.trainingCorpus = corpus;

        // get mallet training instances
        InstanceList trainingData = corpus.getInstances();

        //int numTopics = MalletConfig.numTopics.value;
        numTopics = MAX_TOPICS;

        // create new topic model
        topicModel = new SimpleTopicModel(numTopics, MalletConfig.alpha.value, MalletConfig.beta.value);

        topicModel.setRandomSeed(MalletConfig.randomSeed.value);

        topicModel.addInstances(trainingData);

        topicModel.setTopicDisplay(MalletConfig.showTopicsInterval.value, MalletConfig.topWords.value);
        topicModel.setNumIterations(MalletConfig.numIterations.value);
        topicModel.setOptimizeInterval(MalletConfig.optimizeInterval.value);
        topicModel.setBurninPeriod(MalletConfig.optimizeBurnIn.value);
        topicModel.setSymmetricAlpha(MalletConfig.useSymmetricAlpha.value);

        topicModel.setNumThreads(MalletConfig.numThreads.value);

        topicModel.estimate();

        inferencer = topicModel.getInferencer();

    }

    /**
     * get maximum likelihood topic corresponding to given text
     *
     * @param text
     * @return
     */
    public int getMaxLikelihoodTopic(String text) {

        Instance instance = trainingCorpus.getInstance(text);

        // TODO : make inferencer params configurable
        double[] topicDistribution = inferencer.getSampledDistribution(instance, INFERENCER_ITERATIONS, 1, 1);

        return TopicboxUtil.getMaxPosition(topicDistribution);

    }

    /**
     * get maximum likelihood topic matrix for given doc collection
     * TODO : implement this properly
     *
     * @param docs
     * @return
     */
    public Object[][] inferTopics(List<String> docs) {

        List[] topics = new ArrayList[numTopics];
        
        for (String doc : docs) {
            int topicNumber = getMaxLikelihoodTopic(doc);
            if (topics[topicNumber] == null) {
                topics[topicNumber] = new ArrayList();
            }
            topics[topicNumber].add(doc);
        }

        Object[][] result = new Object[numTopics][];
        for (int i=0; i<numTopics; i++) {
            result[i] = topics[i].toArray();
        }

        return result;
    }


    // TODO : refactor this / no need for proxy methods

    public boolean hasTopics() {
        return (topicModel != null) && (topicModel.numTopics > 0);
    }

    /**
     * get array of top keywords for each topics along with associated probabilities
     *
     * @return
     */
    public Object[][] getModelTopKeywords() {

        ArrayList<TreeSet<IDSorter>> topicSortedWords = topicModel.getSortedWords();
        Object[][] result = new Object[numTopics][];

        for (int i=0; i<numTopics; i++) {

            TreeSet<IDSorter> sortedWords = topicSortedWords.get(i);

            int limit = (sortedWords.size() < MAX_KEYWORDS) ? sortedWords.size() : MAX_KEYWORDS;
            result[i] = new Object[limit][2];
            int entryCnt = 0;

            for (IDSorter ids : sortedWords) {
                result[i][entryCnt++] = new Object[] { topicModel.getAlphabet().lookupObject(ids.getID()), ids.getWeight() };
                if (entryCnt == MAX_KEYWORDS) {
                    break;
                }
            }

        }

        return result;
        
    }

    public boolean modelComplete() {
        return topicModel != null && topicModel.estimationComplete();
    }

    public long getExpectedCompletionTime() {
        return topicModel.getExpectedCompleteTime();
    }

    public boolean estimationStarted() {
        return topicModel != null && topicModel.estimationStarted();
    }

    /**
     * simple single-threaded extension of mallet's topic model
     * allowing for tracking of estimation process
     */
    private class SimpleTopicModel extends ParallelTopicModel {

        private int iteration = 0;

        private boolean estimationStarted = false;
        private boolean estimationComplete = false;

        private long lastIterationTime = 0;

        public SimpleTopicModel(int numberOfTopics, double alphaSum, double beta) {
            super(numberOfTopics, alphaSum, beta);
        }

        /**
         * get model estimation iteration number
         *
         * @return
         */
        public int getIteration() {
            return iteration;
        }

        /**
         * check whether model estimation has completed
         *
         * @return
         */
        public boolean estimationComplete() {
            return estimationComplete;
        }

        public boolean estimationStarted() {
            return estimationStarted;
        }

        /**
         * get estimated time-to-complete for given estimation task
         *
         * @return
         */
        public long getExpectedCompleteTime() {
            return lastIterationTime * (numIterations - getIteration());
        }

        /**
         * estimate model / simple single-threaded iteration
         *
         * @throws java.io.IOException
         */
        @Override
        public void estimate() throws IOException {

            Randoms random = new Randoms();
            WorkerRunnable worker = new WorkerRunnable(numTopics, alpha, alphaSum, beta, random,
                    data, typeTopicCounts, tokensPerTopic, 0, data.size());

            worker.initializeAlphaStatistics(docLengthCounts.length);
            worker.makeOnlyThread();

            estimationStarted = true;

            for (iteration = 1; iteration <= numIterations; iteration++) {

                long start = System.currentTimeMillis();

                displayTopWords(wordsPerTopic, false);

                if (iteration > burninPeriod && optimizeInterval != 0
                        && iteration % saveSampleInterval == 0) {
                    worker.collectAlphaStatistics();
                }

                worker.run();

                if (iteration > burninPeriod && optimizeInterval != 0
                        && iteration % optimizeInterval == 0) {
                    optimizeAlpha(new WorkerRunnable[]{worker});
                    optimizeBeta(new WorkerRunnable[]{worker});
                }

                lastIterationTime = System.currentTimeMillis() - start;

            }

            estimationComplete = true;

        }

    }

}
