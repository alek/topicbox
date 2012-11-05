package com.voidsearch.topicbox.lda;

import com.voidsearch.topicbox.source.LocalFileSource;
import org.codehaus.jackson.map.ObjectMapper;
import org.testng.annotations.Test;

import java.io.File;
import java.util.Map;

/**
 * simple task for topic model keyword->task generation
 */

public class GenerateKeywordTopicMatrixTest {

    @Test
    public void nullTest() throws Exception {

        // train simple model
        TopicModel model = new TopicModel();
        model.setNumTopics(10);
        TextCorpus corpus = new TextCorpus();
        corpus.add(new LocalFileSource<String>(new File("webapp/css/isotope.css")));
        model.queueUpdate(corpus);
        while (!model.modelComplete()) {
            Thread.sleep(1000);
        }

//        double[][] matrix = model.getKeywordTopicMatrix(5);
//
//        for (int i=0; i<matrix.length; i++) {
//            for (int j=0; j<matrix[i].length; j++) {
//                System.out.print(matrix[i][j] + " ");
//            }
//            System.out.println();
//        }


        Map result = model.getCooccurrenceMatrix(5);
        ObjectMapper mapper = new ObjectMapper();

        System.out.println(mapper.writeValueAsString(result));
        
    }

}
