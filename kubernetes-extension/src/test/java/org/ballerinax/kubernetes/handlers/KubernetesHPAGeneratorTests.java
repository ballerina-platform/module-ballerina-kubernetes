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

import io.fabric8.kubernetes.api.model.autoscaling.v1.HorizontalPodAutoscaler;
import org.ballerinax.kubernetes.KubernetesConstants;
import org.ballerinax.kubernetes.exceptions.KubernetesPluginException;
import org.ballerinax.kubernetes.models.DeploymentModel;
import org.ballerinax.kubernetes.models.EnvVarValueModel;
import org.ballerinax.kubernetes.models.KubernetesContext;
import org.ballerinax.kubernetes.models.PodAutoscalerModel;
import org.ballerinax.kubernetes.models.ProbeModel;
import org.ballerinax.kubernetes.utils.Utils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Test kubernetes HPA generation.
 */
public class KubernetesHPAGeneratorTests extends HandlerTestSuite {


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
        ProbeModel probeModel = new ProbeModel();
        probeModel.setPort(9090);
        deploymentModel.setLivenessProbe(probeModel);
        deploymentModel.setSingleYAML(false);
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
            File tempFile = Paths.get("target", "kubernetes", module.name.toString(), "hello_hpa.yaml").toFile();
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
        HorizontalPodAutoscaler podAutoscaler = Utils.loadYaml(yamlFile);
        Assert.assertEquals(podAutoscaler.getMetadata().getName(), hpaName);
        Assert.assertEquals(podAutoscaler.getMetadata().getLabels().get(KubernetesConstants
                .KUBERNETES_SELECTOR_KEY), selector);
        Assert.assertEquals(podAutoscaler.getSpec().getMaxReplicas().intValue(), maxReplicas);
        Assert.assertEquals(podAutoscaler.getSpec().getMinReplicas().intValue(), minReplicas);
        Assert.assertEquals(podAutoscaler.getSpec().getTargetCPUUtilizationPercentage().intValue(), cpuPercentage);
        Assert.assertEquals(podAutoscaler.getSpec().getScaleTargetRef().getName(), deploymentName);
    }
}
