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

import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.SecretBuilder;
import io.fabric8.kubernetes.client.internal.SerializationUtils;
import org.ballerinax.kubernetes.exceptions.KubernetesPluginException;
import org.ballerinax.kubernetes.models.DeploymentModel;
import org.ballerinax.kubernetes.models.EnvVarValueModel;
import org.ballerinax.kubernetes.models.SecretModel;
import org.ballerinax.kubernetes.utils.KubernetesUtils;

import java.io.IOException;
import java.util.Collection;

import static org.ballerinax.kubernetes.KubernetesConstants.BALLERINA_CONF_FILE_NAME;
import static org.ballerinax.kubernetes.KubernetesConstants.SECRET_FILE_POSTFIX;
import static org.ballerinax.kubernetes.KubernetesConstants.YAML;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.isBlank;

/**
 * Generates kubernetes secret.
 */
public class SecretHandler extends AbstractArtifactHandler {


    private void generate(SecretModel secretModel) throws KubernetesPluginException {
        Secret secret = new SecretBuilder()
                .withNewMetadata()
                .withNamespace(dataHolder.getNamespace())
                .withName(secretModel.getName())
                .endMetadata()
                .withData(secretModel.getData())
                .build();
        try {
            String secretContent = SerializationUtils.dumpWithoutRuntimeStateAsYaml(secret);
            KubernetesUtils.writeToFile(secretContent, SECRET_FILE_POSTFIX +
                    YAML);
        } catch (IOException e) {
            String errorMessage = "error while generating yaml file for secret: " + secretModel.getName();
            throw new KubernetesPluginException(errorMessage, e);
        }
    }

    @Override
    public void createArtifacts() throws KubernetesPluginException {
        //secret
        int count = 0;
        Collection<SecretModel> secretModels = dataHolder.getSecretModelSet();
        if (secretModels.size() > 0) {
            OUT.println();
        }
        for (SecretModel secretModel : secretModels) {
            count++;
            if (!isBlank(secretModel.getBallerinaConf())) {
                if (secretModel.getData().size() != 1) {
                    throw new KubernetesPluginException("there can be only 1 ballerina config file");
                }
                DeploymentModel deploymentModel = dataHolder.getDeploymentModel();
                deploymentModel.setCommandArgs(" --b7a.config.file=${CONFIG_FILE}");
                EnvVarValueModel envVarValueModel = new EnvVarValueModel(secretModel.getMountPath() +
                        BALLERINA_CONF_FILE_NAME);
                deploymentModel.addEnv("CONFIG_FILE", envVarValueModel);
                dataHolder.setDeploymentModel(deploymentModel);
            }
            generate(secretModel);
            OUT.print("\t@kubernetes:Secret \t\t\t - complete " + count + "/" + secretModels.size() + "\r");
        }

    }

}
