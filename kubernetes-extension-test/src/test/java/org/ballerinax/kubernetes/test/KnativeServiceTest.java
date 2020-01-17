package org.ballerinax.kubernetes.test;

import com.spotify.docker.client.messages.ImageInfo;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import org.ballerinax.kubernetes.exceptions.KubernetesPluginException;
import org.ballerinax.kubernetes.test.utils.DockerTestException;
import org.ballerinax.kubernetes.test.utils.KnativeTestUtils;
import org.ballerinax.kubernetes.utils.KnativeUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.ballerinax.kubernetes.KubernetesConstants.DOCKER;
import static org.ballerinax.kubernetes.KubernetesConstants.KUBERNETES;
import static org.ballerinax.kubernetes.test.utils.KubernetesTestUtils.getCommand;
import static org.ballerinax.kubernetes.test.utils.KubernetesTestUtils.getDockerImage;
import static org.ballerinax.kubernetes.test.utils.KubernetesTestUtils.getExposedPorts;

public class KnativeServiceTest {

    private static final Path BAL_DIRECTORY = Paths.get("src", "test", "resources", "deployment");
    private static final Path DOCKER_TARGET_PATH = BAL_DIRECTORY.resolve(DOCKER);
    private static final Path KUBERNETES_TARGET_PATH = BAL_DIRECTORY.resolve(KUBERNETES);
    private static final String DOCKER_IMAGE = "pizza-shop:latest";

    /**
     * Build bal file with deployment having annotations.
     *
     * @throws IOException               Error when loading the generated yaml.
     * @throws InterruptedException      Error when compiling the ballerina file.
     * @throws KubernetesPluginException Error when deleting the generated artifacts folder.
     */
    @Test
    public void annotationsTest() throws IOException, InterruptedException, KubernetesPluginException,
            DockerTestException {
        Assert.assertEquals(KnativeTestUtils.compileBallerinaFile(BAL_DIRECTORY, "dep_annotations.bal"), 0);

        // Check if docker image exists and correct
        validateDockerfile();
        validateDockerImage();

        // Validate deployment yaml
        File deploymentYAML = KUBERNETES_TARGET_PATH.resolve("dep_annotations_deployment.yaml").toFile();
        Assert.assertTrue(deploymentYAML.exists());
        Deployment deployment = KnativeTestUtils.loadYaml(deploymentYAML);
        Assert.assertEquals(deployment.getMetadata().getAnnotations().size(), 2,
                "Invalid number of annotations found.");
        Assert.assertEquals(deployment.getMetadata().getAnnotations().get("anno1"), "anno1Val",
                "Invalid annotation found.");
        Assert.assertEquals(deployment.getMetadata().getAnnotations().get("anno2"), "anno2Val",
                "Invalid annotation found.");

        KnativeUtils.deleteDirectory(KUBERNETES_TARGET_PATH);
        KnativeUtils.deleteDirectory(DOCKER_TARGET_PATH);
        KnativeTestUtils.deleteDockerImage(DOCKER_IMAGE);
    }

    /**
     * Build bal file with deployment having pod annotations.
     *
     * @throws IOException               Error when loading the generated yaml.
     * @throws InterruptedException      Error when compiling the ballerina file.
     * @throws KubernetesPluginException Error when deleting the generated artifacts folder.
     */
    @Test
    public void podAnnotationsTest() throws IOException, InterruptedException, KubernetesPluginException,
            DockerTestException {
        Assert.assertEquals(KnativeTestUtils.compileBallerinaFile(BAL_DIRECTORY, "pod_annotations.bal"), 0);

        // Check if docker image exists and correct
        validateDockerfile();
        validateDockerImage();

        // Validate deployment yaml
        File deploymentYAML = KUBERNETES_TARGET_PATH.resolve("pod_annotations_deployment.yaml").toFile();
        Assert.assertTrue(deploymentYAML.exists());
        Deployment deployment = KnativeTestUtils.loadYaml(deploymentYAML);
        Assert.assertEquals(deployment.getSpec().getTemplate().getMetadata().getAnnotations().size(), 2,
                "Invalid number of annotations found.");
        Assert.assertEquals(deployment.getSpec().getTemplate().getMetadata().getAnnotations().get("anno1"), "anno1Val",
                "Invalid annotation found.");
        Assert.assertEquals(deployment.getSpec().getTemplate().getMetadata().getAnnotations().get("anno2"), "anno2Val",
                "Invalid annotation found.");

        KnativeUtils.deleteDirectory(KUBERNETES_TARGET_PATH);
        KnativeUtils.deleteDirectory(DOCKER_TARGET_PATH);
        KnativeTestUtils.deleteDockerImage(DOCKER_IMAGE);
    }

