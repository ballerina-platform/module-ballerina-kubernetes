package org.ballerinax.kubernetes.processors;

import org.ballerinalang.model.tree.AnnotationAttachmentNode;
import org.ballerinax.kubernetes.exceptions.KubernetesPluginException;
import org.ballerinax.kubernetes.models.KubernetesModel;
import org.ballerinax.kubernetes.models.ServiceModel;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.ballerinax.kubernetes.KubernetesConstants.SVC_POSTFIX;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.getMap;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.getValidName;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.resolveValue;

/**
 * Service annotation processor.
 */
public class ServiceAnnotationProcessor implements AnnotationProcessor {

    /**
     * Enum for Service configurations.
     */
    private enum ServiceConfiguration {
        name,
        labels,
        serviceType,
        port
    }


    /**
     * Process annotations and create service model object.
     *
     * @param entityName     ballerina service name
     * @param attachmentNode annotation attachment node.
     * @return Service model object
     */
    public Set<KubernetesModel> processAnnotation(String entityName, AnnotationAttachmentNode attachmentNode) throws
            KubernetesPluginException {
        ServiceModel serviceModel = new ServiceModel();
        List<BLangRecordLiteral.BLangRecordKeyValue> keyValues =
                ((BLangRecordLiteral) ((BLangAnnotationAttachment) attachmentNode).expr).getKeyValuePairs();
        for (BLangRecordLiteral.BLangRecordKeyValue keyValue : keyValues) {
            ServiceConfiguration serviceConfiguration =
                    ServiceConfiguration.valueOf(keyValue.getKey().toString());
            String annotationValue = resolveValue(keyValue.getValue().toString());
            switch (serviceConfiguration) {
                case name:
                    serviceModel.setName(getValidName(annotationValue));
                    break;
                case labels:
                    serviceModel.setLabels(getMap(((BLangRecordLiteral) keyValue.valueExpr).keyValuePairs));
                    break;
                case serviceType:
                    serviceModel.setServiceType(annotationValue);
                    break;
                case port:
                    serviceModel.setPort(Integer.parseInt(annotationValue));
                    break;
                default:
                    break;
            }
        }
        if (serviceModel.getName() == null) {
            serviceModel.setName(getValidName(entityName) + SVC_POSTFIX);
        }
        Set<KubernetesModel> models = new HashSet<>();
        models.add(serviceModel);
        return models;
    }
}
