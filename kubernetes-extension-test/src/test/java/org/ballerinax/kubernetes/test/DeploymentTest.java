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
import io.fabric8.kubernetes.api.model.extensions.Deployment;
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
 * Test creating kubernetes deployment artifacts.
 */
public class DeploymentTest {
    private final String balDirectory = Paths.get("src").resolve("test").resolve("resources").resolve("deployment")
            .toAbsolutePath().toString();
    private final String targetPath = Paths.get(balDirectory).resolve(KUBERNETES).toString();
    private final String dockerImage = "pizza-shop:latest";
    
    /**
     * Build bal file with deployment having annotations.
     *
     * @throws IOException               Error when loading the generated yaml.
     * @throws InterruptedException      Error when compiling the ballerina file.
     * @throws KubernetesPluginException Error when deleting the generated artifacts folder.
     */
    @Test
    public void annotationsTest() throws IOException, InterruptedException, KubernetesPluginException {
        Assert.assertEquals(KubernetesTestUtils.compileBallerinaFile(balDirectory, "dep_annotations.bal"), 0);
        
        // Check if docker image exists and correct
        validateDockerfile();
        validateDockerImage();
        
        // Validate deployment yaml
        File deploymentYAML = Paths.get(targetPath).resolve("dep_annotations_deployment.yaml").toFile();
        Assert.assertTrue(deploymentYAML.exists());
        Deployment deployment = KubernetesHelper.loadYaml(deploymentYAML);
        Assert.assertEquals(deployment.getMetadata().getAnnotations().size(), 2,
                "Invalid number of annotations found.");
        Assert.assertEquals(deployment.getMetadata().getAnnotations().get("anno1"), "anno1Val",
                "Invalid annotation found.");
        Assert.assertEquals(deployment.getMetadata().getAnnotations().get("anno2"), "anno2Val",
                "Invalid annotation found.");
        
        KubernetesUtils.deleteDirectory(targetPath);
        KubernetesTestUtils.deleteDockerImage(dockerImage);
    }
    
    /**
     * Build bal file with deployment having pod annotations.
     *
     * @throws IOException               Error when loading the generated yaml.
     * @throws InterruptedException      Error when compiling the ballerina file.
     * @throws KubernetesPluginException Error when deleting the generated artifacts folder.
     */
    @Test
    public void podAnnotationsTest() throws IOException, InterruptedException, KubernetesPluginException {
        Assert.assertEquals(KubernetesTestUtils.compileBallerinaFile(balDirectory, "pod_annotations.bal"), 0);
        
        // Check if docker image exists and correct
        validateDockerfile();
        validateDockerImage();
        
        // Validate deployment yaml
        File deploymentYAML = Paths.get(targetPath).resolve("pod_annotations_deployment.yaml").toFile();
        Assert.assertTrue(deploymentYAML.exists());
        Deployment deployment = KubernetesHelper.loadYaml(deploymentYAML);
        Assert.assertEquals(deployment.getSpec().getTemplate().getMetadata().getAnnotations().size(), 2,
                "Invalid number of annotations found.");
        Assert.assertEquals(deployment.getSpec().getTemplate().getMetadata().getAnnotations().get("anno1"), "anno1Val",
                "Invalid annotation found.");
        Assert.assertEquals(deployment.getSpec().getTemplate().getMetadata().getAnnotations().get("anno2"), "anno2Val",
                "Invalid annotation found.");
        
        KubernetesUtils.deleteDirectory(targetPath);
        KubernetesTestUtils.deleteDockerImage(dockerImage);
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
        Assert.assertNotEquals(imageInspect, null, "Image not found");
    }
}
