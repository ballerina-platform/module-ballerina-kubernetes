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
import org.ballerinalang.model.tree.ServiceNode;
import org.ballerinalang.model.tree.SimpleVariableNode;
import org.ballerinax.kubernetes.exceptions.KubernetesPluginException;
import org.ballerinax.kubernetes.models.IngressModel;
import org.ballerinax.kubernetes.models.KubernetesContext;
import org.ballerinax.kubernetes.models.SecretModel;
import org.ballerinax.kubernetes.utils.KubernetesUtils;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangNamedArgsExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeInit;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.ballerinax.kubernetes.KubernetesConstants.ANONYMOUS_POSTFIX;
import static org.ballerinax.kubernetes.KubernetesConstants.BALLERINA_RUNTIME;
import static org.ballerinax.kubernetes.KubernetesConstants.INGRESS_HOSTNAME_POSTFIX;
import static org.ballerinax.kubernetes.KubernetesConstants.INGRESS_POSTFIX;
import static org.ballerinax.kubernetes.KubernetesConstants.LISTENER_PATH_VARIABLE;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.getMap;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.getValidName;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.isBlank;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.resolveValue;

/**
 * Ingress annotation processor.
 */
public class IngressAnnotationProcessor extends AbstractAnnotationProcessor {

    @Override
    public void processAnnotation(SimpleVariableNode variableNode, AnnotationAttachmentNode attachmentNode) throws
            KubernetesPluginException {
        IngressModel ingressModel = getIngressModelFromAnnotation(attachmentNode);
        String listenerName = variableNode.getName().getValue();
        if (isBlank(ingressModel.getName())) {
            ingressModel.setName(getValidName(listenerName) + INGRESS_POSTFIX);
        }
        if (isBlank(ingressModel.getHostname())) {
            ingressModel.setHostname(getValidName(listenerName) + INGRESS_HOSTNAME_POSTFIX);
        }
        ingressModel.setListenerName(listenerName);
    
        BLangTypeInit bListener = (BLangTypeInit) ((BLangSimpleVariable) variableNode).expr;
        if (bListener.argsExpr.size() == 2) {
            if (bListener.argsExpr.get(1) instanceof BLangNamedArgsExpression) {
                BLangNamedArgsExpression configArg = (BLangNamedArgsExpression) bListener.argsExpr.get(1);
                BLangRecordLiteral bConfigRecordLiteral = (BLangRecordLiteral) configArg.expr;
                List<BLangRecordLiteral.BLangRecordKeyValue> listenerConfig = bConfigRecordLiteral.getKeyValuePairs();
                processListener(listenerName, listenerConfig);
            }
        }
        
        KubernetesContext.getInstance().getDataHolder().addIngressModel(ingressModel);
    }

    /**
     * Extract key-store/trust-store file location from listener.
     *
     * @param listenerName          Listener name
     * @param secureSocketKeyValues secureSocket annotation struct
     * @return List of @{@link SecretModel} objects
     */
    private Set<SecretModel> processSecureSocketAnnotation(String listenerName, List<BLangRecordLiteral
            .BLangRecordKeyValue> secureSocketKeyValues) throws KubernetesPluginException {
        Set<SecretModel> secrets = new HashSet<>();
        String keyStoreFile = null;
        String trustStoreFile = null;
        for (BLangRecordLiteral.BLangRecordKeyValue keyValue : secureSocketKeyValues) {
            //extract file paths.
            String key = keyValue.getKey().toString();
            if ("keyStore".equals(key)) {
                keyStoreFile = extractFilePath(keyValue);
            } else if ("trustStore".equals(key)) {
                trustStoreFile = extractFilePath(keyValue);
            }
        }
        if (keyStoreFile != null && trustStoreFile != null) {
            if (getMountPath(keyStoreFile).equals(getMountPath(trustStoreFile))) {
                // trust-store and key-store mount to same path
                String keyStoreContent = readSecretFile(keyStoreFile);
                String trustStoreContent = readSecretFile(trustStoreFile);
                SecretModel secretModel = new SecretModel();
                secretModel.setName(getValidName(listenerName) + "-secure-socket");
                secretModel.setMountPath(getMountPath(keyStoreFile));
                Map<String, String> dataMap = new HashMap<>();
                dataMap.put(String.valueOf(Paths.get(keyStoreFile).getFileName()), keyStoreContent);
                dataMap.put(String.valueOf(Paths.get(trustStoreFile).getFileName()), trustStoreContent);
                secretModel.setData(dataMap);
                secrets.add(secretModel);
                return secrets;
            }
        }
        if (keyStoreFile != null) {
            String keyStoreContent = readSecretFile(keyStoreFile);
            SecretModel secretModel = new SecretModel();
            secretModel.setName(getValidName(listenerName) + "-keystore");
            secretModel.setMountPath(getMountPath(keyStoreFile));
            Map<String, String> dataMap = new HashMap<>();
            dataMap.put(String.valueOf(Paths.get(keyStoreFile).getFileName()), keyStoreContent);
            secretModel.setData(dataMap);
            secrets.add(secretModel);
        }
        if (trustStoreFile != null) {
            String trustStoreContent = readSecretFile(trustStoreFile);
            SecretModel secretModel = new SecretModel();
            secretModel.setName(getValidName(listenerName) + "-truststore");
            secretModel.setMountPath(getMountPath(trustStoreFile));
            Map<String, String> dataMap = new HashMap<>();
            dataMap.put(String.valueOf(Paths.get(trustStoreFile).getFileName()), trustStoreContent);
            secretModel.setData(dataMap);
            secrets.add(secretModel);
        }
        return secrets;
    }

