package org.ballerinax.kubernetes.processors;

import org.ballerinalang.model.tree.AnnotationAttachmentNode;
import org.ballerinax.kubernetes.exceptions.KubernetesPluginException;
import org.ballerinax.kubernetes.models.IngressModel;
import org.ballerinax.kubernetes.models.KubernetesModel;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.ballerinax.kubernetes.KubernetesConstants.INGRESS_HOSTNAME_POSTFIX;
import static org.ballerinax.kubernetes.KubernetesConstants.INGRESS_POSTFIX;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.getMap;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.getValidName;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.resolveValue;

/**
 * Ingress annotation processor.
 */
public class IngressAnnotationProcessor implements AnnotationProcessor {

    /**
     * Enum  for svc configurations.
     */
    private enum IngressConfiguration {
        name,
        labels,
        hostname,
        path,
        targetPath,
        ingressClass,
        enableTLS
    }


    /**
     * Process annotations and create Ingress model object.
     *
     * @param entityName     Ballerina service name
     * @param attachmentNode annotation attachment node.
     * @return Ingress model object
     */
    public Set<KubernetesModel> processAnnotation(String entityName, AnnotationAttachmentNode attachmentNode) throws
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
                case path:
                    ingressModel.setPath(annotationValue);
                    break;
                case targetPath:
                    ingressModel.setTargetPath(annotationValue);
                    break;
                case hostname:
                    ingressModel.setHostname(annotationValue);
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
        if (ingressModel.getName() == null || ingressModel.getName().length() == 0) {
            ingressModel.setName(getValidName(entityName) + INGRESS_POSTFIX);
        }
        if (ingressModel.getHostname() == null || ingressModel.getHostname().length() == 0) {
            ingressModel.setHostname(getValidName(entityName) + INGRESS_HOSTNAME_POSTFIX);
        }
        Set<KubernetesModel> models = new HashSet<>();
        models.add(ingressModel);
        return models;
    }
}
