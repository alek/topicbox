package com.voidsearch.topicbox.source;

import org.testng.annotations.Test;

import java.io.File;

public class LocalFileSourceTest {

    @Test
    public void nullTest() throws Exception {
        LocalFileSource<String> source = new LocalFileSource<String>(new File("/etc/"));
        while (source.hasNext()) {
            System.out.println(source.next());
        }
    }
}