    /**
     * Build bal file with deployment having annotations.
     *
     * @throws IOException               Error when loading the generated yaml.
     * @throws InterruptedException      Error when compiling the ballerina file.
     * @throws KubernetesPluginException Error when deleting the generated artifacts folder.
     */
    @Test
    public void podTolerationsTest() throws IOException, InterruptedException, KubernetesPluginException,
            DockerTestException {
        Assert.assertEquals(KnativeTestUtils.compileBallerinaFile(BAL_DIRECTORY, "pod_tolerations.bal"), 0);

        // Check if docker image exists and correct
        validateDockerfile();
        validateDockerImage();

        // Validate deployment yaml
        File deploymentYAML = KUBERNETES_TARGET_PATH.resolve("pod_tolerations_deployment.yaml").toFile();
        Assert.assertTrue(deploymentYAML.exists());
        Deployment deployment = KnativeTestUtils.loadYaml(deploymentYAML);
        Assert.assertNotNull(deployment.getSpec());
        Assert.assertNotNull(deployment.getSpec().getTemplate());
        Assert.assertNotNull(deployment.getSpec().getTemplate().getSpec());
        Assert.assertNotNull(deployment.getSpec().getTemplate().getSpec().getTolerations());
        Assert.assertEquals(deployment.getSpec().getTemplate().getSpec().getTolerations().size(), 1,
                "Toleration missing.");
        Assert.assertEquals(deployment.getSpec().getTemplate().getSpec().getTolerations().get(0).getKey(), "app",
                "Invalid toleration key.");
        Assert.assertEquals(deployment.getSpec().getTemplate().getSpec().getTolerations().get(0).getOperator(), "Equal",
                "Invalid toleration operator.");
        Assert.assertEquals(deployment.getSpec().getTemplate().getSpec().getTolerations().get(0).getValue(), "blue",
                "Invalid toleration value.");
        Assert.assertEquals(deployment.getSpec().getTemplate().getSpec().getTolerations().get(0).getEffect(),
                "NoSchedule", "Invalid toleration effect.");
        Assert.assertEquals(deployment.getSpec().getTemplate().getSpec().getTolerations().get(0).getTolerationSeconds()
                .longValue(), 0L, "Invalid toleration seconds.");

        KnativeUtils.deleteDirectory(KUBERNETES_TARGET_PATH);
        KnativeUtils.deleteDirectory(DOCKER_TARGET_PATH);
        KnativeTestUtils.deleteDockerImage(DOCKER_IMAGE);
    }

    /**
     * Build bal file with CMD of Dockerfile overridden.
     *
     * @throws IOException               Error when loading the generated yaml.
     * @throws InterruptedException      Error when compiling the ballerina file.
     * @throws KubernetesPluginException Error when deleting the generated artifacts folder.
     */
    @Test
    public void overrideCMDTest() throws IOException, InterruptedException, KubernetesPluginException,
            DockerTestException {
        Assert.assertEquals(KnativeTestUtils.compileBallerinaFile(BAL_DIRECTORY, "cmd_override.bal"), 0);

        // Check if docker image exists and correct
        validateDockerfile();

        List<String> ports = getExposedPorts(DOCKER_IMAGE);
        Assert.assertEquals(ports.size(), 1);
        Assert.assertEquals(ports.get(0), "9090/tcp");
        // Validate ballerina.conf in run command
        Assert.assertEquals(getCommand(DOCKER_IMAGE).toString(),
                "[/bin/sh, -c, java -jar cmd_override.jar --b7a.http.accesslog.console=true]");

        // Validate deployment yaml
        File deploymentYAML = KUBERNETES_TARGET_PATH.resolve("cmd_override_deployment.yaml").toFile();
        Assert.assertTrue(deploymentYAML.exists());

        KnativeUtils.deleteDirectory(KUBERNETES_TARGET_PATH);
        KnativeUtils.deleteDirectory(DOCKER_TARGET_PATH);
        KnativeTestUtils.deleteDockerImage(DOCKER_IMAGE);
    }

    /**
     * Validate if Dockerfile is created.
     */
    public void validateDockerfile() {
        File dockerFile = DOCKER_TARGET_PATH.resolve("Dockerfile").toFile();
        Assert.assertTrue(dockerFile.exists());
    }

    /**
     * Validate contents of the Dockerfile.
     */
    public void validateDockerImage() throws DockerTestException, InterruptedException {
        ImageInfo imageInspect = getDockerImage(DOCKER_IMAGE);
        Assert.assertNotEquals(imageInspect, null, "Image not found");
    }
}
