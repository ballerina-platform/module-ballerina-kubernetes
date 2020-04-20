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

import io.fabric8.kubernetes.api.model.apps.DeploymentStrategy;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.ballerinax.docker.generator.models.CopyFileModel;
import org.ballerinax.kubernetes.KubernetesConstants;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.ballerinax.docker.generator.DockerGenConstants.OPENJDK_8_JRE_ALPINE_BASE_IMAGE;

/**
 * Kubernetes deployment annotations model class.
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class DeploymentModel extends KubernetesModel {
    private Map<String, String> podAnnotations;
    private int replicas;
    private ProbeModel livenessProbe;
    private ProbeModel readinessProbe;
    private String namespace;
    private String imagePullPolicy;
    private String image;
    private boolean buildImage;
    private String baseImage;
    private Map<String, EnvVarValueModel> env;
    private String username;
    private String password;
    private boolean push;
    private String cmd;
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
    private String registry;
    private DeploymentBuildExtension buildExtension;
    private List<PodTolerationModel> podTolerations;
    private DeploymentStrategy strategy;
    private Map<String, String> nodeSelector;
    private String serviceAccountName;
    private List<ServiceAccountTokenModel> serviceAccountTokenModel;

    public DeploymentModel() {
        // Initialize with default values.
        this.replicas = 1;
        this.buildImage = true;
        this.baseImage = OPENJDK_8_JRE_ALPINE_BASE_IMAGE;
        this.push = false;
        this.labels = new LinkedHashMap<>();
        this.nodeSelector = new LinkedHashMap<>();
        this.env = new LinkedHashMap<>();
        this.imagePullPolicy = KubernetesConstants.ImagePullPolicy.IfNotPresent.name();
        this.dependsOn = new HashSet<>();

        // Configure Docker Host based on operating system.
        this.ports = new HashSet<>();
        this.secretModels = new HashSet<>();
        this.configMapModels = new HashSet<>();
        this.volumeClaimModels = new HashSet<>();
        this.copyFiles = new HashSet<>();
        this.imagePullSecrets = new HashSet<>();
        this.singleYAML = true;
        this.commandArgs = "";
        this.registry = "";
        this.serviceAccountTokenModel = new ArrayList<>();
    }

    public Map<String, String> getPodAnnotations() {
        return podAnnotations;
    }

    public void setPodAnnotations(Map<String, String> podAnnotations) {
        this.podAnnotations = podAnnotations;
    }

    public void setLivenessProbe(ProbeModel livenessProbe) {
        this.livenessProbe = livenessProbe;

        // setting default values
        if (null != this.livenessProbe) {
            if (this.livenessProbe.getInitialDelaySeconds() == -1) {
                this.livenessProbe.setInitialDelaySeconds(10);
            }

            if (this.livenessProbe.getPeriodSeconds() == -1) {
                this.livenessProbe.setPeriodSeconds(5);
            }
        }
    }

    public void setReadinessProbe(ProbeModel readinessProbe) {
        this.readinessProbe = readinessProbe;

        // setting default values
        if (null != this.readinessProbe) {
            if (this.readinessProbe.getInitialDelaySeconds() == -1) {
                this.readinessProbe.setInitialDelaySeconds(3);
            }

            if (this.readinessProbe.getPeriodSeconds() == -1) {
                this.readinessProbe.setPeriodSeconds(1);
            }
        }
    }

    public void addPort(int port) {
        this.ports.add(port);
    }

    public void addLabel(String key, String value) {
        this.labels.put(key, value);
    }

    public void addEnv(String key, EnvVarValueModel value) {
        env.put(key, value);
    }

    @Override
    public String toString() {
        return "DeploymentModel{" +
                "podAnnotations=" + podAnnotations +
                ", replicas=" + replicas +
                ", livenessProbe=" + livenessProbe +
                ", namespace='" + namespace +
                ", imagePullPolicy='" + imagePullPolicy +
                ", image='" + image +
                ", buildImage=" + buildImage +
                ", baseImage='" + baseImage +
                ", env=" + env +
                ", username='" + username +
                ", push=" + push +
                ", cmd=" + cmd +
                ", dockerHost='" + dockerHost +
                ", dockerCertPath='" + dockerCertPath +
                ", ports=" + ports +
                ", podAutoscalerModel=" + podAutoscalerModel +
                ", secretModels=" + secretModels +
                ", configMapModels=" + configMapModels +
                ", volumeClaimModels=" + volumeClaimModels +
                ", copyFiles=" + copyFiles +
                ", dependsOn=" + dependsOn +
                ", imagePullSecrets=" + imagePullSecrets +
                ", commandArgs='" + commandArgs +
                ", singleYAML=" + singleYAML +
                ", registry='" + registry +
                ", buildExtension=" + buildExtension +
                ", podTolerations=" + podTolerations +
                ", serviceAccountTokenModel=" + serviceAccountTokenModel +
                '}';
    }
}
