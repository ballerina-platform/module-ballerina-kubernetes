package org.ballerinax.kubernetes.processors;

import org.ballerinalang.model.tree.AnnotationAttachmentNode;
import org.ballerinalang.model.tree.EndpointNode;
import org.ballerinalang.model.tree.ServiceNode;
import org.ballerinax.kubernetes.exceptions.KubernetesPluginException;
import org.ballerinax.kubernetes.models.KubernetesDataHolder;
import org.ballerinax.kubernetes.models.PersistentVolumeClaimModel;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangArrayLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.ballerinax.kubernetes.utils.KubernetesUtils.getValidName;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.resolveValue;

/**
 * Persistent volume claim annotation processor.
 */
public class VolumeClaimAnnotationProcessor implements AnnotationProcessor {

    /**
     * Enum class for volume configurations.
     */
    private enum VolumeClaimConfig {
        name,
        mountPath,
        readOnly,
        accessMode,
        volumeClaimSize
    }

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
        KubernetesDataHolder.getInstance().addPersistentVolumeClaims(volumeClaimModels);
    }

    @Override
    public void processAnnotation(EndpointNode endpointNode, AnnotationAttachmentNode attachmentNode)
            throws KubernetesPluginException {
        throw new UnsupportedOperationException();
    }
}
