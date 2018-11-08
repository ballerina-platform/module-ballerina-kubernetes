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
 * Unit test cases for istio gateway models.
 *
 * @since 0.983.0
 */
public class IstioGatewayGeneratorTests {
    @Test
    public void testSimpleGateway() {
        IstioGatewayModel istioGatewayModel = new IstioGatewayModel();
        istioGatewayModel.setName("my-gateway");
        istioGatewayModel.setNamespace("ballerina");
        
        Map<String, String> selectors = new LinkedHashMap<>();
        selectors.put("app", "my-gatweway-controller");
        istioGatewayModel.setSelector(selectors);
        
        List<IstioServerModel> serverModels = new LinkedList<>();
        
        // First server
        IstioServerModel serverModel = new IstioServerModel();
        IstioPortModel portModel = new IstioPortModel();
        portModel.setNumber(80);
        portModel.setProtocol("HTTP");
        portModel.setName("http");
        serverModel.setPort(portModel);
        Set<String> hostModels = new LinkedHashSet<>();
        hostModels.add("uk.bookinfo.com");
        hostModels.add("eu.bookinfo.com");
        serverModel.setHosts(hostModels);
        IstioServerModel.TLSOptions tlsOptions = new IstioServerModel.TLSOptions();
        tlsOptions.setHttpsRedirect(true);
        serverModel.setTls(tlsOptions);
        serverModels.add(serverModel);
        
        istioGatewayModel.setServers(serverModels);
        
        KubernetesContext.getInstance().getDataHolder().addIstioGatewayModel("sample-svc", istioGatewayModel);
        try {
            new IstioGatewayHandler().createArtifacts();
            File yamlFile = new File("target" + File.separator + "kubernetes" + File.separator +
                                     "hello" + ISTIO_GATEWAY_FILE_POSTFIX + YAML);
            Yaml yaml = new Yaml();
            Map gateway = (LinkedHashMap) yaml.load(FileUtils.readFileToString(yamlFile));
    
            // metadata
            Map metadata = (Map) gateway.get("metadata");
            Assert.assertEquals("my-gateway", metadata.get("name"));
            Assert.assertEquals("ballerina", metadata.get("namespace"));
    
            Map spec = (Map) gateway.get("spec");
            
            Map selector = (Map) spec.get("selector");
            Assert.assertEquals("my-gatweway-controller", selector.get("app"));
    
            List servers = (List) spec.get("servers");
            Map server = (Map) servers.get(0);
            
            Map port = (Map) server.get("port");
            Assert.assertEquals(80, port.get("number"));
            Assert.assertEquals("HTTP", port.get("protocol"));
            Assert.assertEquals("http", port.get("name"));
    
            List<String> hosts = (List<String>) server.get("hosts");
            Assert.assertTrue(hosts.contains("uk.bookinfo.com"));
            Assert.assertTrue(hosts.contains("eu.bookinfo.com"));
    
            Map tls = (Map) server.get("tls");
            Assert.assertEquals(true, tls.get("httpsRedirect"));
    
            yamlFile.deleteOnExit();
        } catch (IOException e) {
            Assert.fail("Unable to write to file: " + e.getMessage());
        } catch (KubernetesPluginException e) {
            Assert.fail("Unable to generate yaml: " + e.getMessage());
        }
    }
}
