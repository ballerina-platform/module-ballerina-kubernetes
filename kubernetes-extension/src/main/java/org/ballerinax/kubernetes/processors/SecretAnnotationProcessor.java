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

import org.apache.commons.codec.binary.Base64;
import org.ballerinalang.model.tree.AnnotationAttachmentNode;
import org.ballerinalang.model.tree.FunctionNode;
import org.ballerinalang.model.tree.IdentifierNode;
import org.ballerinalang.model.tree.ServiceNode;
import org.ballerinax.kubernetes.exceptions.KubernetesPluginException;
import org.ballerinax.kubernetes.models.KubernetesContext;
import org.ballerinax.kubernetes.models.SecretModel;
import org.ballerinax.kubernetes.utils.KubernetesUtils;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangArrayLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.ballerinax.kubernetes.KubernetesConstants.MAIN_FUNCTION_NAME;
import static org.ballerinax.kubernetes.KubernetesConstants.SECRET_POSTFIX;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.getBooleanValue;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.getMap;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.getStringValue;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.getValidName;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.isBlank;

/**
 * Secrets annotation processor.
 */
public class SecretAnnotationProcessor extends AbstractAnnotationProcessor {

    @Override
    public void processAnnotation(ServiceNode serviceNode, AnnotationAttachmentNode attachmentNode) throws
            KubernetesPluginException {
        processSecret(serviceNode.getName(), attachmentNode);
    }
    
    @Override
    public void processAnnotation(FunctionNode functionNode, AnnotationAttachmentNode attachmentNode) throws
            KubernetesPluginException {
        if (!MAIN_FUNCTION_NAME.equals(functionNode.getName().getValue())) {
            throw new KubernetesPluginException("@kubernetes:Secret{} annotation cannot be attached to a non main " +
                                                "function.");
        }
        
        processSecret(functionNode.getName(), attachmentNode);
    }
    
    private void processSecret(IdentifierNode nodeID, AnnotationAttachmentNode attachmentNode) throws
            KubernetesPluginException {
        Set<SecretModel> secrets = new HashSet<>();
        List<BLangRecordLiteral.BLangRecordKeyValue> keyValues =
                ((BLangRecordLiteral) ((BLangAnnotationAttachment) attachmentNode).expr).getKeyValuePairs();
        for (BLangRecordLiteral.BLangRecordKeyValue keyValue : keyValues) {
            List<BLangExpression> secretAnnotation = ((BLangArrayLiteral) keyValue.valueExpr).exprs;
            for (BLangExpression bLangExpression : secretAnnotation) {
                SecretModel secretModel = new SecretModel();
                List<BLangRecordLiteral.BLangRecordKeyValue> annotationValues =
                        ((BLangRecordLiteral) bLangExpression).getKeyValuePairs();
                for (BLangRecordLiteral.BLangRecordKeyValue annotation : annotationValues) {
                    SecretMountConfig secretMountConfig =
                            SecretMountConfig.valueOf(annotation.getKey().toString());
                    switch (secretMountConfig) {
                        case name:
                            secretModel.setName(getValidName(getStringValue(annotation.getValue())));
                            break;
                        case labels:
                            secretModel.setLabels(getMap(keyValue.getValue()));
                            break;
                        case annotations:
                            secretModel.setAnnotations(getMap(keyValue.getValue()));
                            break;
                        case mountPath:
                            secretModel.setMountPath(getStringValue(annotation.getValue()));
                            break;
                        case data:
                            List<BLangExpression> data = ((BLangArrayLiteral) annotation.valueExpr).exprs;
                            secretModel.setData(getDataForSecret(data));
                            break;
                        case readOnly:
                            secretModel.setReadOnly(getBooleanValue(annotation.getValue()));
                            break;
                        default:
                            break;
                    }
                }
                if (isBlank(secretModel.getName())) {
                    secretModel.setName(getValidName(nodeID.getValue()) + SECRET_POSTFIX);
                }
                secrets.add(secretModel);
            }
        }
        KubernetesContext.getInstance().getDataHolder().addSecrets(secrets);
    }

    private Map<String, String> getDataForSecret(List<BLangExpression> data) throws KubernetesPluginException {
        Map<String, String> dataMap = new HashMap<>();
        for (BLangExpression bLangExpression : data) {
            Path dataFilePath = Paths.get(getStringValue(bLangExpression));
            String key = String.valueOf(dataFilePath.getFileName());
            String content = Base64.encodeBase64String(KubernetesUtils.readFileContent(dataFilePath));
            dataMap.put(key, content);
        }
        return dataMap;
    }

    /**
     * Enum class for volume configurations.
     */
    private enum SecretMountConfig {
        name,
        labels,
        annotations,
        mountPath,
        readOnly,
        data
    }
}
