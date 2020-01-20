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


import org.ballerinax.kubernetes.KubernetesConstants;
import org.ballerinax.kubernetes.exceptions.KubernetesPluginException;
import org.ballerinax.kubernetes.models.knative.KnativeContainerModel;
import org.ballerinax.kubernetes.models.knative.KnativeContext;
import org.ballerinax.kubernetes.models.knative.ServiceModel;

import java.util.Map;

import static org.ballerinax.docker.generator.utils.DockerGenUtils.extractUberJarName;


/**
 * Generates kubernetes service from annotations.
 */
public class KnativeContainerHandler extends KnativeAbstractArtifactHandler {

    /**
     * Generate kubernetes service definition from annotation.
     *
     * @throws KubernetesPluginException If an error occurs while generating artifact.
     */
    private void generate(KnativeContainerModel serviceModel) throws KubernetesPluginException {

    }
    @Override
    public void createArtifacts() throws KubernetesPluginException {
        // Service
        ServiceModel deploymentModel = knativeDataHolder.getServiceModel();
        Map<String, KnativeContainerModel> serviceModels = knativeDataHolder.getbListenerToK8sServiceMap();
        int count = 0;
        for (KnativeContainerModel serviceModel : serviceModels.values()) {
            count++;
            String balxFileName = extractUberJarName(KnativeContext.getInstance().getDataHolder()
                    .getUberJarPath());
            serviceModel.addLabel(KubernetesConstants.KUBERNETES_SELECTOR_KEY, balxFileName);
            serviceModel.setSelector(balxFileName);
            generate(serviceModel);
            deploymentModel.addPort(serviceModel.getTargetPort());
        }
    }
}
