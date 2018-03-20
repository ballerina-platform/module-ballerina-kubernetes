package org.ballerinax.kubernetes.handlers;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ConfigMapBuilder;
import io.fabric8.kubernetes.client.internal.SerializationUtils;
import org.ballerinax.kubernetes.exceptions.KubernetesPluginException;
import org.ballerinax.kubernetes.models.ConfigMapModel;

/**
 * Generates kubernetes Config Map.
 */
public class ConfigMapHandler implements ArtifactHandler {

    ConfigMapModel configMapModel;

    public ConfigMapHandler(ConfigMapModel configMapModel) {
        this.configMapModel = configMapModel;

    }

    @Override
    public String generate() throws KubernetesPluginException {
        ConfigMap configMap = new ConfigMapBuilder()
                .withNewMetadata()
                .withName(configMapModel.getName())
                .endMetadata()
                .withData(configMapModel.getData())
                .build();
        try {
            return SerializationUtils.dumpWithoutRuntimeStateAsYaml(configMap);
        } catch (JsonProcessingException e) {
            String errorMessage = "Error while parsing yaml file for config map: " + configMapModel.getName();
            throw new KubernetesPluginException(errorMessage, e);
        }
    }
}
