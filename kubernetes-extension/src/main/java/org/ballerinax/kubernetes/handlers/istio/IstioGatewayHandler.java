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

import org.ballerinax.kubernetes.exceptions.KubernetesPluginException;
import org.ballerinax.kubernetes.handlers.AbstractArtifactHandler;
import org.ballerinax.kubernetes.models.istio.IstioGatewayModel;
import org.ballerinax.kubernetes.models.istio.IstioServerModel;
import org.ballerinax.kubernetes.utils.KubernetesUtils;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.representer.Represent;
import org.yaml.snakeyaml.representer.Representer;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import static org.ballerinax.kubernetes.KubernetesConstants.ISTIO_GATEWAY_FILE_POSTFIX;
import static org.ballerinax.kubernetes.KubernetesConstants.YAML;

/**
 *
 */
public class IstioGatewayHandler extends AbstractArtifactHandler {
    
    @Override
    public void createArtifacts() throws KubernetesPluginException {
        Set<IstioGatewayModel> gatewayModels = dataHolder.getIstioGatewayModels();
        int size = gatewayModels.size();
        if (size > 0) {
            OUT.println();
        }
        
        int count = 0;
        for (IstioGatewayModel gatewayModel : gatewayModels) {
            count++;
            
            // Validate number of selectors.
            if (gatewayModel.getSelector().size() == 0) {
                throw new KubernetesPluginException("'" + gatewayModel.getName() + "' Istio Gateway needs one or more" +
                                                    " selectors.");
            }
            
            // Validate number of servers.
            if (gatewayModel.getServers().size() == 0) {
                throw new KubernetesPluginException("'" + gatewayModel.getName() + "' Istio Gateway needs one or more" +
                                                    " servers.");
            }
            
            // Validate server.hosts
            for (IstioServerModel serverModel : gatewayModel.getServers()) {
                if (serverModel.getHosts().size() == 0) {
                    throw new KubernetesPluginException("'" + gatewayModel.getName() + "' Istio Gateway needs one or" +
                                                        " more server hosts.");
                }
    
                if (serverModel.getPort().getNumber() <= 0) {
                    throw new KubernetesPluginException("'" + gatewayModel.getName() + "' Istio Gateway ports cannot" +
                                                        " be less than 0.");
                }
                
                if (serverModel.getTls() != null && "SIMPLE".equals(serverModel.getTls().getMode()) &&
                    (serverModel.getTls().getServerCertificate() == null ||
                     serverModel.getTls().getPrivateKey() == null)) {
                    throw new KubernetesPluginException("'" + gatewayModel.getName() + "' Istio Gateway TLS mode is" +
                                                        " SIMPLE, hence serverCertificate and privateKey fields are" +
                                                        " required.");
                }
    
                if (serverModel.getTls() != null && "MUTUAL".equals(serverModel.getTls().getMode()) &&
                    (serverModel.getTls().getServerCertificate() == null ||
                     serverModel.getTls().getPrivateKey() == null ||
                     serverModel.getTls().getCaCertificates() == null)) {
                    throw new KubernetesPluginException("'" + gatewayModel.getName() + "' Istio Gateway TLS mode is" +
                                                        " MUTUAL, hence serverCertificate, privateKey and" +
                                                        " caCertificates fields are required.");
                }
            }
            
            generate(gatewayModel);
            OUT.print("\t@kubernetes:IstioGatewayModel \t\t - complete " + count + "/" + gatewayModels.size() + "\r");
        }
    }
    
    private void generate(IstioGatewayModel gatewayModel) throws KubernetesPluginException {
        try {
            Map<String, Object> gatewayYamlModel = new LinkedHashMap<>();
            gatewayYamlModel.put("apiVersion", "networking.istio.io/v1alpha3");
            gatewayYamlModel.put("kind", "Gateway");
            String gatewayYamlString = KubernetesUtils.getAsYaml(gatewayYamlModel);
            KubernetesUtils.writeToFile(gatewayYamlString, ISTIO_GATEWAY_FILE_POSTFIX + YAML);
        } catch (IOException e) {
            String errorMessage = "Error while generating yaml file for istio gateway: " +
                                  gatewayModel.getName();
            throw new KubernetesPluginException(errorMessage, e);
        }
    }
    
    private class IstioGateway extends Representer {
        private class RepresentIstioGatewayModel implements Represent {
    
            @Override
            public Node representData(Object o) {
                IstioGatewayModel gatewayModel = (IstioGatewayModel) o;
                represe
            }
        }
    }
}
