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
import org.ballerinax.kubernetes.exceptions.KubernetesPluginException;
import org.ballerinax.kubernetes.models.KubernetesContext;
import org.ballerinax.kubernetes.models.istio.IstioGatewayModel;
import org.ballerinax.kubernetes.processors.AbstractAnnotationProcessor;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;

import java.util.List;

import static org.ballerinax.kubernetes.utils.KubernetesUtils.getMap;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.resolveValue;

/**
 * public type IstioPortProtocol "HTTP"|"HTTPS"|"GRPC"|"HTTP2"|"MONGO"|"TCP"|"TLS";
 *
 * public type IstioPortConfig {
 *     int number;
 *     IstioPortProtocol protocol;
 *     string? name;
 * };
 *
 * public type IstioTLSOptionMode "PASSTHROUGH"|"SIMPLE"|"MUTUAL";
 *
 * public type IstioTLSOptionConfig record {
 *     boolean httpRedirect;
 *     IstioTLSOptionMode? mode;
 *     string? serverCertificate;
 *     string? privateKey;
 *     string? caCertificates;
 *     string[]? subjectAltNames;
 * };
 *
 * public type IstioServerConfig record {
 *     IstioPortConfig port;
 *     string[] hosts;
 *     IstioTLSOptionConfig? tls;
 * };
 *
 * public type IstioGatewayConfig record {
 *     string name;
 *     string? namespace;
 *     map<string>? labels;
 *     map<string>? annotations;
 *     map<string> selector;
 *     IstioServerConfig[] servers;
 * };
 */

/**
 *
 */
public class IstioGatewayAnnotationProcessor extends AbstractAnnotationProcessor {
    @Override
    public void processAnnotation(ServiceNode serviceNode, AnnotationAttachmentNode attachmentNode)
            throws KubernetesPluginException {
        List<BLangRecordLiteral.BLangRecordKeyValue> keyValues =
                ((BLangRecordLiteral) ((BLangAnnotationAttachment)attachmentNode).expr).getKeyValuePairs();
        
        this.processIstioGatewayAnnotation(keyValues);
    }
    
    @Override
    public void processAnnotation(EndpointNode endpointNode, AnnotationAttachmentNode attachmentNode)
            throws KubernetesPluginException {
        List<BLangRecordLiteral.BLangRecordKeyValue> keyValues =
                ((BLangRecordLiteral) ((BLangAnnotationAttachment)attachmentNode).expr).getKeyValuePairs();
    
        this.processIstioGatewayAnnotation(keyValues);
    }
    
    private void processIstioGatewayAnnotation(List<BLangRecordLiteral.BLangRecordKeyValue> gatewayFields)
            throws KubernetesPluginException {
        for (BLangRecordLiteral.BLangRecordKeyValue gatewayField : gatewayFields) {
            IstioGatewayModel gatewayModel = new IstioGatewayModel();
            switch (IstioGatewayConfig.valueOf(gatewayField.getKey().toString())) {
                case name:
                    gatewayModel.setName(resolveValue(gatewayField.getValue().toString()));
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
                    break;
            }
            KubernetesContext.getInstance().getDataHolder().addIstioGatewayModel(gatewayModel);
        }
    }
    
    private
    
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
        labels,
        annotations,
        selector,
        servers
    }
}
