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

package org.ballerinax.kubernetes.handlers.istio;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.ballerinax.kubernetes.KubernetesConstants;
import org.ballerinax.kubernetes.exceptions.KubernetesPluginException;
import org.ballerinax.kubernetes.handlers.AbstractArtifactHandler;
import org.ballerinax.kubernetes.models.istio.IstioGatewayModel;
import org.ballerinax.kubernetes.models.istio.IstioServerModel;
import org.ballerinax.kubernetes.utils.KubernetesUtils;
import org.yaml.snakeyaml.DumperOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.ballerinax.kubernetes.KubernetesConstants.ISTIO_GATEWAY_FILE_POSTFIX;
import static org.ballerinax.kubernetes.KubernetesConstants.YAML;

/**
 * Generates istio gateway artifacts.
 *
 * @since 0.985.0
 */
public class IstioGatewayHandler extends AbstractArtifactHandler {
    
    /**
     * {@inheritDoc}
     * Performs validations and creates the artifacts.
     */
    @Override
    public void createArtifacts() throws KubernetesPluginException {
        Map<String, IstioGatewayModel> gatewayModels = dataHolder.getIstioGatewayModels();
        int size = gatewayModels.size();
        if (size > 0) {
            OUT.println();
        }
        
        int count = 0;
        for (IstioGatewayModel gatewayModel : gatewayModels.values()) {
            count++;
            
            // Validate number of selectors.
            if (null == gatewayModel.getSelector() || gatewayModel.getSelector().size() == 0) {
                Map<String, String> selectors = new LinkedHashMap<>();
                selectors.put(KubernetesConstants.ISTIO_GATEWAY_SELECTOR, "ingressgateway");
                gatewayModel.setSelector(selectors);;
            }
            
            // Validate number of servers.
            if (null == gatewayModel.getServers() || gatewayModel.getServers().size() == 0) {
                throw new KubernetesPluginException("'" + gatewayModel.getName() + "' istio gateway needs one or more" +
                                                    " servers.");
            }
            
            // Validate server.hosts
            for (IstioServerModel serverModel : gatewayModel.getServers()) {
                if (null == serverModel.getHosts() || serverModel.getHosts().size() == 0) {
                    throw new KubernetesPluginException("'" + gatewayModel.getName() + "' istio gateway needs one or" +
                                                        " more server hosts.");
                }
    
                if (null == serverModel.getPort() || serverModel.getPort().getNumber() < 0) {
                    throw new KubernetesPluginException("'" + gatewayModel.getName() + "' istio gateway ports cannot" +
                                                        " be less than 0. found: " + serverModel.getPort().getNumber());
                }
                
                if (serverModel.getTls() != null && ("SIMPLE".equals(serverModel.getTls().getMode()) &&
                    (serverModel.getTls().getServerCertificate() == null ||
                     serverModel.getTls().getPrivateKey() == null))) {
                    throw new KubernetesPluginException("'" + gatewayModel.getName() + "' istio gateway TLS mode is" +
                                                        " SIMPLE, hence serverCertificate and privateKey fields are" +
                                                        " required.");
                }
    
                if (serverModel.getTls() != null && ("MUTUAL".equals(serverModel.getTls().getMode()) &&
                    (serverModel.getTls().getServerCertificate() == null ||
                     serverModel.getTls().getPrivateKey() == null ||
                     serverModel.getTls().getCaCertificates() == null))) {
                    throw new KubernetesPluginException("'" + gatewayModel.getName() + "' istio gateway TLS mode is" +
                                                        " MUTUAL, hence serverCertificate, privateKey and" +
                                                        " caCertificates fields are required.");
                }
            }
            
            generate(gatewayModel);
            OUT.print("\t@kubernetes:IstioGatewayModel \t\t - complete " + count + "/" + size + "\r");
        }
    }
    
    /**
     * Generate the artifacts.
     * @param gatewayModel The gateway model.
     * @throws KubernetesPluginException Error if occurred when writing to file.
     */
    private void generate(IstioGatewayModel gatewayModel) throws KubernetesPluginException {
        try {
            Map<String, Object> gatewayYamlModel = new LinkedHashMap<>();
            gatewayYamlModel.put("apiVersion", "networking.istio.io/v1alpha3");
            gatewayYamlModel.put("kind", "Gateway");
            
            // metadata
            Map<String, Object> metadata = new LinkedHashMap<>();
            metadata.put("name", gatewayModel.getName());
            if (null != dataHolder.getNamespace()) {
                metadata.put("namespace", dataHolder.getNamespace());
            }
            if (null != gatewayModel.getLabels() && gatewayModel.getLabels().size() > 0) {
                metadata.put("labels", gatewayModel.getLabels());
            }
            if (null != gatewayModel.getAnnotations() && gatewayModel.getAnnotations().size() > 0) {
                metadata.put("annotations", gatewayModel.getAnnotations());
            }
            gatewayYamlModel.put("metadata", metadata);
            
            // spec
            Map<String, Object> spec = new LinkedHashMap<>();
            spec.put("selector", gatewayModel.getSelector());
            
            // servers
            List<Map<String, Object>> servers = new LinkedList<>();
            if (null != gatewayModel.getServers()) {
                for (IstioServerModel serverModel : gatewayModel.getServers()) {
                    Map<String, Object> server = new LinkedHashMap<>();
                    
                    // hosts
                    if (null != serverModel.getHosts() && serverModel.getHosts().size() > 0) {
                        server.put("hosts", new ArrayList<>(serverModel.getHosts()));
                    }
                    
                    // port
                    Map<String, Object> port = new LinkedHashMap<>();
                    port.put("number", serverModel.getPort().getNumber());
                    port.put("protocol", serverModel.getPort().getProtocol());
                    port.put("name", serverModel.getPort().getName());
                    server.put("port", port);
                    
                    // tls
                    if (null != serverModel.getTls()) {
                        Map<String, Object> tls = new LinkedHashMap<>();
                        tls.put("httpsRedirect", serverModel.getTls().isHttpsRedirect());
                        if (null != serverModel.getTls().getMode()) {
                            tls.put("mode", serverModel.getTls().getMode());
                        }
                        if (null != serverModel.getTls().getServerCertificate()) {
                            tls.put("serverCertificate", serverModel.getTls().getServerCertificate());
                        }
                        if (null != serverModel.getTls().getPrivateKey()) {
                            tls.put("privateKey", serverModel.getTls().getPrivateKey());
                        }
                        if (null != serverModel.getTls().getCaCertificates()) {
                            tls.put("caCertificates", serverModel.getTls().getCaCertificates());
                        }
                        if (null != serverModel.getTls().getSubjectAltNames() &&
                            serverModel.getTls().getSubjectAltNames().size() > 0) {
                            tls.put("subjectAltNames", serverModel.getTls().getSubjectAltNames());
                        }
                        server.put("tls", tls);
                    }
        
                    servers.add(server);
                }
            }
    
            if (servers.size() > 0) {
                spec.put("servers", servers);
            }
            gatewayYamlModel.put("spec", spec);
    
            DumperOptions options = new DumperOptions();
            options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
    
            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            String gatewayYamlString = mapper.writeValueAsString(gatewayYamlModel);
            
            KubernetesUtils.writeToFile(gatewayYamlString, ISTIO_GATEWAY_FILE_POSTFIX + YAML);
        } catch (IOException e) {
            String errorMessage = "Error while generating yaml file for istio gateway: " + gatewayModel.getName();
            throw new KubernetesPluginException(errorMessage, e);
        }
    }
}
