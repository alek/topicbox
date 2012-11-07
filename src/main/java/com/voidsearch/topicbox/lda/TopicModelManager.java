package com.voidsearch.topicbox.lda;

import com.voidsearch.topicbox.server.TopicboxServer;
import com.voidsearch.topicbox.source.TopicboxDataSourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * TopicModelManager
 * <p/>
 * manage retrieval & submission of task estimation tasks
 */

public class TopicModelManager {

    private static final Logger logger = LoggerFactory.getLogger(TopicboxServer.class.getName());

    Queue<TopicModel> models = new ConcurrentLinkedQueue<TopicModel>();

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
     * check whether given model has been submitted
     *
     * @param taskName
     * @param numTopics
     * @param dataSource
     * @return
     */
    public boolean containsModel(String taskName, int numTopics, String dataSource) {
        return getModel(taskName, numTopics, dataSource) != null;
    }

    public boolean containsModel(String taskName, int numTopics) {
        return containsModel(taskName, numTopics, null);
    }

    /**
     * get model corresponding to given task
     *
     * @param taskName
     * @param numTopics
     * @param dataSource
     * @return
     */
    public TopicModel getModel(String taskName, int numTopics, String dataSource) {

        for (TopicModel model : models) {

            if (taskName.equals(model.getTaskName())
                    && numTopics == model.getNumTopics()) {

                if (dataSource == null || dataSource.equals(model.getDataSource())) {
                    return model;
                }

            }
        }
        return null;
    }

    public TopicModel getModel(String taskName, int numTopics) {
        return getModel(taskName, numTopics, null);
    }

    /**
     * get model descriptions
     *
     * @return
     */
    public Set<TopicModel.ModelDescription> getModelDescriptions() {
        Set<TopicModel.ModelDescription> result = new HashSet<TopicModel.ModelDescription>();
        for (TopicModel model : models) {
            result.add(model.getDesciption());
        }
        return result;
    }

    /**
     * get number of models currently available
     *
     * @return
     */
    public int getModelCount() {
        return models.size();
    }

    /**
     * submit lda estimation task
     *
     * @param taskName
     */
    public void submitTask(String taskName, String dataSource, int numTopics) throws Exception {

        if (logger.isDebugEnabled()) {
            logger.info("submitting task : " + taskName);
        }

        if (!containsModel(taskName, numTopics, dataSource)) {

            TopicModel model = new TopicModel(taskName);
            model.setDataSource(dataSource);
            if (numTopics > 0) {
                model.setNumTopics(numTopics);
            }
            model.queueUpdate(TopicboxDataSourceFactory.getData(taskName, dataSource));
            models.add(model);
        }

    }

}
