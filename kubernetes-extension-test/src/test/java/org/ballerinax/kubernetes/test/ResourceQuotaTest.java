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

import io.fabric8.kubernetes.api.model.ResourceQuota;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import org.ballerinax.kubernetes.exceptions.KubernetesPluginException;
import org.ballerinax.kubernetes.test.utils.DockerTestException;
import org.ballerinax.kubernetes.test.utils.KubernetesTestUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.ballerinax.kubernetes.KubernetesConstants.DOCKER;
import static org.ballerinax.kubernetes.KubernetesConstants.KUBERNETES;
import static org.ballerinax.kubernetes.test.utils.KubernetesTestUtils.getDockerImage;
import static org.ballerinax.kubernetes.test.utils.KubernetesTestUtils.getExposedPorts;

/**
 * Test creating resource quotas.
 */
public class ResourceQuotaTest {
    private static final Path BAL_DIRECTORY = Paths.get("src", "test", "resources", "resource-quotas");
    private static final Path DOCKER_TARGET_PATH = BAL_DIRECTORY.resolve(DOCKER);
    private static final Path KUBERNETES_TARGET_PATH = BAL_DIRECTORY.resolve(KUBERNETES);
    private static final String DOCKER_IMAGE = "pizza-shop:latest";
    
    /**
     * Build bal file with resource quotas.
     *
     * @throws IOException               Error when loading the generated yaml.
     * @throws InterruptedException      Error when compiling the ballerina file.
     * @throws KubernetesPluginException Error when deleting the generated artifacts folder.
     */
    @Test
    public void simpleQuotaTest() throws IOException, InterruptedException, KubernetesPluginException,
            DockerTestException {
        Assert.assertEquals(KubernetesTestUtils.compileBallerinaFile(BAL_DIRECTORY, "simple_quota.bal"), 0);
        
        // Check if docker image exists and correct
        validateDockerfile();
        validateDockerImage();
    
        // Validate resource quota yaml.
        File resourceQuotaYaml = KUBERNETES_TARGET_PATH.resolve("simple_quota_resource_quota.yaml").toFile();
        Assert.assertTrue(resourceQuotaYaml.exists());
        
        ResourceQuota resourceQuota = KubernetesTestUtils.loadYaml(resourceQuotaYaml);
        Assert.assertEquals(resourceQuota.getMetadata().getName(), "compute-resources");
        
        Assert.assertEquals(resourceQuota.getMetadata().getLabels().size(), 1, "Invalid number of labels.");
        Assert.assertEquals(resourceQuota.getMetadata().getLabels().get("priority"), "high",
                "Invalid label value found.");
        
        Assert.assertEquals(resourceQuota.getSpec().getHard().size(), 5, "Invalid number of hard limits.");
        Assert.assertEquals(resourceQuota.getSpec().getHard().get("pods").getAmount(), "4", "Invalid number of pods.");
        Assert.assertEquals(resourceQuota.getSpec().getHard().get("requests.cpu").getAmount(), "1",
                "Invalid number of cpu requests.");
        Assert.assertEquals(resourceQuota.getSpec().getHard().get("requests.memory").getAmount(), "1Gi",
                "Invalid number of memory requests.");
        Assert.assertEquals(resourceQuota.getSpec().getHard().get("limits.cpu").getAmount(), "2",
                "Invalid number of cpu limits");
        Assert.assertEquals(resourceQuota.getSpec().getHard().get("limits.memory").getAmount(), "2Gi",
                "Invalid number of memory limits.");
    
        Assert.assertEquals(resourceQuota.getSpec().getScopes().size(), 0, "Unexpected number of scopes.");
    
        KubernetesUtils.deleteDirectory(KUBERNETES_TARGET_PATH);
        KubernetesUtils.deleteDirectory(DOCKER_TARGET_PATH);
        KubernetesTestUtils.deleteDockerImage(DOCKER_IMAGE);
    }
    
