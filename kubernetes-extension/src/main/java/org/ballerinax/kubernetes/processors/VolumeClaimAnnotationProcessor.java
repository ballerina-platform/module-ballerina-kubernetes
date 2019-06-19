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
import org.ballerinax.kubernetes.exceptions.KubernetesPluginException;
import org.ballerinax.kubernetes.models.KubernetesContext;
import org.ballerinax.kubernetes.models.PersistentVolumeClaimModel;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangListConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.ballerinax.kubernetes.KubernetesConstants.MAIN_FUNCTION_NAME;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.getBooleanValue;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.getMap;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.getStringValue;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.getValidName;

/**
 * Persistent volume claim annotation processor.
 */
public class VolumeClaimAnnotationProcessor extends AbstractAnnotationProcessor {
    
    /**
     * Process PersistentVolumeClaim annotations for services.
     *
     * @param serviceNode The service node.
     * @param attachmentNode Attachment Node
     * @throws KubernetesPluginException When error occurs while parsing the annotations.
     */
    public void processAnnotation(ServiceNode serviceNode, AnnotationAttachmentNode attachmentNode) throws
            KubernetesPluginException {
        processVolumeClaims(attachmentNode);
    }
    
    /**
     * Process PersistentVolumeClaim annotations for functions.
     *
     * @param functionNode   The function node.
     * @param attachmentNode Matching annotation.
     * @throws KubernetesPluginException When error occurs while parsing the annotations.
     */
    @Override
    public void processAnnotation(FunctionNode functionNode, AnnotationAttachmentNode attachmentNode) throws
            KubernetesPluginException {
        if (!MAIN_FUNCTION_NAME.equals(functionNode.getName().getValue())) {
            throw new KubernetesPluginException("@kubernetes:PersistentVolumeClaim annotation cannot be attached to " +
                                                "a non main function.");
        }
        
        processVolumeClaims(attachmentNode);
    }
    
    /**
     * Process PersistentVolumeClaim annotations.
     *
     * @param attachmentNode Annotation node.
     * @throws KubernetesPluginException When error occurs while parsing the annotations.
     */
    private void processVolumeClaims(AnnotationAttachmentNode attachmentNode) throws KubernetesPluginException {
        Set<PersistentVolumeClaimModel> volumeClaimModels = new HashSet<>();
        List<BLangRecordLiteral.BLangRecordKeyValue> keyValues =
                ((BLangRecordLiteral) ((BLangAnnotationAttachment) attachmentNode).expr).getKeyValuePairs();
        for (BLangRecordLiteral.BLangRecordKeyValue keyValue : keyValues) {
            List<BLangExpression> secretAnnotation = ((BLangListConstructorExpr) keyValue.valueExpr).exprs;
            for (BLangExpression bLangExpression : secretAnnotation) {
                PersistentVolumeClaimModel claimModel = new PersistentVolumeClaimModel();
                List<BLangRecordLiteral.BLangRecordKeyValue> annotationValues =
                        ((BLangRecordLiteral) bLangExpression).getKeyValuePairs();
                for (BLangRecordLiteral.BLangRecordKeyValue annotation : annotationValues) {
                    VolumeClaimConfig volumeMountConfig =
                            VolumeClaimConfig.valueOf(annotation.getKey().toString());
                    switch (volumeMountConfig) {
                        case name:
                            claimModel.setName(getValidName(getStringValue(annotation.getValue())));
                            break;
                        case labels:
                            claimModel.setLabels(getMap(keyValue.getValue()));
                            break;
                        case annotations:
                            claimModel.setAnnotations(getMap(keyValue.getValue()));
                            break;
                        case mountPath:
                            claimModel.setMountPath(getStringValue(annotation.getValue()));
                            break;
                        case accessMode:
                            claimModel.setAccessMode(getStringValue(annotation.getValue()));
                            break;
                        case volumeClaimSize:
                            claimModel.setVolumeClaimSize(getStringValue(annotation.getValue()));
                            break;
                        case readOnly:
                            claimModel.setReadOnly(getBooleanValue(annotation.getValue()));
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
        labels,
        annotations,
        mountPath,
        readOnly,
        accessMode,
        volumeClaimSize,
    }
}
