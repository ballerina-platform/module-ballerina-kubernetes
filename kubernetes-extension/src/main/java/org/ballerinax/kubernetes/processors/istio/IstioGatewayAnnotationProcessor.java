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

package org.ballerinax.kubernetes.processors.istio;

import org.ballerinalang.model.tree.AnnotationAttachmentNode;
import org.ballerinalang.model.tree.EndpointNode;
import org.ballerinalang.model.tree.ServiceNode;
import org.ballerinalang.model.tree.expressions.ExpressionNode;
import org.ballerinalang.model.tree.expressions.RecordLiteralNode;
import org.ballerinax.kubernetes.exceptions.KubernetesPluginException;
import org.ballerinax.kubernetes.models.KubernetesContext;
import org.ballerinax.kubernetes.models.istio.IstioGatewayModel;
import org.ballerinax.kubernetes.models.istio.IstioPortModel;
import org.ballerinax.kubernetes.models.istio.IstioServerModel;
import org.ballerinax.kubernetes.processors.AbstractAnnotationProcessor;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.BLangEndpoint;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangArrayLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static org.ballerinax.kubernetes.KubernetesConstants.ISTIO_GATEWAY_POSTFIX;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.getArray;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.getMap;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.getValidName;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.isBlank;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.resolveValue;

/**
 * Istio gateway annotation processor.
 */
public class IstioGatewayAnnotationProcessor extends AbstractAnnotationProcessor {
    @Override
    public void processAnnotation(ServiceNode serviceNode, AnnotationAttachmentNode attachmentNode)
            throws KubernetesPluginException {
        List<BLangRecordLiteral.BLangRecordKeyValue> keyValues =
                ((BLangRecordLiteral) ((BLangAnnotationAttachment) attachmentNode).expr).getKeyValuePairs();
    
        IstioGatewayModel gwModel = this.processIstioGatewayAnnotation(keyValues);
        if (isBlank(gwModel.getName())) {
            gwModel.setName(getValidName(serviceNode.getName().getValue()) + ISTIO_GATEWAY_POSTFIX);
        }
        RecordLiteralNode anonymousEndpoint = serviceNode.getAnonymousEndpointBind();
        List<BLangRecordLiteral.BLangRecordKeyValue> endpointConfig =
                ((BLangRecordLiteral) anonymousEndpoint).getKeyValuePairs();
        
        if (null == gwModel.getServers() || gwModel.getServers().size() == 0) {
            List<IstioServerModel> serversModel = new LinkedList<>();
            IstioServerModel serverModel = new IstioServerModel();
            
            IstioPortModel portModel = new IstioPortModel();
            portModel.setNumber(extractPort(endpointConfig));
            portModel.setProtocol("HTTP");
            serverModel.setPort(portModel);
    
            if (null == serverModel.getHosts() || serverModel.getHosts().size() == 0) {
                Set<String> hosts = new LinkedHashSet<>();
                hosts.add("*");
                serverModel.setHosts(hosts);
            }
            
            serversModel.add(serverModel);
            gwModel.setServers(serversModel);
        }
        
        KubernetesContext.getInstance().getDataHolder().addIstioGatewayModel(gwModel);
    }
    
    @Override
    public void processAnnotation(EndpointNode endpointNode, AnnotationAttachmentNode attachmentNode)
            throws KubernetesPluginException {
        List<BLangRecordLiteral.BLangRecordKeyValue> keyValues =
                ((BLangRecordLiteral) ((BLangAnnotationAttachment) attachmentNode).expr).getKeyValuePairs();
    
        IstioGatewayModel gwModel = this.processIstioGatewayAnnotation(keyValues);
        if (isBlank(gwModel.getName())) {
            gwModel.setName(getValidName(endpointNode.getName().getValue()) + ISTIO_GATEWAY_POSTFIX);
        }
        
        List<BLangRecordLiteral.BLangRecordKeyValue> endpointConfig =
                ((BLangRecordLiteral) ((BLangEndpoint) endpointNode).configurationExpr).getKeyValuePairs();
        
        if (null == gwModel.getServers() || gwModel.getServers().size() == 0) {
            List<IstioServerModel> serversModel = new LinkedList<>();
            IstioServerModel serverModel = new IstioServerModel();
        
            IstioPortModel portModel = new IstioPortModel();
            portModel.setNumber(extractPort(endpointConfig));
            portModel.setProtocol("HTTP");
            portModel.setName("http");
            serverModel.setPort(portModel);
        
            if (null == serverModel.getHosts() || serverModel.getHosts().size() == 0) {
                Set<String> hosts = new LinkedHashSet<>();
                hosts.add("*");
                serverModel.setHosts(hosts);
            }
        
            serversModel.add(serverModel);
            gwModel.setServers(serversModel);
        }
        
        KubernetesContext.getInstance().getDataHolder().addIstioGatewayModel(gwModel);
    }
    