    /**
     * Build bal file with resource quota having a scope.
     *
     * @throws IOException               Error when loading the generated yaml.
     * @throws InterruptedException      Error when compiling the ballerina file.
     * @throws KubernetesPluginException Error when deleting the generated artifacts folder.
     */
    @Test
    public void quotaWithScopeTest() throws IOException, InterruptedException, KubernetesPluginException,
            DockerTestException {
        Assert.assertEquals(KubernetesTestUtils.compileBallerinaFile(BAL_DIRECTORY, "quota_with_scope.bal"), 0);
        
        // Check if docker image exists and correct
        validateDockerfile();
        validateDockerImage();
        
        // Validate resource quota yaml
        File resourceQuotaYaml = KUBERNETES_TARGET_PATH.resolve("quota_with_scope_resource_quota.yaml").toFile();
        Assert.assertTrue(resourceQuotaYaml.exists());
        ResourceQuota resourceQuota = KubernetesTestUtils.loadYaml(resourceQuotaYaml);
        Assert.assertEquals(resourceQuota.getMetadata().getName(), "compute-resources");
        
        Assert.assertEquals(resourceQuota.getSpec().getHard().size(), 5, "Invalid number of hard limits.");
        Assert.assertEquals(resourceQuota.getSpec().getHard().get("pods").getAmount(), "4", "Invalid number of pods.");
        Assert.assertEquals(resourceQuota.getSpec().getHard().get("requests.cpu").getAmount(), "1",
                "Invalid number of cpu requests.");
        Assert.assertEquals(resourceQuota.getSpec().getHard().get("requests.memory").getAmount(), "1Gi",
                "Invalid number of memory requests.");
        Assert.assertEquals(resourceQuota.getSpec().getHard().get("limits.cpu").getAmount(), "2",
                "Invalid number of cpu limits");
        Assert.assertEquals(resourceQuota.getSpec().getHard().get("limits.memory").getAmount(), "2Gi",
                "Invalid number of memory limits.");
        
        Assert.assertEquals(resourceQuota.getSpec().getScopes().size(), 1, "Unexpected number of scopes.");
        Assert.assertEquals(resourceQuota.getSpec().getScopes().get(0), "BestEffort", "Invalid scope found.");
        
        KubernetesUtils.deleteDirectory(KUBERNETES_TARGET_PATH);
        KubernetesUtils.deleteDirectory(DOCKER_TARGET_PATH);
        KubernetesTestUtils.deleteDockerImage(DOCKER_IMAGE);
    }
    
    /**
     * Build bal file having multiple resource quotas.
     *
     * @throws IOException               Error when loading the generated yaml.
     * @throws InterruptedException      Error when compiling the ballerina file.
     * @throws KubernetesPluginException Error when deleting the generated artifacts folder.
     */
    @Test
    public void multipleQuotaTest() throws IOException, InterruptedException, KubernetesPluginException,
            DockerTestException {
        Assert.assertEquals(KubernetesTestUtils.compileBallerinaFile(BAL_DIRECTORY, "multiple_quotas.bal"), 0);
        
        // Check if docker image exists and correct
        validateDockerfile();
        validateDockerImage();
        
        // Validate resource quota yaml.
        File resourceQuotaYaml = KUBERNETES_TARGET_PATH.resolve("multiple_quotas_resource_quota.yaml").toFile();
        Assert.assertTrue(resourceQuotaYaml.exists());
        List<ResourceQuota> resourceQuotas = KubernetesTestUtils.loadYaml(resourceQuotaYaml);
    
        Assert.assertEquals(resourceQuotas.get(0).getMetadata().getName(), "compute-resources");
        Assert.assertEquals(resourceQuotas.get(0).getSpec().getHard().size(), 5, "Invalid number of hard limits.");
        Assert.assertEquals(resourceQuotas.get(0).getSpec().getHard().get("pods").getAmount(), "4",
                "Invalid number of pods.");
        Assert.assertEquals(resourceQuotas.get(0).getSpec().getHard().get("requests.cpu").getAmount(), "1",
                "Invalid number of cpu requests.");
        Assert.assertEquals(resourceQuotas.get(0).getSpec().getHard().get("requests.memory").getAmount(), "1Gi",
                "Invalid number of memory requests.");
        Assert.assertEquals(resourceQuotas.get(0).getSpec().getHard().get("limits.cpu").getAmount(), "2",
                "Invalid number of cpu limits");
        Assert.assertEquals(resourceQuotas.get(0).getSpec().getHard().get("limits.memory").getAmount(), "2Gi",
                "Invalid number of memory limits.");
    
        
        Assert.assertEquals(resourceQuotas.get(1).getMetadata().getName(), "minimum-resources");
        Assert.assertEquals(resourceQuotas.get(1).getSpec().getHard().size(), 1, "Invalid number of hard limits.");
        Assert.assertEquals(resourceQuotas.get(1).getSpec().getHard().get("pods").getAmount(), "1",
                "Invalid number of pods.");

        KubernetesUtils.deleteDirectory(KUBERNETES_TARGET_PATH);
        KubernetesUtils.deleteDirectory(DOCKER_TARGET_PATH);
        KubernetesTestUtils.deleteDockerImage(DOCKER_IMAGE);
    }
    
