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

import org.apache.commons.codec.binary.Base64;
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
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static org.ballerinax.kubernetes.KubernetesConstants.INGRESS_FILE_POSTFIX;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.isEmpty;

/**
 * Process Kubernetes Annotations and generate Artifacts.
 */
class KubernetesAnnotationProcessor {

    private static final String DOCKER = "docker";
    private static final String BALX = ".balx";
    private static final String DEPLOYMENT_POSTFIX = "-deployment";
    private static final String HPA_POSTFIX = "-hpa";
    private static final String DEPLOYMENT_FILE_POSTFIX = "_deployment";
    private static final String SVC_FILE_POSTFIX = "_svc";
    private static final String SECRET_FILE_POSTFIX = "_secret";
    private static final String CONFIG_MAP_FILE_POSTFIX = "_config_map";
    private static final String VOLUME_CLAIM_FILE_POSTFIX = "_volume_claim";
    private static final String HPA_FILE_POSTFIX = "_hpa";
    private static final String YAML = ".yaml";
    private static final String DOCKER_LATEST_TAG = ":latest";
    private PrintStream out = System.out;

    /**
     * Generate kubernetes artifacts.
     *
     * @param kubernetesDataHolder Kubernetes data holder object
     * @param balxFilePath         ballerina file path
     * @param outputDir            output directory to save artifacts
     * @throws KubernetesPluginException if an error ocurrs while generating artifacts
     */
    void createArtifacts(KubernetesDataHolder kubernetesDataHolder, String balxFilePath,
                         String outputDir) throws KubernetesPluginException {
        DeploymentModel deploymentModel = kubernetesDataHolder.getDeploymentModel();
        if (deploymentModel == null) {
            deploymentModel = getDefaultDeploymentModel(balxFilePath);
        }
        kubernetesDataHolder.setDeploymentModel(deploymentModel);
//        deploymentModel.setPorts(kubernetesDataHolder.getPorts());
        deploymentModel.setPodAutoscalerModel(kubernetesDataHolder.getPodAutoscalerModel());
        deploymentModel.setSecretModels(kubernetesDataHolder.getSecretModelSet());
        deploymentModel.setConfigMapModels(kubernetesDataHolder.getConfigMapModelSet());
        deploymentModel.setVolumeClaimModels(kubernetesDataHolder.getVolumeClaimModelSet());

        // Service
        Collection<ServiceModel> serviceModels = kubernetesDataHolder.getbEndpointToK8sServiceMap().values();
        int count = 0;
        for (ServiceModel serviceModel : serviceModels) {
            count++;
            generateService(serviceModel, balxFilePath, outputDir);
            out.print("@kubernetes:Service \t\t\t - complete " + count + "/" + serviceModels.size() + "\r");
        }

        //ingress
//        count = 0;
//        Map<IngressModel, Set<String>> ingressModels = kubernetesDataHolder.getbServiceToIngressMap();
//        if (ingressModels.size() > 0) {
//            out.println();
//        }
//        int size = ingressModels.size();
//        Map<String, ServiceModel> endpointMap = kubernetesDataHolder.getEndpointToServiceModelMap();
//        Iterator<Map.Entry<IngressModel, Set<String>>> iterator = ingressModels.entrySet().iterator();
//        Map<String, Set<SecretModel>> secretModelsMap = kubernetesDataHolder.getSecretModels();
//        while (iterator.hasNext()) {
//            Map.Entry<IngressModel, Set<String>> pair = iterator.next();
//            IngressModel ingressModel = pair.getKey();
//            Set<String> endpoints = pair.getValue();
//            for (String endpointName : endpoints) {
//                ServiceModel serviceModel = endpointMap.get(endpointName);
//                ingressModel.setBalxName(serviceModel.getName());
//                ingressModel.setServicePort(serviceModel.getPort());
//                if (secretModelsMap.get(endpointName) != null && secretModelsMap.get(endpointName).size() != 0) {
//                    ingressModel.setEnableTLS(true);
//                }
//            }
//            generateIngress(ingressModel, balxFilePath, outputDir);
//            count++;
//            out.print("@kubernetes:Ingress \t\t\t - complete " + count + "/" + size + "\r");
//            iterator.remove();
//        }

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
            generateConfigMaps(configMapModel, balxFilePath, outputDir);
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
            generatePersistentVolumeClaim(claimModel, balxFilePath, outputDir);
            out.print("@kubernetes:VolumeClaim \t\t - complete " + count + "/" + volumeClaims.size() + "\r");
        }
        out.println();
        generateDeployment(deploymentModel, balxFilePath, outputDir);
        out.println();
        out.println("@kubernetes:Deployment \t\t\t - complete 1/1");

