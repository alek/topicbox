package com.voidsearch.topicbox.lda;

import com.voidsearch.topicbox.server.TopicboxServer;
import com.voidsearch.topicbox.source.TopicboxDataSourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * TopicModelTaskManager
 *
 * manage retrieval & submission of task estimation tasks
 *
 */

public class TopicModelTaskManager {

    private static final Logger logger = LoggerFactory.getLogger(TopicboxServer.class.getName());

    Map<String, TopicModelGenerator> taskMap = new ConcurrentHashMap<String, TopicModelGenerator>();

    private TopicModelTaskManager() {}

    private static class TopicModelTaskManagerHolder {
        public static final TopicModelTaskManager INSTANCE = new TopicModelTaskManager();
    }

    /**
     * get task manager instance
     *
     * @return
     */
    public static TopicModelTaskManager getInstance() {
        return TopicModelTaskManagerHolder.INSTANCE;
    }

    /**
     * get model generator corresponding to given task
     *
     * @param taskName
     * @return
     */
    public TopicModelGenerator getGenerator(String taskName) {
        return taskMap.get(taskName);
    }

    /**
     * get number of models currently available
     *
     * @return
     */
    public int getModelCount() {
        return taskMap.size();
    }
    
    /**
     * get model corresponding to given task
     *
     * @param taskName
     * @return
     */
    public TopicModel getModel(String taskName) {
        return taskMap.get(taskName).getModel();
    }

    /**
     * get random model
     * (for testing)
     *
     * @return
     */
    public TopicModel getRandomModel() {
        return getRandomModelGenerator().getModel();
    }


    /**
     * get a random model generator
     * (for testing)
     *
     * @return
     */
    public TopicModelGenerator getRandomModelGenerator() {
        return taskMap.get(new ArrayList(taskMap.keySet()).get((new Random().nextInt(taskMap.size()))));
    }

    /**
     * submit lda estimation task
     *
     * @param taskName
     */
    public void submitTask(String taskName) {

        if (logger.isDebugEnabled()) {
            logger.info("submitting task : " + taskName);
        }

        if (!taskMap.containsKey(taskName)) {
            try {
                TopicModelGenerator generator = new TopicModelGenerator();
                generator.setCorpus(TopicboxDataSourceFactory.getData(taskName));
                generator.start();
                taskMap.put(taskName, generator);
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("error submitting task : " + taskName);
            }
        }

    }

}
