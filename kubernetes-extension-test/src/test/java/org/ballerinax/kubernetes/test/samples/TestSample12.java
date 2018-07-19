package org.ballerinax.kubernetes.test.samples;

import org.ballerinax.kubernetes.test.utils.KubernetesTestUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

public class TestSample12 implements SampleTester {

    @Test
    public void compileSample() throws IOException, InterruptedException {
        Assert.assertEquals(KubernetesTestUtils.compileBallerinaFile
                (SAMPLE_DIR + File.separator + "sample12",
                        "hello_world_copy_file.bal"), 0);
    }
}
