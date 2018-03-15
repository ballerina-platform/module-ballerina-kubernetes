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

import java.util.List;

/**
 * Docker annotations model class.
 */
public class DockerModel {
    private String name;
    private String registry;
    private String tag;
    private boolean push;
    private String username;
    private String password;
    private boolean buildImage;
    private String baseImage;
    private List<Integer> ports;
    private boolean enableDebug;
    private int debugPort;
    private String balxFileName;
    private boolean isService;

    public DockerModel() {
        // Initialize with default values except for image name
        this.tag = "latest";
        this.push = false;
        this.buildImage = true;
        this.baseImage = "ballerina/ballerina:latest";
        this.enableDebug = false;
        this.debugPort = 5005;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public List<Integer> getPorts() {
        return ports;
    }

    public void setPorts(List<Integer> ports) {
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

    @Override
    public String toString() {
        return "DockerModel{" +
                "name='" + name + '\'' +
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
}
