package com.voidsearch.topicbox.util;

import java.util.HashMap;
import java.util.Map;

public class TopicboxUtil {


    /**
     * unpack uri query params string into simple map
     * TODO : implement this properly
     * @param query
     * @return
     */
    public static Map<String, String> unpackQueryParams(String query) {
        Map<String, String> result = new HashMap<String, String>();
        String[] parts = query.split("&");
        for (String part : parts) {
            String[] keyval = part.split("=");
            if (keyval.length == 2) {
                result.put(keyval[0], keyval[1]);
            }
        }
        return result;
    }

    /**
     * get position of max in array
     * TODO : implement proper generic with comparable
     *
     * @param vals
     * @return
     */
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