    /**
     * Process @Kubernetes:IstioGateway annotation.
     * @param gatewayFields Fields of the gateway annotation.
     * @throws KubernetesPluginException Unable to process annotations.
     */
    private IstioGatewayModel processIstioGatewayAnnotation(List<BLangRecordLiteral.BLangRecordKeyValue> gatewayFields)
            throws KubernetesPluginException {
        IstioGatewayModel gatewayModel = new IstioGatewayModel();
        for (BLangRecordLiteral.BLangRecordKeyValue gatewayField : gatewayFields) {
            switch (IstioGatewayConfig.valueOf(gatewayField.getKey().toString())) {
                case name:
                    gatewayModel.setName(resolveValue(gatewayField.getValue().toString()));
                    break;
                case namespace:
                    gatewayModel.setNamespace(resolveValue(gatewayField.getValue().toString()));
                    break;
                case labels:
                    BLangRecordLiteral labelsField = (BLangRecordLiteral) gatewayField.getValue();
                    gatewayModel.setLabels(getMap(labelsField.getKeyValuePairs()));
                    break;
                case annotations:
                    BLangRecordLiteral annotationsField = (BLangRecordLiteral) gatewayField.getValue();
                    gatewayModel.setAnnotations(getMap(annotationsField.getKeyValuePairs()));
                    break;
                case selector:
                    BLangRecordLiteral selectorsField = (BLangRecordLiteral) gatewayField.getValue();
                    gatewayModel.setSelector(getMap(selectorsField.getKeyValuePairs()));
                    break;
                case servers:
                    processIstioGatewayServerAnnotation(gatewayModel, (BLangArrayLiteral) gatewayField.getValue());
                    break;
                default:
                    throw new KubernetesPluginException("Unknown field found for istio gateway.");
            }
        }
        
        return gatewayModel;
    }
    
    /**
     * Process server field of @Kubernetes:IstioGateway annotation.
     * @param gatewayModel The gateway model.
     * @param serversField List of servers of the gateway.
     * @throws KubernetesPluginException Unable to process annotation
     */
    private void processIstioGatewayServerAnnotation(IstioGatewayModel gatewayModel, BLangArrayLiteral serversField)
            throws KubernetesPluginException {
        List<IstioServerModel> servers = new LinkedList<>();
        for (ExpressionNode serverRecord : serversField.getExpressions()) {
            if (serverRecord instanceof BLangRecordLiteral) {
                BLangRecordLiteral serverFieldRecord = (BLangRecordLiteral) serverRecord;
                IstioServerModel server = new IstioServerModel();
                for (BLangRecordLiteral.BLangRecordKeyValue serverField : serverFieldRecord.getKeyValuePairs()) {
                    switch (IstioServerConfig.valueOf(serverField.getKey().toString())) {
                        case port:
                            BLangRecordLiteral portRecord = (BLangRecordLiteral) serverField.getValue();
                            processIstioGatewayPortAnnotation(server, portRecord.getKeyValuePairs());
                            break;
                        case hosts:
                            BLangArrayLiteral hosts = (BLangArrayLiteral) serverField.getValue();
                            server.setHosts(getArray(hosts));
                            break;
                        case tls:
                            BLangRecordLiteral tlsRecord = (BLangRecordLiteral) serverField.getValue();
                            processIstioGatewayTLSAnnotation(server, tlsRecord.getKeyValuePairs());
                            break;
                        default:
                            throw new KubernetesPluginException("Unknown field found for istio gateway server.");
                    }
                }
                servers.add(server);
            }
        }
        gatewayModel.setServers(servers);
    }
    
