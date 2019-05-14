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

import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceBuilder;
import io.fabric8.kubernetes.client.internal.SerializationUtils;
import org.ballerinax.kubernetes.KubernetesConstants;
import org.ballerinax.kubernetes.exceptions.KubernetesPluginException;
import org.ballerinax.kubernetes.models.DeploymentModel;
import org.ballerinax.kubernetes.models.KubernetesContext;
import org.ballerinax.kubernetes.models.ServiceModel;
import org.ballerinax.kubernetes.utils.KubernetesUtils;

import java.io.IOException;
import java.util.Map;

import static org.ballerinax.kubernetes.KubernetesConstants.SVC_FILE_POSTFIX;
import static org.ballerinax.kubernetes.KubernetesConstants.YAML;


/**
 * Generates kubernetes service from annotations.
 */
public class ServiceHandler extends AbstractArtifactHandler {

    /**
     * Generate kubernetes service definition from annotation.
     *
     * @throws KubernetesPluginException If an error occurs while generating artifact.
     */
    private void generate(ServiceModel serviceModel) throws KubernetesPluginException {
        if (null == serviceModel.getPortName()) {
            serviceModel.setPortName(serviceModel.getProtocol() + "-" + serviceModel.getName());
        }
        
        Service service = new ServiceBuilder()
                .withNewMetadata()
                .withName(serviceModel.getName())
                .withNamespace(dataHolder.getNamespace())
                .addToLabels(serviceModel.getLabels())
                .endMetadata()
                .withNewSpec()
                .addNewPort()
                .withName(serviceModel.getPortName())
                .withProtocol(KubernetesConstants.KUBERNETES_SVC_PROTOCOL)
                .withPort(serviceModel.getPort())
                .withNewTargetPort(serviceModel.getTargetPort())
                .endPort()
                .addToSelector(KubernetesConstants.KUBERNETES_SELECTOR_KEY, serviceModel.getSelector())
                .withSessionAffinity(serviceModel.getSessionAffinity())
                .withType(serviceModel.getServiceType())
                .endSpec()
                .build();
        try {
            String serviceYAML = SerializationUtils.dumpWithoutRuntimeStateAsYaml(service);
            KubernetesUtils.writeToFile(serviceYAML, SVC_FILE_POSTFIX + YAML);
        } catch (IOException e) {
            String errorMessage = "error while generating yaml file for service: " + serviceModel.getName();
            throw new KubernetesPluginException(errorMessage, e);
        }

    }

    @Override
    public void createArtifacts() throws KubernetesPluginException {
        // Service
        DeploymentModel deploymentModel = dataHolder.getDeploymentModel();
        Map<String, ServiceModel> serviceModels = dataHolder.getbListenerToK8sServiceMap();
        int count = 0;
        for (ServiceModel serviceModel : serviceModels.values()) {
            count++;
            String balxFileName = KubernetesUtils.extractBalxName(KubernetesContext.getInstance().getDataHolder()
                    .getBalxFilePath());
            serviceModel.addLabel(KubernetesConstants.KUBERNETES_SELECTOR_KEY, balxFileName);
            serviceModel.setSelector(balxFileName);
            generate(serviceModel);
            deploymentModel.addPort(serviceModel.getTargetPort());
            OUT.println();
            OUT.print("\t@kubernetes:Service \t\t\t - complete " + count + "/" + serviceModels.size() + "\r");
        }
    }


}
