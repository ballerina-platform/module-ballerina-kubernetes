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

import java.util.Map;

/**
 * Kubernetes ingress annotations model class.
 */
public class IngressModel {
    private String name;
    private Map<String, String> labels;
    private String hostname;
    private String path;
    private String ingressClass;
    private String serviceName;
    private int servicePort;
    private String targetPath;
    private boolean enableTLS;

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

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getIngressClass() {
        return ingressClass;
    }

    public void setIngressClass(String ingressClass) {
        this.ingressClass = ingressClass;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public int getServicePort() {
        return servicePort;
    }

    public void setServicePort(int servicePort) {
        this.servicePort = servicePort;
    }

    public String getTargetPath() {
        return targetPath;
    }

    public void setTargetPath(String targetPath) {
        this.targetPath = targetPath;
    }

    public boolean isEnableTLS() {
        return enableTLS;
    }

    public void setEnableTLS(boolean enableTLS) {
        this.enableTLS = enableTLS;
    }

    @Override
    public String toString() {
        return "IngressModel{" +
                "name='" + name + '\'' +
                ", labels=" + labels +
                ", hostname='" + hostname + '\'' +
                ", path='" + path + '\'' +
                ", ingressClass='" + ingressClass + '\'' +
                ", serviceName='" + serviceName + '\'' +
                ", servicePort=" + servicePort +
                ", targetPath='" + targetPath + '\'' +
                ", enableTLS=" + enableTLS +
                '}';
    }
}
