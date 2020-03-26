/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinax.kubernetes.test;

import com.github.dockerjava.api.command.InspectImageResponse;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.Volume;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import org.ballerinax.kubernetes.KubernetesConstants;
import org.ballerinax.kubernetes.exceptions.KubernetesPluginException;
import org.ballerinax.kubernetes.test.utils.DockerTestException;
import org.ballerinax.kubernetes.test.utils.KubernetesTestUtils;
import org.ballerinax.kubernetes.utils.KubernetesUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.ballerinax.kubernetes.KubernetesConstants.DOCKER;
import static org.ballerinax.kubernetes.KubernetesConstants.KUBERNETES;
import static org.ballerinax.kubernetes.test.utils.KubernetesTestUtils.getDockerImage;

/**
 * Test cases for Default Mode for config and secret volumes.
 */
public class DefaultModeTest {

    private static final Path BAL_DIRECTORY = Paths.get("src", "test", "resources", "default-mode");
    private static final Path DOCKER_TARGET_PATH = BAL_DIRECTORY.resolve(DOCKER);
    private static final Path KUBERNETES_TARGET_PATH = BAL_DIRECTORY.resolve(KUBERNETES);
    private static final String DOCKER_IMAGE_SECRET = "default_mode_secret:latest";
    private static final String DOCKER_IMAGE_CONFIG = "default_mode_config_map:latest";

    /**
     * Build bal file with deployment having annotations.
     *
     * @throws IOException               Error when loading the generated yaml.
     * @throws InterruptedException      Error when compiling the ballerina file.
     * @throws KubernetesPluginException Error when deleting the generated artifacts folder.
     */
    @Test
    public void defaultModeSecretTest() throws IOException, InterruptedException, KubernetesPluginException,
            DockerTestException {
        Assert.assertEquals(KubernetesTestUtils.compileBallerinaFile(BAL_DIRECTORY, "default_mode_secret.bal"), 0);

        // Check if docker image exists and correct
        validateDockerfile();
        validateDockerImage(DOCKER_IMAGE_SECRET);

        // Validate deployment yaml
        File deploymentYAML = KUBERNETES_TARGET_PATH.resolve("default_mode_secret_deployment.yaml").toFile();
        Assert.assertTrue(deploymentYAML.exists());
        Deployment deployment = KubernetesTestUtils.loadYaml(deploymentYAML);
        // Assert Containers
        Container container = deployment.getSpec().getTemplate().getSpec().getContainers().get(0);
        Assert.assertEquals(container.getVolumeMounts().size(), 1);
        Assert.assertEquals(container.getImage(), DOCKER_IMAGE_SECRET);
        Assert.assertEquals(container.getImagePullPolicy(), KubernetesConstants.ImagePullPolicy.IfNotPresent.name());
        Assert.assertEquals(container.getPorts().size(), 1);

        Assert.assertEquals(deployment.getSpec().getTemplate().getSpec().getVolumes().size(), 1);
        Volume volume = deployment.getSpec().getTemplate().getSpec().getVolumes().get(0);
        Assert.assertEquals(volume.getSecret().getSecretName(), "helloworld-secret");
        Assert.assertEquals(volume.getSecret().getDefaultMode().intValue(), 755);

        KubernetesUtils.deleteDirectory(KUBERNETES_TARGET_PATH);
        KubernetesUtils.deleteDirectory(DOCKER_TARGET_PATH);
        KubernetesTestUtils.deleteDockerImage(DOCKER_IMAGE_SECRET);
    }

    @Test
    public void defaultModeConfigTest() throws IOException, InterruptedException, KubernetesPluginException,
            DockerTestException {
        Assert.assertEquals(KubernetesTestUtils.compileBallerinaFile(BAL_DIRECTORY, "default_mode_config_map.bal"), 0);

        // Check if docker image exists and correct
        validateDockerfile();
        validateDockerImage(DOCKER_IMAGE_CONFIG);

        // Validate deployment yaml
        File deploymentYAML = KUBERNETES_TARGET_PATH.resolve("default_mode_config_map_deployment.yaml").toFile();
        Assert.assertTrue(deploymentYAML.exists());
        Deployment deployment = KubernetesTestUtils.loadYaml(deploymentYAML);
        // Assert Containers
        Container container = deployment.getSpec().getTemplate().getSpec().getContainers().get(0);
        Assert.assertEquals(container.getVolumeMounts().size(), 1);
        Assert.assertEquals(container.getImage(), DOCKER_IMAGE_CONFIG);
        Assert.assertEquals(container.getImagePullPolicy(), KubernetesConstants.ImagePullPolicy.IfNotPresent.name());
        Assert.assertEquals(container.getPorts().size(), 1);

        Assert.assertEquals(deployment.getSpec().getTemplate().getSpec().getVolumes().size(), 1);
        Volume volume = deployment.getSpec().getTemplate().getSpec().getVolumes().get(0);
        Assert.assertEquals(volume.getConfigMap().getName(), "helloworld-config-map");
        Assert.assertEquals(volume.getConfigMap().getDefaultMode().intValue(), 777);

        KubernetesUtils.deleteDirectory(KUBERNETES_TARGET_PATH);
        KubernetesUtils.deleteDirectory(DOCKER_TARGET_PATH);
        KubernetesTestUtils.deleteDockerImage(DOCKER_IMAGE_CONFIG);
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
    public void validateDockerImage(String image) throws DockerTestException, InterruptedException {
        InspectImageResponse imageInspect = getDockerImage(image);
        Assert.assertNotNull(imageInspect.getConfig(), "Image not found");
    }
}
