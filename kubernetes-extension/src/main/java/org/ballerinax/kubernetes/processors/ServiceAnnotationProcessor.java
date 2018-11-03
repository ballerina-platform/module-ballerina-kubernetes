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

package org.ballerinax.kubernetes.processors;

import org.ballerinalang.model.tree.AnnotationAttachmentNode;
import org.ballerinalang.model.tree.EndpointNode;
import org.ballerinalang.model.tree.ServiceNode;
import org.ballerinalang.model.tree.expressions.RecordLiteralNode;
import org.ballerinax.kubernetes.KubernetesConstants;
import org.ballerinax.kubernetes.exceptions.KubernetesPluginException;
import org.ballerinax.kubernetes.models.KubernetesContext;
import org.ballerinax.kubernetes.models.ServiceModel;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.BLangEndpoint;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;

import java.util.List;

import static org.ballerinax.kubernetes.KubernetesConstants.SVC_POSTFIX;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.getMap;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.getValidName;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.isBlank;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.resolveValue;

/**
 * Service annotation processor.
 */
public class ServiceAnnotationProcessor extends AbstractAnnotationProcessor {

    @Override
    public void processAnnotation(EndpointNode endpointNode, AnnotationAttachmentNode attachmentNode) throws
            KubernetesPluginException {
        ServiceModel serviceModel = getServiceModelFromAnnotation(attachmentNode);
        if (isBlank(serviceModel.getName())) {
            serviceModel.setName(getValidName(endpointNode.getName().getValue()) + SVC_POSTFIX);
        }
        List<BLangRecordLiteral.BLangRecordKeyValue> endpointConfig =
                ((BLangRecordLiteral) ((BLangEndpoint) endpointNode).configurationExpr).getKeyValuePairs();
        // If service annotation port is not empty, then endpoint port is used for the k8s svc target port while
        // service annotation port is used for k8s port.
        // If service annotation port is empty, then endpoint port is used for both port and target port of the k8s
        // svc.
        if (serviceModel.getPort() == -1) {
            serviceModel.setPort(extractPort(endpointConfig));
        }
        serviceModel.setTargetPort(extractPort(endpointConfig));
        KubernetesContext.getInstance().getDataHolder().addBEndpointToK8sServiceMap(endpointNode.getName().getValue()
                , serviceModel);
    }

    @Override
    public void processAnnotation(ServiceNode serviceNode, AnnotationAttachmentNode attachmentNode) throws
            KubernetesPluginException {
        RecordLiteralNode anonymousEndpoint = serviceNode.getAnonymousEndpointBind();
        if (anonymousEndpoint == null) {
            throw new KubernetesPluginException("Adding @kubernetes:Service{} annotation to a service is only " +
                    "supported when service is bind to an anonymous endpoint");
        }
        ServiceModel serviceModel = getServiceModelFromAnnotation(attachmentNode);
        if (isBlank(serviceModel.getName())) {
            serviceModel.setName(getValidName(serviceNode.getName().getValue()) + SVC_POSTFIX);
        }
        List<BLangRecordLiteral.BLangRecordKeyValue> endpointConfig =
                ((BLangRecordLiteral) anonymousEndpoint).getKeyValuePairs();
        // If service annotation port is not empty, then endpoint port is used for the k8s svc target port while
        // service annotation port is used for k8s port.
        // If service annotation port is empty, then endpoint port is used for both port and target port of the k8s
        // svc.
        if (serviceModel.getPort() == -1) {
            serviceModel.setPort(extractPort(endpointConfig));
        }
        serviceModel.setTargetPort(extractPort(endpointConfig));
        KubernetesContext.getInstance().getDataHolder().addBEndpointToK8sServiceMap(serviceNode.getName().getValue(),
                serviceModel);
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

    private ServiceModel getServiceModelFromAnnotation(AnnotationAttachmentNode attachmentNode) throws
            KubernetesPluginException {
        ServiceModel serviceModel = new ServiceModel();
        List<BLangRecordLiteral.BLangRecordKeyValue> keyValues =
                ((BLangRecordLiteral) ((BLangAnnotationAttachment) attachmentNode).expr).getKeyValuePairs();
        for (BLangRecordLiteral.BLangRecordKeyValue keyValue : keyValues) {
            ServiceConfiguration serviceConfiguration =
                    ServiceConfiguration.valueOf(keyValue.getKey().toString());
            String annotationValue = resolveValue(keyValue.getValue().toString());
            switch (serviceConfiguration) {
                case name:
                    serviceModel.setName(getValidName(annotationValue));
                    break;
                case labels:
                    serviceModel.setLabels(getMap(((BLangRecordLiteral) keyValue.valueExpr).keyValuePairs));
                    break;
                case serviceType:
                    serviceModel.setServiceType(KubernetesConstants.ServiceType.valueOf(annotationValue).name());
                    break;
                case port:
                    try {
                        serviceModel.setPort(Integer.parseInt(annotationValue));
                    } catch (NumberFormatException e) {
                        throw new KubernetesPluginException("Listener endpoint port must be an integer to use " +
                                "@kubernetes annotations.");
                    }
                    break;
                case sessionAffinity:
                    serviceModel.setSessionAffinity(annotationValue);
                    break;
                default:
                    break;
            }
        }
        return serviceModel;
    }

    /**
     * Enum for Service configurations.
     */
    private enum ServiceConfiguration {
        name,
        labels,
        serviceType,
        port,
        sessionAffinity
    }
}
