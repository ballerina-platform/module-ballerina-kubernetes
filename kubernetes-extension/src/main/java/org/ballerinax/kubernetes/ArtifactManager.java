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

package org.ballerinax.kubernetes;

import org.ballerinax.kubernetes.exceptions.KubernetesPluginException;
import org.ballerinax.kubernetes.handlers.ConfigMapHandler;
import org.ballerinax.kubernetes.handlers.DeploymentHandler;
import org.ballerinax.kubernetes.handlers.DockerHandler;
import org.ballerinax.kubernetes.handlers.HPAHandler;
import org.ballerinax.kubernetes.handlers.HelmChartHandler;
import org.ballerinax.kubernetes.handlers.IngressHandler;
import org.ballerinax.kubernetes.handlers.JobHandler;
import org.ballerinax.kubernetes.handlers.PersistentVolumeClaimHandler;
import org.ballerinax.kubernetes.handlers.ResourceQuotaHandler;
import org.ballerinax.kubernetes.handlers.SecretHandler;
import org.ballerinax.kubernetes.handlers.ServiceHandler;
import org.ballerinax.kubernetes.handlers.istio.IstioGatewayHandler;
import org.ballerinax.kubernetes.handlers.istio.IstioVirtualServiceHandler;
import org.ballerinax.kubernetes.handlers.openshift.OpenShiftBuildConfigHandler;
import org.ballerinax.kubernetes.handlers.openshift.OpenShiftImageStreamHandler;
import org.ballerinax.kubernetes.handlers.openshift.OpenShiftRouteHandler;
import org.ballerinax.kubernetes.models.DeploymentModel;
import org.ballerinax.kubernetes.models.KubernetesContext;
import org.ballerinax.kubernetes.models.KubernetesDataHolder;
import org.ballerinax.kubernetes.utils.KubernetesUtils;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.ballerinax.kubernetes.KubernetesConstants.DEPLOYMENT_POSTFIX;
import static org.ballerinax.kubernetes.KubernetesConstants.DOCKER_LATEST_TAG;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.getValidName;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.isBlank;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.printInstruction;

/**
 * Generate and write artifacts to files.
 */
public class ArtifactManager {
    private static final Map<String, String> instructions = new LinkedHashMap<>();
    private KubernetesDataHolder kubernetesDataHolder;

    ArtifactManager() {
        this.kubernetesDataHolder = KubernetesContext.getInstance().getDataHolder();
    }

    /**
     * Generate kubernetes artifacts.
     *
     * @throws KubernetesPluginException if an error occurs while generating artifacts
     */
    void   createArtifacts() throws KubernetesPluginException {
        // add default kubernetes instructions.
        setDefaultKubernetesInstructions();
        
        if (kubernetesDataHolder.getJobModel() != null) {
            new JobHandler().createArtifacts();
            new DockerHandler().createArtifacts();
            return;
        } else {
            new ServiceHandler().createArtifacts();
            new IngressHandler().createArtifacts();
            new SecretHandler().createArtifacts();
            new PersistentVolumeClaimHandler().createArtifacts();
            new ResourceQuotaHandler().createArtifacts();
            new ConfigMapHandler().createArtifacts();
            new DeploymentHandler().createArtifacts();
            new HPAHandler().createArtifacts();
            new DockerHandler().createArtifacts();
            new HelmChartHandler().createArtifacts();
            new IstioGatewayHandler().createArtifacts();
            new IstioVirtualServiceHandler().createArtifacts();
            if (kubernetesDataHolder.getOpenShiftBuildExtensionModel() != null ||
                kubernetesDataHolder.getOpenShiftRouteModels().size() > 0) {
                // Clean all instructions
                instructions.clear();
                new OpenShiftBuildConfigHandler().createArtifacts();
                new OpenShiftImageStreamHandler().createArtifacts();
                new OpenShiftRouteHandler().createArtifacts();
            }
        }
        
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
        DeploymentModel deploymentModel = kubernetesDataHolder.getDeploymentModel();
        kubernetesDataHolder.setDeploymentModel(deploymentModel);
        String balxFileName = KubernetesUtils.extractBalxName(kubernetesDataHolder.getBalxFilePath());
        if (isBlank(deploymentModel.getName())) {
            if (balxFileName != null) {
                deploymentModel.setName(getValidName(balxFileName) + DEPLOYMENT_POSTFIX);
            }
        }
        if (isBlank(deploymentModel.getImage())) {
            deploymentModel.setImage(balxFileName + DOCKER_LATEST_TAG);
        }
        deploymentModel.addLabel(KubernetesConstants.KUBERNETES_SELECTOR_KEY, balxFileName);
        kubernetesDataHolder.setDeploymentModel(deploymentModel);
    }
    
    /**
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
    private void setDefaultKubernetesInstructions() {
        instructions.put("\tRun the following command to deploy the Kubernetes artifacts: ",
                "\tkubectl apply -f " + this.kubernetesDataHolder.getArtifactOutputPath().toAbsolutePath());
        
        DeploymentModel model = this.kubernetesDataHolder.getDeploymentModel();
        instructions.put("\tRun the following command to install the application using Helm: ",
                "\thelm install --name " + model.getName() + " " +
                this.kubernetesDataHolder.getArtifactOutputPath().resolve(model.getName()).toAbsolutePath());
    }
}
