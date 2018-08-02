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

import io.fabric8.kubernetes.api.model.HorizontalPodAutoscaler;
import io.fabric8.kubernetes.api.model.HorizontalPodAutoscalerBuilder;
import io.fabric8.kubernetes.client.internal.SerializationUtils;
import org.ballerinax.kubernetes.KubernetesConstants;
import org.ballerinax.kubernetes.exceptions.KubernetesPluginException;
import org.ballerinax.kubernetes.models.DeploymentModel;
import org.ballerinax.kubernetes.models.PodAutoscalerModel;
import org.ballerinax.kubernetes.utils.KubernetesUtils;

import java.io.IOException;

import static org.ballerinax.kubernetes.KubernetesConstants.HPA_FILE_POSTFIX;
import static org.ballerinax.kubernetes.KubernetesConstants.HPA_POSTFIX;
import static org.ballerinax.kubernetes.KubernetesConstants.YAML;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.getValidName;

/**
 * Generates kubernetes Horizontal Pod Autoscaler from annotations.
 */
public class HPAHandler extends AbstractArtifactHandler {

    private void generate(PodAutoscalerModel podAutoscalerModel) throws KubernetesPluginException {
        HorizontalPodAutoscaler horizontalPodAutoscaler = new HorizontalPodAutoscalerBuilder()
                .withNewMetadata()
                .withName(podAutoscalerModel.getName())
                .withNamespace(dataHolder.getNamespace())
                .withLabels(podAutoscalerModel.getLabels())
                .endMetadata()
                .withNewSpec()
                .withMaxReplicas(podAutoscalerModel.getMaxReplicas())
                .withMinReplicas(podAutoscalerModel.getMinReplicas())
                .withTargetCPUUtilizationPercentage(podAutoscalerModel.getCpuPercentage())
                .withNewScaleTargetRef("extensions/v1beta1", "Deployment", podAutoscalerModel.getDeployment())
                .endSpec()
                .build();
        try {
            String serviceContent = SerializationUtils.dumpWithoutRuntimeStateAsYaml(horizontalPodAutoscaler);
            KubernetesUtils.writeToFile(serviceContent, HPA_FILE_POSTFIX + YAML);
        } catch (IOException e) {
            String errorMessage = "Error while generating yaml file for autoscaler: " + podAutoscalerModel.getName();
            throw new KubernetesPluginException(errorMessage, e);
        }
    }

    @Override
    public void createArtifacts() throws KubernetesPluginException {
        DeploymentModel deploymentModel = dataHolder.getDeploymentModel();
        PodAutoscalerModel podAutoscalerModel = deploymentModel.getPodAutoscalerModel();
        if (podAutoscalerModel == null) {
            return;
        }
        String balxFileName = KubernetesUtils.extractBalxName(dataHolder.getBalxFilePath());
        podAutoscalerModel.addLabel(KubernetesConstants.KUBERNETES_SELECTOR_KEY, balxFileName);
        podAutoscalerModel.setDeployment(deploymentModel.getName());
        if (podAutoscalerModel.getMaxReplicas() == 0) {
            podAutoscalerModel.setMaxReplicas(deploymentModel.getReplicas() + 1);
        }
        if (podAutoscalerModel.getMinReplicas() == 0) {
            podAutoscalerModel.setMinReplicas(deploymentModel.getReplicas());
        }
        if (podAutoscalerModel.getName() == null || podAutoscalerModel.getName().length() == 0) {
            podAutoscalerModel.setName(getValidName(balxFileName) + HPA_POSTFIX);
        }
        generate(podAutoscalerModel);
        OUT.println("\t@kubernetes:HPA \t\t\t - complete 1/1");
    }
}
