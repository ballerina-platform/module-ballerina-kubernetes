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
    private Map<String, ServiceModel> bEndpointToK8sServiceMap;
    private Map<String, IngressModel> bServiceToIngressMap;
    private Map<String, Set<SecretModel>> endPointToSecretMap;
    private Set<SecretModel> secretModelSet;
    private Set<ConfigMapModel> configMapModelSet;
    private Set<PersistentVolumeClaimModel> volumeClaimModelSet;
    private static KubernetesDataHolder instance;

    private KubernetesDataHolder() {
        this.bEndpointToK8sServiceMap = new HashMap<>();
        this.bServiceToIngressMap = new HashMap<>();
        endPointToSecretMap = new HashMap<>();
        secretModelSet = new HashSet<>();
        configMapModelSet = new HashSet<>();
        volumeClaimModelSet = new HashSet<>();
    }

    public static KubernetesDataHolder getInstance() {
        if (instance == null) {
            synchronized (KubernetesDataHolder.class) {
                if (instance == null) {
                    instance = new KubernetesDataHolder();
                }
            }
        }
        return instance;
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

    public Map<String, Set<SecretModel>> getSecretModels() {
        return endPointToSecretMap;
    }

    public void addEndpointSecret(String endpointName, Set<SecretModel> secretModel) {
        this.endPointToSecretMap.put(endpointName, secretModel);
    }

    public Set<SecretModel> getSecretModelSet() {
        return secretModelSet;
    }

    public void addSecrets(Set<SecretModel> secrets) {
        this.secretModelSet.addAll(secrets);
    }

    public Set<ConfigMapModel> getConfigMapModelSet() {
        return configMapModelSet;
    }

    public void addConfigMaps(Set<ConfigMapModel> configMaps) {
        this.configMapModelSet.addAll(configMaps);
    }

    public Set<PersistentVolumeClaimModel> getVolumeClaimModelSet() {
        return volumeClaimModelSet;
    }

    public void addPersistentVolumeClaims(Set<PersistentVolumeClaimModel> persistentVolumeClaims) {
        this.volumeClaimModelSet.addAll(persistentVolumeClaims);
    }

    public Map<String, ServiceModel> getbEndpointToK8sServiceMap() {
        return bEndpointToK8sServiceMap;
    }

    public void addBEndpointToK8sServiceMap(String endpointName, ServiceModel serviceModel) {
        this.bEndpointToK8sServiceMap.put(endpointName, serviceModel);
    }

    public Map<String, IngressModel> getbServiceToIngressMap() {
        return bServiceToIngressMap;
    }

    public void addBServiceToIngressMap(String ballerinaServiceName, IngressModel ingressModel) {
        this.bServiceToIngressMap.put(ballerinaServiceName, ingressModel);
    }

    public ServiceModel getServiceModel(String endpointName){
        return bEndpointToK8sServiceMap.get(endpointName);
    }
}
