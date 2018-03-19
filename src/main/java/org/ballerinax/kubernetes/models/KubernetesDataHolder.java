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
import java.util.Map;
import java.util.Set;

/**
 * Model class to store kubernetes artifacts.
 */
public class KubernetesDataHolder {
    private DeploymentModel deploymentModel;
    private PodAutoscalerModel podAutoscalerModel;
    private Map<String, ServiceModel> endpointToServiceModelMap;
    private Map<IngressModel, Set<String>> ingressToEndpointMap;
    private Set<Integer> ports;
    private Map<String, Set<SecretModel>> endPointToSecretMap;
    private Set<SecretModel> secrets;

    public KubernetesDataHolder() {
        endpointToServiceModelMap = new HashMap<>();
        ingressToEndpointMap = new HashMap();
        ports = new HashSet<>();
        endPointToSecretMap = new HashMap<>();
        secrets = new HashSet<>();
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

    public Map<IngressModel, Set<String>> getIngressToEndpointMap() {
        return ingressToEndpointMap;
    }

    public void addIngressModel(IngressModel ingressModel, Set<String> endpoints) {
        this.ingressToEndpointMap.put(ingressModel, endpoints);
    }

    public Map<String, ServiceModel> getEndpointToServiceModelMap() {
        return endpointToServiceModelMap;
    }

    public void addServiceModel(String endpointName, ServiceModel serviceModel) {
        this.endpointToServiceModelMap.put(endpointName, serviceModel);
    }

    public Map<String, Set<SecretModel>> getSecretModels() {
        return endPointToSecretMap;
    }


    public void addEndpointSecret(String endpointName, Set<SecretModel> secretModel) {
        this.endPointToSecretMap.put(endpointName, secretModel);
    }

    public Set<SecretModel> getSecrets() {
        return secrets;
    }

    public void addSecrets(Set<SecretModel> secrets) {
        this.secrets.addAll(secrets);
    }
}
