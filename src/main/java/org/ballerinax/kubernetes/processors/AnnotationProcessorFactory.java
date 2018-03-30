package org.ballerinax.kubernetes.processors;

import org.ballerinax.kubernetes.exceptions.KubernetesPluginException;

public class AnnotationProcessorFactory {

    public static AnnotationProcessor getAnnotationProcessorInstance(String type) throws KubernetesPluginException {
        switch (type) {
            case "Ingress":
                return new IngressAnnotationProcessor();
            case "HPA":
                return new HPAAnnotationProcessor();
            case "Deployment":
                return new DeploymentAnnotationProcessor();
            case "Secret":
                return new SecretAnnotationProcessor();
            case "ConfigMap":
                return new ConfigMapAnnotationProcessor();
            case "PersistentVolumeClaim":
                return new VolumeClaimAnnotationProcessor();
            default:
                throw new KubernetesPluginException("Error while getting annotation processor for" + type);
        }
    }
}
