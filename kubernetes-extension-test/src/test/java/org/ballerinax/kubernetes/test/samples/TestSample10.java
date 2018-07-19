package org.ballerinax.kubernetes.test.samples;

import org.ballerinax.kubernetes.test.utils.KubernetesTestUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

public class TestSample10 implements SampleTester {

    @Test
    public void compileSample() throws IOException, InterruptedException {
        Assert.assertEquals(KubernetesTestUtils.compileBallerinaProject(
                (SAMPLE_DIR + File.separator + "sample10")), 0);
    }
}
