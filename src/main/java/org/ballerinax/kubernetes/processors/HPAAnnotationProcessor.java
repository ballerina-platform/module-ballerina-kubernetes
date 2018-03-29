package org.ballerinax.kubernetes.processors;

import org.ballerinalang.model.tree.AnnotationAttachmentNode;
import org.ballerinax.kubernetes.exceptions.KubernetesPluginException;
import org.ballerinax.kubernetes.models.KubernetesModel;
import org.ballerinax.kubernetes.models.PodAutoscalerModel;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.ballerinax.kubernetes.utils.KubernetesUtils.getMap;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.getValidName;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.resolveValue;

/**
 * HPA annotation processor.
 */
public class HPAAnnotationProcessor implements AnnotationProcessor {

    /**
     * Enum class for pod autoscaler configurations.
     */
    private enum PodAutoscalerConfiguration {
        name,
        labels,
        minReplicas,
        maxReplicas,
        cpuPercentage
    }

    /**
     * Process annotations and create service model object.
     *
     * @param attachmentNode annotation attachment node.
     * @return Service model object
     */
    public Set<KubernetesModel> processAnnotation(String entityName, AnnotationAttachmentNode attachmentNode) throws
            KubernetesPluginException {
        PodAutoscalerModel podAutoscalerModel = new PodAutoscalerModel();
        List<BLangRecordLiteral.BLangRecordKeyValue> keyValues =
                ((BLangRecordLiteral) ((BLangAnnotationAttachment) attachmentNode).expr).getKeyValuePairs();
        for (BLangRecordLiteral.BLangRecordKeyValue keyValue : keyValues) {
            PodAutoscalerConfiguration podAutoscalerConfiguration =
                    PodAutoscalerConfiguration.valueOf(keyValue.getKey().toString());
            String annotationValue = resolveValue(keyValue.getValue().toString());
            switch (podAutoscalerConfiguration) {
                case name:
                    podAutoscalerModel.setName(getValidName(annotationValue));
                    break;
                case labels:
                    podAutoscalerModel.setLabels(getMap(((BLangRecordLiteral) keyValue.valueExpr).keyValuePairs));
                    break;
                case cpuPercentage:
                    podAutoscalerModel.setCpuPercentage(Integer.parseInt(annotationValue));
                    break;
                case minReplicas:
                    podAutoscalerModel.setMinReplicas(Integer.parseInt(annotationValue));
                    break;
                case maxReplicas:
                    podAutoscalerModel.setMaxReplicas(Integer.parseInt(annotationValue));
                    break;
                default:
                    break;
            }
        }
        Set<KubernetesModel> models = new HashSet<>();
        models.add(podAutoscalerModel);
        return models;
    }
}
