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
import org.ballerinax.kubernetes.handlers.IngressHandler;
import org.ballerinax.kubernetes.handlers.JobHandler;
import org.ballerinax.kubernetes.handlers.PersistentVolumeClaimHandler;
import org.ballerinax.kubernetes.handlers.SecretHandler;
import org.ballerinax.kubernetes.handlers.ServiceHandler;
import org.ballerinax.kubernetes.models.DeploymentModel;
import org.ballerinax.kubernetes.models.KubernetesDataHolder;
import org.ballerinax.kubernetes.utils.KubernetesUtils;

import java.util.HashMap;

import static org.ballerinax.kubernetes.KubernetesConstants.DEPLOYMENT_POSTFIX;
import static org.ballerinax.kubernetes.KubernetesConstants.DOCKER_LATEST_TAG;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.getValidName;

/**
 * Generate and write artifacts to files.
 */
class ArtifactManager {

    private final String balxFilePath;
    private final String outputDir;
    private KubernetesDataHolder kubernetesDataHolder;

    ArtifactManager(String balxFilePath, String outputDir) {
        this.balxFilePath = balxFilePath;
        this.outputDir = outputDir;
        this.kubernetesDataHolder = KubernetesDataHolder.getInstance();
    }

    /**
     * Generate kubernetes artifacts.
     *
     * @throws KubernetesPluginException if an error occurs while generating artifacts
     */
    void createArtifacts() throws KubernetesPluginException {
        if (kubernetesDataHolder.getJobModel() != null) {
            new JobHandler().createArtifacts();
            printKubernetesInstructions(outputDir);
            return;
        }
        DeploymentModel deploymentModel = kubernetesDataHolder.getDeploymentModel();
        if (deploymentModel == null) {
            deploymentModel = getDefaultDeploymentModel();
        }
        kubernetesDataHolder.setDeploymentModel(deploymentModel);
        new ServiceHandler().createArtifacts();
        new IngressHandler().createArtifacts();
        new SecretHandler().createArtifacts();
        new PersistentVolumeClaimHandler().createArtifacts();
        new ConfigMapHandler().createArtifacts();
        new DeploymentHandler().createArtifacts();
        new HPAHandler().createArtifacts();
        new DockerHandler().createArtifacts();
        printKubernetesInstructions(outputDir);
    }


    private void printKubernetesInstructions(String outputDir) {
        KubernetesUtils.printInstruction("\n\nRun following command to deploy kubernetes artifacts: ");
        KubernetesUtils.printInstruction("kubectl apply -f " + outputDir);
    }


    /**
     * Get DeploymentModel object with default values.
     *
     * @return DeploymentModel object with default values
     */
    private DeploymentModel getDefaultDeploymentModel() {
        DeploymentModel deploymentModel = new DeploymentModel();
        String balxName = KubernetesUtils.extractBalxName(balxFilePath);
        String deploymentName = balxName + DEPLOYMENT_POSTFIX;
        deploymentModel.setName(getValidName(deploymentName));
        deploymentModel.setImagePullPolicy(KubernetesConstants.DEPLOYMENT_IMAGE_PULL_POLICY_DEFAULT);
        deploymentModel.setEnableLiveness(KubernetesConstants.DEPLOYMENT_LIVENESS_DISABLE);
        int defaultReplicas = 1;
        deploymentModel.setReplicas(defaultReplicas);
        deploymentModel.addLabel(KubernetesConstants.KUBERNETES_SELECTOR_KEY, balxName);
        deploymentModel.setEnv(new HashMap<>());
        deploymentModel.setImage(balxName + DOCKER_LATEST_TAG);
        deploymentModel.setBuildImage(true);
        deploymentModel.setPush(false);

        return deploymentModel;
    }

}
