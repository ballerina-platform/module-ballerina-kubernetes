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
import org.ballerinalang.model.types.TypeTags;
import org.ballerinax.kubernetes.exceptions.KubernetesPluginException;
import org.ballerinax.kubernetes.models.KubernetesContext;
import org.ballerinax.kubernetes.models.istio.IstioVirtualService;
import org.ballerinax.kubernetes.processors.AbstractAnnotationProcessor;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangArrayLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.ballerinax.kubernetes.utils.KubernetesUtils.getArray;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.getMap;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.resolveValue;

/**
 * Istio virtual service annotation processor.
 */
public class IstioVirtualServiceAnnotationProcessor extends AbstractAnnotationProcessor {
    
    @Override
    public void processAnnotation(ServiceNode serviceNode, AnnotationAttachmentNode attachmentNode)
            throws KubernetesPluginException {
        List<BLangRecordLiteral.BLangRecordKeyValue> keyValues =
                ((BLangRecordLiteral) ((BLangAnnotationAttachment) attachmentNode).expr).getKeyValuePairs();
    
        this.processIstioVSAnnotation(keyValues);
    }
    
    @Override
    public void processAnnotation(EndpointNode endpointNode, AnnotationAttachmentNode attachmentNode)
            throws KubernetesPluginException {
        List<BLangRecordLiteral.BLangRecordKeyValue> keyValues =
                ((BLangRecordLiteral) ((BLangAnnotationAttachment) attachmentNode).expr).getKeyValuePairs();
    
        this.processIstioVSAnnotation(keyValues);
    }
    
    /**
     * Process @Kubernetes:IstioGateway annotation.
     * @param gatewayFields Fields of the gateway annotation.
     * @throws KubernetesPluginException Unable to process annotations.
     */
    private void processIstioVSAnnotation(List<BLangRecordLiteral.BLangRecordKeyValue> gatewayFields)
            throws KubernetesPluginException {
        IstioVirtualService vsModel = new IstioVirtualService();
        for (BLangRecordLiteral.BLangRecordKeyValue gatewayField : gatewayFields) {
            switch (IstioVSConfig.valueOf(gatewayField.getKey().toString())) {
                case name:
                    vsModel.setName(resolveValue(gatewayField.getValue().toString()));
                    break;
                case namespace:
                    vsModel.setNamespace(resolveValue(gatewayField.getValue().toString()));
                    break;
                case labels:
                    BLangRecordLiteral labelsField = (BLangRecordLiteral) gatewayField.getValue();
                    vsModel.setLabels(getMap(labelsField.getKeyValuePairs()));
                    break;
                case annotations:
                    BLangRecordLiteral annotationsField = (BLangRecordLiteral) gatewayField.getValue();
                    vsModel.setAnnotations(getMap(annotationsField.getKeyValuePairs()));
                    break;
                case hosts:
                    BLangArrayLiteral hostsField = (BLangArrayLiteral) gatewayField.getValue();
                    List<String> hostsList = new ArrayList<>(getArray(hostsField));
                    vsModel.setHosts(hostsList);
                    break;
                case gateways:
                    BLangArrayLiteral gatewaysField = (BLangArrayLiteral)  gatewayField.getValue();
                    List<String> gatewayList = new ArrayList<>(getArray(gatewaysField));
                    vsModel.setGateways(gatewayList);
                    break;
                case http:
                    BLangArrayLiteral httpFields = (BLangArrayLiteral) gatewayField.getValue();
                    List<Object> httpModels = (List<Object>) processAnnotation(httpFields);
                    vsModel.setHttp(httpModels);
                    break;
                case tls:
                    BLangArrayLiteral tlsFields = (BLangArrayLiteral) gatewayField.getValue();
                    List<Object> tlsModels = (List<Object>) processAnnotation(tlsFields);
                    vsModel.setTls(tlsModels);
                    break;
                case tcp:
                    BLangArrayLiteral tcpFields = (BLangArrayLiteral) gatewayField.getValue();
                    List<Object> tcpModels = (List<Object>) processAnnotation(tcpFields);
                    vsModel.setTcp(tcpModels);
                    break;
                default:
                    throw new KubernetesPluginException("Unknown field found for istio virtual service.");
            }
        }
        KubernetesContext.getInstance().getDataHolder().addIstioVirtualServiceModels(vsModel);
    }
    
    private Object processAnnotation(ExpressionNode annotationValue) throws KubernetesPluginException {
        if (annotationValue instanceof BLangArrayLiteral) {
            BLangArrayLiteral arrayValue = (BLangArrayLiteral) annotationValue;
            List<Object> arrayModels = new LinkedList<>();
            for (ExpressionNode expression : arrayValue.getExpressions()) {
                arrayModels.add(processAnnotation(expression));
            }
            return arrayModels;
        } else if (annotationValue instanceof BLangRecordLiteral) {
            BLangRecordLiteral serverFieldRecord = (BLangRecordLiteral) annotationValue;
            Map<String, Object> mapModels = new LinkedHashMap<>();
            for (BLangRecordLiteral.BLangRecordKeyValue keyValuePair : serverFieldRecord.getKeyValuePairs()) {
                mapModels.put(keyValuePair.getKey().toString(), processAnnotation(keyValuePair.getValue()));
            }
            return mapModels;
        } else if (annotationValue instanceof BLangLiteral) {
            BLangLiteral literal = (BLangLiteral) annotationValue;
            if (literal.typeTag == TypeTags.INT_TAG) {
                return Integer.parseInt((literal).getValue().toString());
            } else if (literal.typeTag == TypeTags.BOOLEAN_TAG) {
                return Boolean.parseBoolean((literal).getValue().toString());
            } else if (literal.typeTag == TypeTags.FLOAT_TAG) {
                return Float.parseFloat((literal).getValue().toString());
            } else {
                return resolveValue((literal).getValue().toString());
            }
        } else {
            throw new KubernetesPluginException("Unable to resolve annotation values.");
        }
    }
    
    private enum IstioVSConfig {
        name,
        namespace,
        labels,
        annotations,
        hosts,
        gateways,
        http,
        tls,
        tcp
    }
}
