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
package org.ballerinalang.artifactgen.models;

import java.util.List;
import java.util.Map;

/**
 * Kubernetes deployment annotations model class.
 */
public class DeploymentModel {
    private String name;
    private Map<String, String> labels;
    private int replicas;
    private String liveness;
    private int livenessPort;
    private int initialDelaySeconds;
    private int periodSeconds;
    private String imagePullPolicy;
    private String namespace;
    private String image;
    private Map<String, String> env;
    private List<Integer> ports;

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

    public String getLiveness() {
        return liveness;
    }

    public void setLiveness(String liveness) {
        this.liveness = liveness;
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

    @Override
    public String toString() {
        return "DeploymentModel{" +
                "name='" + name + '\'' +
                ", labels=" + labels +
                ", replicas=" + replicas +
                ", liveness='" + liveness + '\'' +
                ", livenessPort=" + livenessPort +
                ", initialDelaySeconds=" + initialDelaySeconds +
                ", periodSeconds=" + periodSeconds +
                ", imagePullPolicy='" + imagePullPolicy + '\'' +
                ", namespace='" + namespace + '\'' +
                ", image='" + image + '\'' +
                ", env=" + env +
                ", ports=" + ports +
                '}';
    }
}
