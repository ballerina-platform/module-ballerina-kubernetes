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

package org.ballerinax.kubernetes.handlers;

import io.fabric8.kubernetes.api.KubernetesHelper;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.extensions.Deployment;
import org.ballerinax.kubernetes.KubernetesConstants;
import org.ballerinax.kubernetes.exceptions.KubernetesPluginException;
import org.ballerinax.kubernetes.models.DeploymentModel;
import org.ballerinax.kubernetes.utils.KubernetesUtils;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Generates kubernetes Service from annotations.
 */
public class KubernetesDeploymentGeneratorTests {

    private final Logger log = LoggerFactory.getLogger(KubernetesDeploymentGeneratorTests.class);
    private final String deploymentName = "MyDeployment";
    private final String selector = "TestAPP";
    private final String imageName = "SampleImage:v1.0.0";
    private final String imagePullPolicy = "Always";
    private final int replicas = 5;


    @Test
    public void testServiceGenerate() {
        DeploymentModel deploymentModel = new DeploymentModel();
        deploymentModel.setName(deploymentName);
        Map<String, String> labels = new HashMap<>();
        labels.put(KubernetesConstants.KUBERNETES_SELECTOR_KEY, selector);
        deploymentModel.addPort(9090);
        deploymentModel.addPort(9091);
        deploymentModel.addPort(9092);
        deploymentModel.setLabels(labels);
        deploymentModel.setImage(imageName);
        deploymentModel.setImagePullPolicy(imagePullPolicy);
        deploymentModel.setEnableLiveness("enable");
        deploymentModel.setLivenessPort(9090);
        Map<String, String> env = new HashMap<>();
        env.put("ENV_VAR", "ENV");
        deploymentModel.setEnv(env);
        deploymentModel.setReplicas(replicas);

        try {
            String deploymentYAML = new DeploymentHandler(deploymentModel).generate();
            Assert.assertNotNull(deploymentYAML);
            File artifactLocation = new File("target/kubernetes");
            artifactLocation.mkdir();
            File tempFile = File.createTempFile("temp", deploymentModel.getName() + ".yaml", artifactLocation);
            KubernetesUtils.writeToFile(deploymentYAML, tempFile.getPath());
            log.info("Generated YAML: \n" + deploymentYAML);
            Assert.assertTrue(tempFile.exists());
            testGeneratedYAML(tempFile);
            tempFile.deleteOnExit();
        } catch (IOException e) {
            Assert.fail("Unable to write to file");
        } catch (KubernetesPluginException e) {
            Assert.fail("Unable to generate yaml from service");
        }
    }

    private void testGeneratedYAML(File yamlFile) throws IOException {
        Deployment deployment = KubernetesHelper.loadYaml(yamlFile);
        Assert.assertEquals(deploymentName, deployment.getMetadata().getName());
        Assert.assertEquals(selector, deployment.getMetadata().getLabels().get(KubernetesConstants
                .KUBERNETES_SELECTOR_KEY));
        Assert.assertEquals(replicas, deployment.getSpec().getReplicas().intValue());

        Assert.assertEquals(1, deployment.getSpec().getTemplate().getSpec().getContainers().size());
        Container container = deployment.getSpec().getTemplate().getSpec().getContainers().get(0);
        Assert.assertEquals(imageName, container.getImage());
        Assert.assertEquals(imagePullPolicy, container.getImagePullPolicy());
        Assert.assertEquals(3, container.getPorts().size());
        Assert.assertEquals(5, container.getLivenessProbe().getPeriodSeconds().intValue());
        Assert.assertEquals(10, container.getLivenessProbe().getInitialDelaySeconds().intValue());
        Assert.assertEquals(1, container.getEnv().size());
    }
}
