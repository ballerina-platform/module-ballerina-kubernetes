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

import com.spotify.docker.client.DockerHost;
import org.ballerinax.docker.generator.models.CopyFileModel;
import org.ballerinax.kubernetes.KubernetesConstants;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static org.ballerinax.docker.generator.DockerGenConstants.BALLERINA_BASE_IMAGE;
import static org.ballerinax.kubernetes.KubernetesConstants.DOCKER_CERT_PATH;
import static org.ballerinax.kubernetes.KubernetesConstants.DOCKER_HOST;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.isBlank;

/**
 * Kubernetes deployment annotations model class.
 */
public class DeploymentModel extends KubernetesModel {
    private Map<String, String> podAnnotations;
    private int replicas;
    private boolean enableLiveness;
    private int livenessPort;
    private int initialDelaySeconds;
    private int periodSeconds;
    private String imagePullPolicy;
    private String image;
    private boolean buildImage;
    private String baseImage;
    private Map<String, EnvVarValueModel> env;
    private String username;
    private String password;
    private boolean push;
    private String dockerHost;
    private String dockerCertPath;
    private Set<Integer> ports;
    private PodAutoscalerModel podAutoscalerModel;
    private Set<SecretModel> secretModels;
    private Set<ConfigMapModel> configMapModels;
    private Set<PersistentVolumeClaimModel> volumeClaimModels;
    private Set<CopyFileModel> copyFiles;
    private Set<String> dependsOn;
    private Set<String> imagePullSecrets;
    private String commandArgs;
    private boolean singleYAML;

    public DeploymentModel() {
        // Initialize with default values.
        this.replicas = 1;
        this.enableLiveness = false;
        this.periodSeconds = 5;
        this.initialDelaySeconds = 10;
        this.buildImage = true;
        String baseImageVersion = getClass().getPackage().getImplementationVersion();
        this.baseImage = BALLERINA_BASE_IMAGE + ":" + baseImageVersion;
        this.push = false;
        this.labels = new LinkedHashMap<>();
        this.env = new LinkedHashMap<>();
        this.imagePullPolicy = KubernetesConstants.ImagePullPolicy.IfNotPresent.name();
        this.dependsOn = new HashSet<>();

        // Configure Docker Host based on operating system.
        String operatingSystem = System.getProperty("os.name").toLowerCase(Locale.getDefault());
        if (operatingSystem.contains("win")) {
            this.setDockerHost(DockerHost.defaultWindowsEndpoint());
        } else {
            this.setDockerHost(DockerHost.defaultUnixEndpoint());
        }
    
        String dockerHost = System.getenv(DOCKER_HOST);
        if (!isBlank(dockerHost)) {
            this.dockerHost = dockerHost;
        }
        String dockerCertPath = System.getenv(DOCKER_CERT_PATH);
        if (!isBlank(dockerCertPath)) {
            this.dockerCertPath = dockerCertPath;
        }
        
        this.ports = new HashSet<>();
        this.secretModels = new HashSet<>();
        this.configMapModels = new HashSet<>();
        this.volumeClaimModels = new HashSet<>();
        this.copyFiles = new HashSet<>();
        this.imagePullSecrets = new HashSet<>();
        this.singleYAML = true;
        this.commandArgs = "";
    }
    
    public Map<String, String> getPodAnnotations() {
        return podAnnotations;
    }
    
    public void setPodAnnotations(Map<String, String> podAnnotations) {
        this.podAnnotations = podAnnotations;
    }
    
    public int getReplicas() {
        return replicas;
    }

    public void setReplicas(int replicas) {
        this.replicas = replicas;
    }

    public boolean isEnableLiveness() {
        return enableLiveness;
    }

    public void setEnableLiveness(boolean enableLiveness) {
        this.enableLiveness = enableLiveness;
    }

    public int getInitialDelaySeconds() {
        return initialDelaySeconds;
    }

    public void setInitialDelaySeconds(int initialDelaySeconds) {
        this.initialDelaySeconds = initialDelaySeconds;
    }

    public int getPeriodSeconds() {
        return periodSeconds;
    }

    public void setPeriodSeconds(int periodSeconds) {
        this.periodSeconds = periodSeconds;
    }

