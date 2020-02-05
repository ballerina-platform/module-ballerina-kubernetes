/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import com.spotify.docker.client.messages.ImageInfo;
import io.fabric8.kubernetes.api.model.apps.Deployment;
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
 * Test cases for deployment liveness probes.
 */
public class LivenessProbeTest {
    private static final Path BAL_DIRECTORY = Paths.get("src", "test", "resources", "deployment", "liveness-probe");
    private static final Path DOCKER_TARGET_PATH = BAL_DIRECTORY.resolve(DOCKER);
    private static final Path KUBERNETES_TARGET_PATH = BAL_DIRECTORY.resolve(KUBERNETES);
    private static final String DOCKER_IMAGE = "pizza-shop:latest";
    
    /**
     * Build bal file with deployment having liveness disabled.
     *
     * @throws IOException               Error when loading the generated yaml.
     * @throws InterruptedException      Error when compiling the ballerina file.
     * @throws KubernetesPluginException Error when deleting the generated artifacts folder.
     */
    @Test
    public void disabledTest() throws IOException, InterruptedException, KubernetesPluginException,
            DockerTestException {
        Assert.assertEquals(KubernetesTestUtils.compileBallerinaFile(BAL_DIRECTORY, "disabled.bal"), 0);
        
        // Check if docker image exists and correct
        validateDockerfile();
        validateDockerImage();
        
        // Validate deployment yaml
        File deploymentYAML = KUBERNETES_TARGET_PATH.resolve("disabled_deployment.yaml").toFile();
        Assert.assertTrue(deploymentYAML.exists());
        Deployment deployment = KubernetesTestUtils.loadYaml(deploymentYAML);
        Assert.assertNotNull(deployment.getSpec());
        Assert.assertNotNull(deployment.getSpec().getTemplate());
        Assert.assertNotNull(deployment.getSpec().getTemplate().getSpec());
        Assert.assertTrue(deployment.getSpec().getTemplate().getSpec().getContainers().size() > 0);
        Assert.assertNull(deployment.getSpec().getTemplate().getSpec().getContainers().get(0).getLivenessProbe());
        
        KubernetesUtils.deleteDirectory(KUBERNETES_TARGET_PATH);
        KubernetesUtils.deleteDirectory(DOCKER_TARGET_PATH);
        KubernetesTestUtils.deleteDockerImage(DOCKER_IMAGE);
    }
    
    /**
     * Build bal file with deployment having liveness enabled using a boolean.
     *
     * @throws IOException               Error when loading the generated yaml.
     * @throws InterruptedException      Error when compiling the ballerina file.
     * @throws KubernetesPluginException Error when deleting the generated artifacts folder.
     */
    @Test
    public void enabledTest() throws IOException, InterruptedException, KubernetesPluginException, DockerTestException {
        Assert.assertEquals(KubernetesTestUtils.compileBallerinaFile(BAL_DIRECTORY, "enabled.bal"), 0);
        
        // Check if docker image exists and correct
        validateDockerfile();
        validateDockerImage();
        
        // Validate deployment yaml
        File deploymentYAML = KUBERNETES_TARGET_PATH.resolve("enabled_deployment.yaml").toFile();
        Assert.assertTrue(deploymentYAML.exists());
        Deployment deployment = KubernetesTestUtils.loadYaml(deploymentYAML);
        Assert.assertNotNull(deployment.getSpec());
        Assert.assertNotNull(deployment.getSpec().getTemplate());
        Assert.assertNotNull(deployment.getSpec().getTemplate().getSpec());
        Assert.assertTrue(deployment.getSpec().getTemplate().getSpec().getContainers().size() > 0);
        Assert.assertNotNull(deployment.getSpec().getTemplate().getSpec().getContainers().get(0).getLivenessProbe(),
                "Liveness probe is missing.");
        Assert.assertEquals(deployment.getSpec().getTemplate().getSpec().getContainers().get(0).getLivenessProbe()
                .getInitialDelaySeconds().intValue(), 10, "initialDelay in liveness probe is missing.");
        Assert.assertEquals(deployment.getSpec().getTemplate().getSpec().getContainers().get(0).getLivenessProbe()
                .getPeriodSeconds().intValue(), 5, "periodSeconds in liveness probe is missing.");
        Assert.assertEquals(deployment.getSpec().getTemplate().getSpec().getContainers().get(0).getLivenessProbe()
                .getTcpSocket().getPort().getIntVal().intValue(), 9090, "TCP port in liveness probe is missing.");
        
        KubernetesUtils.deleteDirectory(KUBERNETES_TARGET_PATH);
        KubernetesUtils.deleteDirectory(DOCKER_TARGET_PATH);
        KubernetesTestUtils.deleteDockerImage(DOCKER_IMAGE);
    }
    
