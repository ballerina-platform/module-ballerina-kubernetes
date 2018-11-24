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
import org.ballerinax.kubernetes.models.PersistentVolumeClaimModel;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangArrayLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.ballerinax.kubernetes.utils.KubernetesUtils.getMap;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.getValidName;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.resolveValue;

/**
 * Persistent volume claim annotation processor.
 */
public class VolumeClaimAnnotationProcessor extends AbstractAnnotationProcessor {

    /**
     * Process PersistentVolumeClaim annotations.
     *
     * @param attachmentNode Attachment Node
     */
    public void processAnnotation(ServiceNode serviceNode, AnnotationAttachmentNode attachmentNode) throws
            KubernetesPluginException {
        Set<PersistentVolumeClaimModel> volumeClaimModels = new HashSet<>();
        List<BLangRecordLiteral.BLangRecordKeyValue> keyValues =
                ((BLangRecordLiteral) ((BLangAnnotationAttachment) attachmentNode).expr).getKeyValuePairs();
        for (BLangRecordLiteral.BLangRecordKeyValue keyValue : keyValues) {
            List<BLangExpression> secretAnnotation = ((BLangArrayLiteral) keyValue.valueExpr).exprs;
            for (BLangExpression bLangExpression : secretAnnotation) {
                PersistentVolumeClaimModel claimModel = new PersistentVolumeClaimModel();
                List<BLangRecordLiteral.BLangRecordKeyValue> annotationValues =
                        ((BLangRecordLiteral) bLangExpression).getKeyValuePairs();
                for (BLangRecordLiteral.BLangRecordKeyValue annotation : annotationValues) {
                    VolumeClaimConfig volumeMountConfig =
                            VolumeClaimConfig.valueOf(annotation.getKey().toString());
                    String annotationValue = resolveValue(annotation.getValue().toString());
                    switch (volumeMountConfig) {
                        case name:
                            claimModel.setName(getValidName(annotationValue));
                            break;
                        case namespace:
                            claimModel.setNamespace(annotationValue);
                            break;
                        case labels:
                            claimModel.setLabels(getMap(((BLangRecordLiteral) keyValue.valueExpr).keyValuePairs));
                            break;
                        case annotations:
                            claimModel.setAnnotations(getMap(((BLangRecordLiteral) keyValue.valueExpr).keyValuePairs));
                            break;
                        case mountPath:
                            claimModel.setMountPath(annotationValue);
                            break;
                        case accessMode:
                            claimModel.setAccessMode(annotationValue);
                            break;
                        case volumeClaimSize:
                            claimModel.setVolumeClaimSize(annotationValue);
                            break;
                        case readOnly:
                            claimModel.setReadOnly(Boolean.parseBoolean(annotationValue));
                            break;
                        default:
                            break;
                    }
                }
                volumeClaimModels.add(claimModel);
            }
        }
        KubernetesContext.getInstance().getDataHolder().addPersistentVolumeClaims(volumeClaimModels);
    }

    /**
     * Enum class for volume configurations.
     */
    private enum VolumeClaimConfig {
        name,
        namespace,
        labels,
        annotations,
        mountPath,
        readOnly,
        accessMode,
        volumeClaimSize,
    }
}
