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

import io.fabric8.kubernetes.api.model.EnvVar;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.ballerinax.kubernetes.KubernetesConstants.DOCKER;
import static org.ballerinax.kubernetes.KubernetesConstants.KUBERNETES;
import static org.ballerinax.kubernetes.test.utils.KubernetesTestUtils.getExposedPorts;

/**
 * Test setting environment variables for deployments.
 */
public class EnvVarTest {
    private static final Path BAL_DIRECTORY = Paths.get("src", "test", "resources", "env-vars");
    private static final Path TARGET_PATH = BAL_DIRECTORY.resolve(KUBERNETES);
    private static final String DOCKER_IMAGE = "pizza-shop:latest";
    
    /**
     * Build bal file with deployment having name value environment variables.
     * @throws IOException Error when loading the generated yaml.
     * @throws InterruptedException Error when compiling the ballerina file.
     * @throws KubernetesPluginException Error when deleting the generated artifacts folder.
     */
    @Test
    public void nameValueTest() throws IOException, InterruptedException, KubernetesPluginException,
            DockerTestException {
        Assert.assertEquals(KubernetesTestUtils.compileBallerinaFile(BAL_DIRECTORY, "name_value.bal"), 0);
        
        // Check if docker image exists and correct
        validateDockerfile();
        validateDockerImage();
        
        // Validate deployment yaml
        File deploymentYAML = TARGET_PATH.resolve("name_value_deployment.yaml").toFile();
        Assert.assertTrue(deploymentYAML.exists());
        Deployment deployment = KubernetesTestUtils.loadYaml(deploymentYAML);
        List<EnvVar> envVars = deployment.getSpec().getTemplate().getSpec().getContainers().get(0).getEnv();
        Assert.assertEquals(envVars.size(), 2, "Invalid number of environment variables found.");
        Assert.assertEquals(envVars.get(0).getName(), "location", "Invalid environment variable name found.");
        Assert.assertEquals(envVars.get(0).getValue(), "SL", "Invalid environment variable value found.");
        Assert.assertEquals(envVars.get(1).getName(), "city", "Invalid environment variable name found.");
        Assert.assertEquals(envVars.get(1).getValue(), "COLOMBO", "Invalid environment variable value found.");
    
        KubernetesUtils.deleteDirectory(TARGET_PATH);
        KubernetesTestUtils.deleteDockerImage(DOCKER_IMAGE);
    }
    
    /**
     * Build bal file with deployment having name value environment variables having build environment variable.
     *
     * @throws IOException               Error when loading the generated yaml.
     * @throws InterruptedException      Error when compiling the ballerina file.
     * @throws KubernetesPluginException Error when deleting the generated artifacts folder.
     */
    @Test
    public void nameValueBuildEnvVarTest() throws IOException, InterruptedException, KubernetesPluginException,
            DockerTestException {
        Map<String, String> bRunEnvVar = new HashMap<>();
        bRunEnvVar.put("DATABASE_USERNAME", "root");
        bRunEnvVar.put("DATABASE_PASSWORD", "rootpwd");
        Assert.assertEquals(KubernetesTestUtils.compileBallerinaFile(BAL_DIRECTORY, "build_name_value.bal", bRunEnvVar),
                0);
        
        // Check if docker image exists and correct
        validateDockerfile();
        validateDockerImage();
        
        // Validate deployment yaml
        File deploymentYAML = TARGET_PATH.resolve("build_name_value_deployment.yaml").toFile();
        Assert.assertTrue(deploymentYAML.exists());
        Deployment deployment = KubernetesTestUtils.loadYaml(deploymentYAML);
        List<EnvVar> envVars = deployment.getSpec().getTemplate().getSpec().getContainers().get(0).getEnv();
        Assert.assertEquals(envVars.size(), 4, "Invalid number of environment variables found.");
        Assert.assertEquals(envVars.get(0).getName(), "location", "Invalid environment variable name found.");
        Assert.assertEquals(envVars.get(0).getValue(), "SL", "Invalid environment variable value found.");
        Assert.assertEquals(envVars.get(1).getName(), "city", "Invalid environment variable name found.");
        Assert.assertEquals(envVars.get(1).getValue(), "COLOMBO", "Invalid environment variable value found.");
        Assert.assertEquals(envVars.get(2).getName(), "DATABASE_USERNAME", "Invalid environment variable name found.");
        Assert.assertEquals(envVars.get(2).getValue(), "root", "Invalid environment variable value found.");
        Assert.assertEquals(envVars.get(3).getName(), "DATABASE_PASSWORD", "Invalid environment variable name found.");
        Assert.assertEquals(envVars.get(3).getValue(), "rootpwd", "Invalid environment variable value found.");
        
        KubernetesUtils.deleteDirectory(TARGET_PATH);
        KubernetesTestUtils.deleteDockerImage(DOCKER_IMAGE);
    }
    
