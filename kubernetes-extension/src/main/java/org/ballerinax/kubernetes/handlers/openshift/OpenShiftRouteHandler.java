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

package org.ballerinax.kubernetes.handlers.openshift;

import io.fabric8.kubernetes.client.internal.SerializationUtils;
import io.fabric8.openshift.api.model.Route;
import io.fabric8.openshift.api.model.RouteBuilder;
import org.ballerinax.kubernetes.exceptions.KubernetesPluginException;
import org.ballerinax.kubernetes.handlers.AbstractArtifactHandler;
import org.ballerinax.kubernetes.models.ServiceModel;
import org.ballerinax.kubernetes.models.openshift.OpenShiftRouteModel;
import org.ballerinax.kubernetes.utils.KubernetesUtils;

import java.io.IOException;
import java.util.Map;

import static org.ballerinax.kubernetes.KubernetesConstants.OPENSHIFT_ROUTE_FILE_POSTFIX;
import static org.ballerinax.kubernetes.KubernetesConstants.YAML;

/**
 * Generates OpenShift's Routes.
 */
public class OpenShiftRouteHandler extends AbstractArtifactHandler {
    @Override
    public void createArtifacts() throws KubernetesPluginException {
        Map<String, OpenShiftRouteModel> routeModels = dataHolder.getOpenShiftRouteModels();
        int count = 0;
        for (Map.Entry<String, OpenShiftRouteModel> routeModel : routeModels.entrySet()) {
            count++;
            ServiceModel serviceModel = dataHolder.getServiceModel(routeModel.getKey());
            generate(routeModel.getValue(), serviceModel);
            OUT.println("\t@kubernetes:OpenShiftRoute \t\t - complete " + count + "/" + routeModels.size() + "\r");
        }
    }
    
    /**
     * Generate the yaml file for a route model.
     *
     * @param routeModel The model.
     * @param serviceModel Matching service model.
     * @throws KubernetesPluginException When an error occurs while writing yaml files.
     */
    private void generate(OpenShiftRouteModel routeModel, ServiceModel serviceModel) throws KubernetesPluginException {
        try {
            Route route = new RouteBuilder()
                    .withNewMetadata()
                    .withName(routeModel.getName())
                    .withLabels(routeModel.getLabels())
                    .withAnnotations(routeModel.getAnnotations())
                    .withNamespace(routeModel.getNamespace())
                    .endMetadata()
                    .withNewSpec()
                    .withHost(routeModel.getHost())
                    .withNewPort()
                    .withNewTargetPort(serviceModel.getTargetPort())
                    .endPort()
                    .withNewTo()
                    .withKind("Service")
                    .withName(serviceModel.getName())
                    .withWeight(100)
                    .endTo()
                    .endSpec()
                    .build();
            
            String resourceQuotaContent = SerializationUtils.dumpWithoutRuntimeStateAsYaml(route);
            KubernetesUtils.writeToFile(resourceQuotaContent, OPENSHIFT_ROUTE_FILE_POSTFIX + YAML);
        } catch (IOException e) {
            String errorMessage = "Error while generating OpenShift Route yaml file: " +
                                  routeModel.getName();
            throw new KubernetesPluginException(errorMessage, e);
        }
    }
}
