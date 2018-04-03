package org.ballerinax.kubernetes.processors;

import org.ballerinalang.model.tree.AnnotationAttachmentNode;
import org.ballerinalang.model.tree.EndpointNode;
import org.ballerinalang.model.tree.ServiceNode;
import org.ballerinax.kubernetes.exceptions.KubernetesPluginException;

/**
 * Annotation processor interface.
 */
public interface AnnotationProcessor {
    /**
     * Process annotations and create model object.
     *
     * @param serviceNode    Ballerina Service node
     * @param attachmentNode annotation attachment node.
     * @throws KubernetesPluginException if an error occurs while processing annotation.
     */
    void processAnnotation(ServiceNode serviceNode, AnnotationAttachmentNode attachmentNode)
            throws KubernetesPluginException;

    /**
     * Process annotations and create model object.
     *
     * @param endpointNode   Ballerina endpoint node
     * @param attachmentNode annotation attachment node.
     * @throws KubernetesPluginException if an error occurs while processing annotation.
     */
    void processAnnotation(EndpointNode endpointNode, AnnotationAttachmentNode attachmentNode)
            throws KubernetesPluginException;
}
