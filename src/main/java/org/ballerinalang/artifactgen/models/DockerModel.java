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

/**
 * Docker annotations model class.
 */
public class DockerModel {
    private boolean debugEnable;
    private int debugPort;
    private String name;
    private String registry;
    private String tag;
    private String username;
    private String password;
    private String balxFileName;
    private String balxFilePath;
    private boolean push;
    private boolean isService;
    private boolean imageBuild;
    private String baseImage;
    private List<Integer> ports;

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

    public String getBalxFilePath() {
        return balxFilePath;
    }

    public void setBalxFilePath(String balxFilePath) {
        this.balxFilePath = balxFilePath;
    }

    public boolean isImageBuild() {
        return imageBuild;
    }

    public void setImageBuild(boolean imageBuild) {
        this.imageBuild = imageBuild;
    }

    public String getBaseImage() {
        return baseImage;
    }

    public void setBaseImage(String baseImage) {
        this.baseImage = baseImage;
    }

    public boolean isDebugEnable() {
        return debugEnable;
    }

    public void setDebugEnable(boolean debugEnable) {
        this.debugEnable = debugEnable;
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
                "debugEnable=" + debugEnable +
                ", debugPort=" + debugPort +
                ", name='" + name + '\'' +
                ", registry='" + registry + '\'' +
                ", tag='" + tag + '\'' +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", balxFileName='" + balxFileName + '\'' +
                ", balxFilePath='" + balxFilePath + '\'' +
                ", push=" + push +
                ", isService=" + isService +
                ", imageBuild=" + imageBuild +
                ", baseImage='" + baseImage + '\'' +
                ", ports=" + ports +
                '}';
    }
}
