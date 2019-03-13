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

import io.fabric8.kubernetes.client.internal.SerializationUtils;
import me.snowdrop.istio.api.networking.v1alpha3.Gateway;
import me.snowdrop.istio.api.networking.v1alpha3.GatewayBuilder;
import me.snowdrop.istio.api.networking.v1alpha3.Server;
import me.snowdrop.istio.api.networking.v1alpha3.ServerBuilder;
import me.snowdrop.istio.api.networking.v1alpha3.TLSOptions;
import me.snowdrop.istio.api.networking.v1alpha3.TLSOptionsBuilder;
import me.snowdrop.istio.api.networking.v1alpha3.TLSOptionsMode;
import org.ballerinax.kubernetes.KubernetesConstants;
import org.ballerinax.kubernetes.exceptions.KubernetesPluginException;
import org.ballerinax.kubernetes.handlers.AbstractArtifactHandler;
import org.ballerinax.kubernetes.models.istio.IstioGatewayModel;
import org.ballerinax.kubernetes.models.istio.IstioServerModel;
import org.ballerinax.kubernetes.utils.KubernetesUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
                gatewayModel.setSelector(selectors);
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
            Gateway gateway = new GatewayBuilder()
                    .withNewMetadata()
                    .withName(gatewayModel.getName())
                    .withNamespace(dataHolder.getNamespace())
                    .withLabels(gatewayModel.getLabels())
                    .withAnnotations(gatewayModel.getAnnotations())
                    .endMetadata()
                    .withNewSpec()
                    .withSelector(gatewayModel.getSelector())
                    .withServers(populateServers(gatewayModel.getServers()))
                    .endSpec()
                    .build();
            
            String gatewayContent = SerializationUtils.dumpWithoutRuntimeStateAsYaml(gateway);
            KubernetesUtils.writeToFile(gatewayContent, ISTIO_GATEWAY_FILE_POSTFIX + YAML);
        } catch (IOException e) {
            String errorMessage = "error while generating yaml file for istio gateway: " + gatewayModel.getName();
            throw new KubernetesPluginException(errorMessage, e);
        }
    }
    
    private List<Server> populateServers(List<IstioServerModel> serverModels) {
        return serverModels.stream()
                .map(serverModel -> new ServerBuilder()
                        .withHosts(new ArrayList<>(serverModel.getHosts()))
                        .withNewPort()
                        .withNumber(serverModel.getPort().getNumber())
                        .withProtocol(serverModel.getPort().getProtocol())
                        .withName(serverModel.getPort().getName())
                        .endPort()
                        .withTls(populateTLS(serverModel.getTls()))
                        .build())
                .collect(Collectors.toList());
    }
    
    private TLSOptions populateTLS(IstioServerModel.TLSOptions tls) {
        if (null == tls) {
            return null;
        }
        
        return new TLSOptionsBuilder()
            .withHttpsRedirect(tls.isHttpsRedirect())
            .withMode(TLSOptionsMode.valueOf(tls.getMode()))
            .withServerCertificate(tls.getServerCertificate())
            .withPrivateKey(tls.getPrivateKey())
            .withCaCertificates(tls.getCaCertificates())
            .withSubjectAltNames(new ArrayList<>(tls.getSubjectAltNames()))
            .build();
    }
}
