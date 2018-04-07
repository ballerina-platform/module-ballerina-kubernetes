/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

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
