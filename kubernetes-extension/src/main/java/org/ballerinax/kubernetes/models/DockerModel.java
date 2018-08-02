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

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import static org.ballerinax.kubernetes.KubernetesConstants.UNIX_DEFAULT_DOCKER_HOST;
import static org.ballerinax.kubernetes.KubernetesConstants.WINDOWS_DEFAULT_DOCKER_HOST;

/**
 * Docker annotations model class.
 */
public class DockerModel extends KubernetesModel {
    private String registry;
    private String tag;
    private boolean push;
    private String username;
    private String password;
    private boolean buildImage;
    private String baseImage;
    private Set<Integer> ports;
    private boolean enableDebug;
    private int debugPort;
    private String dockerHost;
    private boolean isService;
    private String balxFileName;
    private String dockerCertPath;
    private String commandArg;
    private Set<ExternalFileModel> externalFiles;

    public DockerModel() {
        // Initialize with default values except for image name
        this.tag = "latest";
        this.push = false;
        this.buildImage = true;
        String baseImageVersion = getClass().getPackage().getImplementationVersion();
        this.baseImage = "ballerina/ballerina:" + baseImageVersion;
        this.enableDebug = false;
        this.debugPort = 5005;
        String operatingSystem = System.getProperty("os.name").toLowerCase(Locale.getDefault());
        if (operatingSystem.contains("win")) {
            this.dockerHost = WINDOWS_DEFAULT_DOCKER_HOST;
        } else {
            this.dockerHost = UNIX_DEFAULT_DOCKER_HOST;
        }
        externalFiles = new HashSet<>();
    }

    public String getRegistry() {
        return registry;
    }

    public void setRegistry(String registry) {
        this.registry = registry;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
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

    public Set<Integer> getPorts() {
        return ports;
    }

    public void setPorts(Set<Integer> ports) {
        this.ports = ports;
    }

    public String getBalxFileName() {
        return balxFileName;
    }

    public void setBalxFileName(String balxFileName) {
        this.balxFileName = balxFileName;
    }

    public boolean isService() {
        return isService;
    }

    public void setService(boolean service) {
        isService = service;
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

    public boolean isEnableDebug() {
        return enableDebug;
    }

    public void setEnableDebug(boolean enableDebug) {
        this.enableDebug = enableDebug;
    }

    public int getDebugPort() {
        return debugPort;
    }

    public void setDebugPort(int debugPort) {
        this.debugPort = debugPort;
    }

    public String getDockerHost() {
        return dockerHost;
    }

    public void setDockerHost(String dockerHost) {
        this.dockerHost = dockerHost;
    }

    @Override
    public String toString() {
        return "DockerModel{" +
                "name='" + getName() + '\'' +
                ", registry='" + registry + '\'' +
                ", tag='" + tag + '\'' +
                ", push=" + push +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", buildImage=" + buildImage +
                ", baseImage='" + baseImage + '\'' +
                ", ports=" + ports +
                ", enableDebug=" + enableDebug +
                ", debugPort=" + debugPort +
                ", balxFileName='" + balxFileName + '\'' +
                ", isService=" + isService +
                '}';
    }

    public String getDockerCertPath() {
        return dockerCertPath;
    }

    public void setDockerCertPath(String dockerCertPath) {
        this.dockerCertPath = dockerCertPath;
    }

    public String getCommandArg() {
        return commandArg;
    }

    public void setCommandArg(String commandArg) {
        this.commandArg = commandArg;
    }

    public Set<ExternalFileModel> getExternalFiles() {
        return externalFiles;
    }

    public void setExternalFiles(Set<ExternalFileModel> externalFiles) {
        this.externalFiles = externalFiles;
    }
}
