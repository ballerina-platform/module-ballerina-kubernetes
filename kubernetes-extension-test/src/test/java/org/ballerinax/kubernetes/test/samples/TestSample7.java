package org.ballerinax.kubernetes.test.samples;

import io.fabric8.docker.api.model.ImageInspect;
import org.ballerinax.kubernetes.test.utils.KubernetesTestUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

import static org.ballerinax.kubernetes.KubernetesConstants.DOCKER;
import static org.ballerinax.kubernetes.KubernetesConstants.KUBERNETES;
import static org.ballerinax.kubernetes.test.utils.KubernetesTestUtils.getDockerImage;

public class TestSample7 implements SampleTester {

    private final String sourceDirPath = SAMPLE_DIR + File.separator + "sample7";
    private final String targetPath = sourceDirPath + File.separator + KUBERNETES;
    private final String dockerImage = "hello_world_secret_mount_k8s:latest";
    private final String selectorApp = "pizzashack";

    @BeforeClass
    public void compileSample() throws IOException, InterruptedException {
        Assert.assertEquals(KubernetesTestUtils.compileBallerinaFile(sourceDirPath, "hello_world_secret_mount_k8s" +
                ".bal"), 0);
    }

    @Test
    public void validateDockerfile() {
        File dockerFile = new File(targetPath + File.separator + DOCKER + File.separator + "Dockerfile");
        Assert.assertTrue(dockerFile.exists());
    }

    @Test
    public void validateDockerImage() {
        ImageInspect imageInspect = getDockerImage(dockerImage);
        Assert.assertEquals(1, imageInspect.getContainerConfig().getExposedPorts().size());
        Assert.assertTrue(imageInspect.getContainerConfig().getExposedPorts().keySet().contains("9090/tcp"));
    }


}
