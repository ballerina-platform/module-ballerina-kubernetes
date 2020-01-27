/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinax.kubernetes.handlers.knative;

import io.fabric8.kubernetes.api.model.Quantity;
import io.fabric8.kubernetes.api.model.QuantityBuilder;
import io.fabric8.kubernetes.api.model.ResourceQuota;
import io.fabric8.kubernetes.api.model.ResourceQuotaBuilder;
import io.fabric8.kubernetes.client.internal.SerializationUtils;
import org.ballerinax.kubernetes.exceptions.KubernetesPluginException;
import org.ballerinax.kubernetes.models.knative.ResourceQuotaModel;
import org.ballerinax.kubernetes.utils.KnativeUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.ballerinax.kubernetes.KubernetesConstants.RESOURCE_QUOTA_FILE_POSTFIX;
import static org.ballerinax.kubernetes.KubernetesConstants.YAML;

/**
 * Generates kubernetes resource quotas.
 */
public class KnativeResourceQuotaHandler extends KnativeAbstractArtifactHandler {

    private void generate(ResourceQuotaModel resourceQuotaModel) throws KubernetesPluginException {
        ResourceQuota resourceQuota = new ResourceQuotaBuilder()
                .withNewMetadata()
                .withName(resourceQuotaModel.getName())
                .withLabels(resourceQuotaModel.getLabels())
                .withAnnotations(resourceQuotaModel.getAnnotations())
                .endMetadata()
                .withNewSpec()
                .withHard(getHard(resourceQuotaModel.getHard()))
                .withScopes(new ArrayList<>(resourceQuotaModel.getScopes()))
                .endSpec()
                .build();
        try {
            String resourceQuotaContent = SerializationUtils.dumpWithoutRuntimeStateAsYaml(resourceQuota);
            KnativeUtils.writeToFile(resourceQuotaContent, RESOURCE_QUOTA_FILE_POSTFIX + YAML);
        } catch (IOException e) {
            String errorMessage = "error while generating yaml file for resource quotas: " +
                    resourceQuotaModel.getName();
            throw new KubernetesPluginException(errorMessage, e);
        }
    }

    /**
     * Convert hard limits map.
     * @param hard Hard limit map from model.
     * @return Converted map.
     */
    private Map<String, Quantity> getHard(Map<String, String> hard) {
        return hard.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, hardEntry -> new QuantityBuilder()
                        .withAmount(hardEntry.getValue())
                        .build()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createArtifacts() throws KubernetesPluginException {
        int count = 0;
        Set<ResourceQuotaModel> resourceQuotas = knativeDataHolder.getResourceQuotaModels();
        if (resourceQuotas.size() > 0) {
            OUT.println();
        }
        for (ResourceQuotaModel resourceQuotaModel : resourceQuotas) {
            count++;
            generate(resourceQuotaModel);
            OUT.print("\t@kubernetes:ResourceQuota \t\t - complete " + count + "/" + resourceQuotas.size() + "\r");
        }
    }
}
