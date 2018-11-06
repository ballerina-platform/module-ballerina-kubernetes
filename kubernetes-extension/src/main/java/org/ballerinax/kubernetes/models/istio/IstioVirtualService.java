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

package org.ballerinax.kubernetes.models.istio;

import org.ballerinax.kubernetes.models.KubernetesModel;

import java.util.List;
import java.util.Map;

/**
 * Istio virtual service annotation model class.
 */
public class IstioVirtualService extends KubernetesModel {
    private String namespace;
    private Map<String, String> labels;
    private Map<String, String> annotations;
    private List<String> hosts;
    private List<String> gateways;
    private List<IstioHttpRoute> http;
    private List<Object> tls;
    private List<Object> tcp;
    
    public String getNamespace() {
        return namespace;
    }
    
    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }
    
    public Map<String, String> getLabels() {
        return labels;
    }
    
    public void setLabels(Map<String, String> labels) {
        this.labels = labels;
    }
    
    public Map<String, String> getAnnotations() {
        return annotations;
    }
    
    public void setAnnotations(Map<String, String> annotations) {
        this.annotations = annotations;
    }
    
    public List<String> getHosts() {
        return hosts;
    }
    
    public void setHosts(List<String> hosts) {
        this.hosts = hosts;
    }
    
    public List<String> getGateways() {
        return gateways;
    }
    
    public void setGateways(List<String> gateways) {
        this.gateways = gateways;
    }
    
    public List<IstioHttpRoute> getHttp() {
        return http;
    }
    
    public void setHttp(List<IstioHttpRoute> http) {
        this.http = http;
    }
    
    public List<Object> getTls() {
        return tls;
    }
    
    public void setTls(List<Object> tls) {
        this.tls = tls;
    }
    
    public List<Object> getTcp() {
        return tcp;
    }
    
    public void setTcp(List<Object> tcp) {
        this.tcp = tcp;
    }
}