    /**
     * Build bal file with deployment having name and fieldRef environment variables.
     * @throws IOException Error when loading the generated yaml.
     * @throws InterruptedException Error when compiling the ballerina file.
     * @throws KubernetesPluginException Error when deleting the generated artifacts folder.
     */
    @Test
    public void fieldRefTest() throws IOException, InterruptedException, KubernetesPluginException,
            DockerTestException {
        Assert.assertEquals(KubernetesTestUtils.compileBallerinaFile(BAL_DIRECTORY, "field_ref_value.bal"), 0);
        
        // Check if docker image exists and correct
        validateDockerfile();
        validateDockerImage();
        
        // Validate deployment yaml
        File deploymentYAML = TARGET_PATH.resolve("field_ref_value_deployment.yaml").toFile();
        Assert.assertTrue(deploymentYAML.exists());
        Deployment deployment = KubernetesTestUtils.loadYaml(deploymentYAML);
        List<EnvVar> envVars = deployment.getSpec().getTemplate().getSpec().getContainers().get(0).getEnv();
        Assert.assertEquals(envVars.size(), 2, "Invalid number of environment variables found.");
        
        Assert.assertEquals(envVars.get(0).getName(), "MY_NODE_NAME", "Invalid environment variable name found.");
        Assert.assertEquals(envVars.get(0).getValueFrom().getFieldRef().getFieldPath(), "spec.nodeName",
                "Invalid environment variable value found.");
        
        Assert.assertEquals(envVars.get(1).getName(), "MY_POD_NAME", "Invalid environment variable name found.");
        Assert.assertEquals(envVars.get(1).getValueFrom().getFieldRef().getFieldPath(), "metadata.name",
                "Invalid environment variable value found.");
        
        KubernetesUtils.deleteDirectory(TARGET_PATH);
        KubernetesTestUtils.deleteDockerImage(DOCKER_IMAGE);
    }
    
    /**
     * Build bal file with deployment having name and configMapKeyRef environment variables.
     * @throws IOException Error when loading the generated yaml.
     * @throws InterruptedException Error when compiling the ballerina file.
     * @throws KubernetesPluginException Error when deleting the generated artifacts folder.
     */
    @Test
    public void secretKeyRefTest() throws IOException, InterruptedException, KubernetesPluginException,
            DockerTestException {
        Assert.assertEquals(KubernetesTestUtils.compileBallerinaFile(BAL_DIRECTORY, "secret_key_ref.bal"), 0);
        
        // Check if docker image exists and correct
        validateDockerfile();
        validateDockerImage();
        
        // Validate deployment yaml
        File deploymentYAML = TARGET_PATH.resolve("secret_key_ref_deployment.yaml").toFile();
        Assert.assertTrue(deploymentYAML.exists());
        Deployment deployment = KubernetesTestUtils.loadYaml(deploymentYAML);
        List<EnvVar> envVars = deployment.getSpec().getTemplate().getSpec().getContainers().get(0).getEnv();
        Assert.assertEquals(envVars.size(), 2, "Invalid number of environment variables found.");
        
        Assert.assertEquals(envVars.get(0).getName(), "SECRET_USERNAME", "Invalid environment variable name found.");
        Assert.assertEquals(envVars.get(0).getValueFrom().getSecretKeyRef().getName(), "test-secret",
                "Invalid environment variable value found.");
        Assert.assertEquals(envVars.get(0).getValueFrom().getSecretKeyRef().getKey(), "username",
                "Invalid environment variable value found.");
        
        Assert.assertEquals(envVars.get(1).getName(), "SECRET_PASSWORD", "Invalid environment variable name found.");
        Assert.assertEquals(envVars.get(1).getValueFrom().getSecretKeyRef().getName(), "test-secret",
                "Invalid environment variable value found.");
        Assert.assertEquals(envVars.get(1).getValueFrom().getSecretKeyRef().getKey(), "password",
                "Invalid environment variable value found.");
        
        KubernetesUtils.deleteDirectory(TARGET_PATH);
        KubernetesTestUtils.deleteDockerImage(DOCKER_IMAGE);
    }
    