    /**
     * Process port fields of @Kubernetes:IstioGateway annotations's server field.
     * @param server The server model.
     * @param portFields The fields of the server's port.
     * @throws KubernetesPluginException Unable to process annotation
     */
    private void processIstioGatewayPortAnnotation(IstioServerModel server,
                                                   List<BLangRecordLiteral.BLangRecordKeyValue> portFields)
            throws KubernetesPluginException {
        IstioPortModel portModel = new IstioPortModel();
        for (BLangRecordLiteral.BLangRecordKeyValue portField : portFields) {
            switch (IstioPortConfig.valueOf(portField.getKey().toString())) {
                case number:
                    portModel.setNumber(Integer.parseInt(portField.getValue().toString()));
                    break;
                case protocol:
                    portModel.setProtocol(portField.getValue().toString());
                    break;
                case name:
                    portModel.setName(portField.getValue().toString());
                    break;
                default:
                    throw new KubernetesPluginException("Unknown field found for istio gateway server port.");
            }
        }
        server.setPort(portModel);
    }
    
    /**
     * Process tls option fields of @Kubernetes:IstioGateway annotations's server field.
     * @param server The server model.
     * @param tlsFields The fields of the server's tls options.
     * @throws KubernetesPluginException Unable to process annotation
     */
    private void processIstioGatewayTLSAnnotation(IstioServerModel server,
                                                  List<BLangRecordLiteral.BLangRecordKeyValue> tlsFields)
            throws KubernetesPluginException {
        IstioServerModel.TLSOptions tlsOptions = new IstioServerModel.TLSOptions();
        for (BLangRecordLiteral.BLangRecordKeyValue tlsField : tlsFields) {
            switch (TLSOptionConfig.valueOf(tlsField.getKey().toString())) {
                case httpsRedirect:
                    tlsOptions.setHttpsRedirect(Boolean.parseBoolean(tlsField.getValue().toString()));
                    break;
                case mode:
                    tlsOptions.setMode(tlsField.getValue().toString());
                    break;
                case serverCertificate:
                    tlsOptions.setServerCertificate(tlsField.getValue().toString());
                    break;
                case privateKey:
                    tlsOptions.setPrivateKey(tlsField.getValue().toString());
                    break;
                case caCertificates:
                    tlsOptions.setCaCertificates(tlsField.getValue().toString());
                    break;
                case subjectAltNames:
                    BLangArrayLiteral subjectAltNames = (BLangArrayLiteral) tlsField.getValue();
                    tlsOptions.setSubjectAltNames(getArray(subjectAltNames));
                    break;
                default:
                    throw new KubernetesPluginException("Unknown field found for istio gateway server tls options.");
            }
        }
        
        server.setTls(tlsOptions);
    }
    
    private int extractPort(List<BLangRecordLiteral.BLangRecordKeyValue> endpointConfig) throws
            KubernetesPluginException {
        for (BLangRecordLiteral.BLangRecordKeyValue keyValue : endpointConfig) {
            String key = keyValue.getKey().toString();
            if ("port".equals(key)) {
                try {
                    return Integer.parseInt(keyValue.getValue().toString());
                } catch (NumberFormatException e) {
                    throw new KubernetesPluginException("Listener endpoint port must be an integer to use " +
                                                        "@kubernetes annotations.");
                }
            }
        }
        throw new KubernetesPluginException("Unable to extract port from endpoint");
    }
    
    private enum TLSOptionConfig {
        httpsRedirect,
        mode,
        serverCertificate,
        privateKey,
        caCertificates,
        subjectAltNames
    }
    
    private enum IstioPortConfig {
        number,
        protocol,
        name
    }
    
    private enum IstioServerConfig {
        port,
        hosts,
        tls
    }
    
    private enum IstioGatewayConfig {
        name,
        namespace,
        labels,
        annotations,
        selector,
        servers
    }
}
