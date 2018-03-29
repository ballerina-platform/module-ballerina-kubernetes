package org.ballerinax.kubernetes.processors;

import org.apache.commons.codec.binary.Base64;
import org.ballerinalang.model.tree.AnnotationAttachmentNode;
import org.ballerinax.kubernetes.exceptions.KubernetesPluginException;
import org.ballerinax.kubernetes.models.KubernetesModel;
import org.ballerinax.kubernetes.models.SecretModel;
import org.ballerinax.kubernetes.utils.KubernetesUtils;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangArrayLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;

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
 * Secrets annotation processor.
 */
public class SecretAnnotationProcessor implements AnnotationProcessor {

    /**
     * Enum class for volume configurations.
     */
    private enum SecretMountConfig {
        name,
        mountPath,
        readOnly,
        data
    }

    /**
     * Process Secrets annotations.
     *
     * @param attachmentNode Attachment Node
     * @return List of @{@link SecretModel} objects
     */
    public Set<KubernetesModel> processAnnotation(String entity, AnnotationAttachmentNode attachmentNode) throws
            KubernetesPluginException {
        Set<KubernetesModel> secrets = new HashSet<>();
        List<BLangRecordLiteral.BLangRecordKeyValue> keyValues =
                ((BLangRecordLiteral) ((BLangAnnotationAttachment) attachmentNode).expr).getKeyValuePairs();
        for (BLangRecordLiteral.BLangRecordKeyValue keyValue : keyValues) {
            List<BLangExpression> secretAnnotation = ((BLangArrayLiteral) keyValue.valueExpr).exprs;
            for (BLangExpression bLangExpression : secretAnnotation) {
                SecretModel secretModel = new SecretModel();
                List<BLangRecordLiteral.BLangRecordKeyValue> annotationValues =
                        ((BLangRecordLiteral) bLangExpression).getKeyValuePairs();
                for (BLangRecordLiteral.BLangRecordKeyValue annotation : annotationValues) {
                    SecretMountConfig volumeMountConfig =
                            SecretMountConfig.valueOf(annotation.getKey().toString());
                    String annotationValue = resolveValue(annotation.getValue().toString());
                    switch (volumeMountConfig) {
                        case name:
                            secretModel.setName(getValidName(annotationValue));
                            break;
                        case mountPath:
                            secretModel.setMountPath(annotationValue);
                            break;
                        case data:
                            List<BLangExpression> data = ((BLangArrayLiteral) annotation.valueExpr).exprs;
                            secretModel.setData(getDataForSecret(data));
                            break;
                        case readOnly:
                            secretModel.setReadOnly(Boolean.parseBoolean(annotationValue));
                            break;
                        default:
                            break;
                    }
                }
                secrets.add(secretModel);
            }
        }
        return secrets;
    }

    private Map<String, String> getDataForSecret(List<BLangExpression> data) throws KubernetesPluginException {
        Map<String, String> dataMap = new HashMap<>();
        for (BLangExpression bLangExpression : data) {
            Path dataFilePath = Paths.get(((BLangLiteral) bLangExpression).getValue().toString());
            String key = String.valueOf(dataFilePath.getFileName());
            String content = Base64.encodeBase64String(KubernetesUtils.readFileContent(dataFilePath));
            dataMap.put(key, content);
        }
        return dataMap;
    }
}
