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

import me.snowdrop.istio.api.networking.v1alpha3.Gateway;
import org.ballerinax.kubernetes.KubernetesConstants;
import org.ballerinax.kubernetes.exceptions.KubernetesPluginException;
import org.ballerinax.kubernetes.handlers.istio.IstioGatewayHandler;
import org.ballerinax.kubernetes.models.istio.IstioGatewayModel;
import org.ballerinax.kubernetes.models.istio.IstioPortModel;
import org.ballerinax.kubernetes.models.istio.IstioServerModel;
import org.ballerinax.kubernetes.utils.Utils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.ballerinax.kubernetes.KubernetesConstants.ISTIO_GATEWAY_FILE_POSTFIX;
import static org.ballerinax.kubernetes.KubernetesConstants.YAML;

/**
 * Unit test cases for istio gateway models.
 *
 * @since 0.985.0
 */
public class IstioGatewayGeneratorTests extends HandlerTestSuite {
    @Test
    public void testSimpleGateway() {
        IstioGatewayModel istioGatewayModel = new IstioGatewayModel();
        istioGatewayModel.setName("my-gateway");
        
        Map<String, String> selectors = new LinkedHashMap<>();
        selectors.put(KubernetesConstants.KUBERNETES_SELECTOR_KEY, "my-gatweway-controller");
        istioGatewayModel.setSelector(selectors);
        
        List<IstioServerModel> serverModels = new LinkedList<>();
        
        // First server
        IstioServerModel serverModel = new IstioServerModel();
        IstioPortModel portModel = new IstioPortModel();
        portModel.setNumber(80);
        portModel.setProtocol("HTTP");
        portModel.setName("http");
        serverModel.setPort(portModel);
        List<String> hostModels = new LinkedList<>();
        hostModels.add("uk.bookinfo.com");
        hostModels.add("eu.bookinfo.com");
        serverModel.setHosts(hostModels);
        IstioServerModel.TLSOptions tlsOptions = new IstioServerModel.TLSOptions();
        tlsOptions.setHttpsRedirect(true);
        serverModel.setTls(tlsOptions);
        serverModels.add(serverModel);
        
        istioGatewayModel.setServers(serverModels);
        
        dataHolder.addIstioGatewayModel("sample-svc", istioGatewayModel);
        try {
            new IstioGatewayHandler().createArtifacts();
            File gwYaml = dataHolder.getK8sArtifactOutputPath()
                    .resolve("hello" + ISTIO_GATEWAY_FILE_POSTFIX + YAML).toFile();
            Gateway gateway = Utils.loadYaml(gwYaml);
            
            // metadata
            Assert.assertNotNull(gateway.getMetadata());
            Assert.assertEquals(gateway.getMetadata().getName(), "my-gateway");
    
            Assert.assertNotNull(gateway.getSpec());
            Assert.assertEquals(gateway.getSpec().getSelector().get(KubernetesConstants.KUBERNETES_SELECTOR_KEY),
                    "my-gatweway-controller");
    
            Assert.assertEquals(gateway.getSpec().getServers().size(), 1);
            Assert.assertEquals(gateway.getSpec().getServers().get(0).getPort().getNumber().intValue(), 80);
            Assert.assertEquals(gateway.getSpec().getServers().get(0).getPort().getProtocol(), "HTTP");
            Assert.assertEquals(gateway.getSpec().getServers().get(0).getPort().getName(), "http");
    
            Assert.assertEquals(gateway.getSpec().getServers().get(0).getHosts().size(), 2);
            Assert.assertEquals(gateway.getSpec().getServers().get(0).getHosts().get(0), "uk.bookinfo.com");
            Assert.assertEquals(gateway.getSpec().getServers().get(0).getHosts().get(1), "eu.bookinfo.com");
    
            Assert.assertNotNull(gateway.getSpec().getServers().get(0).getTls());
            Assert.assertTrue(gateway.getSpec().getServers().get(0).getTls().getHttpsRedirect());
    
            gwYaml.deleteOnExit();
        } catch (IOException e) {
            Assert.fail("Unable to write to file: " + e.getMessage());
        } catch (KubernetesPluginException e) {
            Assert.fail("Unable to generate yaml: " + e.getMessage());
        }
    }
}