    public String getImagePullPolicy() {
        return imagePullPolicy;
    }

    public void setImagePullPolicy(String imagePullPolicy) {
        this.imagePullPolicy = imagePullPolicy;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Set<Integer> getPorts() {
        return ports;
    }

    public void addPort(int port) {
        this.ports.add(port);
    }

    public Map<String, EnvVarValueModel> getEnv() {
        return env;
    }

    public void setEnv(Map<String, EnvVarValueModel> env) {
        this.env = env;
    }

    public int getLivenessPort() {
        return livenessPort;
    }

    public void setLivenessPort(int livenessPort) {
        this.livenessPort = livenessPort;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isPush() {
        return push;
    }

    public void setPush(boolean push) {
        this.push = push;
    }

    public boolean isBuildImage() {
        return buildImage;
    }

    public void setBuildImage(boolean buildImage) {
        this.buildImage = buildImage;
    }

    public String getBaseImage() {
        return baseImage;
    }

    public void setBaseImage(String baseImage) {
        this.baseImage = baseImage;
    }

    public void addLabel(String key, String value) {
        this.labels.put(key, value);
    }

    public void addEnv(String key, EnvVarValueModel value) {
        env.put(key, value);
    }

    public PodAutoscalerModel getPodAutoscalerModel() {
        return podAutoscalerModel;
    }

    public void setPodAutoscalerModel(PodAutoscalerModel podAutoscalerModel) {
        this.podAutoscalerModel = podAutoscalerModel;
    }

    @Override
    public String toString() {
        return "DeploymentModel{" +
                "name='" + getName() + '\'' +
                ", labels=" + labels +
                ", replicas=" + replicas +
                ", enableLiveness='" + enableLiveness + '\'' +
                ", livenessPort=" + livenessPort +
                ", initialDelaySeconds=" + initialDelaySeconds +
                ", periodSeconds=" + periodSeconds +
                ", imagePullPolicy='" + imagePullPolicy + '\'' +
                ", image='" + image + '\'' +
                ", buildImage=" + buildImage +
                ", baseImage='" + baseImage + '\'' +
                ", env=" + env +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", push=" + push +
                ", ports=" + ports +
                ", podAutoscalerModel=" + podAutoscalerModel +
                '}';
    }

    public String getDockerHost() {
        return dockerHost;
    }

    public void setDockerHost(String dockerHost) {
        this.dockerHost = dockerHost;
    }

    public Set<SecretModel> getSecretModels() {
        return secretModels;
    }

    public void setSecretModels(Set<SecretModel> secretModels) {
        this.secretModels = secretModels;
    }

    public Set<ConfigMapModel> getConfigMapModels() {
        return configMapModels;
    }

    public void setConfigMapModels(Set<ConfigMapModel> configMapModels) {
        this.configMapModels = configMapModels;
    }

    public Set<PersistentVolumeClaimModel> getVolumeClaimModels() {
        return volumeClaimModels;
    }

    public void setVolumeClaimModels(Set<PersistentVolumeClaimModel> volumeClaimModels) {
        this.volumeClaimModels = volumeClaimModels;
    }

    public String getDockerCertPath() {
        return dockerCertPath;
    }

    public void setDockerCertPath(String dockerCertPath) {
        this.dockerCertPath = dockerCertPath;
    }

    public String getCommandArgs() {
        return commandArgs;
    }

    public void setCommandArgs(String commandArg) {
        this.commandArgs = commandArg;
    }

    public Set<CopyFileModel> getCopyFiles() {
        return copyFiles;
    }

    public void setCopyFiles(Set<CopyFileModel> copyFiles) {
        this.copyFiles = copyFiles;
    }

    public boolean isSingleYAML() {
        return singleYAML;
    }

    public void setSingleYAML(boolean singleYAML) {
        this.singleYAML = singleYAML;
    }

    public Set<String> getDependsOn() {
        return dependsOn;
    }

    public void setDependsOn(Set<String> dependsOn) {
        this.dependsOn = dependsOn;
    }

    public Set<String> getImagePullSecrets() {
        return imagePullSecrets;
    }

    public void setImagePullSecrets(Set<String> imagePullSecrets) {
        this.imagePullSecrets = imagePullSecrets;
    }
}
