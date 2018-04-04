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
import org.ballerinax.kubernetes.handlers.PersistentVolumeClaimHandler;
import org.ballerinax.kubernetes.handlers.SecretHandler;
import org.ballerinax.kubernetes.handlers.ServiceHandler;
import org.ballerinax.kubernetes.models.ConfigMapModel;
import org.ballerinax.kubernetes.models.DeploymentModel;
import org.ballerinax.kubernetes.models.DockerModel;
import org.ballerinax.kubernetes.models.IngressModel;
import org.ballerinax.kubernetes.models.KubernetesDataHolder;
import org.ballerinax.kubernetes.models.PersistentVolumeClaimModel;
import org.ballerinax.kubernetes.models.PodAutoscalerModel;
import org.ballerinax.kubernetes.models.SecretModel;
import org.ballerinax.kubernetes.models.ServiceModel;
import org.ballerinax.kubernetes.utils.KubernetesUtils;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.ballerinax.kubernetes.KubernetesConstants.BALX;
import static org.ballerinax.kubernetes.KubernetesConstants.CONFIG_MAP_FILE_POSTFIX;
import static org.ballerinax.kubernetes.KubernetesConstants.DEPLOYMENT_FILE_POSTFIX;
import static org.ballerinax.kubernetes.KubernetesConstants.DEPLOYMENT_POSTFIX;
import static org.ballerinax.kubernetes.KubernetesConstants.DOCKER;
import static org.ballerinax.kubernetes.KubernetesConstants.DOCKER_LATEST_TAG;
import static org.ballerinax.kubernetes.KubernetesConstants.HPA_FILE_POSTFIX;
import static org.ballerinax.kubernetes.KubernetesConstants.HPA_POSTFIX;
import static org.ballerinax.kubernetes.KubernetesConstants.INGRESS_FILE_POSTFIX;
import static org.ballerinax.kubernetes.KubernetesConstants.SECRET_FILE_POSTFIX;
import static org.ballerinax.kubernetes.KubernetesConstants.SVC_FILE_POSTFIX;
import static org.ballerinax.kubernetes.KubernetesConstants.VOLUME_CLAIM_FILE_POSTFIX;
import static org.ballerinax.kubernetes.KubernetesConstants.YAML;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.getValidName;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.isEmpty;

/**
 * Generate and write artifacts to files.
 */
class ArtifactManager {

    private PrintStream out = System.out;
    private String balxFilePath;
    private String outputDir;
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
        DeploymentModel deploymentModel = kubernetesDataHolder.getDeploymentModel();
        if (deploymentModel == null) {
            deploymentModel = getDefaultDeploymentModel();
        }
        kubernetesDataHolder.setDeploymentModel(deploymentModel);
        deploymentModel.setPodAutoscalerModel(kubernetesDataHolder.getPodAutoscalerModel());
        deploymentModel.setSecretModels(kubernetesDataHolder.getSecretModelSet());
        deploymentModel.setConfigMapModels(kubernetesDataHolder.getConfigMapModelSet());
        deploymentModel.setVolumeClaimModels(kubernetesDataHolder.getVolumeClaimModelSet());

        // Service
        Map<String, ServiceModel> serviceModels = kubernetesDataHolder.getbEndpointToK8sServiceMap();
        int count = 0;
        for (ServiceModel serviceModel : serviceModels.values()) {
            count++;
            generateService(serviceModel);
            deploymentModel.addPort(serviceModel.getPort());
            out.print("@kubernetes:Service \t\t\t - complete " + count + "/" + serviceModels.size() + "\r");
        }
        // ingress
        count = 0;
        Set<IngressModel> ingressModels = kubernetesDataHolder.getIngressModelSet();
        int size = ingressModels.size();
        if (size > 0) {
            out.println();
        }
        Map<String, Set<SecretModel>> secretModelsMap = kubernetesDataHolder.getSecretModels();
        for (IngressModel ingressModel : ingressModels) {
            ServiceModel serviceModel = kubernetesDataHolder.getServiceModel(ingressModel.getEndpointName());
            ingressModel.setServiceName(serviceModel.getName());
            ingressModel.setServicePort(serviceModel.getPort());
            if (secretModelsMap.get(ingressModel.getEndpointName()) != null && secretModelsMap.get(ingressModel
                    .getEndpointName()).size() != 0) {
                ingressModel.setEnableTLS(true);
            }
            generateIngress(ingressModel);
            count++;
            out.print("@kubernetes:Ingress \t\t\t - complete " + count + "/" + size + "\r");
        }