    /**
     * Build bal file with resource quota having invalid scope. This should fail.
     *
     * @throws IOException               Error when loading the generated yaml.
     * @throws InterruptedException      Error when compiling the ballerina file.
     * @throws KubernetesPluginException Error when deleting the generated artifacts folder.
     */
    @Test
    public void invalidTest() throws IOException, InterruptedException, KubernetesPluginException {
        Assert.assertEquals(KubernetesTestUtils.compileBallerinaFile(BAL_DIRECTORY, "quota-with-invalid-scope.bal"), 1);
        KubernetesUtils.deleteDirectory(KUBERNETES_TARGET_PATH);
        KubernetesUtils.deleteDirectory(DOCKER_TARGET_PATH);
    }
    
    /**
     * Build bal file with resource quotas on a main function.
     *
     * @throws IOException               Error when loading the generated yaml.
     * @throws InterruptedException      Error when compiling the ballerina file.
     * @throws KubernetesPluginException Error when deleting the generated artifacts folder.
     */
    @Test
    public void quotaOnMainFunctionTest() throws IOException, InterruptedException, KubernetesPluginException,
            DockerTestException {
        Assert.assertEquals(KubernetesTestUtils.compileBallerinaFile(BAL_DIRECTORY, "on_main_function.bal"), 0);
        
        // Check if docker image exists and correct.
        validateDockerfile();
        Assert.assertNotNull(getDockerImage(DOCKER_IMAGE));
    
        // Validate deployment yaml.
        File deploymentYAML = KUBERNETES_TARGET_PATH.resolve("on_main_function_deployment.yaml").toFile();
        Assert.assertTrue(deploymentYAML.exists());
        Deployment deployment = KubernetesTestUtils.loadYaml(deploymentYAML);
        Assert.assertEquals("simple-quota", deployment.getMetadata().getName());
        
        // Validate resource quota yaml.
        File resourceQuotaYaml = KUBERNETES_TARGET_PATH.resolve("on_main_function_resource_quota.yaml").toFile();
        Assert.assertTrue(resourceQuotaYaml.exists());
        ResourceQuota resourceQuota = KubernetesTestUtils.loadYaml(resourceQuotaYaml);
        Assert.assertEquals(resourceQuota.getMetadata().getName(), "compute-resources");
        
        Assert.assertEquals(resourceQuota.getMetadata().getLabels().size(), 1, "Invalid number of labels.");
        Assert.assertEquals(resourceQuota.getMetadata().getLabels().get("priority"), "high",
                "Invalid label value found.");
        
        Assert.assertEquals(resourceQuota.getSpec().getHard().size(), 5, "Invalid number of hard limits.");
        Assert.assertEquals(resourceQuota.getSpec().getHard().get("pods").getAmount(), "4", "Invalid number of pods.");
        Assert.assertEquals(resourceQuota.getSpec().getHard().get("requests.cpu").getAmount(), "1",
                "Invalid number of cpu requests.");
        Assert.assertEquals(resourceQuota.getSpec().getHard().get("requests.memory").getAmount(), "1Gi",
                "Invalid number of memory requests.");
        Assert.assertEquals(resourceQuota.getSpec().getHard().get("limits.cpu").getAmount(), "2",
                "Invalid number of cpu limits");
        Assert.assertEquals(resourceQuota.getSpec().getHard().get("limits.memory").getAmount(), "2Gi",
                "Invalid number of memory limits.");
        
        Assert.assertEquals(resourceQuota.getSpec().getScopes().size(), 0, "Unexpected number of scopes.");
        
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
        List<String> ports = getExposedPorts(DOCKER_IMAGE);
        Assert.assertEquals(ports.size(), 1);
        Assert.assertEquals(ports.get(0), "9099/tcp");
    }
}
