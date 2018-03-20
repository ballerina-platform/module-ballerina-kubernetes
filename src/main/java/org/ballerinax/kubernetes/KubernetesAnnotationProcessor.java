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
import org.ballerinalang.model.tree.AnnotationAttachmentNode;
import org.ballerinax.kubernetes.exceptions.KubernetesPluginException;
import org.ballerinax.kubernetes.handlers.DeploymentHandler;
import org.ballerinax.kubernetes.handlers.DockerHandler;
import org.ballerinax.kubernetes.handlers.HPAHandler;
import org.ballerinax.kubernetes.handlers.IngressHandler;
import org.ballerinax.kubernetes.handlers.SecretHandler;
import org.ballerinax.kubernetes.handlers.ServiceHandler;
import org.ballerinax.kubernetes.models.DeploymentModel;
import org.ballerinax.kubernetes.models.DockerModel;
import org.ballerinax.kubernetes.models.IngressModel;
import org.ballerinax.kubernetes.models.KubernetesDataHolder;
import org.ballerinax.kubernetes.models.PodAutoscalerModel;
import org.ballerinax.kubernetes.models.SecretModel;
import org.ballerinax.kubernetes.models.ServiceModel;
import org.ballerinax.kubernetes.utils.KubernetesUtils;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangArrayLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
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
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Process Kubernetes Annotations and generate Artifacts.
 */
class KubernetesAnnotationProcessor {

    private static final String DOCKER = "docker";
    private static final String BALX = ".balx";
    private static final String DEPLOYMENT_POSTFIX = "-deployment";
    private static final String SVC_POSTFIX = "-svc";
    private static final String INGRESS_POSTFIX = "-ingress";
    private static final String HPA_POSTFIX = "-hpa";
    private static final String DEPLOYMENT_FILE_POSTFIX = "_deployment";
    private static final String SVC_FILE_POSTFIX = "_svc";
    private static final String SECRET_FILE_POSTFIX = "_secret";
    private static final String INGRESS_FILE_POSTFIX = "_ingress";
    private static final String HPA_FILE_POSTFIX = "_hpa";
    private static final String YAML = ".yaml";
    private static final String DOCKER_LATEST_TAG = ":latest";
    private static final String INGRESS_HOSTNAME_POSTFIX = ".com";
    private static final String DEFAULT_BASE_IMAGE = "ballerina/ballerina:latest";
    private PrintStream out = System.out;

    /**
     * Generate label map by splitting the labels string.
     *
     * @param labels labels string.
     * @return Map of labels with selector.
     */
    private Map<String, String> getLabelMap(String labels) {
        Map<String, String> labelMap = new HashMap<>();
        if (labels != null) {
            labelMap = Pattern.compile("\\s*,\\s*")
                    .splitAsStream(labels.trim())
                    .map(s -> s.split(":", 2))
                    .collect(Collectors.toMap(a -> a[0], a -> a.length > 1 ? a[1] : ""));
        }
        return labelMap;
    }

    /**
     * Generate environment variable map by splitting the env string.
     *
     * @param env env string.
     * @return Map of environment variables.
     */
    private Map<String, String> getEnvVars(String env) {
        if (env == null) {
            return null;
        }
        return Pattern.compile("\\s*,\\s*")
                .splitAsStream(env.trim())
                .map(s -> s.split(":", 2))
                .collect(Collectors.toMap(a -> a[0], a -> a.length > 1 ? a[1] : ""));
    }


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
        deploymentModel.setPorts(kubernetesDataHolder.getPorts());
        deploymentModel.setPodAutoscalerModel(kubernetesDataHolder.getPodAutoscalerModel());
        deploymentModel.setSecretModels(kubernetesDataHolder.getSecrets());
        generateDeployment(deploymentModel, balxFilePath, outputDir);
        out.println();
        out.println("@kubernetes:deployment \t\t - complete 1/1");

        //svc
        Collection<ServiceModel> serviceModels = kubernetesDataHolder.getEndpointToServiceModelMap().values();
        int count = 0;
        for (ServiceModel serviceModel : serviceModels) {
            count++;
            generateService(serviceModel, balxFilePath, outputDir);
            out.print("@kubernetes:service \t\t - complete " + count + "/" + serviceModels.size() + "\r");
        }
        out.println();

