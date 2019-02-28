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
import org.ballerinalang.model.tree.ServiceNode;
import org.ballerinalang.model.tree.SimpleVariableNode;
import org.ballerinax.kubernetes.KubernetesConstants;
import org.ballerinax.kubernetes.exceptions.KubernetesPluginException;
import org.ballerinax.kubernetes.models.KubernetesContext;
import org.ballerinax.kubernetes.models.ServiceModel;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeInit;

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
    public void processAnnotation(ServiceNode serviceNode, AnnotationAttachmentNode attachmentNode) throws
            KubernetesPluginException {
        BLangService bService = (BLangService) serviceNode;
        for (BLangExpression attachedExpr : bService.getAttachedExprs()) {
            // If not anonymous endpoint throw error.
            if (attachedExpr instanceof BLangSimpleVarRef) {
                throw new KubernetesPluginException("adding @kubernetes:Service{} annotation to a service is only " +
                                                    "supported when the service has an anonymous listener");
            }
            
        }
        ServiceModel serviceModel = getServiceModelFromAnnotation(attachmentNode);
        if (isBlank(serviceModel.getName())) {
            serviceModel.setName(getValidName(serviceNode.getName().getValue()) + SVC_POSTFIX);
        }

        // If service annotation port is not empty, then listener port is used for the k8s svc target port while
        // service annotation port is used for k8s port.
        // If service annotation port is empty, then listener port is used for both port and target port of the k8s
        // svc.
        BLangTypeInit bListener = (BLangTypeInit) bService.getAttachedExprs().get(0);
        if (serviceModel.getPort() == -1) {
            serviceModel.setPort(extractPort(bListener));
        }
        serviceModel.setTargetPort(extractPort(bListener));
        KubernetesContext.getInstance().getDataHolder().addBListenerToK8sServiceMap(serviceNode.getName().getValue(),
                serviceModel);
    }
    
    @Override
    public void processAnnotation(SimpleVariableNode variableNode, AnnotationAttachmentNode attachmentNode)
            throws KubernetesPluginException {
        ServiceModel serviceModel = getServiceModelFromAnnotation(attachmentNode);
        if (isBlank(serviceModel.getName())) {
            serviceModel.setName(getValidName(variableNode.getName().getValue()) + SVC_POSTFIX);
        }
        
        // If service annotation port is not empty, then listener port is used for the k8s svc target port while
        // service annotation port is used for k8s port.
        // If service annotation port is empty, then listener port is used for both port and target port of the k8s
        // svc.
        BLangTypeInit bListener = (BLangTypeInit) ((BLangSimpleVariable) variableNode).expr;
        if (serviceModel.getPort() == -1) {
            serviceModel.setPort(extractPort(bListener));
        }
        serviceModel.setTargetPort(extractPort(bListener));
        KubernetesContext.getInstance().getDataHolder().addBListenerToK8sServiceMap(variableNode.getName().getValue()
                , serviceModel);
    }
    
    private int extractPort(BLangTypeInit bListener) throws
            KubernetesPluginException {
        try {
            return Integer.parseInt(bListener.argsExpr.get(0).toString());
        } catch (NumberFormatException e) {
            throw new KubernetesPluginException("unable to parse port of the service: " +
                                            bListener.argsExpr.get(0).toString());
        }
    }

    private ServiceModel getServiceModelFromAnnotation(AnnotationAttachmentNode attachmentNode) throws
            KubernetesPluginException {
        ServiceModel serviceModel = new ServiceModel();
        List<BLangRecordLiteral.BLangRecordKeyValue> keyValues =
                ((BLangRecordLiteral) ((BLangAnnotationAttachment) attachmentNode).expr).getKeyValuePairs();
        for (BLangRecordLiteral.BLangRecordKeyValue keyValue : keyValues) {
            ServiceConfiguration serviceConfiguration =
                    ServiceConfiguration.valueOf(keyValue.getKey().toString());
            switch (serviceConfiguration) {
                case name:
                    serviceModel.setName(getValidName(resolveValue(keyValue.getValue().toString())));
                    break;
                case labels:
                    serviceModel.setLabels(getMap(((BLangRecordLiteral) keyValue.valueExpr).keyValuePairs));
                    break;
                case annotations:
                    serviceModel.setAnnotations(getMap(((BLangRecordLiteral) keyValue.valueExpr).keyValuePairs));
                    break;
                case serviceType:
                    serviceModel.setServiceType(KubernetesConstants.ServiceType.valueOf(resolveValue(
                            keyValue.getValue().toString())).name());
                    break;
                case port:
                    serviceModel.setPort(Integer.parseInt(resolveValue(keyValue.getValue().toString())));
                    break;
                case sessionAffinity:
                    serviceModel.setSessionAffinity(resolveValue(keyValue.getValue().toString()));
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
        annotations,
        serviceType,
        port,
        sessionAffinity
    }
}