    /**
     * Build bal file with deployment having liveness enabled using an empty record.
     *
     * @throws IOException               Error when loading the generated yaml.
     * @throws InterruptedException      Error when compiling the ballerina file.
     * @throws KubernetesPluginException Error when deleting the generated artifacts folder.
     */
    @Test
    public void enabledWithEmptyRecordTest() throws IOException, InterruptedException, KubernetesPluginException,
            DockerTestException {
        Assert.assertEquals(KubernetesTestUtils.compileBallerinaFile(BAL_DIRECTORY, "enabled_with_record.bal"), 0);
        
        // Check if docker image exists and correct
        validateDockerfile();
        validateDockerImage();
        
        // Validate deployment yaml
        File deploymentYAML = KUBERNETES_TARGET_PATH.resolve("enabled_with_record_deployment.yaml").toFile();
        Assert.assertTrue(deploymentYAML.exists());
        Deployment deployment = KubernetesTestUtils.loadYaml(deploymentYAML);
        Assert.assertNotNull(deployment.getSpec());
        Assert.assertNotNull(deployment.getSpec().getTemplate());
        Assert.assertNotNull(deployment.getSpec().getTemplate().getSpec());
        Assert.assertTrue(deployment.getSpec().getTemplate().getSpec().getContainers().size() > 0);
        Assert.assertNotNull(deployment.getSpec().getTemplate().getSpec().getContainers().get(0).getLivenessProbe(),
                "Liveness probe is missing.");
        Assert.assertEquals(deployment.getSpec().getTemplate().getSpec().getContainers().get(0).getLivenessProbe()
                .getInitialDelaySeconds().intValue(), 10, "initialDelay in liveness probe is missing.");
        Assert.assertEquals(deployment.getSpec().getTemplate().getSpec().getContainers().get(0).getLivenessProbe()
                .getPeriodSeconds().intValue(), 5, "periodSeconds in liveness probe is missing.");
        Assert.assertEquals(deployment.getSpec().getTemplate().getSpec().getContainers().get(0).getLivenessProbe()
                .getTcpSocket().getPort().getIntVal().intValue(), 9090, "TCP port in liveness probe is missing.");
        
        KubernetesUtils.deleteDirectory(KUBERNETES_TARGET_PATH);
        KubernetesUtils.deleteDirectory(DOCKER_TARGET_PATH);
        KubernetesTestUtils.deleteDockerImage(DOCKER_IMAGE);
    }
    
