package com.voidsearch.topicbox.source;

import java.io.*;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

public class LocalFileSource<T> implements Iterator<T> {

    private BufferedReader rdr;
    private T currentEntry;

    ConcurrentLinkedQueue<BufferedReader> readerQueue = new ConcurrentLinkedQueue<BufferedReader>();

    public LocalFileSource(File inputFile) throws Exception {
        if (inputFile.isDirectory()) {
            for (File file : inputFile.listFiles())  {
                if (!file.isHidden() && !file.isDirectory()) {
                    try {
                        readerQueue.add(new BufferedReader(new InputStreamReader(new FileInputStream(file))));
                    } catch (FileNotFoundException e) { } // ignore inaccessible files
                }
            }
        } else {
            readerQueue.add(new BufferedReader(new InputStreamReader(new FileInputStream(inputFile))));
        }
    }

    public boolean hasNext() {
        readNext();
        return currentEntry != null;
    }

    public void remove() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void readNext() {
        try {
            if (readerQueue.size() > 0) {
                currentEntry = (T)readerQueue.peek().readLine();
                if (currentEntry == null) {
                    readerQueue.peek().close();
                    readerQueue.remove();
                    readNext();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public T next() {
        return currentEntry;
    }
}
