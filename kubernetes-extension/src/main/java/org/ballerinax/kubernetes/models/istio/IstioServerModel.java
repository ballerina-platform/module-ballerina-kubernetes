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

import java.util.Set;

/**
 * Istio gateway server annotation model class.
 */
public class IstioServerModel {
    private IstioPortModel port;
    private Set<String> hosts;
    private TLSOptions tls;
    
    public IstioPortModel getPort() {
        return port;
    }
    
    public void setPort(IstioPortModel port) {
        this.port = port;
    }
    
    public TLSOptions getTls() {
        return tls;
    }
    
    public void setTls(TLSOptions tls) {
        this.tls = tls;
    }
    
    public Set<String> getHosts() {
        return hosts;
    }
    
    public void setHosts(Set<String> hosts) {
        this.hosts = hosts;
    }
    
    /**
     * Istio gateway server TLS option annotation model class.
     */
    public static class TLSOptions {
        private boolean httpsRedirect = false;
        private String mode;
        private String serverCertificate;
        private String privateKey;
        private String caCertificates;
        private Set<String> subjectAltNames;
    
        public boolean isHttpsRedirect() {
            return httpsRedirect;
        }
    
        public void setHttpsRedirect(boolean httpsRedirect) {
            this.httpsRedirect = httpsRedirect;
        }
    
        public String getMode() {
            return mode;
        }
    
        public void setMode(String mode) {
            this.mode = mode;
        }
    
        public String getServerCertificate() {
            return serverCertificate;
        }
    
        public void setServerCertificate(String serverCertificate) {
            this.serverCertificate = serverCertificate;
        }
    
        public String getPrivateKey() {
            return privateKey;
        }
    
        public void setPrivateKey(String privateKey) {
            this.privateKey = privateKey;
        }
    
        public String getCaCertificates() {
            return caCertificates;
        }
    
        public void setCaCertificates(String caCertificates) {
            this.caCertificates = caCertificates;
        }
    
        public Set<String> getSubjectAltNames() {
            return subjectAltNames;
        }
    
        public void setSubjectAltNames(Set<String> subjectAltNames) {
            this.subjectAltNames = subjectAltNames;
        }
    }
}
