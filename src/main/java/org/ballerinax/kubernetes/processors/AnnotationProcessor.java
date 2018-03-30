package org.ballerinax.kubernetes.processors;

import org.ballerinalang.model.tree.AnnotatableNode;
import org.ballerinalang.model.tree.AnnotationAttachmentNode;
import org.ballerinax.kubernetes.exceptions.KubernetesPluginException;
import org.ballerinax.kubernetes.models.KubernetesModel;

import java.util.Set;

/**
 * Annotation processor interface.
 */
public interface AnnotationProcessor {
    /**
     * Process annotations and create model object.
     *
     * @param entityName node of the attachment
     * @param attachmentNode annotation attachment node.
     * @throws KubernetesPluginException if an error occurs while processing annotation
     */
    void processAnnotation(String entityName, AnnotationAttachmentNode attachmentNode)
            throws KubernetesPluginException;
}