    private String readSecretFile(String filePath) throws KubernetesPluginException {
        if (filePath.contains("${ballerina.home}")) {
            // Resolve variable locally before reading file.
            String ballerinaHome = System.getProperty("ballerina.home");
            filePath = filePath.replace("${ballerina.home}", ballerinaHome);
        }
        Path dataFilePath = Paths.get(filePath);
        return Base64.encodeBase64String(KubernetesUtils.readFileContent(dataFilePath));
    }

    private String getMountPath(String mountPath) {
        if (mountPath.contains("${ballerina.home}")) {
            // replace mount path with container's ballerina.home
            mountPath = mountPath.replace("${ballerina.home}", BALLERINA_RUNTIME);
        }
        return String.valueOf(Paths.get(mountPath).getParent());
    }

    private String extractFilePath(BLangRecordLiteral.BLangRecordKeyValue keyValue) {
        List<BLangRecordLiteral.BLangRecordKeyValue> keyStoreConfigs = ((BLangRecordLiteral) keyValue
                .valueExpr).getKeyValuePairs();
        for (BLangRecordLiteral.BLangRecordKeyValue keyStoreConfig : keyStoreConfigs) {
            String configKey = keyStoreConfig.getKey().toString();
            if (LISTENER_PATH_VARIABLE.equals(configKey)) {
                return keyStoreConfig.getValue().toString();
            }
        }
        return null;
    }

    private IngressModel getIngressModelFromAnnotation(AnnotationAttachmentNode attachmentNode) throws
            KubernetesPluginException {
        IngressModel ingressModel = new IngressModel();
        List<BLangRecordLiteral.BLangRecordKeyValue> keyValues =
                ((BLangRecordLiteral) ((BLangAnnotationAttachment) attachmentNode).expr).getKeyValuePairs();
        for (BLangRecordLiteral.BLangRecordKeyValue keyValue : keyValues) {
            IngressConfiguration ingressConfiguration =
                    IngressConfiguration.valueOf(keyValue.getKey().toString());
            String annotationValue = resolveValue(keyValue.getValue().toString());
            switch (ingressConfiguration) {
                case name:
                    ingressModel.setName(getValidName(annotationValue));
                    break;
                case namespace:
                    ingressModel.setNamespace(getValidName(annotationValue));
                    break;
                case labels:
                    ingressModel.setLabels(getMap(((BLangRecordLiteral) keyValue.valueExpr).keyValuePairs));
                    break;
                case annotations:
                    ingressModel.setAnnotations(getMap(((BLangRecordLiteral) keyValue.valueExpr)
                            .keyValuePairs));
                    break;
                case hostname:
                    ingressModel.setHostname(annotationValue);
                    break;
                case path:
                    ingressModel.setPath(annotationValue);
                    break;
                case targetPath:
                    ingressModel.setTargetPath(annotationValue);
                    break;
                case ingressClass:
                    ingressModel.setIngressClass(annotationValue);
                    break;
                case enableTLS:
                    ingressModel.setEnableTLS(Boolean.parseBoolean(annotationValue));
                    break;
                default:
                    break;
            }
        }
        return ingressModel;
    }

    private void processListener(String listenerName, List<BLangRecordLiteral.BLangRecordKeyValue> listenerConfig)
            throws KubernetesPluginException {
        for (BLangRecordLiteral.BLangRecordKeyValue keyValue : listenerConfig) {
            String key = keyValue.getKey().toString();
            if ("secureSocket".equals(key)) {
                List<BLangRecordLiteral.BLangRecordKeyValue> sslKeyValues =
                        ((BLangRecordLiteral) keyValue.valueExpr).getKeyValuePairs();
                Set<SecretModel> secretModels = processSecureSocketAnnotation(listenerName, sslKeyValues);
                KubernetesContext.getInstance().getDataHolder().addListenerSecret(listenerName, secretModels);
                KubernetesContext.getInstance().getDataHolder().addSecrets(secretModels);
            }
        }
    }

    @Override
    public void processAnnotation(ServiceNode serviceNode, AnnotationAttachmentNode attachmentNode) throws
            KubernetesPluginException {
        BLangService bService = (BLangService) serviceNode;
        if (bService.attachExpr instanceof BLangTypeInit) {
            throw new KubernetesPluginException("Adding @kubernetes:Ingress{} annotation to a service is only " +
                    "supported when service is bind to an anonymous listener");
        }
        IngressModel ingressModel = getIngressModelFromAnnotation(attachmentNode);

        //processing anonymous listener
        String listenerName = serviceNode.getName().getValue();
        if (isBlank(ingressModel.getName())) {
            ingressModel.setName(getValidName(listenerName) + ANONYMOUS_POSTFIX + INGRESS_POSTFIX);
        }
        if (isBlank(ingressModel.getHostname())) {
            ingressModel.setHostname(getValidName(listenerName) + INGRESS_HOSTNAME_POSTFIX);
        }
        ingressModel.setListenerName(listenerName);
    
        BLangTypeInit bListener = (BLangTypeInit) bService.attachExpr;
        if (bListener.argsExpr.size() == 2) {
            if (bListener.argsExpr.get(1) instanceof BLangRecordLiteral) {
                BLangRecordLiteral bConfigRecordLiteral = (BLangRecordLiteral) bListener.argsExpr.get(1);
                List<BLangRecordLiteral.BLangRecordKeyValue> listenerConfig =
                        bConfigRecordLiteral.getKeyValuePairs();
                processListener(listenerName, listenerConfig);
            }
        }
        
        KubernetesContext.getInstance().getDataHolder().addIngressModel(ingressModel);

    }

    /**
     * Enum  for ingress configurations.
     */
    private enum IngressConfiguration {
        name,
        namespace,
        labels,
        annotations,
        hostname,
        path,
        targetPath,
        ingressClass,
        enableTLS,
    }
}

