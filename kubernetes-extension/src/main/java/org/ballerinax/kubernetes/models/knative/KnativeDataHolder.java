/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinax.kubernetes.models.knative;

import org.ballerinax.docker.generator.models.DockerModel;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Class to store kubernetes models.
 */

public class KnativeDataHolder {
    private boolean canProcess;
    private ServiceModel serviceModel;
    private DockerModel dockerModel;
    private org.ballerinax.kubernetes.models.knative.PodAutoscalerModel podAutoscalerModel;
    private Map<String, org.ballerinax.kubernetes.models.knative.KnativeContainerModel> bListenerToK8sServiceMap;
    private Map<String, Set<SecretModel>> bListenerToSecretMap;
    private Set<SecretModel> secretModelSet;
    private Set<ConfigMapModel> configMapModelSet;
    private Set<org.ballerinax.kubernetes.models.knative.ResourceQuotaModel> resourceQuotaModels;
    private Path uberJarPath;
    private Path k8sArtifactOutputPath;
    private Path dockerArtifactOutputPath;
    private String namespace;
    private Path sourceRoot;
    private boolean isProject = false;

    KnativeDataHolder(Path sourceRoot) {
        this.sourceRoot = sourceRoot;
        this.bListenerToK8sServiceMap = new HashMap<>();
        this.bListenerToSecretMap = new HashMap<>();
        this.secretModelSet = new HashSet<>();
        this.configMapModelSet = new HashSet<>();
        this.serviceModel = new ServiceModel();
        this.resourceQuotaModels = new HashSet<>();
    }

    public Path getSourceRoot() {
        return sourceRoot;
    }

    public boolean isProject() {
        return isProject;
    }

    public void setProject(boolean project) {
        isProject = project;
    }

    public ServiceModel getServiceModel() {
        return serviceModel;
    }

    public void setServiceModel(ServiceModel serviceModel) {
        this.serviceModel = serviceModel;
    }

    public org.ballerinax.kubernetes.models.knative.PodAutoscalerModel getPodAutoscalerModel() {
        return podAutoscalerModel;
    }

    public void setPodAutoscalerModel(PodAutoscalerModel podAutoscalerModel) {
        this.podAutoscalerModel = podAutoscalerModel;
    }

    public Map<String, Set<SecretModel>> getSecretModels() {
        return bListenerToSecretMap;
    }

    public void addListenerSecret(String listenerName, Set<SecretModel>
            secretModel) {
        this.bListenerToSecretMap.put(listenerName, secretModel);
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


    public Set<org.ballerinax.kubernetes.models.knative.ResourceQuotaModel> getResourceQuotaModels() {
        return resourceQuotaModels;
    }

    public void setResourceQuotaModels(Set<ResourceQuotaModel> resourceQuotaModels) {
        this.resourceQuotaModels = resourceQuotaModels;
    }

    public Map<String, org.ballerinax.kubernetes.models.knative.KnativeContainerModel> getbListenerToK8sServiceMap() {
        return bListenerToK8sServiceMap;
    }

    public void addBListenerToK8sServiceMap(String listenerName, org.ballerinax.kubernetes.models.knative.
            KnativeContainerModel knativeContainerModel) {
        this.bListenerToK8sServiceMap.put(listenerName, knativeContainerModel);
    }

    public KnativeContainerModel getServiceModel(String listener) {
        return bListenerToK8sServiceMap.get(listener);
    }

    public boolean isCanProcess() {
        return canProcess;
    }

    public void setCanProcess(boolean canProcess) {
        this.canProcess = canProcess;
    }

    public Path getUberJarPath() {
        return uberJarPath;
    }

    public void setUberJarPath(Path uberJarPath) {
        this.uberJarPath = uberJarPath;
    }

    public Path getK8sArtifactOutputPath() {
        return k8sArtifactOutputPath;
    }

    public void setK8sArtifactOutputPath(Path k8sArtifactOutputPath) {
        this.k8sArtifactOutputPath = k8sArtifactOutputPath;
    }

    public Path getDockerArtifactOutputPath() {
        return dockerArtifactOutputPath;
    }

    public void setDockerArtifactOutputPath(Path dockerArtifactOutputPath) {
        this.dockerArtifactOutputPath = dockerArtifactOutputPath;
    }

    public DockerModel getDockerModel() {
        return dockerModel;
    }

    public void setDockerModel(DockerModel dockerModel) {
        this.dockerModel = dockerModel;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

}