        //secret
        count = 0;
        Collection<SecretModel> secretModels = kubernetesDataHolder.getSecretModelSet();
        if (secretModels.size() > 0) {
            out.println();
        }
        for (SecretModel secretModel : secretModels) {
            count++;
            generateSecrets(secretModel, balxFilePath, outputDir);
            out.print("@kubernetes:Secret \t\t\t - complete " + count + "/" + secretModels.size() + "\r");
        }

        //configMap
        count = 0;
        Collection<ConfigMapModel> configMapModels = kubernetesDataHolder.getConfigMapModelSet();
        if (configMapModels.size() > 0) {
            out.println();
        }
        for (ConfigMapModel configMapModel : configMapModels) {
            count++;
            if (configMapModel.isBallerinaConf()) {
                if (configMapModel.getData().size() != 1) {
                    throw new KubernetesPluginException("There can be only 1 ballerina config file");
                }
                deploymentModel.setCommandArgs(" --config ${CONFIG_FILE} ");
                deploymentModel.addEnv("CONFIG_FILE", configMapModel.getMountPath() + File.separator +
                        configMapModel.getData().keySet().iterator().next());
            }
            generateConfigMaps(configMapModel);
            out.print("@kubernetes:ConfigMap \t\t\t - complete " + count + "/" + configMapModels.size() + "\r");
        }

        //volume mount
        count = 0;
        Collection<PersistentVolumeClaimModel> volumeClaims = kubernetesDataHolder.getVolumeClaimModelSet();
        if (volumeClaims.size() > 0) {
            out.println();
        }
        for (PersistentVolumeClaimModel claimModel : volumeClaims) {
            count++;
            generatePersistentVolumeClaim(claimModel);
            out.print("@kubernetes:VolumeClaim \t\t - complete " + count + "/" + volumeClaims.size() + "\r");
        }
        out.println();
        generateDeployment(deploymentModel);
        out.println();
        out.println("@kubernetes:Deployment \t\t\t - complete 1/1");

