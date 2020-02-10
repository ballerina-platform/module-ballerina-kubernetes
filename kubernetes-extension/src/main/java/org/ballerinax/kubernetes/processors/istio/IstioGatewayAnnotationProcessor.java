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
import org.ballerinalang.model.tree.ServiceNode;
import org.ballerinalang.model.tree.SimpleVariableNode;
import org.ballerinalang.model.tree.expressions.ExpressionNode;
import org.ballerinax.kubernetes.exceptions.KubernetesPluginException;
import org.ballerinax.kubernetes.models.KubernetesContext;
import org.ballerinax.kubernetes.models.istio.IstioGatewayModel;
import org.ballerinax.kubernetes.models.istio.IstioPortModel;
import org.ballerinax.kubernetes.models.istio.IstioServerModel;
import org.ballerinax.kubernetes.processors.AbstractAnnotationProcessor;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangListConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;

import java.util.LinkedList;
import java.util.List;

import static org.ballerinax.kubernetes.KubernetesConstants.ISTIO_GATEWAY_POSTFIX;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.convertRecordFields;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.getList;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.getMap;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.getStringValue;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.getValidName;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.isBlank;

/**
 * Istio gateway annotation processor.
 *
 * @since 0.985.0
 */
public class IstioGatewayAnnotationProcessor extends AbstractAnnotationProcessor {
    @Override
    public void processAnnotation(ServiceNode serviceNode, AnnotationAttachmentNode attachmentNode)
            throws KubernetesPluginException {
        List<BLangRecordLiteral.BLangRecordKeyValueField> keyValues =
            convertRecordFields(((BLangRecordLiteral) ((BLangAnnotationAttachment) attachmentNode).expr).getFields());
    
        IstioGatewayModel gwModel = this.processIstioGatewayAnnotation(keyValues);
        if (isBlank(gwModel.getName())) {
            gwModel.setName(getValidName(serviceNode.getName().getValue()) + ISTIO_GATEWAY_POSTFIX);
        }
        
        setDefaultValues(gwModel);
        KubernetesContext.getInstance().getDataHolder().addIstioGatewayModel(serviceNode.getName().getValue(), gwModel);
    }
    
    @Override
    public void processAnnotation(SimpleVariableNode variableNode, AnnotationAttachmentNode attachmentNode)
            throws KubernetesPluginException {
        List<BLangRecordLiteral.BLangRecordKeyValueField> keyValues =
            convertRecordFields(((BLangRecordLiteral) ((BLangAnnotationAttachment) attachmentNode).expr).getFields());
    
        IstioGatewayModel gwModel = this.processIstioGatewayAnnotation(keyValues);
        if (isBlank(gwModel.getName())) {
            gwModel.setName(getValidName(variableNode.getName().getValue()) + ISTIO_GATEWAY_POSTFIX);
        }
    
        setDefaultValues(gwModel);
        KubernetesContext.getInstance().getDataHolder().addIstioGatewayModel(variableNode.getName().getValue(),
                gwModel);
    }
    
    /**
     * Set default values for the gateway model.
     *
     * @param gwModel The gateway model.
     */
    private void setDefaultValues(IstioGatewayModel gwModel) {
        if (null == gwModel.getServers() || gwModel.getServers().size() == 0) {
            List<IstioServerModel> serversModel = new LinkedList<>();
            IstioServerModel serverModel = new IstioServerModel();
            
            IstioPortModel portModel = new IstioPortModel();
            portModel.setNumber(80);
            portModel.setProtocol("HTTP");
            portModel.setName("http");
            serverModel.setPort(portModel);
            
            if (null == serverModel.getHosts() || serverModel.getHosts().size() == 0) {
                List<String> hosts = new LinkedList<>();
                hosts.add("*");
                serverModel.setHosts(hosts);
            }
            
            serversModel.add(serverModel);
            gwModel.setServers(serversModel);
        }
    }
    
