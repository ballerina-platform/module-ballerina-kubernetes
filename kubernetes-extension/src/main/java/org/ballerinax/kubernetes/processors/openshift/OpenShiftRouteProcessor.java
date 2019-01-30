/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinax.kubernetes.processors.openshift;

import org.ballerinalang.model.tree.AnnotationAttachmentNode;
import org.ballerinalang.model.tree.IdentifierNode;
import org.ballerinalang.model.tree.ServiceNode;
import org.ballerinalang.model.tree.SimpleVariableNode;
import org.ballerinax.kubernetes.exceptions.KubernetesPluginException;
import org.ballerinax.kubernetes.models.KubernetesContext;
import org.ballerinax.kubernetes.models.openshift.OpenShiftRouteModel;
import org.ballerinax.kubernetes.processors.AbstractAnnotationProcessor;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;

import java.util.List;

import static org.ballerinax.kubernetes.KubernetesConstants.OPENSHIFT_ROUTE_POSTFIX;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.getMap;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.getValidName;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.isBlank;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.resolveValue;

/**
 * Annotation processor for OpenShift's Route.
 */
public class OpenShiftRouteProcessor extends AbstractAnnotationProcessor {
    @Override
    public void processAnnotation(ServiceNode serviceNode, AnnotationAttachmentNode attachmentNode)
            throws KubernetesPluginException {
        this.processRouteAnnotation(serviceNode.getName(), attachmentNode);
    }
    
    @Override
    public void processAnnotation(SimpleVariableNode variableNode, AnnotationAttachmentNode attachmentNode)
            throws KubernetesPluginException {
        this.processRouteAnnotation(variableNode.getName(), attachmentNode);
    }
    
    /**
     * Process @kubernetes:OpenShiftRoute annotation.
     *
     * @param identifierNode Node of which the annotation was attached to.
     * @param attachmentNode The annotation node.
     * @throws KubernetesPluginException Unable to process annotations.
     */
    private void processRouteAnnotation(IdentifierNode identifierNode, AnnotationAttachmentNode attachmentNode)
            throws KubernetesPluginException {
    
        List<BLangRecordLiteral.BLangRecordKeyValue> bcFields =
                ((BLangRecordLiteral) ((BLangAnnotationAttachment) attachmentNode).expr).getKeyValuePairs();
        
        boolean setWithDomainValue = false;
        OpenShiftRouteModel openShiftRoute = new OpenShiftRouteModel();
        for (BLangRecordLiteral.BLangRecordKeyValue bcField : bcFields) {
            switch (OpenShiftRouteFields.valueOf(bcField.getKey().toString())) {
                case name:
                    openShiftRoute.setName(resolveValue(bcField.getValue().toString()));
                    break;
                case namespace:
                    openShiftRoute.setNamespace(resolveValue(bcField.getValue().toString()));
                    break;
                case labels:
                    BLangRecordLiteral labelsField = (BLangRecordLiteral) bcField.getValue();
                    openShiftRoute.setLabels(getMap(labelsField.getKeyValuePairs()));
                    break;
                case annotations:
                    BLangRecordLiteral annotationsField = (BLangRecordLiteral) bcField.getValue();
                    openShiftRoute.setAnnotations(getMap(annotationsField.getKeyValuePairs()));
                    break;
                case host:
                    if (bcField.getValue() instanceof BLangRecordLiteral) {
                        BLangRecordLiteral hostRecord = (BLangRecordLiteral) bcField.getValue();
                        String domainValue = hostRecord.getKeyValuePairs().get(0).getValue().toString();
                        openShiftRoute.setHost(resolveValue(domainValue));
                        setWithDomainValue = true;
                    } else {
                        openShiftRoute.setHost(resolveValue(bcField.getValue().toString()));
                    }
                    break;
                default:
                    throw new KubernetesPluginException("Unknown field found for OpenShiftRoute annotation.");
            }
        }
    
        if (isBlank(openShiftRoute.getName())) {
            openShiftRoute.setName(getValidName(identifierNode.getValue()) + OPENSHIFT_ROUTE_POSTFIX);
        }
    
        // If domain is used for setting the host, then update the host as <service_name>-<namespace>.<domain>
        if (setWithDomainValue) {
            // Setting the host using domain name required namespace.
            if (null == openShiftRoute.getNamespace() || "".equals(openShiftRoute.getNamespace().trim())) {
                throw new KubernetesPluginException("'namespace' field is required when using 'domain' field for " +
                                                    "setting the host of the @kubernetes:OpenShiftRoute{} annotation.");
            }
            openShiftRoute.setHost(openShiftRoute.getName() + "-" + openShiftRoute.getNamespace() + "." +
                                   openShiftRoute.getHost());
        }
    
        KubernetesContext.getInstance().getDataHolder().addOpenShiftRouteModel(identifierNode.getValue(),
                openShiftRoute);
    }
    
    private enum OpenShiftRouteFields {
        name,
        namespace,
        labels,
        annotations,
        host,
        domain
    }
}
