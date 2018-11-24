/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 *
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

import org.ballerinax.docker.generator.models.CopyFileModel;
import org.ballerinax.kubernetes.KubernetesConstants;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static org.ballerinax.docker.generator.DockerGenConstants.BALLERINA_BASE_IMAGE;
import static org.ballerinax.kubernetes.KubernetesConstants.UNIX_DEFAULT_DOCKER_HOST;
import static org.ballerinax.kubernetes.KubernetesConstants.WINDOWS_DEFAULT_DOCKER_HOST;

/**
 * Job model class.
 */
public class JobModel extends KubernetesModel {
    private String restartPolicy;
    private int backoffLimit;
    private int activeDeadlineSeconds;
    private String schedule;
    private Map<String, EnvVarValueModel> env;
    private String imagePullPolicy;
    private String image;
    private boolean buildImage;
    private String dockerHost;
    private String username;
    private String password;
    private String baseImage;
    private boolean push;
    private String dockerCertPath;
    private Set<String> imagePullSecrets;
    private Set<CopyFileModel> copyFiles;
    private boolean singleYAML;

    public JobModel() {
        this.labels = new HashMap<>();
        this.env = new LinkedHashMap<>();
        this.copyFiles = new HashSet<>();
        this.restartPolicy = KubernetesConstants.RestartPolicy.Never.name();
        String baseImageVersion = getClass().getPackage().getImplementationVersion();
        this.setBaseImage(BALLERINA_BASE_IMAGE + ":" + baseImageVersion);
        this.setPush(false);
        this.buildImage = true;
        this.labels = new HashMap<>();
        this.setEnv(new HashMap<>());
        this.setImagePullPolicy("IfNotPresent");
        String operatingSystem = System.getProperty("os.name").toLowerCase(Locale.getDefault());
        if (operatingSystem.contains("win")) {
            this.dockerHost = WINDOWS_DEFAULT_DOCKER_HOST;
        } else {
            this.dockerHost = UNIX_DEFAULT_DOCKER_HOST;
        }
        this.activeDeadlineSeconds = 20;
        this.imagePullSecrets = new HashSet<>();
        this.singleYAML = true;
    }

    public void addLabel(String key, String value) {
        this.labels.put(key, value);
    }

    public String getRestartPolicy() {
        return restartPolicy;
    }

    public void setRestartPolicy(String restartPolicy) {
        this.restartPolicy = restartPolicy;
    }

    public int getBackoffLimit() {
        return backoffLimit;
    }

    public void setBackoffLimit(int backoffLimit) {
        this.backoffLimit = backoffLimit;
    }

    public int getActiveDeadlineSeconds() {
        return activeDeadlineSeconds;
    }

    public void setActiveDeadlineSeconds(int activeDeadlineSeconds) {
        this.activeDeadlineSeconds = activeDeadlineSeconds;
    }

    public String getSchedule() {
        return schedule;
    }

    public void setSchedule(String schedule) {
        this.schedule = schedule;
    }

    public Map<String, EnvVarValueModel> getEnv() {
        return env;
    }

    public void setEnv(Map<String, EnvVarValueModel> env) {
        this.env = env;
    }

    public void addEnv(String key, EnvVarValueModel value) {
        env.put(key, value);
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

    public boolean isBuildImage() {
        return buildImage;
    }

    public void setBuildImage(boolean buildImage) {
        this.buildImage = buildImage;
    }

    public String getDockerHost() {
        return dockerHost;
    }

    public void setDockerHost(String dockerHost) {
        this.dockerHost = dockerHost;
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

    public String getBaseImage() {
        return baseImage;
    }

    public void setBaseImage(String baseImage) {
        this.baseImage = baseImage;
    }

    public boolean isPush() {
        return push;
    }

    public void setPush(boolean push) {
        this.push = push;
    }

    public String getDockerCertPath() {
        return dockerCertPath;
    }

    public void setDockerCertPath(String dockerCertPath) {
        this.dockerCertPath = dockerCertPath;
    }

    public Set<String> getImagePullSecrets() {
        return imagePullSecrets;
    }

    public void setImagePullSecrets(Set<String> imagePullSecrets) {
        this.imagePullSecrets = imagePullSecrets;
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

}
