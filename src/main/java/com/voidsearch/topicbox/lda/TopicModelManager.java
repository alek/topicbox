package com.voidsearch.topicbox.lda;

import com.voidsearch.topicbox.server.TopicboxServer;
import com.voidsearch.topicbox.source.TopicboxDataSourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * TopicModelManager
 *
 * manage retrieval & submission of task estimation tasks
 *
 */

public class TopicModelManager {

    private static final Logger logger = LoggerFactory.getLogger(TopicboxServer.class.getName());

    Map<String, TopicModel> taskMap = new ConcurrentHashMap<String, TopicModel>();

    private TopicModelManager() {}

    private static class TopicModelTaskManagerHolder {
        public static final TopicModelManager INSTANCE = new TopicModelManager();
    }

    /**
     * get task manager instance
     *
     * @return
     */
    public static TopicModelManager getInstance() {
        return TopicModelTaskManagerHolder.INSTANCE;
    }

    /**
     * get model corresponding to given task
     *
     * @param taskName
     * @return
     */
    public TopicModel getModel(String taskName) {
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
    
    public boolean containsModel(String taskName) {
        return taskMap.containsKey(taskName);
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
                TopicModel model = new TopicModel();
                model.queueUpdate(TopicboxDataSourceFactory.getData(taskName));
                taskMap.put(taskName, model);
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("error submitting task : " + taskName);
            }
        }

    }

}
