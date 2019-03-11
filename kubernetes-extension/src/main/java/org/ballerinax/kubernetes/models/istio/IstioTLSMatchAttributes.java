/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Istio TLS match attribute model.
 */
public class IstioTLSMatchAttributes {
    private Set<String> sniHosts = new LinkedHashSet<>();
    private Set<String> destinationSubnets;
    private int port;
    private Map<String, String> sourceLabels;
    private Set<String> gateways;
    
    public Set<String> getSniHosts() {
        return sniHosts;
    }
    
    public void setSniHosts(Set<String> sniHosts) {
        this.sniHosts = sniHosts;
    }
    
    public Set<String> getDestinationSubnets() {
        return destinationSubnets;
    }
    
    public void setDestinationSubnets(Set<String> destinationSubnets) {
        this.destinationSubnets = destinationSubnets;
    }
    
    public int getPort() {
        return port;
    }
    
    public void setPort(int port) {
        this.port = port;
    }
    
    public Map<String, String> getSourceLabels() {
        return sourceLabels;
    }
    
    public void setSourceLabels(Map<String, String> sourceLabels) {
        this.sourceLabels = sourceLabels;
    }
    
    public Set<String> getGateways() {
        return gateways;
    }
    
    public void setGateways(Set<String> gateways) {
        this.gateways = gateways;
    }
}