        printKubernetesInstructions(outputDir);
    }


    private void generateDeployment(DeploymentModel deploymentModel) throws KubernetesPluginException {
        String balxFileName = KubernetesUtils.extractBalxName(balxFilePath);
        if (isEmpty(deploymentModel.getName())) {
            deploymentModel.setName(getValidName(balxFileName) + DEPLOYMENT_POSTFIX);
        }
        if (isEmpty(deploymentModel.getImage())) {
            deploymentModel.setImage(balxFileName + DOCKER_LATEST_TAG);
        }
        deploymentModel.addLabel(KubernetesConstants.KUBERNETES_SELECTOR_KEY, balxFileName);
        if ("enable".equals(deploymentModel.getEnableLiveness()) && deploymentModel.getLivenessPort() == 0) {
            //set first port as liveness port
            deploymentModel.setLivenessPort(deploymentModel.getPorts().iterator().next());
        }
        String deploymentContent = new DeploymentHandler(deploymentModel).generate();
        try {
            KubernetesUtils.writeToFile(deploymentContent, outputDir + File
                    .separator + balxFileName + DEPLOYMENT_FILE_POSTFIX + YAML);
            //generate dockerfile and docker image
            generateDocker(deploymentModel);
            // generate HPA
            generatePodAutoscaler(deploymentModel);
        } catch (IOException e) {
            throw new KubernetesPluginException("Error while writing deployment content", e);
        }
    }

    private void generateService(ServiceModel serviceModel) throws KubernetesPluginException {
        String balxFileName = KubernetesUtils.extractBalxName(balxFilePath);
        serviceModel.addLabel(KubernetesConstants.KUBERNETES_SELECTOR_KEY, balxFileName);
        serviceModel.setSelector(balxFileName);
        String serviceContent = new ServiceHandler(serviceModel).generate();
        try {
            KubernetesUtils.writeToFile(serviceContent, outputDir + File
                    .separator + balxFileName + SVC_FILE_POSTFIX + YAML);
        } catch (IOException e) {
            throw new KubernetesPluginException("Error while writing service content", e);
        }
    }

    private void generateSecrets(SecretModel secretModel, String balxFilePath, String outputDir) throws
            KubernetesPluginException {
        String balxFileName = KubernetesUtils.extractBalxName(balxFilePath);
        String secretContent = new SecretHandler(secretModel).generate();
        try {
            KubernetesUtils.writeToFile(secretContent, outputDir + File
                    .separator + balxFileName + SECRET_FILE_POSTFIX + YAML);
        } catch (IOException e) {
            throw new KubernetesPluginException("Error while writing secret content", e);
        }
    }

    private void generateConfigMaps(ConfigMapModel configMapModel) throws
            KubernetesPluginException {
        String balxFileName = KubernetesUtils.extractBalxName(balxFilePath);
        String configMapContent = new ConfigMapHandler(configMapModel).generate();
        try {
            KubernetesUtils.writeToFile(configMapContent, outputDir + File
                    .separator + balxFileName + CONFIG_MAP_FILE_POSTFIX + YAML);
        } catch (IOException e) {
            throw new KubernetesPluginException("Error while writing config map content", e);
        }
    }

    private void generatePersistentVolumeClaim(PersistentVolumeClaimModel volumeClaimModel) throws
            KubernetesPluginException {
        String balxFileName = KubernetesUtils.extractBalxName(balxFilePath);
        String configMapContent = new PersistentVolumeClaimHandler(volumeClaimModel).generate();
        try {
            KubernetesUtils.writeToFile(configMapContent, outputDir + File
                    .separator + balxFileName + VOLUME_CLAIM_FILE_POSTFIX + YAML);
        } catch (IOException e) {
            throw new KubernetesPluginException("Error while writing volume claim content", e);
        }
    }

    private void generateIngress(IngressModel ingressModel) throws KubernetesPluginException {
        String balxFileName = KubernetesUtils.extractBalxName(balxFilePath);
        ingressModel.addLabel(KubernetesConstants.KUBERNETES_SELECTOR_KEY, balxFileName);
        String serviceContent = new IngressHandler(ingressModel).generate();
        try {
            KubernetesUtils.writeToFile(serviceContent, outputDir + File
                    .separator + balxFileName + INGRESS_FILE_POSTFIX + YAML);
        } catch (IOException e) {
            throw new KubernetesPluginException("Error while writing ingress content", e);
        }
    }

    private void generatePodAutoscaler(DeploymentModel deploymentModel) throws KubernetesPluginException {
        PodAutoscalerModel podAutoscalerModel = deploymentModel.getPodAutoscalerModel();
        if (podAutoscalerModel == null) {
            return;
        }
        String balxFileName = KubernetesUtils.extractBalxName(balxFilePath);
        podAutoscalerModel.addLabel(KubernetesConstants.KUBERNETES_SELECTOR_KEY, balxFileName);
        podAutoscalerModel.setDeployment(deploymentModel.getName());
        if (podAutoscalerModel.getMaxReplicas() == 0) {
            podAutoscalerModel.setMaxReplicas(deploymentModel.getReplicas() + 1);
        }
        if (podAutoscalerModel.getMinReplicas() == 0) {
            podAutoscalerModel.setMinReplicas(deploymentModel.getReplicas());
        }
        if (podAutoscalerModel.getName() == null || podAutoscalerModel.getName().length() == 0) {
            podAutoscalerModel.setName(getValidName(balxFileName) + HPA_POSTFIX);
        }
        String serviceContent = new HPAHandler(podAutoscalerModel).generate();
        try {
            out.println();
            KubernetesUtils.writeToFile(serviceContent, outputDir + File
                    .separator + balxFileName + HPA_FILE_POSTFIX + YAML);
            out.print("@kubernetes:HPA \t\t\t - complete 1/1");
        } catch (IOException e) {
            throw new KubernetesPluginException("Error while writing HPA content", e);
        }
    }

    private void printKubernetesInstructions(String outputDir) {
        KubernetesUtils.printInstruction("\nRun following command to deploy kubernetes artifacts: ");
        KubernetesUtils.printInstruction("kubectl apply -f " + outputDir);
    }

    /**
     * Create docker artifacts.
     *
     * @param deploymentModel Deployment model
     * @throws KubernetesPluginException If an error occurs while generating artifacts
     */
    private void generateDocker(DeploymentModel deploymentModel)
            throws KubernetesPluginException {
        DockerModel dockerModel = new DockerModel();
        String dockerImage = deploymentModel.getImage();
        String imageTag = dockerImage.substring(dockerImage.lastIndexOf(":") + 1, dockerImage.length());
        dockerModel.setBaseImage(deploymentModel.getBaseImage());
        dockerModel.setName(dockerImage);
        dockerModel.setTag(imageTag);
        dockerModel.setEnableDebug(false);
        dockerModel.setUsername(deploymentModel.getUsername());
        dockerModel.setPassword(deploymentModel.getPassword());
        dockerModel.setPush(deploymentModel.isPush());
        dockerModel.setBalxFileName(KubernetesUtils.extractBalxName(balxFilePath) + BALX);
        dockerModel.setPorts(deploymentModel.getPorts());
        dockerModel.setService(true);
        dockerModel.setDockerHost(deploymentModel.getDockerHost());
        dockerModel.setDockerCertPath(deploymentModel.getDockerCertPath());
        dockerModel.setBuildImage(deploymentModel.isBuildImage());
        dockerModel.setCommandArg(deploymentModel.getCommandArgs());

        DockerHandler dockerArtifactHandler = new DockerHandler(dockerModel);
        String dockerContent = dockerArtifactHandler.generate();
        try {
            out.print("@kubernetes:Docker \t\t\t - complete 0/3 \r");
            String dockerOutputDir = outputDir + File.separator + DOCKER;
            KubernetesUtils.writeToFile(dockerContent, dockerOutputDir + File.separator + "Dockerfile");
            out.print("@kubernetes:Docker \t\t\t - complete 1/3 \r");
            String balxDestination = dockerOutputDir + File.separator + KubernetesUtils.extractBalxName
                    (balxFilePath) + BALX;
            KubernetesUtils.copyFile(balxFilePath, balxDestination);
            //check image build is enabled.
            if (dockerModel.isBuildImage()) {
                dockerArtifactHandler.buildImage(dockerModel, dockerOutputDir);
                out.print("@kubernetes:Docker \t\t\t - complete 2/3 \r");
                Files.delete(Paths.get(balxDestination));
                //push only if image build is enabled.
                if (dockerModel.isPush()) {
                    dockerArtifactHandler.pushImage(dockerModel);
                }
                out.print("@kubernetes:Docker \t\t\t - complete 3/3");
            }
        } catch (IOException e) {
            throw new KubernetesPluginException("Unable to write Dockerfile content");
        } catch (InterruptedException e) {
            throw new KubernetesPluginException("Unable to create docker images " + e.getMessage());
        }
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
        deploymentModel.setNamespace(KubernetesConstants.DEPLOYMENT_NAMESPACE_DEFAULT);
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
