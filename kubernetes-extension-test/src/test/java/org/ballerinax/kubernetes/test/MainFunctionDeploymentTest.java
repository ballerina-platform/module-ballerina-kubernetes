/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.HorizontalPodAutoscaler;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaim;
import io.fabric8.kubernetes.api.model.Secret;
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
 * Test case for creating a deployment using a main function.
 */
public class MainFunctionDeploymentTest {
    private static final Path BAL_DIRECTORY = Paths.get("src", "test", "resources", "deployment");
    private static final Path TARGET_PATH = BAL_DIRECTORY.resolve(KUBERNETES);
    private static final String DOCKER_IMAGE = "main_function:latest";
    
    /**
     * Build bal file with main function and annotations.
     *
     * @throws IOException               Error when loading the generated yaml.
     * @throws InterruptedException      Error when compiling the ballerina file.
     * @throws KubernetesPluginException Error when deleting the generated artifacts folder.
     */
    @Test
    public void mainFuncDeploymentTest() throws IOException, InterruptedException, KubernetesPluginException,
            DockerTestException {
        Assert.assertEquals(KubernetesTestUtils.compileBallerinaFile(BAL_DIRECTORY, "main_function.bal"), 0);
        
        // Check if docker image exists and correct
        validateDockerfile();
        validateDockerImage();
        
        // Validate deployment yaml
        File deploymentYAML = TARGET_PATH.resolve("main_function_deployment.yaml").toFile();
        Assert.assertTrue(deploymentYAML.exists());
        Deployment deployment = KubernetesTestUtils.loadYaml(deploymentYAML);
        Assert.assertEquals(deployment.getMetadata().getLabels().size(), 2, "Invalid number of labels found.");
        Assert.assertEquals(deployment.getMetadata().getLabels().get(KubernetesConstants.KUBERNETES_SELECTOR_KEY),
                "main_function", "Invalid label found.");
        Assert.assertEquals(deployment.getMetadata().getLabels().get("task_type"), "printer", "Invalid label found.");
    
        // Validate volume claim yaml.
        File volumeClaimYaml = TARGET_PATH.resolve("main_function_volume_claim.yaml").toFile();
        Assert.assertTrue(volumeClaimYaml.exists());
        PersistentVolumeClaim volumeClaim = KubernetesTestUtils.loadYaml(volumeClaimYaml);
        Assert.assertNotNull(volumeClaim);
        Assert.assertEquals(volumeClaim.getMetadata().getName(), "local-pv-2");
        Assert.assertEquals(volumeClaim.getSpec().getAccessModes().size(), 1);
    
        // Validate secret.
        File secretYaml = TARGET_PATH.resolve("main_function_secret.yaml").toFile();
        Assert.assertTrue(secretYaml.exists());
        Secret privateSecret = KubernetesTestUtils.loadYaml(secretYaml);
        Assert.assertNotNull(privateSecret);
        Assert.assertEquals(privateSecret.getData().size(), 1);
    
        // Validate horizontal pod scalar.
        File hpaYaml = TARGET_PATH.resolve("main_function_hpa.yaml").toFile();
        Assert.assertTrue(hpaYaml.exists());
        HorizontalPodAutoscaler podAutoscaler = KubernetesTestUtils.loadYaml(hpaYaml);
        Assert.assertNotNull(podAutoscaler);
        Assert.assertEquals(podAutoscaler.getMetadata().getName(), "main-function-hpa");
        Assert.assertEquals(podAutoscaler.getMetadata().getLabels().get(KubernetesConstants
                .KUBERNETES_SELECTOR_KEY), "main_function");
        Assert.assertEquals(podAutoscaler.getSpec().getMaxReplicas().intValue(), 2);
        Assert.assertEquals(podAutoscaler.getSpec().getMinReplicas().intValue(), 1);
        Assert.assertEquals(podAutoscaler.getSpec().getMetrics().size(), 1, "CPU metric is missing.");
        Assert.assertEquals(podAutoscaler.getSpec().getMetrics().get(0).getResource().getName(), "cpu",
                "Invalid resource name.");
        Assert.assertEquals(podAutoscaler.getSpec().getMetrics().get(0).getResource().getTargetAverageUtilization()
                .intValue(), 50);
        Assert.assertEquals(podAutoscaler.getSpec().getScaleTargetRef().getName(), "pizzashack");
    
        Container container = deployment.getSpec().getTemplate().getSpec().getContainers().get(0);
        Assert.assertEquals(container.getEnv().get(0).getName(), "CONFIG_FILE");
        Assert.assertEquals(container.getEnv().get(0).getValue(), "/home/ballerina/conf/ballerina.conf");
        
        KubernetesUtils.deleteDirectory(TARGET_PATH);
        KubernetesTestUtils.deleteDockerImage(DOCKER_IMAGE);
    }
    
    /**
     * Build bal file with non-main function and annotations.
     *
     * @throws IOException               Error when loading the generated yaml.
     * @throws InterruptedException      Error when compiling the ballerina file.
     */
    @Test
    public void nonMainFuncTest() throws IOException, InterruptedException {
        Assert.assertEquals(KubernetesTestUtils.compileBallerinaFile(BAL_DIRECTORY, "non_main_function.bal"), 1);
    }
    
    /**
     * Validate if Dockerfile is created.
     */
    public void validateDockerfile() {
        File dockerFile = TARGET_PATH.resolve(DOCKER).resolve("Dockerfile").toFile();
        Assert.assertTrue(dockerFile.exists());
    }
    
    /**
     * Validate contents of the Dockerfile.
     */
    public void validateDockerImage() throws DockerTestException, InterruptedException {
        ImageInfo dockerImage = getDockerImage(DOCKER_IMAGE);
        Assert.assertNotNull(dockerImage, "Image not found");
    }
}
