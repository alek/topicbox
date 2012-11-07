package com.voidsearch.topicbox.lda;

import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.topics.TopicInferencer;
import cc.mallet.topics.WorkerRunnable;
import cc.mallet.types.IDSorter;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import cc.mallet.util.Randoms;
import com.voidsearch.topicbox.util.TopicboxUtil;

import java.io.IOException;
import java.util.*;

public class TopicModel {

    private static final int MAX_TOPICS = 50;       // max latent topics
    private static final int MAX_KEYWORDS = 25;     // max top keywords per topic
    private static final int INFERENCER_ITERATIONS = 5;

    private String taskName;
    private int numTopics;
    private String dataSource;

    private TextCorpus trainingCorpus;
    private SimpleTopicModel topicModel;
    private TopicInferencer inferencer;
    
    private String[] topicNames;

    public TopicModel(String taskName) {
        this.taskName = taskName;
        numTopics = MAX_TOPICS;
    }
    
    public String getTaskName() {
        return taskName;
    }

    /**
     * set number of topics to be estimated in given model
     *
     * @param numTopics
     */
    public void setNumTopics(int numTopics) {
        this.numTopics = numTopics;
    }

    /**
     * get number of topics to be estimated
     *
     * @return
     */
    public int getNumTopics() {
        return numTopics;
    }
    
    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }

    public String getDataSource() {
        return dataSource;
    }

    /**
     * get description of current model state
     *
     * @return
     */
    public ModelDescription getDesciption() {
        return new ModelDescription(this);
    }

    /**
     * queue text corpus for model update
     *
     * @param corpus
     * @throws Exception
     */
    public void queueUpdate(TextCorpus corpus) throws Exception {
        TopicModelGenerator generator = new TopicModelGenerator();
        generator.setCorpus(corpus);
        generator.setModel(this);
        generator.start();
    }

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
        
        topicNames = new String[numTopics];

        inferencer = topicModel.getInferencer();

    }

    public TextCorpus getCorpus() {
        return trainingCorpus;
    }

    public boolean ready() {
        return inferencer != null;
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
            if (topics[i] != null) {
                result[i] = topics[i].toArray();
            }
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
        return getModelTopKeywords(MAX_KEYWORDS);
    }

    public Object[][] getModelTopKeywords(int maxKeywords) {

        ArrayList<TreeSet<IDSorter>> topicSortedWords = topicModel.getSortedWords();
        Object[][] result = new Object[numTopics][];

        for (int i=0; i<numTopics; i++) {

            TreeSet<IDSorter> sortedWords = topicSortedWords.get(i);

            int limit = (sortedWords.size() < maxKeywords) ? sortedWords.size() : maxKeywords;
            result[i] = new Object[limit][2];
            int entryCnt = 0;

            for (IDSorter ids : sortedWords) {
                result[i][entryCnt++] = new Object[] { topicModel.getAlphabet().lookupObject(ids.getID()), ids.getWeight() };
                if (entryCnt == maxKeywords) {
                    break;
                }
            }

        }

        return result;
        
    }

    /**
     * get model information regarding given keyword
     *
     * @param keyword
     * @return
     */
    public Map getKeywordInfo(String keyword) {

        Map<String, Object> result = new HashMap<String, Object>();

        result.put("topicEntries", getTopicEntries(keyword));

        // TODO get number of occurences in source data & add to result

        return result;
    }

    /**
     * get cross-topic distribution for given keyword
     * TODO : replace with more sensible structures
     *
     * @param keyword
     * @return
     */
    private List getTopicEntries(String keyword) {

        List topicEntries = new ArrayList();

        ArrayList<TreeSet<IDSorter>> topicSortedWords = topicModel.getSortedWords();

        for (int i=0; i<numTopics; i++) {
            TreeSet<IDSorter> sortedWords = topicSortedWords.get(i);
            for (IDSorter ids : sortedWords) {
                String entry = topicModel.getAlphabet().lookupObject(ids.getID()).toString();
                if (keyword.equals(entry)) {
                    topicEntries.add(new Object[]{i, ids});
                }
            }
        }

        return topicEntries;
    }

    /**
     * get model information regarding given topic
     *
     * @param topicNumber
     * @return
     */
    public Map getTopicInfo(int topicNumber) {
        return null;
    }


    /**
     * generate keyword->topic probability matrix
     * for top @maxKeywords in each topic
     *
     * @param maxKeywordsPerTopic
     */
    public double[][] getKeywordTopicMatrix(int maxKeywordsPerTopic) {

        double[][] result = new double[numTopics*maxKeywordsPerTopic][numTopics];

        // get top keywords & retrieve topic distribution
        
        Object[][] topKeywords = getModelTopKeywords(maxKeywordsPerTopic);

        // TODO - cleanup the Object[][] mess
        for (int topic=0; topic < topKeywords.length; topic++) {
            for (int keywordId=0; keywordId < topKeywords[topic].length; keywordId++) {
                Object[] keywordEntry = ((Object[])topKeywords[topic][keywordId]);
                for (Object entry : getTopicEntries((String)keywordEntry[0])) {
                    Integer topicNumber = (Integer)((Object[])entry)[0];
                    IDSorter sorterEntry = (IDSorter)((Object[])entry)[1];
                    result[topic*maxKeywordsPerTopic + keywordId][topicNumber] = sorterEntry.getWeight();
                }
            }
        }

        return result;

    }

    /**
     * get complete co-occurence matrix & associated label data
     * (prepared for d3-friendly json serialization)
     *
     * TODO : return more efficient structure & reformat for d3 at service handler side
     * 
     * @param maxKeywordsPerTopic
     * @return
     */
    public Map getCooccurrenceMatrix(int maxKeywordsPerTopic) {

        Map result = new HashMap<String, Object>();

        // create node list

        List<Map> nodeList = new ArrayList<Map>();

        Object[][] topKeywords = getModelTopKeywords(maxKeywordsPerTopic);

        for (int topic=0; topic < topKeywords.length; topic++) {
            for (int keywordId=0; keywordId < topKeywords[topic].length; keywordId++) {

                Object[] keywordEntry = ((Object[])topKeywords[topic][keywordId]);
                String keyword = (String)keywordEntry[0];

                Map nodeEntry = new HashMap();
                nodeEntry.put("name", keyword);
                nodeEntry.put("group", topic+1);
                nodeList.add(nodeEntry);

            }

        }

        result.put("nodes", nodeList);

        // create links list

        List<Map> linksList = new ArrayList<Map>();

        double[][] keywordTopic = getKeywordTopicMatrix(maxKeywordsPerTopic);

        for (int keywordID = 0; keywordID < keywordTopic.length; keywordID++) {
            for (int topic=0; topic < keywordTopic[keywordID].length; topic++) {
                if (keywordTopic[keywordID][topic] != 0) {
                    for (int j=0; j<keywordTopic.length; j++) {
                        if (keywordTopic[j][topic] != 0) {
                            Map listEntry = new HashMap();
                            listEntry.put("source", keywordID);
                            listEntry.put("target", j);
                            listEntry.put("value", topic);
                            linksList.add(listEntry);
                        }
                    }
                }
            }
        }

        result.put("links", linksList);

        return result;
    }

    /**
     * check whether model estimation is complete
     *
     * @return
     */
    public boolean modelComplete() {
        return topicModel != null && topicModel.estimationComplete();
    }


    /**
     * get expected time-to-completion for model estimation
     *
     * @return
     */
    public long getExpectedCompletionTime() {
        return topicModel.getExpectedCompleteTime();
    }

    /**
     * check whether estimation process for given model had started
     *
     * @return
     */
    public boolean estimationStarted() {
        return topicModel != null && topicModel.estimationStarted();
    }

    /**
     * update topic name
     *
     * @param topicNumber
     * @param name
     */
    public void updateTopicName(int topicNumber, String name) {
        topicNames[topicNumber] = name;
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

    /**
     * simple model-updating thread
     */
    private class TopicModelGenerator extends Thread {

        private TopicModel model;
        private TextCorpus corpus;

        public void setModel(TopicModel model) {
            this.model = model;
        }
        
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

    }


    /**
     * simple model description
     * (for jackson's direct bean-style serialization)
     */
    public class ModelDescription {

        String taskName;
        int numTopics;
        String dataSource;
        boolean estimationStarted;
        boolean modelComplete;

        public ModelDescription(TopicModel model) {
            this.taskName = model.getTaskName();
            this.numTopics = model.getNumTopics();
            this.dataSource = model.getDataSource();
            this.estimationStarted = model.estimationStarted();
            this.modelComplete = model.modelComplete();
        }

        public String getTaskName() {
            return taskName;
        }

        public String getDataSource() {
            return dataSource;
        }

        public int getNumTopics() {
            return numTopics;
        }

        public boolean isEstimationStarted() {
            return estimationStarted;
        }

        public boolean isModelComplete() {
            return modelComplete;
        }

    }

}
