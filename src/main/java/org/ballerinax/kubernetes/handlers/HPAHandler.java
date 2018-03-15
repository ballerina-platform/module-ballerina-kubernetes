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

import com.fasterxml.jackson.core.JsonProcessingException;
import io.fabric8.kubernetes.api.model.HorizontalPodAutoscaler;
import io.fabric8.kubernetes.api.model.HorizontalPodAutoscalerBuilder;
import io.fabric8.kubernetes.client.internal.SerializationUtils;
import org.ballerinax.kubernetes.exceptions.KubernetesPluginException;
import org.ballerinax.kubernetes.models.PodAutoscalerModel;

import static org.ballerinax.kubernetes.utils.KubernetesUtils.printError;

/**
 * Generates kubernetes Horizontal Pod Autoscaler from annotations.
 */
public class HPAHandler implements ArtifactHandler {


    private PodAutoscalerModel podAutoscalerModel;

    public HPAHandler(PodAutoscalerModel podAutoscalerModel) {
        this.podAutoscalerModel = podAutoscalerModel;
    }

    @Override
    /**
     * Generate kubernetes Horizontal pod autoscaler definition from annotation.
     *
     * @return Generated kubernetes {@link Ingress} definition
     * @throws KubernetesPluginException If an error occurs while generating artifact.
     */
    public String generate() throws KubernetesPluginException {
        HorizontalPodAutoscaler horizontalPodAutoscaler = new HorizontalPodAutoscalerBuilder()
                .withNewMetadata()
                .withName(podAutoscalerModel.getName())
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
            return SerializationUtils.dumpWithoutRuntimeStateAsYaml(horizontalPodAutoscaler);
        } catch (JsonProcessingException e) {
            String errorMessage = "Error while generating yaml file for autoscaler: " + podAutoscalerModel.getName();
            printError(errorMessage);
            throw new KubernetesPluginException(errorMessage, e);
        }
    }
}
