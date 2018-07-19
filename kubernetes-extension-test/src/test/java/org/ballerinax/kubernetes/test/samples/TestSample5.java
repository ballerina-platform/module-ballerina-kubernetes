package org.ballerinax.kubernetes.test.samples;

import io.fabric8.docker.api.model.ImageInspect;
import io.fabric8.kubernetes.api.KubernetesHelper;
import io.fabric8.kubernetes.api.model.HorizontalPodAutoscaler;
import org.ballerinax.kubernetes.KubernetesConstants;
import org.ballerinax.kubernetes.test.utils.KubernetesTestUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

import static org.ballerinax.kubernetes.KubernetesConstants.DOCKER;
import static org.ballerinax.kubernetes.KubernetesConstants.KUBERNETES;
import static org.ballerinax.kubernetes.test.utils.KubernetesTestUtils.getDockerImage;

public class TestSample5 implements SampleTester {

    private final String sourceDirPath = SAMPLE_DIR + File.separator + "sample5";
    private final String targetPath = sourceDirPath + File.separator + KUBERNETES;
    private final String dockerImage = "ballerina.com/pizzashack:2.1.0";
    private final String selectorApp = "pizzashack";

    @BeforeClass
    public void compileSample() throws IOException, InterruptedException {
        Assert.assertEquals(KubernetesTestUtils.compileBallerinaFile(sourceDirPath, "pizzashack.bal"), 0);
    }

    @Test
    public void validateDockerfile() {
        File dockerFile = new File(targetPath + File.separator + DOCKER + File.separator + "Dockerfile");
        Assert.assertTrue(dockerFile.exists());
    }

    @Test
    public void validateDockerImage() {
        ImageInspect imageInspect = getDockerImage(dockerImage);
        Assert.assertEquals(2, imageInspect.getContainerConfig().getExposedPorts().size());
        Assert.assertTrue(imageInspect.getContainerConfig().getExposedPorts().keySet().contains("9090/tcp"));
        Assert.assertTrue(imageInspect.getContainerConfig().getExposedPorts().keySet().contains("9095/tcp"));
    }

    @Test
    public void validatePodAutoscaler() throws IOException {
        File hpaYAML = new File(targetPath + File.separator + "pizzashack_hpa.yaml");
        Assert.assertTrue(hpaYAML.exists());
        HorizontalPodAutoscaler podAutoscaler = KubernetesHelper.loadYaml(hpaYAML);
        Assert.assertEquals("pizzashack-hpa", podAutoscaler.getMetadata().getName());
        Assert.assertEquals(selectorApp, podAutoscaler.getMetadata().getLabels().get(KubernetesConstants
                .KUBERNETES_SELECTOR_KEY));
        Assert.assertEquals(2, podAutoscaler.getSpec().getMaxReplicas().intValue());
        Assert.assertEquals(1, podAutoscaler.getSpec().getMinReplicas().intValue());
        Assert.assertEquals(50, podAutoscaler.getSpec().getTargetCPUUtilizationPercentage().intValue());
        Assert.assertEquals("pizzashack-deployment", podAutoscaler.getSpec().getScaleTargetRef().getName());
    }
}
