package org.ballerinax.kubernetes.processors;

import org.apache.commons.codec.binary.Base64;
import org.ballerinalang.model.tree.AnnotationAttachmentNode;
import org.ballerinalang.model.tree.EndpointNode;
import org.ballerinalang.model.tree.ServiceNode;
import org.ballerinax.kubernetes.exceptions.KubernetesPluginException;
import org.ballerinax.kubernetes.models.IngressModel;
import org.ballerinax.kubernetes.models.KubernetesDataHolder;
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

import static org.ballerinax.kubernetes.KubernetesConstants.INGRESS_HOSTNAME_POSTFIX;
import static org.ballerinax.kubernetes.KubernetesConstants.INGRESS_POSTFIX;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.getMap;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.getValidName;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.isEmpty;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.resolveValue;

/**
 * Ingress annotation processor.
 */
public class IngressAnnotationProcessor implements AnnotationProcessor {


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

    @Override
    public void processAnnotation(EndpointNode endpointNode, AnnotationAttachmentNode attachmentNode) throws
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
        String endpointName = endpointNode.getName().getValue();
        if (isEmpty(ingressModel.getName())) {
            ingressModel.setName(getValidName(endpointName) + INGRESS_POSTFIX);
        }
        if (isEmpty(ingressModel.getHostname())) {
            ingressModel.setHostname(getValidName(endpointName) + INGRESS_HOSTNAME_POSTFIX);
        }
        ingressModel.setEndpointName(endpointName);
        List<BLangRecordLiteral.BLangRecordKeyValue> endpointConfig =
                ((BLangRecordLiteral) ((BLangEndpoint) endpointNode).configurationExpr).getKeyValuePairs();
        for (BLangRecordLiteral.BLangRecordKeyValue keyValue : endpointConfig) {
            String key = keyValue.getKey().toString();
            switch (key) {
                case "secureSocket":
                    List<BLangRecordLiteral.BLangRecordKeyValue> sslKeyValues = ((BLangRecordLiteral) keyValue
                            .valueExpr).getKeyValuePairs();
                    Set<SecretModel> secretModels = processSecureSocketAnnotation(endpointName, sslKeyValues);
                    KubernetesDataHolder.getInstance().addEndpointSecret(endpointName, secretModels);
                    KubernetesDataHolder.getInstance().addSecrets(secretModels);
                    break;
                default:
                    break;

            }
        }
        KubernetesDataHolder.getInstance().addIngressModel(ingressModel);
    }

    @Override
    public void processAnnotation(ServiceNode serviceNode, AnnotationAttachmentNode attachmentNode) throws
            KubernetesPluginException {
        throw new UnsupportedOperationException();
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
            mountPath = mountPath.replace("${ballerina.home}", "/ballerina/runtime");
        }
        return String.valueOf(Paths.get(mountPath).getParent());
    }

    private String extractFilePath(BLangRecordLiteral.BLangRecordKeyValue keyValue) {
        List<BLangRecordLiteral.BLangRecordKeyValue> keyStoreConfigs = ((BLangRecordLiteral) keyValue
                .valueExpr).getKeyValuePairs();
        for (BLangRecordLiteral.BLangRecordKeyValue keyStoreConfig : keyStoreConfigs) {
            String configKey = keyStoreConfig.getKey().toString();
            if ("filePath".equals(configKey)) {
                return keyStoreConfig.getValue().toString();
            }
        }
        return null;
    }
}

