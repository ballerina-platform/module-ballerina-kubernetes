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
import org.ballerinalang.model.tree.EndpointNode;
import org.ballerinalang.model.tree.ServiceNode;
import org.ballerinalang.model.tree.expressions.RecordLiteralNode;
import org.ballerinax.kubernetes.exceptions.KubernetesPluginException;
import org.ballerinax.kubernetes.models.IngressModel;
import org.ballerinax.kubernetes.models.KubernetesContext;
import org.ballerinax.kubernetes.models.SecretModel;
import org.ballerinax.kubernetes.utils.KubernetesUtils;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.BLangEndpoint;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.ballerinax.kubernetes.KubernetesConstants.ANONYMOUS_POSTFIX;
import static org.ballerinax.kubernetes.KubernetesConstants.BALLERINA_RUNTIME;
import static org.ballerinax.kubernetes.KubernetesConstants.ENDPOINT_PATH_VARIABLE;
import static org.ballerinax.kubernetes.KubernetesConstants.INGRESS_HOSTNAME_POSTFIX;
import static org.ballerinax.kubernetes.KubernetesConstants.INGRESS_POSTFIX;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.getMap;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.getValidName;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.isBlank;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.resolveValue;

/**
 * Ingress annotation processor.
 */
public class IngressAnnotationProcessor extends AbstractAnnotationProcessor {

    @Override
    public void processAnnotation(EndpointNode endpointNode, AnnotationAttachmentNode attachmentNode) throws
            KubernetesPluginException {
        IngressModel ingressModel = getIngressModelFromAnnotation(attachmentNode);
        String endpointName = endpointNode.getName().getValue();
        if (isBlank(ingressModel.getName())) {
            ingressModel.setName(getValidName(endpointName) + INGRESS_POSTFIX);
        }
        if (isBlank(ingressModel.getHostname())) {
            ingressModel.setHostname(getValidName(endpointName) + INGRESS_HOSTNAME_POSTFIX);
        }
        ingressModel.setEndpointName(endpointName);
        List<BLangRecordLiteral.BLangRecordKeyValue> endpointConfig =
                ((BLangRecordLiteral) ((BLangEndpoint) endpointNode).configurationExpr).getKeyValuePairs();
        processEndpoint(endpointName, endpointConfig);
        KubernetesContext.getInstance().getDataHolder().addIngressModel(ingressModel);
    }

    /**
     * Extract key-store/trust-store file location from endpoint.
     *
     * @param endpointName          Endpoint name
     * @param secureSocketKeyValues secureSocket annotation struct
     * @return List of @{@link SecretModel} objects
     */
    private Set<SecretModel> processSecureSocketAnnotation(String endpointName, List<BLangRecordLiteral
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
                secretModel.setName(getValidName(endpointName) + "-secure-socket");
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
            secretModel.setName(getValidName(endpointName) + "-keystore");
            secretModel.setMountPath(getMountPath(keyStoreFile));
            Map<String, String> dataMap = new HashMap<>();
            dataMap.put(String.valueOf(Paths.get(keyStoreFile).getFileName()), keyStoreContent);
            secretModel.setData(dataMap);
            secrets.add(secretModel);
        }
        if (trustStoreFile != null) {
            String trustStoreContent = readSecretFile(trustStoreFile);
            SecretModel secretModel = new SecretModel();
            secretModel.setName(getValidName(endpointName) + "-truststore");
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
            if (ENDPOINT_PATH_VARIABLE.equals(configKey)) {
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

    private void processEndpoint(String endpointName, List<BLangRecordLiteral.BLangRecordKeyValue> endpointConfig)
            throws KubernetesPluginException {
        for (BLangRecordLiteral.BLangRecordKeyValue keyValue : endpointConfig) {
            String key = keyValue.getKey().toString();
            switch (key) {
                case "secureSocket":
                    List<BLangRecordLiteral.BLangRecordKeyValue> sslKeyValues = ((BLangRecordLiteral) keyValue
                            .valueExpr).getKeyValuePairs();
                    Set<SecretModel> secretModels = processSecureSocketAnnotation(endpointName, sslKeyValues);
                    KubernetesContext.getInstance().getDataHolder().addEndpointSecret(endpointName, secretModels);
                    KubernetesContext.getInstance().getDataHolder().addSecrets(secretModels);
                    break;
                default:
                    break;

            }
        }
    }

    @Override
    public void processAnnotation(ServiceNode serviceNode, AnnotationAttachmentNode attachmentNode) throws
            KubernetesPluginException {
        RecordLiteralNode anonymousEndpoint = serviceNode.getAnonymousEndpointBind();
        if (anonymousEndpoint == null) {
            throw new KubernetesPluginException("Adding @kubernetes:Ingress{} annotation to a service is only " +
                    "supported when service is bind to an anonymous endpoint");
        }
        IngressModel ingressModel = getIngressModelFromAnnotation(attachmentNode);

        //processing anonymous endpoint
        String endpointName = serviceNode.getName().getValue();
        if (isBlank(ingressModel.getName())) {
            ingressModel.setName(getValidName(endpointName) + ANONYMOUS_POSTFIX + INGRESS_POSTFIX);
        }
        if (isBlank(ingressModel.getHostname())) {
            ingressModel.setHostname(getValidName(endpointName) + INGRESS_HOSTNAME_POSTFIX);
        }
        ingressModel.setEndpointName(endpointName);
        List<BLangRecordLiteral.BLangRecordKeyValue> endpointConfig =
                ((BLangRecordLiteral) anonymousEndpoint).getKeyValuePairs();
        processEndpoint(endpointName, endpointConfig);
        KubernetesContext.getInstance().getDataHolder().addIngressModel(ingressModel);

    }

    /**
     * Enum  for ingress configurations.
     */
    private enum IngressConfiguration {
        name,
        labels,
        annotations,
        hostname,
        path,
        targetPath,
        ingressClass,
        enableTLS,
    }
}