    /**
     * Build bal file with deployment having name and configMapKeyRef environment variables.
     * @throws IOException Error when loading the generated yaml.
     * @throws InterruptedException Error when compiling the ballerina file.
     * @throws KubernetesPluginException Error when deleting the generated artifacts folder.
     */
    @Test
    public void resourceFieldRefTest() throws IOException, InterruptedException, KubernetesPluginException,
            DockerTestException {
        Assert.assertEquals(KubernetesTestUtils.compileBallerinaFile(BAL_DIRECTORY, "resource_field_ref_value.bal"), 0);
        
        // Check if docker image exists and correct
        validateDockerfile();
        validateDockerImage();
        
        // Validate deployment yaml
        File deploymentYAML = TARGET_PATH.resolve("resource_field_ref_value_deployment.yaml").toFile();
        Assert.assertTrue(deploymentYAML.exists());
        Deployment deployment = KubernetesTestUtils.loadYaml(deploymentYAML);
        List<EnvVar> envVars = deployment.getSpec().getTemplate().getSpec().getContainers().get(0).getEnv();
        Assert.assertEquals(envVars.size(), 2, "Invalid number of environment variables found.");
        
        Assert.assertEquals(envVars.get(0).getName(), "MY_CPU_REQUEST", "Invalid environment variable name found.");
        Assert.assertEquals(envVars.get(0).getValueFrom().getResourceFieldRef().getContainerName(), "client",
                "Invalid environment variable value found.");
        Assert.assertEquals(envVars.get(0).getValueFrom().getResourceFieldRef().getResource(), "requests.cpu",
                "Invalid environment variable value found.");
        
        Assert.assertEquals(envVars.get(1).getName(), "MY_CPU_LIMIT", "Invalid environment variable name found.");
        Assert.assertEquals(envVars.get(1).getValueFrom().getResourceFieldRef().getResource(), "limits.cpu",
                "Invalid environment variable value found.");
        
        KubernetesUtils.deleteDirectory(TARGET_PATH);
        KubernetesTestUtils.deleteDockerImage(DOCKER_IMAGE);
    }
    
    /**
     * Build bal file with deployment having name and configMapKeyRef environment variables.
     * @throws IOException Error when loading the generated yaml.
     * @throws InterruptedException Error when compiling the ballerina file.
     * @throws KubernetesPluginException Error when deleting the generated artifacts folder.
     */
    @Test
    public void configMapKeyRefTest() throws IOException, InterruptedException, KubernetesPluginException,
            DockerTestException {
        Assert.assertEquals(KubernetesTestUtils.compileBallerinaFile(BAL_DIRECTORY, "config_map_key_ref.bal"), 0);
        
        // Check if docker image exists and correct
        validateDockerfile();
        validateDockerImage();
        
        // Validate deployment yaml
        File deploymentYAML = TARGET_PATH.resolve("config_map_key_ref_deployment.yaml").toFile();
        Assert.assertTrue(deploymentYAML.exists());
        Deployment deployment = KubernetesTestUtils.loadYaml(deploymentYAML);
        List<EnvVar> envVars = deployment.getSpec().getTemplate().getSpec().getContainers().get(0).getEnv();
        Assert.assertEquals(envVars.size(), 2, "Invalid number of environment variables found.");
        
        Assert.assertEquals(envVars.get(0).getName(), "SPECIAL_LEVEL_KEY", "Invalid environment variable name found.");
        Assert.assertEquals(envVars.get(0).getValueFrom().getConfigMapKeyRef().getName(), "special-config",
                "Invalid environment variable value found.");
        Assert.assertEquals(envVars.get(0).getValueFrom().getConfigMapKeyRef().getKey(), "special.how",
                "Invalid environment variable value found.");
    
        Assert.assertEquals(envVars.get(1).getName(), "LOG_LEVEL", "Invalid environment variable name found.");
        Assert.assertEquals(envVars.get(1).getValueFrom().getConfigMapKeyRef().getName(), "env-config",
                "Invalid environment variable value found.");
        Assert.assertEquals(envVars.get(1).getValueFrom().getConfigMapKeyRef().getKey(), "log_level",
                "Invalid environment variable value found.");
        
        KubernetesUtils.deleteDirectory(TARGET_PATH);
        KubernetesTestUtils.deleteDockerImage(DOCKER_IMAGE);
    }
    
