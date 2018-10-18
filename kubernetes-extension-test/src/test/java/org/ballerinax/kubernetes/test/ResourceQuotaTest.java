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

import io.fabric8.docker.api.model.ImageInspect;
import io.fabric8.kubernetes.api.KubernetesHelper;
import io.fabric8.kubernetes.api.model.ResourceQuota;
import org.ballerinax.kubernetes.exceptions.KubernetesPluginException;
import org.ballerinax.kubernetes.test.utils.KubernetesTestUtils;
import org.ballerinax.kubernetes.utils.KubernetesUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import static org.ballerinax.kubernetes.KubernetesConstants.DOCKER;
import static org.ballerinax.kubernetes.KubernetesConstants.KUBERNETES;
import static org.ballerinax.kubernetes.test.utils.KubernetesTestUtils.getDockerImage;

/**
 * Test creating resource quotas.
 */
public class ResourceQuotaTest {
    private final String balDirectory = Paths.get("src").resolve("test").resolve("resources").resolve("resource-quotas")
            .toAbsolutePath().toString();
    private final String targetPath = Paths.get(balDirectory).resolve(KUBERNETES).toString();
    private final String dockerImage = "pizza-shop:latest";
    
    /**
     * Build bal file with resource quotas.
     * @throws IOException Error when loading the generated yaml.
     * @throws InterruptedException Error when compiling the ballerina file.
     * @throws KubernetesPluginException Error when deleting the generated artifacts folder.
     */
    @Test
    public void simpleQuotaTest() throws IOException, InterruptedException, KubernetesPluginException {
        Assert.assertEquals(KubernetesTestUtils.compileBallerinaFile(balDirectory, "simple-quota.bal"), 0);
        
        // Check if docker image exists and correct
        validateDockerfile();
        validateDockerImage();
    
        // Validate deployment yaml
        File deploymentYAML = Paths.get(targetPath).resolve("simple-quota_resource_quota.yaml").toFile();
        Assert.assertTrue(deploymentYAML.exists());
        ResourceQuota resourceQuota = KubernetesHelper.loadYaml(deploymentYAML);
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
    
        KubernetesUtils.deleteDirectory(targetPath);
        KubernetesTestUtils.deleteDockerImage(dockerImage);
    }
    
    /**
     * Build bal file with resource quota having a scope.
     * @throws IOException Error when loading the generated yaml.
     * @throws InterruptedException Error when compiling the ballerina file.
     * @throws KubernetesPluginException Error when deleting the generated artifacts folder.
     */
    @Test
    public void quotaWithScopeTest() throws IOException, InterruptedException, KubernetesPluginException {
        Assert.assertEquals(KubernetesTestUtils.compileBallerinaFile(balDirectory, "quota-with-scope.bal"), 0);
        
        // Check if docker image exists and correct
        validateDockerfile();
        validateDockerImage();
        
        // Validate deployment yaml
        File deploymentYAML = Paths.get(targetPath).resolve("quota-with-scope_resource_quota.yaml").toFile();
        Assert.assertTrue(deploymentYAML.exists());
        ResourceQuota resourceQuota = KubernetesHelper.loadYaml(deploymentYAML);
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
        
        KubernetesUtils.deleteDirectory(targetPath);
        KubernetesTestUtils.deleteDockerImage(dockerImage);
    }
    
    /**
     * Build bal file having multiple resource quotas.
     * @throws IOException Error when loading the generated yaml.
     * @throws InterruptedException Error when compiling the ballerina file.
     * @throws KubernetesPluginException Error when deleting the generated artifacts folder.
     */
    @Test
    public void multipleQuotaTest() throws IOException, InterruptedException, KubernetesPluginException {
        Assert.assertEquals(KubernetesTestUtils.compileBallerinaFile(balDirectory, "multiple-quotas.bal"), 0);
        
        // Check if docker image exists and correct
        validateDockerfile();
        validateDockerImage();
        
        // Validate deployment yaml
        File deploymentYAML = Paths.get(targetPath).resolve("multiple-quotas_resource_quota.yaml").toFile();
        Assert.assertTrue(deploymentYAML.exists());
        ResourceQuota resourceQuota = KubernetesHelper.loadYaml(deploymentYAML);
    
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
        
        // TODO: Assert additional quota

        KubernetesUtils.deleteDirectory(targetPath);
        KubernetesTestUtils.deleteDockerImage(dockerImage);
    }
    
    /**
     * Build bal file with deployment having invalid environment variables. This should fail.
     * @throws IOException Error when loading the generated yaml.
     * @throws InterruptedException Error when compiling the ballerina file.
     * @throws KubernetesPluginException Error when deleting the generated artifacts folder.
     */
    @Test
    public void invalidTest() throws IOException, InterruptedException, KubernetesPluginException {
        Assert.assertEquals(KubernetesTestUtils.compileBallerinaFile(balDirectory, "quota-with-invalid-scope.bal"), 1);
        KubernetesUtils.deleteDirectory(targetPath);
    }
    
    /**
     * Validate if Dockerfile is created.
     */
    public void validateDockerfile() {
        File dockerFile = new File(targetPath + File.separator + DOCKER + File.separator + "Dockerfile");
        Assert.assertTrue(dockerFile.exists());
    }
    
    /**
     * Validate contents of the Dockerfile.
     */
    public void validateDockerImage() {
        ImageInspect imageInspect = getDockerImage(dockerImage);
        Assert.assertEquals(1, imageInspect.getContainerConfig().getExposedPorts().size());
        Assert.assertTrue(imageInspect.getContainerConfig().getExposedPorts().keySet().contains("9099/tcp"));
    }
}
