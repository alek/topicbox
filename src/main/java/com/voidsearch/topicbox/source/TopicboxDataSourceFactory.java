package com.voidsearch.topicbox.source;

import com.voidsearch.topicbox.lda.TextCorpus;

import java.io.File;

/**
 * simple task/data factory
 * TODO - replace this with configuration
 */

public class TopicboxDataSourceFactory {

    public enum TaskName {
        TWITTER,
        SYSLOG,
        RSS,
        CUSTOM
    }
    
    public static TextCorpus getData(String taskName) throws Exception {

        TextCorpus corpus = new TextCorpus();

        switch (TaskName.valueOf(taskName.toUpperCase())) {
            case TWITTER:
                //corpus.add(new HttpStreamingSource<String>("http://stream.twitter.com/1/statuses/sample.json?delimited=length"));
                //corpus.add(new HttpStreamingSource<String>("http://localhost/foo"));
                corpus.add(new LocalFileSource<String>(new File("/tmp/foo")));
                break;
            case SYSLOG:
                corpus.add(new LocalFileSource<String>(new File("/var/log/launchd-shutdown.log")));
                break;
            case RSS:
                corpus.add(new LocalFileSource<String>(new File("data/sample/scholarly_work.tsv")));
                break;
            case CUSTOM:
                corpus.add(new LocalFileSource<String>(new File("/tmp/foo")));
                break;
        }

        return corpus;
    }

}