    /**
     * Build bal file with deployment having a combination of environment variables.
     * @throws IOException Error when loading the generated yaml.
     * @throws InterruptedException Error when compiling the ballerina file.
     * @throws KubernetesPluginException Error when deleting the generated artifacts folder.
     */
    @Test
    public void combinedTest() throws IOException, InterruptedException, KubernetesPluginException,
            DockerTestException {
        Assert.assertEquals(KubernetesTestUtils.compileBallerinaFile(BAL_DIRECTORY, "combination.bal"), 0);
        
        // Check if docker image exists and correct
        validateDockerfile();
        validateDockerImage();
        
        // Validate deployment yaml
        File deploymentYAML = TARGET_PATH.resolve("combination_deployment.yaml").toFile();
        Assert.assertTrue(deploymentYAML.exists());
        Deployment deployment = KubernetesTestUtils.loadYaml(deploymentYAML);
        List<EnvVar> envVars = deployment.getSpec().getTemplate().getSpec().getContainers().get(0).getEnv();
        Assert.assertEquals(envVars.size(), 5, "Invalid number of environment variables found.");
        
        Assert.assertEquals(envVars.get(0).getName(), "SPECIAL_LEVEL_KEY", "Invalid environment variable name found.");
        Assert.assertEquals(envVars.get(0).getValueFrom().getConfigMapKeyRef().getName(), "special-config",
                "Invalid environment variable value found.");
        Assert.assertEquals(envVars.get(0).getValueFrom().getConfigMapKeyRef().getKey(), "special.how",
                "Invalid environment variable value found.");
    
        Assert.assertEquals(envVars.get(1).getName(), "MY_NODE_NAME", "Invalid environment variable name found.");
        Assert.assertEquals(envVars.get(1).getValueFrom().getFieldRef().getFieldPath(), "spec.nodeName",
                "Invalid environment variable value found.");
        
        Assert.assertEquals(envVars.get(2).getName(), "location", "Invalid environment variable name found.");
        Assert.assertEquals(envVars.get(2).getValue(), "SL", "Invalid environment variable value found.");
    
        Assert.assertEquals(envVars.get(3).getName(), "MY_CPU_REQUEST", "Invalid environment variable name found.");
        Assert.assertEquals(envVars.get(3).getValueFrom().getResourceFieldRef().getContainerName(), "client",
                "Invalid environment variable value found.");
        Assert.assertEquals(envVars.get(3).getValueFrom().getResourceFieldRef().getResource(), "requests.cpu",
                "Invalid environment variable value found.");
    
        Assert.assertEquals(envVars.get(4).getName(), "SECRET_PASSWORD", "Invalid environment variable name found.");
        Assert.assertEquals(envVars.get(4).getValueFrom().getSecretKeyRef().getName(), "test-secret",
                "Invalid environment variable value found.");
        Assert.assertEquals(envVars.get(4).getValueFrom().getSecretKeyRef().getKey(), "password",
                "Invalid environment variable value found.");
        
        KubernetesUtils.deleteDirectory(TARGET_PATH);
        KubernetesTestUtils.deleteDockerImage(DOCKER_IMAGE);
    }
    
    /**
     * Build bal file with deployment having invalid environment variables. This should fail.
     * @throws IOException Error when loading the generated yaml.
     * @throws InterruptedException Error when compiling the ballerina file.
     * @throws KubernetesPluginException Error when deleting the generated artifacts folder.
     */
    @Test
    public void invalidTest() throws IOException, InterruptedException, KubernetesPluginException {
        Assert.assertEquals(KubernetesTestUtils.compileBallerinaFile(BAL_DIRECTORY, "invalid.bal"), 1);
        KubernetesUtils.deleteDirectory(TARGET_PATH);
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
        List<String> ports = getExposedPorts(DOCKER_IMAGE);
        Assert.assertEquals(ports.size(), 1);
        Assert.assertEquals(ports.get(0), "9099/tcp");
    }
}
