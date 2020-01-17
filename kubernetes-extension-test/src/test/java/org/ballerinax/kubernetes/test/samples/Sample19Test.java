package org.ballerinax.kubernetes.test.samples;

import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.ballerinax.kubernetes.KubernetesConstants;
import org.ballerinax.kubernetes.exceptions.KubernetesPluginException;
import org.ballerinax.kubernetes.models.knative.KnativeService;
import org.ballerinax.kubernetes.test.utils.DockerTestException;
import org.ballerinax.kubernetes.test.utils.KnativeTestUtils;
import org.ballerinax.kubernetes.utils.KnativeUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.ballerinax.kubernetes.KubernetesConstants.DOCKER;
import static org.ballerinax.kubernetes.KubernetesConstants.KUBERNETES;
import static org.ballerinax.kubernetes.test.utils.KubernetesTestUtils.getCommand;
import static org.ballerinax.kubernetes.test.utils.KubernetesTestUtils.getExposedPorts;

public class Sample19Test extends SampleTest {

    private static final Path SOURCE_DIR_PATH = SAMPLE_DIR.resolve("sample19");
    private static final Path DOCKER_TARGET_PATH = SOURCE_DIR_PATH.resolve(DOCKER);
    private static final Path KUBERNETES_TARGET_PATH = SOURCE_DIR_PATH.resolve(KUBERNETES);
    private static final String DOCKER_IMAGE = "hello_world_config_map_k8s:latest";
    private ConfigMap ballerinaConf;
    private ConfigMap dataMap;
    private KnativeService knativeService;

    @BeforeClass
    public void compileSample() throws IOException, InterruptedException {
        Assert.assertEquals(KnativeTestUtils.compileBallerinaFile(SOURCE_DIR_PATH, "hello_world_config_map_k8s.bal")
                , 0);
        File artifactYaml = KUBERNETES_TARGET_PATH.resolve("hello_world_config_map_k8s.yaml").toFile();
        Assert.assertTrue(artifactYaml.exists());
        KubernetesClient client = new DefaultKubernetesClient();
        List<HasMetadata> k8sItems = client.load(new FileInputStream(artifactYaml)).get();
        for (HasMetadata data : k8sItems) {
            switch (data.getKind()) {
                case "Service":
                    knativeService = (KnativeService) data;
                    break;
                case "ConfigMap":
                    switch (data.getMetadata().getName()) {
                        case "helloworld-ballerina-conf-config-map":
                            ballerinaConf = (ConfigMap) data;
                            break;
                        case "helloworld-config-map":
                            dataMap = (ConfigMap) data;
                            break;
                        default:
                            break;
                    }
                    break;
                default:
                    Assert.fail("Unexpected k8s resource found: " + data.getKind());
                    break;
            }
        }
    }

    @Test
    public void validateDeployment() {
        Assert.assertNotNull(knativeService);
        Assert.assertEquals(knativeService.getMetadata().getName(), "hello-world-config-map-k8s-deployment");
        Assert.assertEquals(knativeService.getSpec().getReplicas().intValue(), 1);
        Assert.assertEquals(knativeService.getSpec().getTemplate().getSpec().getVolumes().size(), 3);
        Assert.assertEquals(knativeService.getMetadata().getLabels().get(KubernetesConstants
                .KUBERNETES_SELECTOR_KEY), "hello_world_config_map_k8s");
        Assert.assertEquals(knativeService.getSpec().getTemplate().getSpec().getContainers().size(), 1);

        // Assert Containers
        Container container = knativeService.getSpec().getTemplate().getSpec().getContainers().get(0);
        Assert.assertEquals(container.getVolumeMounts().size(), 3);
        Assert.assertEquals(container.getImage(), DOCKER_IMAGE);
        Assert.assertEquals(container.getPorts().size(), 1);
        Assert.assertEquals(container.getEnv().size(), 1);

        // Validate config file
        Assert.assertEquals(container.getEnv().get(0).getName(), "CONFIG_FILE");
        Assert.assertEquals(container.getEnv().get(0).getValue(), "/home/ballerina/conf/ballerina.conf");
    }

    @Test
    public void validateConfigMap() {
        // Assert ballerina.conf config map
        Assert.assertNotNull(ballerinaConf);
        Assert.assertEquals(1, ballerinaConf.getData().size());

        // Assert Data config map
        Assert.assertNotNull(dataMap);
        Assert.assertEquals(1, dataMap.getData().size());
    }

    @Test
    public void validateDockerfile() {
        File dockerFile = DOCKER_TARGET_PATH.resolve("Dockerfile").toFile();
        Assert.assertTrue(dockerFile.exists());
    }

    @Test
    public void validateDockerImage() throws DockerTestException, InterruptedException {
        List<String> ports = getExposedPorts(DOCKER_IMAGE);
        Assert.assertEquals(ports.size(), 1);
        Assert.assertEquals(ports.get(0), "8080/tcp");
        // Validate ballerina.conf in run command
        Assert.assertEquals(getCommand(DOCKER_IMAGE).toString(),
                "[/bin/sh, -c, java -jar hello_world_config_map_k8s.jar --b7a.config.file=${CONFIG_FILE}]");
    }

    @AfterClass
    public void cleanUp() throws KubernetesPluginException {
        KnativeUtils.deleteDirectory(KUBERNETES_TARGET_PATH);
        KnativeUtils.deleteDirectory(DOCKER_TARGET_PATH);
        KnativeTestUtils.deleteDockerImage(DOCKER_IMAGE);
    }


}
