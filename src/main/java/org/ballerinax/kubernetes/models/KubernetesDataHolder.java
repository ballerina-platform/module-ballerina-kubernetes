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

package org.ballerinax.kubernetes.models;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Model class to store kubernetes artifacts.
 */
public class KubernetesDataHolder {
    private DeploymentModel deploymentModel;
    private PodAutoscalerModel podAutoscalerModel;
    private Map<String, ServiceModel> endpointToServiceModelMap;
    private Map<IngressModel, List<String>> ingressToEndpointMap;
    private Set<Integer> ports;
    private Set<SecretModel> secretModels;

    public KubernetesDataHolder() {
        endpointToServiceModelMap = new HashMap<>();
        ingressToEndpointMap = new HashMap<>();
        ports = new HashSet<>();
        secretModels = new HashSet<>();
    }

    public DeploymentModel getDeploymentModel() {
        return deploymentModel;
    }

    public void setDeploymentModel(DeploymentModel deploymentModel) {
        this.deploymentModel = deploymentModel;
    }

    public PodAutoscalerModel getPodAutoscalerModel() {
        return podAutoscalerModel;
    }

    public void setPodAutoscalerModel(PodAutoscalerModel podAutoscalerModel) {
        this.podAutoscalerModel = podAutoscalerModel;
    }

    public Set<Integer> getPorts() {
        return ports;
    }

    public void addPort(int port) {
        this.ports.add(port);
    }

    public Map<IngressModel, List<String>> getIngressToEndpointMap() {
        return ingressToEndpointMap;
    }

    public void addIngressModel(IngressModel ingressModel, List<String> endpoints) {
        this.ingressToEndpointMap.put(ingressModel, endpoints);
    }

    public Map<String, ServiceModel> getEndpointToServiceModelMap() {
        return endpointToServiceModelMap;
    }

    public void addServiceModel(String endpointName, ServiceModel serviceModel) {
        this.endpointToServiceModelMap.put(endpointName, serviceModel);
    }

    public Set<SecretModel> getSSLFiles() {
        return secretModels;
    }

    public void addSSLFile(SecretModel secretModel) {
        this.secretModels.add(secretModel);
    }
}
