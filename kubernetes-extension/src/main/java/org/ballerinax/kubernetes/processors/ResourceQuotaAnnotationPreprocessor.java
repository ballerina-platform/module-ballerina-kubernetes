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
import org.ballerinalang.model.tree.FunctionNode;
import org.ballerinalang.model.tree.ServiceNode;
import org.ballerinalang.model.tree.SimpleVariableNode;
import org.ballerinax.kubernetes.exceptions.KubernetesPluginException;
import org.ballerinax.kubernetes.models.KubernetesContext;
import org.ballerinax.kubernetes.models.ResourceQuotaModel;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangListConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.ballerinax.kubernetes.KubernetesConstants.MAIN_FUNCTION_NAME;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.convertRecordFields;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.getList;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.getMap;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.getStringValue;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.getValidName;

/**
 * Resource quota annotation processor.
 */
public class ResourceQuotaAnnotationPreprocessor extends AbstractAnnotationProcessor {
    
    @Override
    public void processAnnotation(ServiceNode serviceNode, AnnotationAttachmentNode attachmentNode)
            throws KubernetesPluginException {
        processResourceQuotaAnnotation((BLangAnnotationAttachment) attachmentNode);
    }
    
    @Override
    public void processAnnotation(SimpleVariableNode variableNode, AnnotationAttachmentNode attachmentNode)
            throws KubernetesPluginException {
        processResourceQuotaAnnotation((BLangAnnotationAttachment) attachmentNode);
    }
    
    @Override
    public void processAnnotation(FunctionNode functionNode, AnnotationAttachmentNode attachmentNode)
            throws KubernetesPluginException {
        if (!MAIN_FUNCTION_NAME.equals(functionNode.getName().getValue())) {
            throw new KubernetesPluginException("@kubernetes:ResourceQuota{} annotation must be attached to a " +
                                                "main function.");
        }
        
        processResourceQuotaAnnotation((BLangAnnotationAttachment) attachmentNode);
    }
    
    private void processResourceQuotaAnnotation(BLangAnnotationAttachment attachmentNode)
            throws KubernetesPluginException {
        Set<ResourceQuotaModel> resourceQuotaModels = new HashSet<>();
        List<BLangRecordLiteral.BLangRecordKeyValueField> keyValues =
                convertRecordFields(((BLangRecordLiteral) attachmentNode.expr).getFields());
        for (BLangRecordLiteral.BLangRecordKeyValueField keyValue : keyValues) {
            List<BLangExpression> secretAnnotation = ((BLangListConstructorExpr) keyValue.valueExpr).exprs;
            for (BLangExpression bLangExpression : secretAnnotation) {
                ResourceQuotaModel resourceQuotaModel = new ResourceQuotaModel();
                List<BLangRecordLiteral.BLangRecordKeyValueField> annotationValues =
                        convertRecordFields(((BLangRecordLiteral) bLangExpression).getFields());
                for (BLangRecordLiteral.BLangRecordKeyValueField annotation : annotationValues) {
                    ResourceQuotaConfig resourceQuotaConfig =
                            ResourceQuotaConfig.valueOf(annotation.getKey().toString());
                    switch (resourceQuotaConfig) {
                        case name:
                            resourceQuotaModel.setName(getValidName(getStringValue(annotation.getValue())));
                            break;
                        case labels:
                            resourceQuotaModel.setLabels(getMap(annotation.getValue()));
                            break;
                        case annotations:
                            resourceQuotaModel.setAnnotations(getMap(annotation.getValue()));
                            break;
                        case hard:
                            resourceQuotaModel.setHard(getMap(annotation.getValue()));
                            break;
                        case scopes:
                            resourceQuotaModel.setScopes(new HashSet<>(getList(annotation.getValue())));
                            break;
                        default:
                            break;
                    }
                }
                resourceQuotaModels.add(resourceQuotaModel);
            }
        }
        KubernetesContext.getInstance().getDataHolder().setResourceQuotaModels(resourceQuotaModels);
    }
    
    /**
     * Enum class for resource quota configurations.
     */
    private enum ResourceQuotaConfig {
        name,
        labels,
        annotations,
        hard,
        scopes
    }
}