        printKubernetesInstructions(outputDir);
    }


    private void generateDeployment(DeploymentModel deploymentModel, String balxFilePath, String outputDir) throws
            KubernetesPluginException {
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
            generateDocker(deploymentModel, balxFilePath, outputDir + File.separator + DOCKER);
            // generate HPA
            generatePodAutoscaler(deploymentModel, balxFilePath, outputDir);
        } catch (IOException e) {
            throw new KubernetesPluginException("Error while writing deployment content", e);
        }
    }

    private void generateService(ServiceModel serviceModel, String balxFilePath, String outputDir) throws
            KubernetesPluginException {
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

    private void generateConfigMaps(ConfigMapModel configMapModel, String balxFilePath, String outputDir) throws
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

    private void generatePersistentVolumeClaim(PersistentVolumeClaimModel volumeClaimModel, String balxFilePath,
                                               String outputDir) throws KubernetesPluginException {
        String balxFileName = KubernetesUtils.extractBalxName(balxFilePath);
        String configMapContent = new PersistentVolumeClaimHandler(volumeClaimModel).generate();
        try {
            KubernetesUtils.writeToFile(configMapContent, outputDir + File
                    .separator + balxFileName + VOLUME_CLAIM_FILE_POSTFIX + YAML);
        } catch (IOException e) {
            throw new KubernetesPluginException("Error while writing volume claim content", e);
        }
    }

    private void generateIngress(IngressModel ingressModel, String balxFilePath, String outputDir) throws
            KubernetesPluginException {
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

    private void generatePodAutoscaler(DeploymentModel deploymentModel, String balxFilePath, String outputDir)
            throws KubernetesPluginException {
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
     * @param balxFilePath    output ballerina file path
     * @param outputDir       output directory which data should be written
     * @throws KubernetesPluginException If an error occurs while generating artifacts
     */
    private void generateDocker(DeploymentModel deploymentModel, String balxFilePath, String outputDir)
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
            KubernetesUtils.writeToFile(dockerContent, outputDir + File.separator + "Dockerfile");
            out.print("@kubernetes:Docker \t\t\t - complete 1/3 \r");
            String balxDestination = outputDir + File.separator + KubernetesUtils.extractBalxName
                    (balxFilePath) + BALX;
            KubernetesUtils.copyFile(balxFilePath, balxDestination);
            //check image build is enabled.
            if (dockerModel.isBuildImage()) {
                dockerArtifactHandler.buildImage(dockerModel, outputDir);
                out.print("@kubernetes:Docker \t\t\t - complete 2/3 \r");
                Files.delete(Paths.get(balxDestination));
                //push only if image build is enabled.
                if (dockerModel.isPush()) {
                    dockerArtifactHandler.pushImage(dockerModel);
                }
                out.print("@kubernetes:Docker \t\t\t - complete 3/3");
            }
        } catch (IOException e) {
            throw new KubernetesPluginException("Unable to write Dockerfile content to " + outputDir);
        } catch (InterruptedException e) {
            throw new KubernetesPluginException("Unable to create docker images " + e.getMessage());
        }
    }


    /**
     * Get DeploymentModel object with default values.
     *
     * @param balxFilePath ballerina file path
     * @return DeploymentModel object with default values
     */
    private DeploymentModel getDefaultDeploymentModel(String balxFilePath) {
        DeploymentModel deploymentModel = new DeploymentModel();
        String balxName = KubernetesUtils.extractBalxName(balxFilePath);
        String deploymentName = balxName + "-deployment";
        deploymentModel.setName(getValidName(deploymentName));
        String namespace = KubernetesConstants.DEPLOYMENT_NAMESPACE_DEFAULT;
        deploymentModel.setNamespace(namespace);
        String imagePullPolicy = KubernetesConstants.DEPLOYMENT_IMAGE_PULL_POLICY_DEFAULT;
        deploymentModel.setImagePullPolicy(imagePullPolicy);
        String liveness = KubernetesConstants.DEPLOYMENT_LIVENESS_DISABLE;
        deploymentModel.setEnableLiveness(liveness);
        int defaultReplicas = 1;
        deploymentModel.setReplicas(defaultReplicas);
        deploymentModel.addLabel(KubernetesConstants.KUBERNETES_SELECTOR_KEY, balxName);
        deploymentModel.setEnv(new HashMap<String, String>());
        deploymentModel.setImage(balxName + DOCKER_LATEST_TAG);
        deploymentModel.setBuildImage(true);
        deploymentModel.setPush(false);

        return deploymentModel;
    }


    /**
     * Extract key-store/trust-store file location from endpoint.
     *
     * @param endpointName          Endpoint name
     * @param secureSocketKeyValues secureSocket annotation struct
     * @return List of @{@link SecretModel} objects
     */
    Set<SecretModel> processSecureSocketAnnotation(String endpointName, List<BLangRecordLiteral.BLangRecordKeyValue>
            secureSocketKeyValues) throws KubernetesPluginException {
        Set<SecretModel> secrets = new HashSet<>();
        String keyStoreFile = null;
        String trustStoreFile = null;
        for (BLangRecordLiteral.BLangRecordKeyValue keyValue : secureSocketKeyValues) {
            //extract file paths.
            String key = keyValue.getKey().toString();
            if ("keyStore".equals(key)) {
                keyStoreFile = extractFilePath(keyValue);
            } else if ("trustStore".equals(key)) {
                trustStoreFile = extractFilePath(keyValue);
            }
        }
        if (keyStoreFile != null && trustStoreFile != null) {
            if (getMountPath(keyStoreFile).equals(getMountPath(trustStoreFile))) {
                // trust-store and key-store mount to same path
                String keyStoreContent = readSecretFile(keyStoreFile);
                String trustStoreContent = readSecretFile(trustStoreFile);
                SecretModel secretModel = new SecretModel();
                secretModel.setName(getValidName(endpointName) + "-secure-socket");
                secretModel.setMountPath(getMountPath(keyStoreFile));
                Map<String, String> dataMap = new HashMap<>();
                dataMap.put(String.valueOf(Paths.get(keyStoreFile).getFileName()), keyStoreContent);
                dataMap.put(String.valueOf(Paths.get(trustStoreFile).getFileName()), trustStoreContent);
                secretModel.setData(dataMap);
                secrets.add(secretModel);
                return secrets;
            }
        }
        if (keyStoreFile != null) {
            String keyStoreContent = readSecretFile(keyStoreFile);
            SecretModel secretModel = new SecretModel();
            secretModel.setName(getValidName(endpointName) + "-keystore");
            secretModel.setMountPath(getMountPath(keyStoreFile));
            Map<String, String> dataMap = new HashMap<>();
            dataMap.put(String.valueOf(Paths.get(keyStoreFile).getFileName()), keyStoreContent);
            secretModel.setData(dataMap);
            secrets.add(secretModel);
        }
        if (trustStoreFile != null) {
            String trustStoreContent = readSecretFile(trustStoreFile);
            SecretModel secretModel = new SecretModel();
            secretModel.setName(getValidName(endpointName) + "-truststore");
            secretModel.setMountPath(getMountPath(trustStoreFile));
            Map<String, String> dataMap = new HashMap<>();
            dataMap.put(String.valueOf(Paths.get(trustStoreFile).getFileName()), trustStoreContent);
            secretModel.setData(dataMap);
            secrets.add(secretModel);
        }
        return secrets;
    }


    private String readSecretFile(String filePath) throws KubernetesPluginException {
        if (filePath.contains("${ballerina.home}")) {
            // Resolve variable locally before reading file.
            String ballerinaHome = System.getProperty("ballerina.home");
            filePath = filePath.replace("${ballerina.home}", ballerinaHome);
        }
        Path dataFilePath = Paths.get(filePath);
        return Base64.encodeBase64String(KubernetesUtils.readFileContent(dataFilePath));
    }

    private String getMountPath(String mountPath) {
        if (mountPath.contains("${ballerina.home}")) {
            // replace mount path with container's ballerina.home
            mountPath = mountPath.replace("${ballerina.home}", "/ballerina/runtime");
        }
        return String.valueOf(Paths.get(mountPath).getParent());
    }

    private String extractFilePath(BLangRecordLiteral.BLangRecordKeyValue keyValue) {
        List<BLangRecordLiteral.BLangRecordKeyValue> keyStoreConfigs = ((BLangRecordLiteral) keyValue
                .valueExpr).getKeyValuePairs();
        for (BLangRecordLiteral.BLangRecordKeyValue keyStoreConfig : keyStoreConfigs) {
            String configKey = keyStoreConfig.getKey().toString();
            if ("filePath".equals(configKey)) {
                return keyStoreConfig.getValue().toString();
            }
        }
        return null;
    }


    private String getValidName(String name) {
        return name.toLowerCase(Locale.ENGLISH).replace("_", "-");
    }

}
