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
import org.ballerinax.kubernetes.exceptions.KubernetesPluginException;
import org.ballerinax.kubernetes.models.KubernetesContext;
import org.ballerinax.kubernetes.models.ResourceQuotaModel;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangArrayLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.ballerinax.kubernetes.utils.KubernetesUtils.getMap;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.getValidName;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.resolveValue;

/**
 * Resource quota annotation processor.
 */
public class ResourceQuotaAnnotationPreprocessor extends AbstractAnnotationProcessor {
    @Override
    public void processAnnotation(ServiceNode serviceNode, AnnotationAttachmentNode attachmentNode) throws
            KubernetesPluginException {
        Set<ResourceQuotaModel> resourceQuotaModels = new HashSet<>();
        List<BLangRecordLiteral.BLangRecordKeyValue> keyValues =
                ((BLangRecordLiteral) ((BLangAnnotationAttachment) attachmentNode).expr).getKeyValuePairs();
        for (BLangRecordLiteral.BLangRecordKeyValue keyValue : keyValues) {
            List<BLangExpression> secretAnnotation = ((BLangArrayLiteral) keyValue.valueExpr).exprs;
            for (BLangExpression bLangExpression : secretAnnotation) {
                ResourceQuotaModel resourceQuotaModel = new ResourceQuotaModel();
                List<BLangRecordLiteral.BLangRecordKeyValue> annotationValues =
                        ((BLangRecordLiteral) bLangExpression).getKeyValuePairs();
                for (BLangRecordLiteral.BLangRecordKeyValue annotation : annotationValues) {
                    ResourceQuotaConfig resourceQuotaConfig =
                            ResourceQuotaConfig.valueOf(annotation.getKey().toString());
                    switch (resourceQuotaConfig) {
                        case name:
                            resourceQuotaModel.setName(getValidName(resolveValue(annotation.getValue().toString())));
                            break;
                        case hard:
                            resourceQuotaModel.setHard(getMap(annotationValues));
                            break;
                        case scope:
                            resourceQuotaModel.setScopes(getScopes(annotation));
                            break;
                        default:
                            break;
                    }
                }
                resourceQuotaModels.add(resourceQuotaModel);
            }
        }
        KubernetesContext.getInstance().getDataHolder().addResourceQuotaModels(resourceQuotaModels);
    }
    
    private Set<String> getScopes(BLangRecordLiteral.BLangRecordKeyValue keyValue) {
        Set<String> scopeSet = new LinkedHashSet<>();
        List<BLangExpression> configAnnotation = ((BLangArrayLiteral) keyValue.valueExpr).exprs;
        for (BLangExpression bLangExpression : configAnnotation) {
            scopeSet.add(bLangExpression.toString());
        }
        return scopeSet;
    }
    
    /**
     * Enum class for volume configurations.
     */
    private enum ResourceQuotaConfig {
        name,
        hard,
        scope
    }
}