        //ingress
        count = 0;
        Map<IngressModel, Set<String>> ingressModels = kubernetesDataHolder.getIngressToEndpointMap();
        int size = ingressModels.size();
        Map<String, ServiceModel> endpointMap = kubernetesDataHolder.getEndpointToServiceModelMap();
        Iterator<Map.Entry<IngressModel, Set<String>>> iterator = ingressModels.entrySet().iterator();
        Map<String, Set<SecretModel>> secretModelsMap = kubernetesDataHolder.getSecretModels();
        while (iterator.hasNext()) {
            Map.Entry<IngressModel, Set<String>> pair = iterator.next();
            IngressModel ingressModel = pair.getKey();
            Set<String> endpoints = pair.getValue();
            for (String endpointName : endpoints) {
                ServiceModel serviceModel = endpointMap.get(endpointName);
                ingressModel.setServiceName(serviceModel.getName());
                ingressModel.setServicePort(serviceModel.getPort());
                if (secretModelsMap.get(endpointName) != null && secretModelsMap.get(endpointName).size() != 0) {
                    ingressModel.setEnableTLS(true);
                }
            }
            generateIngress(ingressModel, balxFilePath, outputDir);
            count++;
            out.print("@kubernetes:ingress \t\t - complete " + count + "/" + size + "\r");
            iterator.remove();
        }

        //secret
        count = 0;
        Collection<SecretModel> secretModels = kubernetesDataHolder.getSecrets();
        for (SecretModel secretModel : secretModels) {
            count++;
            generateSecrets(secretModel, balxFilePath, outputDir);
            out.print("@kubernetes:secret \t\t - complete " + count + "/" + secretModels.size() + "\r");
        }
        out.println();

