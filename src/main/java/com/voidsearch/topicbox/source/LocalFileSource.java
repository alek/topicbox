package com.voidsearch.topicbox.source;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Iterator;

public class LocalFileSource<T> implements Iterator<T> {
    
    BufferedReader rdr;
    T currentEntry;
    
    public LocalFileSource(File inputFile) {
        try {
            rdr = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile)));
        } catch (Exception e) {
            e.printStackTrace();
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
            currentEntry = (T)rdr.readLine();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public T next(){
        return currentEntry;
    }
}
