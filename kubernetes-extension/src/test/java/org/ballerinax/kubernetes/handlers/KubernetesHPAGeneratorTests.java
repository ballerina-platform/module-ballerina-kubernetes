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
import io.fabric8.kubernetes.api.model.HorizontalPodAutoscaler;
import org.ballerinax.kubernetes.KubernetesConstants;
import org.ballerinax.kubernetes.exceptions.KubernetesPluginException;
import org.ballerinax.kubernetes.models.DeploymentModel;
import org.ballerinax.kubernetes.models.EnvVarValueModel;
import org.ballerinax.kubernetes.models.KubernetesContext;
import org.ballerinax.kubernetes.models.PodAutoscalerModel;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Test kubernetes HPA generation.
 */
public class KubernetesHPAGeneratorTests {


    private final String hpaName = "MyHPA";
    private final String deploymentName = "MyDeployment";
    private final String selector = "hello";
    private final int cpuPercentage = 90;
    private final int maxReplicas = 10;
    private final int minReplicas = 2;

    @Test
    public void testHPAGenerate() {
        DeploymentModel deploymentModel = new DeploymentModel();
        deploymentModel.setName(deploymentName);
        Map<String, String> labels = new HashMap<>();
        labels.put(KubernetesConstants.KUBERNETES_SELECTOR_KEY, selector);
        deploymentModel.addPort(9090);
        deploymentModel.addPort(9091);
        deploymentModel.addPort(9092);
        deploymentModel.setLabels(labels);
        deploymentModel.setEnableLiveness(true);
        deploymentModel.setLivenessPort(9090);
        Map<String, EnvVarValueModel> env = new HashMap<>();
        EnvVarValueModel testEnvVar = new EnvVarValueModel("ENV");
        env.put("ENV_VAR", testEnvVar);
        deploymentModel.setEnv(env);

        PodAutoscalerModel podAutoscalerModel = new PodAutoscalerModel();
        podAutoscalerModel.setName(hpaName);
        podAutoscalerModel.setCpuPercentage(cpuPercentage);
        podAutoscalerModel.setMaxReplicas(maxReplicas);
        podAutoscalerModel.setMinReplicas(minReplicas);
        podAutoscalerModel.setDeployment(deploymentName);
        podAutoscalerModel.setLabels(labels);
        deploymentModel.setPodAutoscalerModel(podAutoscalerModel);
        KubernetesContext.getInstance().getDataHolder().setPodAutoscalerModel(podAutoscalerModel);
        KubernetesContext.getInstance().getDataHolder().setDeploymentModel(deploymentModel);
        try {
            new HPAHandler().createArtifacts();
            File tempFile = new File("target" + File.separator + "kubernetes" + File.separator + "hello_hpa.yaml");
            Assert.assertTrue(tempFile.exists());
            assertGeneratedYAML(tempFile);
            tempFile.deleteOnExit();
        } catch (IOException e) {
            Assert.fail("Unable to write to file");
        } catch (KubernetesPluginException e) {
            Assert.fail("Unable to generate yaml from service");
        }
    }

    private void assertGeneratedYAML(File yamlFile) throws IOException {
        HorizontalPodAutoscaler podAutoscaler = KubernetesHelper.loadYaml(yamlFile);
        Assert.assertEquals(hpaName, podAutoscaler.getMetadata().getName());
        Assert.assertEquals(selector, podAutoscaler.getMetadata().getLabels().get(KubernetesConstants
                .KUBERNETES_SELECTOR_KEY));
        Assert.assertEquals(maxReplicas, podAutoscaler.getSpec().getMaxReplicas().intValue());
        Assert.assertEquals(minReplicas, podAutoscaler.getSpec().getMinReplicas().intValue());
        Assert.assertEquals(cpuPercentage, podAutoscaler.getSpec().getTargetCPUUtilizationPercentage().intValue());
        Assert.assertEquals(deploymentName, podAutoscaler.getSpec().getScaleTargetRef().getName());
    }
}
