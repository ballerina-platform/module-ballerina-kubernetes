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
import org.ballerinax.kubernetes.models.SecretModel;
import org.ballerinax.kubernetes.utils.KubernetesUtils;

import java.io.IOException;
import java.util.Collection;

import static org.ballerinax.kubernetes.KubernetesConstants.SECRET_FILE_POSTFIX;
import static org.ballerinax.kubernetes.KubernetesConstants.YAML;

/**
 * Generates kubernetes secret.
 */
public class SecretHandler implements ArtifactHandler {


    private void generate(SecretModel secretModel) throws KubernetesPluginException {
        Secret secret = new SecretBuilder()
                .withNewMetadata()
                .withName(secretModel.getName())
                .endMetadata()
                .withData(secretModel.getData())
                .build();
        try {
            String secretContent = SerializationUtils.dumpWithoutRuntimeStateAsYaml(secret);
            KubernetesUtils.writeToFile(secretContent, SECRET_FILE_POSTFIX +
                    YAML);
        } catch (IOException e) {
            String errorMessage = "Error while generating yaml file for secret: " + secretModel.getName();
            throw new KubernetesPluginException(errorMessage, e);
        }
    }

    @Override
    public void createArtifacts() throws KubernetesPluginException {
        //secret
        int count = 0;
        Collection<SecretModel> secretModels = KUBERNETES_DATA_HOLDER.getSecretModelSet();
        if (secretModels.size() > 0) {
            OUT.println();
        }
        for (SecretModel secretModel : secretModels) {
            count++;
            generate(secretModel);
            OUT.print("@kubernetes:Secret \t\t\t - complete " + count + "/" + secretModels.size() + "\r");
        }

    }

}
