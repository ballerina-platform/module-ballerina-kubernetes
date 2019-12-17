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
import java.util.LinkedHashMap;
import java.util.Map;

import static org.ballerinax.docker.generator.utils.DockerGenUtils.extractUberJarName;
import static org.ballerinax.kubernetes.KubernetesConstants.DEPLOYMENT_POSTFIX;
import static org.ballerinax.kubernetes.KubernetesConstants.DOCKER_LATEST_TAG;
import static org.ballerinax.kubernetes.utils.KnativeUtils.getValidName;
import static org.ballerinax.kubernetes.utils.KnativeUtils.isBlank;
import static org.ballerinax.kubernetes.utils.KnativeUtils.printInstruction;

/**
 * Generate and write artifacts to files.
 */
public class KnativeArtifactManager {

    private static final Map<String, String> instructions = new LinkedHashMap<>();
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
    void   createArtifacts() throws KubernetesPluginException {
        // add default kubernetes instructions.
        setDefaultKnativeInstructions();
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
        for (Map.Entry<String, String> instruction : instructions.entrySet()) {
            printInstruction(instruction.getKey());
            printInstruction(instruction.getValue());
            printInstruction("");
        }
    }

    public void populateDeploymentModel() {
        ServiceModel serviceModel = knativeDataHolder.getServiceModel();
        knativeDataHolder.setServiceModel(serviceModel);
        String balxFileName = extractUberJarName(knativeDataHolder.getUberJarPath());
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

    /**setDeploymentModel
     * Returns print instructions.
     *
     * @return instructions.
     */
    public static Map<String, String> getInstructions() {
        return instructions;
    }

    /**
     * Set instructions for kubernetes and helm artifacts.
     */
    private void setDefaultKnativeInstructions() {
        instructions.put("\tRun the following command to deploy the Knative artifacts: ",
                "\tkubectl apply -f " + this.knativeDataHolder.getK8sArtifactOutputPath().toAbsolutePath());

        ServiceModel model = this.knativeDataHolder.getServiceModel();
        instructions.put("\tRun the following command to install the application using Helm: ",
                "\thelm install --name " + model.getName() + " " +
                        this.knativeDataHolder.getK8sArtifactOutputPath().resolve(model.getName()).toAbsolutePath());
    }
}
