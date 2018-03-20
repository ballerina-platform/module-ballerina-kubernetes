package org.ballerinax.kubernetes.handlers;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaim;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaimBuilder;
import io.fabric8.kubernetes.api.model.Quantity;
import io.fabric8.kubernetes.api.model.QuantityBuilder;
import io.fabric8.kubernetes.client.internal.SerializationUtils;
import org.ballerinax.kubernetes.exceptions.KubernetesPluginException;
import org.ballerinax.kubernetes.models.PersistentVolumeClaimModel;

import java.util.HashMap;
import java.util.Map;

/**
 * Generates kubernetes secret.
 */
public class PersistentVolumeClaimHandler implements ArtifactHandler {

    PersistentVolumeClaimModel volumeClaimModel;

    public PersistentVolumeClaimHandler(PersistentVolumeClaimModel volumeClaimModel) {
        this.volumeClaimModel = volumeClaimModel;

    }

    @Override
    public String generate() throws KubernetesPluginException {

        Quantity quantity = new QuantityBuilder()
                .withAmount(volumeClaimModel.getVolumeClaimSize())
                .build();

        Map<String, Quantity> requests = new HashMap<>();
        requests.put("storage", quantity);
        PersistentVolumeClaim secret = new PersistentVolumeClaimBuilder()
                .withNewMetadata()
                .withName(volumeClaimModel.getName())
                .endMetadata()
                .withNewSpec()
                .withAccessModes(volumeClaimModel.getAccessMode())
                .withNewResources()
                .withRequests(requests)
                .endResources()
                .endSpec()
                .build();
        try {
            return SerializationUtils.dumpWithoutRuntimeStateAsYaml(secret);
        } catch (JsonProcessingException e) {
            String errorMessage = "Error while parsing yaml file for volume claim: " + volumeClaimModel.getName();
            throw new KubernetesPluginException(errorMessage, e);
        }
    }
}
