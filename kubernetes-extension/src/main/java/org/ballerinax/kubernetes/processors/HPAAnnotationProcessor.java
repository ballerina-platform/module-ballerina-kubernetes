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
import org.ballerinax.kubernetes.models.PodAutoscalerModel;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;

import java.util.List;

import static org.ballerinax.kubernetes.KubernetesConstants.MAIN_FUNCTION_NAME;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.convertRecordFields;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.getIntValue;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.getMap;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.getStringValue;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.getValidName;

/**
 * HPA annotation processor.
 */
public class HPAAnnotationProcessor extends AbstractAnnotationProcessor {

    @Override
    public void processAnnotation(ServiceNode serviceNode, AnnotationAttachmentNode attachmentNode) throws
            KubernetesPluginException {
        processHPA(attachmentNode);
    }
    
    @Override
    public void processAnnotation(FunctionNode functionNode, AnnotationAttachmentNode attachmentNode) throws
            KubernetesPluginException {
        if (!MAIN_FUNCTION_NAME.equals(functionNode.getName().getValue())) {
            throw new KubernetesPluginException("@kubernetes:HPA{} annotation must be attached to a main " +
                                                "function.");
        }
        
        processHPA(attachmentNode);
    }
    
    private void processHPA(AnnotationAttachmentNode attachmentNode) throws KubernetesPluginException {
        PodAutoscalerModel podAutoscalerModel = new PodAutoscalerModel();
        List<BLangRecordLiteral.BLangRecordKeyValueField> keyValues =
            convertRecordFields(((BLangRecordLiteral) ((BLangAnnotationAttachment) attachmentNode).expr).getFields());
        for (BLangRecordLiteral.BLangRecordKeyValueField keyValue : keyValues) {
            PodAutoscalerConfiguration podAutoscalerConfiguration =
                    PodAutoscalerConfiguration.valueOf(keyValue.getKey().toString());
            switch (podAutoscalerConfiguration) {
                case name:
                    podAutoscalerModel.setName(getValidName(getStringValue(keyValue.getValue())));
                    break;
                case labels:
                    podAutoscalerModel.setLabels(getMap(keyValue.getValue()));
                    break;
                case annotations:
                    podAutoscalerModel.setAnnotations(getMap(keyValue.getValue()));
                    break;
                case cpuPercentage:
                    podAutoscalerModel.setCpuPercentage(getIntValue(keyValue.getValue()));
                    break;
                case minReplicas:
                    podAutoscalerModel.setMinReplicas(getIntValue(keyValue.getValue()));
                    break;
                case maxReplicas:
                    podAutoscalerModel.setMaxReplicas(getIntValue(keyValue.getValue()));
                    break;
                default:
                    break;
            }
        }
        KubernetesContext.getInstance().getDataHolder().setPodAutoscalerModel(podAutoscalerModel);
    }

    /**
     * Enum class for pod autoscaler configurations.
     */
    private enum PodAutoscalerConfiguration {
        name,
        labels,
        annotations,
        minReplicas,
        maxReplicas,
        cpuPercentage
    }
}
