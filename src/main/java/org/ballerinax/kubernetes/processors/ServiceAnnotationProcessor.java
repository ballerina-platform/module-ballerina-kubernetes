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
import org.ballerinax.kubernetes.exceptions.KubernetesPluginException;
import org.ballerinax.kubernetes.models.KubernetesDataHolder;
import org.ballerinax.kubernetes.models.ServiceModel;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.BLangEndpoint;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;

import java.util.List;

import static org.ballerinalang.net.http.HttpConstants.HTTP_DEFAULT_PORT;
import static org.ballerinax.kubernetes.KubernetesConstants.DEFAULT_LISTENER_PORT;
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
        serviceModel.setPort(extractPort(endpointConfig));
        KubernetesDataHolder.getInstance().addBEndpointToK8sServiceMap(endpointNode.getName().getValue(), serviceModel);
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
        serviceModel.setPort(extractPort(endpointConfig));
        KubernetesDataHolder.getInstance().addBEndpointToK8sServiceMap(serviceNode.getName().getValue(), serviceModel);
    }

    private int extractPort(List<BLangRecordLiteral.BLangRecordKeyValue> endpointConfig) {
        for (BLangRecordLiteral.BLangRecordKeyValue keyValue : endpointConfig) {
            String key = keyValue.getKey().toString();
            if ("port".equals(key)) {
                return Integer.parseInt(keyValue.getValue().toString());
            }
        }
        return HTTP_DEFAULT_PORT;
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
                    serviceModel.setServiceType(annotationValue);
                    break;
                case port:
                    serviceModel.setPort(Integer.parseInt(annotationValue));
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
        port
    }
}
