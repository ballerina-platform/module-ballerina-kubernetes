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
 *//*


package org.ballerinax.kubernetes.handlers.knative;

import io.fabric8.kubernetes.api.model.HorizontalPodAutoscaler;
import io.fabric8.kubernetes.api.model.HorizontalPodAutoscalerBuilder;
import io.fabric8.kubernetes.api.model.MetricSpec;
import io.fabric8.kubernetes.api.model.MetricSpecBuilder;
import io.fabric8.kubernetes.client.internal.SerializationUtils;
import org.ballerinax.kubernetes.KubernetesConstants;
import org.ballerinax.kubernetes.exceptions.KubernetesPluginException;
import org.ballerinax.kubernetes.models.knative.PodAutoscalerModel;
import org.ballerinax.kubernetes.models.knative.ServiceModel;

import java.io.IOException;

import static org.ballerinax.docker.generator.utils.DockerGenUtils.extractUberJarName;
import static org.ballerinax.kubernetes.KubernetesConstants.HPA_FILE_POSTFIX;
import static org.ballerinax.kubernetes.KubernetesConstants.HPA_POSTFIX;
import static org.ballerinax.kubernetes.KubernetesConstants.YAML;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.getValidName;

*/
/**
 * Generates kubernetes Horizontal Pod Autoscaler from annotations.
 *//*

public class KnativeHPAHandler extends KnativeAbstractArtifactHandler {

    private void generate(PodAutoscalerModel podAutoscalerModel) throws KubernetesPluginException {
        MetricSpec metricSpec = new MetricSpecBuilder()
                .withType("Resource")
                .withNewResource()
                .withName("cpu")
                //.withTargetAverageUtilization(podAutoscalerModel.getCpuPercentage())
                .endResource()
                .build();

        HorizontalPodAutoscaler horizontalPodAutoscaler = new HorizontalPodAutoscalerBuilder()
                .withNewMetadata()
                .withName(podAutoscalerModel.getName())
                .withNamespace(knativeDataHolder.getNamespace())
                .withLabels(podAutoscalerModel.getLabels())
                .endMetadata()
                .withNewSpec()
                .withMaxReplicas(podAutoscalerModel.getMaxReplicas())
                .withMinReplicas(podAutoscalerModel.getMinReplicas())
                .withMetrics(metricSpec)
                .withNewScaleTargetRef("apps/v1", "Deployment", podAutoscalerModel.getDeployment())
                .endSpec()
                .build();
        try {
            String serviceContent = SerializationUtils.dumpWithoutRuntimeStateAsYaml(horizontalPodAutoscaler);
            KubernetesUtils.writeToFile(serviceContent, HPA_FILE_POSTFIX + YAML);
        } catch (IOException e) {
            String errorMessage = "error while generating yaml file for autoscaler: " + podAutoscalerModel.getName();
            throw new KubernetesPluginException(errorMessage, e);
        }
    }

    @Override
    public void createArtifacts() throws KubernetesPluginException {
        ServiceModel serviceModel = knativeDataHolder.getServiceModel();
        PodAutoscalerModel podAutoscalerModel = serviceModel.getPodAutoscalerModel();
        if (podAutoscalerModel == null) {
            return;
        }
        String balxFileName = extractUberJarName(knativeDataHolder.getUberJarPath());
        podAutoscalerModel.addLabel(KubernetesConstants.KUBERNETES_SELECTOR_KEY, balxFileName);
        podAutoscalerModel.setDeployment(serviceModel.getName());
        if (podAutoscalerModel.getMaxReplicas() == 0) {
            podAutoscalerModel.setMaxReplicas(serviceModel.getReplicas() + 1);
        }
        if (podAutoscalerModel.getMinReplicas() == 0) {
            podAutoscalerModel.setMinReplicas(serviceModel.getReplicas());
        }
        if (podAutoscalerModel.getName() == null || podAutoscalerModel.getName().length() == 0) {
            podAutoscalerModel.setName(getValidName(balxFileName) + HPA_POSTFIX);
        }
        generate(podAutoscalerModel);
        OUT.println();
        OUT.print("\t@Knative:HPA \t\t\t - complete 1/1");
    }






}
*/