    /**
     * Process @istio:Gateway annotation.
     *
     * @param gatewayFields Fields of the gateway annotation.
     * @throws KubernetesPluginException Unable to process annotations.
     */
    private IstioGatewayModel processIstioGatewayAnnotation(
            List<BLangRecordLiteral.BLangRecordKeyValueField> gatewayFields)
            throws KubernetesPluginException {
        IstioGatewayModel gatewayModel = new IstioGatewayModel();
        for (BLangRecordLiteral.BLangRecordKeyValueField gatewayField : gatewayFields) {
            switch (GatewayConfig.valueOf(gatewayField.getKey().toString())) {
                case name:
                    gatewayModel.setName(getValidName(getStringValue(gatewayField.getValue())));
                    break;
                case labels:
                    gatewayModel.setLabels(getMap(gatewayField.getValue()));
                    break;
                case annotations:
                    gatewayModel.setAnnotations(getMap(gatewayField.getValue()));
                    break;
                case selector:
                    gatewayModel.setSelector(getMap(gatewayField.getValue()));
                    break;
                case servers:
                    processIstioGatewayServerAnnotation(gatewayModel,
                            (BLangListConstructorExpr) gatewayField.getValue());
                    break;
                default:
                    throw new KubernetesPluginException("unknown field found for istio gateway.");
            }
        }
        
        return gatewayModel;
    }
    
    /**
     * Process server field of @istio:Gateway annotation.
     *
     * @param gatewayModel The gateway model.
     * @param serversField List of servers of the gateway.
     * @throws KubernetesPluginException Unable to process annotation
     */
    private void processIstioGatewayServerAnnotation(IstioGatewayModel gatewayModel,
                                                     BLangListConstructorExpr serversField)
            throws KubernetesPluginException {
        List<IstioServerModel> servers = new LinkedList<>();
        for (ExpressionNode serverRecord : serversField.getExpressions()) {
            if (serverRecord instanceof BLangRecordLiteral) {
                BLangRecordLiteral serverFieldRecord = (BLangRecordLiteral) serverRecord;
                IstioServerModel server = new IstioServerModel();
                for (BLangRecordLiteral.BLangRecordKeyValueField serverField :
                        convertRecordFields(serverFieldRecord.getFields())) {
                    switch (ServerConfig.valueOf(serverField.getKey().toString())) {
                        case port:
                            BLangRecordLiteral portRecord = (BLangRecordLiteral) serverField.getValue();
                            processIstioGatewayPortAnnotation(server, convertRecordFields(portRecord.getFields()));
                            break;
                        case hosts:
                            server.setHosts(getList(serverField.getValue()));
                            break;
                        case tls:
                            BLangRecordLiteral tlsRecord = (BLangRecordLiteral) serverField.getValue();
                            processIstioGatewayTLSAnnotation(server, convertRecordFields(tlsRecord.getFields()));
                            break;
                        default:
                            throw new KubernetesPluginException("unknown field found for istio gateway server.");
                    }
                }
                servers.add(server);
            }
        }
        gatewayModel.setServers(servers);
    }
    
    /**
     * Process port fields of @istio:Gateway annotations's server field.
     *
     * @param server     The server model.
     * @param portFields The fields of the server's port.
     * @throws KubernetesPluginException Unable to process annotation
     */
    private void processIstioGatewayPortAnnotation(IstioServerModel server,
                                                   List<BLangRecordLiteral.BLangRecordKeyValueField> portFields)
            throws KubernetesPluginException {
        IstioPortModel portModel = new IstioPortModel();
        for (BLangRecordLiteral.BLangRecordKeyValueField portField : portFields) {
            switch (PortConfig.valueOf(portField.getKey().toString())) {
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
                    throw new KubernetesPluginException("unknown field found for istio gateway server port.");
            }
        }
        server.setPort(portModel);
    }
    
    /**
     * Process tls option fields of @istio:Gateway annotations's server field.
     *
     * @param server    The server model.
     * @param tlsFields The fields of the server's tls options.
     * @throws KubernetesPluginException Unable to process annotation
     */
    private void processIstioGatewayTLSAnnotation(IstioServerModel server,
                                                  List<BLangRecordLiteral.BLangRecordKeyValueField> tlsFields)
            throws KubernetesPluginException {
        IstioServerModel.TLSOptions tlsOptions = new IstioServerModel.TLSOptions();
        for (BLangRecordLiteral.BLangRecordKeyValueField tlsField : tlsFields) {
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
                    tlsOptions.setSubjectAltNames(getList(tlsField.getValue()));
                    break;
                default:
                    throw new KubernetesPluginException("unknown field found for istio gateway server tls options.");
            }
        }
        
        server.setTls(tlsOptions);
    }
    
    private enum TLSOptionConfig {
        httpsRedirect,
        mode,
        serverCertificate,
        privateKey,
        caCertificates,
        subjectAltNames
    }
    
    private enum PortConfig {
        number,
        protocol,
        name
    }
    
    private enum ServerConfig {
        port,
        hosts,
        tls
    }
    
    private enum GatewayConfig {
        name,
        labels,
        annotations,
        selector,
        servers
    }
}
