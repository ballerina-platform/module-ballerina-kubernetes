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

package org.ballerinax.kubernetes;

import org.ballerinax.kubernetes.exceptions.KubernetesPluginException;
import org.ballerinax.kubernetes.handlers.knative.KnativeConfigMapHandler;
import org.ballerinax.kubernetes.handlers.knative.KnativeContainerHandler;
import org.ballerinax.kubernetes.handlers.knative.KnativeDockerHandler;
import org.ballerinax.kubernetes.handlers.knative.KnativeResourceQuotaHandler;
import org.ballerinax.kubernetes.handlers.knative.KnativeSecretHandler;
import org.ballerinax.kubernetes.handlers.knative.KnativeServiceHandler;
import org.ballerinax.kubernetes.models.knative.KnativeContext;
import org.ballerinax.kubernetes.models.knative.KnativeDataHolder;
import org.ballerinax.kubernetes.models.knative.ServiceModel;

import java.io.PrintStream;

import static org.ballerinax.docker.generator.utils.DockerGenUtils.extractJarName;
import static org.ballerinax.kubernetes.KubernetesConstants.DEPLOYMENT_POSTFIX;
import static org.ballerinax.kubernetes.KubernetesConstants.DOCKER_LATEST_TAG;
import static org.ballerinax.kubernetes.KubernetesConstants.KNATIVE;
import static org.ballerinax.kubernetes.utils.KnativeUtils.getValidName;
import static org.ballerinax.kubernetes.utils.KnativeUtils.isBlank;
import static org.ballerinax.kubernetes.utils.KnativeUtils.printInstruction;

/**
 * Generate and write artifacts to files.
 */
public class KnativeArtifactManager {

    private static final PrintStream OUT = System.out;
    private KnativeDataHolder knativeDataHolder;

    KnativeArtifactManager() {
        this.knativeDataHolder = KnativeContext.getInstance().getDataHolder();
    }

    /**
     * Generate kubernetes artifacts.
     *
     * @throws KubernetesPluginException if an error occurs while generating artifacts
     */
    void createArtifacts() throws KubernetesPluginException {
        OUT.println("\nGenerating Knative artifacts...");
        new KnativeContainerHandler().createArtifacts();
        new KnativeSecretHandler().createArtifacts();
        new KnativeResourceQuotaHandler().createArtifacts();
        new KnativeConfigMapHandler().createArtifacts();
        new KnativeServiceHandler().createArtifacts();
        new KnativeDockerHandler().createArtifacts();
        printInstructions();
    }

    private void printInstructions() {
        printInstruction("");
        printInstruction("");
        printInstruction("\tExecute the below command to deploy the Knative artifacts: ");
        printInstruction("\tkubectl apply -f " + this.knativeDataHolder.getK8sArtifactOutputPath().resolve(KNATIVE)
                .toAbsolutePath());
        printInstruction("");
    }

    public void populateDeploymentModel() {
        ServiceModel serviceModel = knativeDataHolder.getServiceModel();
        knativeDataHolder.setServiceModel(serviceModel);
        String balxFileName = extractJarName(knativeDataHolder.getUberJarPath());
        if (isBlank(serviceModel.getName())) {
            if (balxFileName != null) {
                serviceModel.setName(getValidName(balxFileName) + DEPLOYMENT_POSTFIX);
            }
        }
        if (isBlank(serviceModel.getImage())) {
            serviceModel.setImage(balxFileName + DOCKER_LATEST_TAG);
        }
        serviceModel.addLabel(KubernetesConstants.KUBERNETES_SELECTOR_KEY, balxFileName);
        knativeDataHolder.setServiceModel(serviceModel);
    }

}
