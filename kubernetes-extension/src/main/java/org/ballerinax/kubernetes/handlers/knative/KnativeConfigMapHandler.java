/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ConfigMapBuilder;
import io.fabric8.kubernetes.client.internal.SerializationUtils;
import org.ballerinax.kubernetes.exceptions.KubernetesPluginException;
import org.ballerinax.kubernetes.models.knative.ConfigMapModel;
import org.ballerinax.kubernetes.models.knative.EnvVarValueModel;
import org.ballerinax.kubernetes.models.knative.ServiceModel;
import org.ballerinax.kubernetes.utils.KnativeUtils;

import java.io.IOException;
import java.util.Collection;

import static org.ballerinax.kubernetes.KubernetesConstants.BALLERINA_CONF_FILE_NAME;
import static org.ballerinax.kubernetes.KubernetesConstants.CONFIG_MAP_FILE_POSTFIX;
import static org.ballerinax.kubernetes.KubernetesConstants.YAML;
import static org.ballerinax.kubernetes.utils.KnativeUtils.isBlank;

/**
 * Generates kubernetes Config Map.
 */
public class KnativeConfigMapHandler extends KnativeAbstractArtifactHandler {

    private void generate(ConfigMapModel configMapModel) throws KubernetesPluginException {
        ConfigMap configMap = new ConfigMapBuilder()
                .withNewMetadata()
                .withName(configMapModel.getName())
                .withNamespace(knativeDataHolder.getNamespace())
                .endMetadata()
                .withData(configMapModel.getData())
                .build();
        try {
            String configMapContent = SerializationUtils.dumpWithoutRuntimeStateAsYaml(configMap);
            KnativeUtils.writeToFile(configMapContent, CONFIG_MAP_FILE_POSTFIX + YAML);
        } catch (IOException e) {
            String errorMessage = "Error while parsing yaml file for config map: " + configMapModel.getName();
            throw new KubernetesPluginException(errorMessage, e);
        }
    }

    @Override
    public void createArtifacts() throws KubernetesPluginException {
        //configMap
        int count = 0;
        Collection<ConfigMapModel> configMapModels = knativeDataHolder.getConfigMapModelSet();
        if (configMapModels.size() > 0) {
            OUT.println();
        }
        for (ConfigMapModel configMapModel : configMapModels) {
            count++;
            if (!isBlank(configMapModel.getBallerinaConf())) {
                if (configMapModel.getData().size() != 1) {
                    throw new KubernetesPluginException("there can be only 1 ballerina config file");
                }
                ServiceModel serviceModel = knativeDataHolder.getServiceModel();
                serviceModel.setCommandArgs(" --b7a.config.file=${CONFIG_FILE}");
                EnvVarValueModel envVarValueModel = new EnvVarValueModel(configMapModel.getMountPath() +
                        BALLERINA_CONF_FILE_NAME);
                serviceModel.addEnv("CONFIG_FILE", envVarValueModel);
                knativeDataHolder.setServiceModel(serviceModel);
            }
            generate(configMapModel);
            OUT.print("\t@knative:ConfigMap \t\t\t - complete " + count + "/" + configMapModels.size() + "\r");
        }
    }
}
