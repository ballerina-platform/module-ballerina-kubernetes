package org.ballerinax.kubernetes.processors;

import org.ballerinalang.model.tree.AnnotationAttachmentNode;
import org.ballerinax.kubernetes.exceptions.KubernetesPluginException;
import org.ballerinax.kubernetes.models.ConfigMapModel;
import org.ballerinax.kubernetes.models.KubernetesModel;
import org.ballerinax.kubernetes.utils.KubernetesUtils;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangArrayLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.ballerinax.kubernetes.utils.KubernetesUtils.getValidName;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.resolveValue;

/**
 * ConfigMap annotation processor.
 */
public class ConfigMapAnnotationProcessor implements AnnotationProcessor {

    /**
     * Enum class for volume configurations.
     */
    private enum ConfigMapMountConfig {
        name,
        mountPath,
        readOnly,
        isBallerinaConf,
        data
    }

    /**
     * Process ConfigMap annotations.
     *
     * @param attachmentNode Attachment Node
     * @return Set of @{@link ConfigMapModel} objects
     */
    public Set<KubernetesModel> processAnnotation(String entity, AnnotationAttachmentNode attachmentNode) throws
            KubernetesPluginException {
        Set<KubernetesModel> configMapModels = new HashSet<>();
        List<BLangRecordLiteral.BLangRecordKeyValue> keyValues =
                ((BLangRecordLiteral) ((BLangAnnotationAttachment) attachmentNode).expr).getKeyValuePairs();
        for (BLangRecordLiteral.BLangRecordKeyValue keyValue : keyValues) {
            List<BLangExpression> configAnnotation = ((BLangArrayLiteral) keyValue.valueExpr).exprs;
            for (BLangExpression bLangExpression : configAnnotation) {
                ConfigMapModel configMapModel = new ConfigMapModel();
                List<BLangRecordLiteral.BLangRecordKeyValue> annotationValues =
                        ((BLangRecordLiteral) bLangExpression).getKeyValuePairs();
                for (BLangRecordLiteral.BLangRecordKeyValue annotation : annotationValues) {
                    ConfigMapMountConfig volumeMountConfig =
                            ConfigMapMountConfig.valueOf(annotation.getKey().toString());
                    String annotationValue = resolveValue(annotation.getValue().toString());
                    switch (volumeMountConfig) {
                        case name:
                            configMapModel.setName(getValidName(annotationValue));
                            break;
                        case mountPath:
                            configMapModel.setMountPath(annotationValue);
                            break;
                        case isBallerinaConf:
                            configMapModel.setBallerinaConf(Boolean.parseBoolean(annotationValue));
                            break;
                        case data:
                            List<BLangExpression> data = ((BLangArrayLiteral) annotation.valueExpr).exprs;
                            configMapModel.setData(getDataForConfigMap(data));
                            break;
                        case readOnly:
                            configMapModel.setReadOnly(Boolean.parseBoolean(annotationValue));
                            break;
                        default:
                            break;
                    }
                }
                configMapModels.add(configMapModel);
            }
        }
        return configMapModels;
    }

    private Map<String, String> getDataForConfigMap(List<BLangExpression> data) throws KubernetesPluginException {
        Map<String, String> dataMap = new HashMap<>();
        for (BLangExpression bLangExpression : data) {
            Path dataFilePath = Paths.get(((BLangLiteral) bLangExpression).getValue().toString());
            String key = String.valueOf(dataFilePath.getFileName());
            String content = new String(KubernetesUtils.readFileContent(dataFilePath), StandardCharsets.UTF_8);
            dataMap.put(key, content);
        }
        return dataMap;
    }
}
