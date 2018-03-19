package org.ballerinax.kubernetes.handlers;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.SecretBuilder;
import io.fabric8.kubernetes.client.internal.SerializationUtils;
import org.ballerinax.kubernetes.exceptions.KubernetesPluginException;
import org.ballerinax.kubernetes.models.SecretModel;

/**
 * Generates kubernetes secret.
 */
public class SecretHandler implements ArtifactHandler {

    SecretModel secretModel;

    public SecretHandler(SecretModel secretModel) {
        this.secretModel = secretModel;

    }

    @Override
    public String generate() throws KubernetesPluginException {
        Secret secret = new SecretBuilder()
                .withNewMetadata()
                .withName(secretModel.getName())
                .endMetadata()
                .withData(secretModel.getData())
                .build();
        try {
            return SerializationUtils.dumpWithoutRuntimeStateAsYaml(secret);
        } catch (JsonProcessingException e) {
            String errorMessage = "Error while parsing yaml file for secret: " + secretModel.getName();
            throw new KubernetesPluginException(errorMessage, e);
        }
    }
}
