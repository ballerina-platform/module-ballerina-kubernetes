package org.ballerinax.kubernetes.test.samples;

import org.ballerinax.kubernetes.test.utils.TestRunnerUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

public class TestSample5 implements SampleTester {

    @Test
    public void compileSample() throws IOException, InterruptedException {
        Assert.assertEquals(TestRunnerUtils.compileBallerinaFile
                (SAMPLE_DIR + File.separator + "sample5",
                        "pizzashack.bal"), 0);
    }
}