        printKubernetesInstructions(outputDir);
    }


    private void generateDeployment(DeploymentModel deploymentModel, String balxFilePath, String outputDir) throws
            KubernetesPluginException {
        String balxFileName = KubernetesUtils.extractBalxName(balxFilePath);
        if (deploymentModel.getName() == null) {
            deploymentModel.setName(getValidName(balxFileName) + DEPLOYMENT_POSTFIX);
        }
        if (deploymentModel.getImage() == null) {
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
            throw new KubernetesPluginException("Error while writing service content", e);
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
            out.print("@kubernetes:hpa \t\t - complete 1/1");
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
        dockerModel.setBuildImage(deploymentModel.isBuildImage());
        DockerHandler dockerArtifactHandler = new DockerHandler(dockerModel);
        String dockerContent = dockerArtifactHandler.generate();
        try {
            out.print("@docker \t\t\t - complete 0/3 \r");
            KubernetesUtils.writeToFile(dockerContent, outputDir + File.separator + "Dockerfile");
            out.print("@docker \t\t\t - complete 1/3 \r");
            String balxDestination = outputDir + File.separator + KubernetesUtils.extractBalxName
                    (balxFilePath) + BALX;
            KubernetesUtils.copyFile(balxFilePath, balxDestination);
            //check image build is enabled.
            if (dockerModel.isBuildImage()) {
                dockerArtifactHandler.buildImage(dockerModel, outputDir);
                out.print("@docker \t\t\t - complete 2/3 \r");
                Files.delete(Paths.get(balxDestination));
                //push only if image build is enabled.
                if (dockerModel.isPush()) {
                    dockerArtifactHandler.pushImage(dockerModel);
                }
                out.print("@docker \t\t\t - complete 3/3");
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
        deploymentModel.setEnv(getEnvVars(null));
        deploymentModel.setBaseImage(DEFAULT_BASE_IMAGE);
        deploymentModel.setImage(balxName + DOCKER_LATEST_TAG);
        deploymentModel.setBuildImage(true);
        deploymentModel.setPush(false);

        return deploymentModel;
    }

    /**
     * Process annotations and create deployment model object.
     *
     * @param attachmentNode annotation attachment node.
     * @return Deployment model object
     */
    DeploymentModel processDeployment(AnnotationAttachmentNode attachmentNode) {
        DeploymentModel deploymentModel = new DeploymentModel();
        List<BLangRecordLiteral.BLangRecordKeyValue> keyValues =
                ((BLangRecordLiteral) ((BLangAnnotationAttachment) attachmentNode).expr).getKeyValuePairs();
        for (BLangRecordLiteral.BLangRecordKeyValue keyValue : keyValues) {
            DeploymentConfiguration deploymentConfiguration =
                    DeploymentConfiguration.valueOf(keyValue.getKey().toString());
            String annotationValue = keyValue.getValue().toString();
            switch (deploymentConfiguration) {
                case name:
                    deploymentModel.setName(getValidName(annotationValue));
                    break;
                case labels:
                    deploymentModel.setLabels(getLabelMap(annotationValue));
                    break;
                case enableLiveness:
                    deploymentModel.setEnableLiveness(annotationValue);
                    break;
                case livenessPort:
                    deploymentModel.setLivenessPort(Integer.parseInt(annotationValue));
                    break;
                case initialDelaySeconds:
                    deploymentModel.setInitialDelaySeconds(Integer.parseInt(annotationValue));
                    break;
                case periodSeconds:
                    deploymentModel.setPeriodSeconds(Integer.parseInt(annotationValue));
                    break;
                case username:
                    deploymentModel.setUsername(annotationValue);
                    break;
                case env:
                    deploymentModel.setEnv(getEnvVars(annotationValue));
                    break;
                case password:
                    deploymentModel.setPassword(annotationValue);
                    break;
                case baseImage:
                    deploymentModel.setBaseImage(annotationValue);
                    break;
                case push:
                    deploymentModel.setPush(Boolean.valueOf(annotationValue));
                    break;
                case buildImage:
                    deploymentModel.setBuildImage(Boolean.valueOf(annotationValue));
                    break;
                case image:
                    deploymentModel.setImage(annotationValue);
                    break;
                case dockerHost:
                    deploymentModel.setDockerHost(annotationValue);
                    break;
                case imagePullPolicy:
                    deploymentModel.setImagePullPolicy(annotationValue);
                    break;
                case replicas:
                    deploymentModel.setReplicas(Integer.parseInt(annotationValue));
                    break;
                default:
                    break;
            }
        }
        return deploymentModel;
    }

    /**
     * Process annotations and create service model object.
     *
     * @param endpointName   ballerina service name
     * @param attachmentNode annotation attachment node.
     * @return Service model object
     */
    ServiceModel processServiceAnnotation(String endpointName, AnnotationAttachmentNode attachmentNode) {
        ServiceModel serviceModel = new ServiceModel();
        List<BLangRecordLiteral.BLangRecordKeyValue> keyValues =
                ((BLangRecordLiteral) ((BLangAnnotationAttachment) attachmentNode).expr).getKeyValuePairs();
        for (BLangRecordLiteral.BLangRecordKeyValue keyValue : keyValues) {
            ServiceConfiguration serviceConfiguration =
                    ServiceConfiguration.valueOf(keyValue.getKey().toString());
            String annotationValue = keyValue.getValue().toString();
            switch (serviceConfiguration) {
                case name:
                    serviceModel.setName(getValidName(annotationValue));
                    break;
                case labels:
                    serviceModel.setLabels(getLabelMap(annotationValue));
                    break;
                case serviceType:
                    serviceModel.setServiceType(annotationValue);
                    break;
                case port:
                    serviceModel.setPort(Integer.parseInt(annotationValue));
                    break;
                default:
                    break;
            }
        }
        if (serviceModel.getName() == null) {
            serviceModel.setName(getValidName(endpointName) + SVC_POSTFIX);
        }
        return serviceModel;
    }

    /**
     * Process annotations and create service model object.
     *
     * @param attachmentNode annotation attachment node.
     * @return Service model object
     */
    PodAutoscalerModel processPodAutoscalerAnnotation(AnnotationAttachmentNode attachmentNode) {
        PodAutoscalerModel podAutoscalerModel = new PodAutoscalerModel();
        List<BLangRecordLiteral.BLangRecordKeyValue> keyValues =
                ((BLangRecordLiteral) ((BLangAnnotationAttachment) attachmentNode).expr).getKeyValuePairs();
        for (BLangRecordLiteral.BLangRecordKeyValue keyValue : keyValues) {
            PodAutoscalerConfiguration podAutoscalerConfiguration =
                    PodAutoscalerConfiguration.valueOf(keyValue.getKey().toString());
            String annotationValue = keyValue.getValue().toString();
            switch (podAutoscalerConfiguration) {
                case name:
                    podAutoscalerModel.setName(getValidName(annotationValue));
                    break;
                case labels:
                    podAutoscalerModel.setLabels(getLabelMap(annotationValue));
                    break;
                case cpuPercentage:
                    podAutoscalerModel.setCpuPercentage(Integer.parseInt(annotationValue));
                    break;
                case minReplicas:
                    podAutoscalerModel.setMinReplicas(Integer.parseInt(annotationValue));
                    break;
                case maxReplicas:
                    podAutoscalerModel.setMaxReplicas(Integer.parseInt(annotationValue));
                    break;
                default:
                    break;
            }
        }
        return podAutoscalerModel;
    }

    /**
     * Process annotations and create Ingress model object.
     *
     * @param serviceName    Ballerina service name
     * @param attachmentNode annotation attachment node.
     * @return Ingress model object
     */
    IngressModel processIngressAnnotation(String serviceName, AnnotationAttachmentNode attachmentNode) {
        IngressModel ingressModel = new IngressModel();
        List<BLangRecordLiteral.BLangRecordKeyValue> keyValues =
                ((BLangRecordLiteral) ((BLangAnnotationAttachment) attachmentNode).expr).getKeyValuePairs();
        for (BLangRecordLiteral.BLangRecordKeyValue keyValue : keyValues) {
            IngressConfiguration ingressConfiguration =
                    IngressConfiguration.valueOf(keyValue.getKey().toString());
            String annotationValue = keyValue.getValue().toString();
            switch (ingressConfiguration) {
                case name:
                    ingressModel.setName(getValidName(annotationValue));
                    break;
                case labels:
                    ingressModel.setLabels(getLabelMap(annotationValue));
                    break;
                case path:
                    ingressModel.setPath(annotationValue);
                    break;
                case targetPath:
                    ingressModel.setTargetPath(annotationValue);
                    break;
                case hostname:
                    ingressModel.setHostname(annotationValue);
                    break;
                case ingressClass:
                    ingressModel.setIngressClass(annotationValue);
                    break;
                case enableTLS:
                    ingressModel.setEnableTLS(Boolean.parseBoolean(annotationValue));
                    break;
                default:
                    break;
            }
        }
        if (ingressModel.getName() == null || ingressModel.getName().length() == 0) {
            ingressModel.setName(getValidName(serviceName) + INGRESS_POSTFIX);
        }
        if (ingressModel.getHostname() == null || ingressModel.getHostname().length() == 0) {
            ingressModel.setHostname(getValidName(serviceName) + INGRESS_HOSTNAME_POSTFIX);
        }
        return ingressModel;
    }

    /**
     * Extract key-store/trust-store file location from endpoint.
     *
     * @param endpointName Endpoint name
     * @param sslKeyValues SSL annotation struct
     * @return List of @{@link SecretModel} objects
     */
    Set<SecretModel> processSSLAnnotation(String endpointName, List<BLangRecordLiteral.BLangRecordKeyValue>
            sslKeyValues) throws KubernetesPluginException {
        Set<SecretModel> secrets = new HashSet<>();
        for (BLangRecordLiteral.BLangRecordKeyValue keyValue : sslKeyValues) {
            //extract file paths.
            String key = keyValue.getKey().toString();
            String value = keyValue.getValue().toString();
            SecretModel secretModel = null;
            if ("keyStoreFile".equals(key)) {
                secretModel = new SecretModel();
                secretModel.setName(getValidName(endpointName) + "-keystore");
            } else if ("trustStoreFile".equals(key)) {
                secretModel = new SecretModel();
                secretModel.setName(getValidName(endpointName) + "-truststore");
            } else {
                continue;
            }
            //TODO: Fix this
            String secretFilePath = value;
            String mountPath = value;
            if (value.contains("${ballerina.home}")) {
                // Resolve variable locally before reading file.
                String ballerinaHome = System.getProperty("ballerina.home");
                secretFilePath = value.replace("${ballerina.home}", ballerinaHome);
                // replace mount path with container's ballerina.home
                mountPath = value.replace("${ballerina.home}", "/ballerina/runtime");
            }
            Path dataFilePath = Paths.get(secretFilePath);
            mountPath = String.valueOf(Paths.get(mountPath).getParent());
            Map<String, String> dataMap = new HashMap<>();
            String content = Base64.encodeBase64String(KubernetesUtils.readFileContent(dataFilePath));
            dataMap.put(String.valueOf(dataFilePath.getFileName()), content);
            secretModel.setData(dataMap);
            secretModel.setMountPath(mountPath);
            secrets.add(secretModel);
        }
        return secrets;
    }

    /**
     * Process Secrets annotations.
     *
     * @param attachmentNode Attachment Node
     * @return List of @{@link SecretModel} objects
     */
    Set<SecretModel> processSecrets(AnnotationAttachmentNode attachmentNode) throws KubernetesPluginException {
        Set<SecretModel> secrets = new HashSet<>();
        List<BLangRecordLiteral.BLangRecordKeyValue> keyValues =
                ((BLangRecordLiteral) ((BLangAnnotationAttachment) attachmentNode).expr).getKeyValuePairs();
        for (BLangRecordLiteral.BLangRecordKeyValue keyValue : keyValues) {
            List<BLangExpression> secretAnnotation = ((BLangArrayLiteral) keyValue.valueExpr).exprs;
            for (BLangExpression bLangExpression : secretAnnotation) {
                SecretModel secretModel = new SecretModel();
                List<BLangRecordLiteral.BLangRecordKeyValue> annotationValues =
                        ((BLangRecordLiteral) bLangExpression).getKeyValuePairs();
                for (BLangRecordLiteral.BLangRecordKeyValue annotation : annotationValues) {
                    SecretConfiguration secretConfiguration =
                            SecretConfiguration.valueOf(annotation.getKey().toString());
                    String annotationValue = annotation.getValue().toString();
                    switch (secretConfiguration) {
                        case name:
                            secretModel.setName(getValidName(annotationValue));
                            break;
                        case mountPath:
                            secretModel.setMountPath(annotationValue);
                            break;
                        case data:
                            List<BLangExpression> data = ((BLangArrayLiteral) annotation.valueExpr).exprs;
                            secretModel.setData(getData(data));
                            break;
                        case readOnly:
                            secretModel.setReadOnly(Boolean.parseBoolean(annotationValue));
                            break;
                        default:
                            break;
                    }
                }
                secrets.add(secretModel);
            }
        }
        return secrets;
    }

    private Map<String, String> getData(List<BLangExpression> data) throws KubernetesPluginException {
        Map<String, String> dataMap = new HashMap<>();
        for (BLangExpression bLangExpression : data) {
            List<BLangRecordLiteral.BLangRecordKeyValue> annotationValues =
                    ((BLangRecordLiteral) bLangExpression).getKeyValuePairs();
            String key = null;
            String content = null;
            for (BLangRecordLiteral.BLangRecordKeyValue annotation : annotationValues) {
                if ("key".equals(annotation.getKey().toString())) {
                    key = annotation.getValue().toString();
                } else if ("filePath".equals(annotation.getKey().toString())) {
                    Path dataFilePath = Paths.get(annotation.getValue().toString());
                    content = Base64.encodeBase64String(KubernetesUtils.readFileContent(dataFilePath));
                }
            }
            dataMap.put(key, content);
        }
        return dataMap;
    }

    private String getValidName(String name) {
        return name.toLowerCase(Locale.ENGLISH).replace("_", "-");
    }

    /**
     * Enum class for DeploymentConfiguration.
     */
    private enum DeploymentConfiguration {
        name,
        labels,
        replicas,
        enableLiveness,
        livenessPort,
        initialDelaySeconds,
        periodSeconds,
        imagePullPolicy,
        namespace,
        image,
        env,
        buildImage,
        dockerHost,
        username,
        password,
        baseImage,
        push
    }

    /**
     * Enum class for svc configurations.
     */
    private enum ServiceConfiguration {
        name,
        labels,
        serviceType,
        port
    }

    /**
     * Enum class for svc configurations.
     */
    private enum IngressConfiguration {
        name,
        labels,
        hostname,
        path,
        targetPath,
        ingressClass,
        enableTLS
    }

    /**
     * Enum class for pod autoscaler configurations.
     */
    private enum PodAutoscalerConfiguration {
        name,
        labels,
        minReplicas,
        maxReplicas,
        cpuPercentage
    }

    /**
     * Enum class for secret volume configurations.
     */
    private enum SecretConfiguration {
        name,
        mountPath,
        readOnly,
        data
    }
}
