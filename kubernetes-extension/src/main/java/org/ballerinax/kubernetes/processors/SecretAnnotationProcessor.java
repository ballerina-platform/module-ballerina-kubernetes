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
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangListConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.ballerinax.kubernetes.KubernetesConstants.BALLERINA_CONF_FILE_NAME;
import static org.ballerinax.kubernetes.KubernetesConstants.BALLERINA_CONF_MOUNT_PATH;
import static org.ballerinax.kubernetes.KubernetesConstants.BALLERINA_HOME;
import static org.ballerinax.kubernetes.KubernetesConstants.BALLERINA_RUNTIME;
import static org.ballerinax.kubernetes.KubernetesConstants.MAIN_FUNCTION_NAME;
import static org.ballerinax.kubernetes.KubernetesConstants.SECRET_POSTFIX;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.convertRecordFields;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.getBooleanValue;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.getIntValue;
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
        List<BLangRecordLiteral.BLangRecordKeyValueField> keyValues =
                convertRecordFields(((BLangRecordLiteral) ((BLangAnnotationAttachment) attachmentNode).expr)
                        .getFields());
        for (BLangRecordLiteral.BLangRecordKeyValueField keyValue : keyValues) {
            String key = keyValue.getKey().toString();
            switch (key) {
                case "secrets":
                    List<BLangExpression> configAnnotation = ((BLangListConstructorExpr) keyValue.valueExpr).exprs;
                    for (BLangExpression bLangExpression : configAnnotation) {
                        SecretModel secretModel = new SecretModel();
                        List<BLangRecordLiteral.BLangRecordKeyValueField> annotationValues =
                                convertRecordFields(((BLangRecordLiteral) bLangExpression).getFields());
                        for (BLangRecordLiteral.BLangRecordKeyValueField annotation : annotationValues) {
                            SecretMountConfig volumeMountConfig =
                                    SecretMountConfig.valueOf(annotation.getKey().toString());
                            switch (volumeMountConfig) {
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
                                    // validate mount path is not set to ballerina home or ballerina runtime
                                    final Path mountPath = Paths.get(getStringValue(annotation.getValue()));
                                    final Path homePath = Paths.get(BALLERINA_HOME);
                                    final Path runtimePath = Paths.get(BALLERINA_RUNTIME);
                                    final Path confPath = Paths.get(BALLERINA_CONF_MOUNT_PATH);
                                    if (mountPath.equals(homePath)) {
                                        throw new KubernetesPluginException("@kubernetes:Secret{} mount path " +
                                                "cannot be ballerina home: " +
                                                BALLERINA_HOME);
                                    }
                                    if (mountPath.equals(runtimePath)) {
                                        throw new KubernetesPluginException("@kubernetes:Secret{} mount path " +
                                                "cannot be ballerina runtime: " +
                                                BALLERINA_RUNTIME);
                                    }
                                    if (mountPath.equals(confPath)) {
                                        throw new KubernetesPluginException("@kubernetes:Secret{} mount path " +
                                                "cannot be ballerina conf file mount " +
                                                "path: " + BALLERINA_CONF_MOUNT_PATH);
                                    }
                                    secretModel.setMountPath(getStringValue(annotation.getValue()));
                                    break;
                                case data:
                                    List<BLangExpression> data =
                                            ((BLangListConstructorExpr) annotation.valueExpr).exprs;
                                    secretModel.setData(getDataForSecret(data));
                                    break;
                                case readOnly:
                                    secretModel.setReadOnly(getBooleanValue(annotation.getValue()));
                                    break;
                                case defaultMode:
                                    secretModel.setDefaultMode(getIntValue(annotation.getValue()));
                                    break;
                                default:
                                    break;
                            }
                        }
                        if (isBlank(secretModel.getName())) {
                            secretModel.setName(getValidName(nodeID.getValue()) + SECRET_POSTFIX);
                        }
                        if (secretModel.getData() != null && secretModel.getData().size() > 0) {
                            secrets.add(secretModel);
                        }
                    }
                    break;
                case "conf":
                    //create a new secret model with ballerina conf and add it to data holder.
                    secrets.add(getBallerinaConfSecret(keyValue.getValue().toString(), nodeID.getValue()));
                    break;
                default:
                    break;

            }
        }
        KubernetesContext.getInstance().getDataHolder().addSecrets(secrets);
    }

    private SecretModel getBallerinaConfSecret(String configFilePath, String serviceName) throws
            KubernetesPluginException {
        //create a new config map model with ballerina conf
        SecretModel secretModel = new SecretModel();
        secretModel.setName(getValidName(serviceName) + "-ballerina-conf" + SECRET_POSTFIX);
        secretModel.setMountPath(BALLERINA_CONF_MOUNT_PATH);
        Path dataFilePath = Paths.get(configFilePath);
        if (!dataFilePath.isAbsolute()) {
            dataFilePath = KubernetesContext.getInstance().getDataHolder().getSourceRoot().resolve(dataFilePath)
                    .normalize();
        }
        String content = Base64.encodeBase64String(KubernetesUtils.readFileContent(dataFilePath));
        Map<String, String> dataMap = new HashMap<>();
        dataMap.put(BALLERINA_CONF_FILE_NAME, content);
        secretModel.setData(dataMap);
        secretModel.setBallerinaConf(configFilePath);
        secretModel.setReadOnly(false);
        return secretModel;
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
        data,
        conf,
        defaultMode
    }
}