    /**
     * Build bal file with deployment having liveness configured.
     *
     * @throws IOException               Error when loading the generated yaml.
     * @throws InterruptedException      Error when compiling the ballerina file.
     * @throws KubernetesPluginException Error when deleting the generated artifacts folder.
     */
    @Test
    public void configuredTest() throws IOException, InterruptedException, KubernetesPluginException,
            DockerTestException {
        Assert.assertEquals(KubernetesTestUtils.compileBallerinaFile(BAL_DIRECTORY, "configured.bal"), 0);
        
        // Check if docker image exists and correct
        validateDockerfile();
        validateDockerImage();
        
        // Validate deployment yaml
        File deploymentYAML = KUBERNETES_TARGET_PATH.resolve("configured_deployment.yaml").toFile();
        Assert.assertTrue(deploymentYAML.exists());
        Deployment deployment = KubernetesTestUtils.loadYaml(deploymentYAML);
        Assert.assertNotNull(deployment.getSpec());
        Assert.assertNotNull(deployment.getSpec().getTemplate());
        Assert.assertNotNull(deployment.getSpec().getTemplate().getSpec());
        Assert.assertTrue(deployment.getSpec().getTemplate().getSpec().getContainers().size() > 0);
        Assert.assertNotNull(deployment.getSpec().getTemplate().getSpec().getContainers().get(0).getLivenessProbe(),
                "Liveness probe is missing.");
        Assert.assertEquals(deployment.getSpec().getTemplate().getSpec().getContainers().get(0).getLivenessProbe()
                .getInitialDelaySeconds().intValue(), 20, "initialDelay in liveness probe is missing.");
        Assert.assertEquals(deployment.getSpec().getTemplate().getSpec().getContainers().get(0).getLivenessProbe()
                .getPeriodSeconds().intValue(), 10, "periodSeconds in liveness probe is missing.");
        Assert.assertEquals(deployment.getSpec().getTemplate().getSpec().getContainers().get(0).getLivenessProbe()
                .getTcpSocket().getPort().getIntVal().intValue(), 8080, "TCP port in liveness probe is missing.");
        
        KubernetesUtils.deleteDirectory(KUBERNETES_TARGET_PATH);
        KubernetesUtils.deleteDirectory(DOCKER_TARGET_PATH);
        KubernetesTestUtils.deleteDockerImage(DOCKER_IMAGE);
    }

    /**
     * Build bal file with deployment having liveness enabled using a boolean.
     *
     * @throws IOException               Error when loading the generated yaml.
     * @throws InterruptedException      Error when compiling the ballerina file.
     * @throws KubernetesPluginException Error when deleting the generated artifacts folder.
     */
    @Test
    public void ftpTest() throws IOException, InterruptedException, KubernetesPluginException, DockerTestException {
        Assert.assertEquals(KubernetesTestUtils.compileBallerinaFile(BAL_DIRECTORY, "ftp.bal"), 0);

        // Check if docker image exists and correct
        validateDockerfile();
        validateDockerImage();

        // Validate deployment yaml
        File deploymentYAML = KUBERNETES_TARGET_PATH.resolve("ftp_deployment.yaml").toFile();
        Assert.assertTrue(deploymentYAML.exists());
        Deployment deployment = KubernetesTestUtils.loadYaml(deploymentYAML);
        Assert.assertNotNull(deployment.getSpec());
        Assert.assertNotNull(deployment.getSpec().getTemplate());
        Assert.assertNotNull(deployment.getSpec().getTemplate().getSpec());
        Assert.assertTrue(deployment.getSpec().getTemplate().getSpec().getContainers().size() > 0);
        Assert.assertNotNull(deployment.getSpec().getTemplate().getSpec().getContainers().get(0).getLivenessProbe(),
                "Liveness probe is missing.");
        Assert.assertEquals(deployment.getSpec().getTemplate().getSpec().getContainers().get(0).getLivenessProbe()
                .getInitialDelaySeconds().intValue(), 10, "initialDelay in liveness probe is missing.");
        Assert.assertEquals(deployment.getSpec().getTemplate().getSpec().getContainers().get(0).getLivenessProbe()
                .getPeriodSeconds().intValue(), 5, "periodSeconds in liveness probe is missing.");
        Assert.assertEquals(deployment.getSpec().getTemplate().getSpec().getContainers().get(0).getLivenessProbe()
                .getTcpSocket().getPort().getIntVal().intValue(), 9090, "TCP port in liveness probe is missing.");

        KubernetesUtils.deleteDirectory(KUBERNETES_TARGET_PATH);
        KubernetesUtils.deleteDirectory(DOCKER_TARGET_PATH);
        KubernetesTestUtils.deleteDockerImage(DOCKER_IMAGE);
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
