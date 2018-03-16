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
import java.util.List;
import java.util.Map;

/**
 * Kubernetes deployment annotations model class.
 */
public class DeploymentModel {
    private String name;
    private Map<String, String> labels;
    private int replicas;
    private String enableLiveness;
    private int livenessPort;
    private int initialDelaySeconds;
    private int periodSeconds;
    private String imagePullPolicy;
    private String namespace;
    private String image;
    private boolean buildImage;
    private String baseImage;
    private Map<String, String> env;
    private String username;
    private String password;
    private boolean push;
    private String dockerHost;
    private List<Integer> ports;
    private PodAutoscalerModel podAutoscalerModel;

    public DeploymentModel() {
        // Initialize with default values.
        this.replicas = 1;
        this.enableLiveness = "disable";
        this.periodSeconds = 5;
        this.initialDelaySeconds = 10;
        this.buildImage = true;
        this.baseImage = "ballerina/ballerina:latest";
        this.push = false;
        this.labels = new HashMap<>();
        this.setImagePullPolicy("IfNotPresent");
        this.dockerHost = "unix:///var/run/docker.sock";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, String> getLabels() {
        return labels;
    }

    public void setLabels(Map<String, String> labels) {
        this.labels = labels;
    }

    public int getReplicas() {
        return replicas;
    }

    public void setReplicas(int replicas) {
        this.replicas = replicas;
    }

    public String getEnableLiveness() {
        return enableLiveness;
    }

    public void setEnableLiveness(String enableLiveness) {
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

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public List<Integer> getPorts() {
        return ports;
    }

    public void setPorts(List<Integer> ports) {
        this.ports = ports;
    }

    public Map<String, String> getEnv() {
        return env;
    }

    public void setEnv(Map<String, String> env) {
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

    public PodAutoscalerModel getPodAutoscalerModel() {
        return podAutoscalerModel;
    }

    public void setPodAutoscalerModel(PodAutoscalerModel podAutoscalerModel) {
        this.podAutoscalerModel = podAutoscalerModel;
    }

    @Override
    public String toString() {
        return "DeploymentModel{" +
                "name='" + name + '\'' +
                ", labels=" + labels +
                ", replicas=" + replicas +
                ", enableLiveness='" + enableLiveness + '\'' +
                ", livenessPort=" + livenessPort +
                ", initialDelaySeconds=" + initialDelaySeconds +
                ", periodSeconds=" + periodSeconds +
                ", imagePullPolicy='" + imagePullPolicy + '\'' +
                ", namespace='" + namespace + '\'' +
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
}
