package com.voidsearch.topicbox.source;

import com.voidsearch.topicbox.lda.TextCorpus;

import java.io.File;
import java.util.Iterator;

/**
 * simple task/data factory
 * TODO - replace this with configuration
 */

public class TopicboxDataSourceFactory {

    public enum TaskName {
        TWITTER,
        SYSLOG,
        SCHOLAR,
        CUSTOM
    }
    
    public static TextCorpus getData(String taskName, String dataSource) throws Exception {

        TextCorpus corpus = new TextCorpus();

        switch (TaskName.valueOf(taskName.toUpperCase())) {
            case TWITTER:
                //corpus.add(new HttpStreamingSource<String>("http://stream.twitter.com/1/statuses/sample.json?delimited=length"));
                //corpus.add(new HttpStreamingSource<String>("http://localhost/foo"));
                corpus.add(new LocalFileSource<String>(new File("/tmp/foo2")));
                break;
            case SYSLOG:
                corpus.add(new LocalFileSource<String>(new File("/var/log/launchd-shutdown.log")));
                break;
            case SCHOLAR:
                corpus.add(new LocalFileSource<String>(new File("data/sample/scholarly_work.tsv")));
                break;
            case CUSTOM:
                corpus.add(getSourceIterator(dataSource));
                break;
        }

        return corpus;
    }

    /**
     * get iterator corresponding to given uri
     *
     * @param uri
     * @return
     * @throws Exception
     */
    private static Iterator getSourceIterator(String uri) throws Exception {
        if (uri.startsWith("http://")) {
            return new HttpStreamingSource(uri);
        } else {
            if ((new File(uri).exists())) {
                return new LocalFileSource<String>(new File(uri));
            } else {
                throw new Exception("file [ " + uri + " ] does not exist");
            }
        }
    }

}
