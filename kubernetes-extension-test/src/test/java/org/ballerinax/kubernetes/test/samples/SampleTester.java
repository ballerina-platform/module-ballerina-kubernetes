package org.ballerinax.kubernetes.test.samples;

import java.io.IOException;

public interface SampleTester {
    String SAMPLE_DIR = System.getProperty("sample.dir");

    void compileSample() throws IOException, InterruptedException;
}
