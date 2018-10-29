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

package org.ballerinax.kubernetes.handlers;

import org.apache.commons.io.FileUtils;
import org.ballerinax.kubernetes.exceptions.KubernetesPluginException;
import org.ballerinax.kubernetes.handlers.istio.IstioGatewayHandler;
import org.ballerinax.kubernetes.models.KubernetesContext;
import org.ballerinax.kubernetes.models.istio.IstioGatewayModel;
import org.ballerinax.kubernetes.models.istio.IstioPortModel;
import org.ballerinax.kubernetes.models.istio.IstioServerModel;
import org.junit.Assert;
import org.junit.Test;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.ballerinax.kubernetes.KubernetesConstants.ISTIO_GATEWAY_FILE_POSTFIX;
import static org.ballerinax.kubernetes.KubernetesConstants.YAML;

/**
 *
 */
public class IstioGatewayGeneratorTests {
    @Test
    public void testResourceQuota() {
        IstioGatewayModel istioGatewayModel = new IstioGatewayModel();
        istioGatewayModel.setName("my-gateway");
        
        Map<String, String> selectors = new LinkedHashMap<>();
        selectors.put("app", "my-gatweway-controller");
        istioGatewayModel.setSelector(selectors);
        
        List<IstioServerModel> servers = new LinkedList<>();
        
        // First server
        IstioServerModel serverModel = new IstioServerModel();
        IstioPortModel portModel = new IstioPortModel();
        portModel.setNumber(80);
        portModel.setProtocol("HTTP");
        portModel.setName("http");
        serverModel.setPort(portModel);
        List<String> hosts = new LinkedList<>();
        hosts.add("uk.bookinfo.com");
        hosts.add("eu.bookinfo.com");
        serverModel.setHosts(hosts);
        IstioServerModel.TLSOptions tlsOptions = new IstioServerModel.TLSOptions();
        tlsOptions.setHttpsRedirect(true);
        serverModel.setTls(tlsOptions);
        servers.add(serverModel);
        
        istioGatewayModel.setServers(servers);
        
        KubernetesContext.getInstance().getDataHolder().addIstioGatewayModel(istioGatewayModel);
        try {
            new IstioGatewayHandler().createArtifacts();
            File yamlFile = new File("target" + File.separator + "kubernetes" + File.separator +
                                     "hello" + ISTIO_GATEWAY_FILE_POSTFIX + YAML);
            Yaml yaml = new Yaml();
            istioGatewayModel = yaml.loadAs(FileUtils.readFileToString(yamlFile), IstioGatewayModel.class);
    
            // metadata
            Assert.assertEquals("my-gateway", istioGatewayModel.getName());
            
            yamlFile.deleteOnExit();
        } catch (IOException e) {
            Assert.fail("Unable to write to file: " + e.getMessage());
        } catch (KubernetesPluginException e) {
            Assert.fail("Unable to generate yaml from service: " + e.getMessage());
        }
    }
    
    // Test with no selector // minimum
    
    // TODO: Test if minimum 1 server is needed
    
    // 1 or more hosts is needed
    
    // Port should be more than 0
    
    // TLS SIMPLE or MUTUAL serverCertificate
    // TLS SIMPLE or MUTUAL privateKey
    // TLS MUTUAL caCertificates
}
