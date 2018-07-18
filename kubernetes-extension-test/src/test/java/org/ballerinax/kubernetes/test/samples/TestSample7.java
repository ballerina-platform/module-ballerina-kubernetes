package org.ballerinax.kubernetes.test.samples;

import org.ballerinax.kubernetes.test.utils.TestRunnerUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

public class TestSample7 implements SampleTester {

    @Test
    public void compileSample() throws IOException, InterruptedException {
        Assert.assertEquals(TestRunnerUtils.compileBallerinaFile
                (SAMPLE_DIR + File.separator + "sample7",
                        "hello_world_secret_mount_k8s.bal"), 0);
    }
}
