package com.voidsearch.topicbox.util;

public class TopicboxUtil {
    
    public static int getMaxPosition(double[] vals) {

        int maxPosition = 0;
        double maxVal = Double.MIN_VALUE;

        for (int i=0; i<vals.length; i++) {
            if (vals[i] > maxVal) {
                maxPosition = i;
                maxVal = vals[i];
            }
        }

        return maxPosition;

    }
}
