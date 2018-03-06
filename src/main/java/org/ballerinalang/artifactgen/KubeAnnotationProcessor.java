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

package org.ballerinalang.artifactgen;

import org.ballerinalang.artifactgen.exceptions.ArtifactGenerationException;
import org.ballerinalang.artifactgen.handlers.DeploymentHandler;
import org.ballerinalang.artifactgen.handlers.DockerHandler;
import org.ballerinalang.artifactgen.handlers.HPAHandler;
import org.ballerinalang.artifactgen.handlers.IngressHandler;
import org.ballerinalang.artifactgen.handlers.ServiceHandler;
import org.ballerinalang.artifactgen.models.DeploymentModel;
import org.ballerinalang.artifactgen.models.DockerModel;
import org.ballerinalang.artifactgen.models.IngressModel;
import org.ballerinalang.artifactgen.models.PodAutoscalerModel;
import org.ballerinalang.artifactgen.models.ServiceModel;
import org.ballerinalang.artifactgen.utils.KubeGenUtils;
import org.ballerinalang.net.http.HttpConstants;
import org.ballerinalang.util.codegen.AnnAttachmentInfo;
import org.ballerinalang.util.codegen.AnnAttributeValue;
import org.ballerinalang.util.codegen.ServiceInfo;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.ballerinalang.artifactgen.KubeGenConstants.DEPLOYMENT_LIVENESS_ENABLE;
import static org.ballerinalang.artifactgen.utils.KubeGenUtils.printDebug;
import static org.ballerinalang.artifactgen.utils.KubeGenUtils.printError;
import static org.ballerinalang.artifactgen.utils.KubeGenUtils.printInfo;
import static org.ballerinalang.artifactgen.utils.KubeGenUtils.printInstruction;
import static org.ballerinalang.artifactgen.utils.KubeGenUtils.printSuccess;

/**
 * Process Kubernetes Annotations and generate Artifacts.
 */
class KubeAnnotationProcessor {

    private static final String KUBERNETES = "kubernetes";
    private static final String DOCKER = "docker";
    private static final String BALX = ".balx";
    private static final String DEPLOYMENT_POSTFIX = "-deployment.yaml";
    private static final String SVC_POSTFIX = "-svc.yaml";
    private static final String INGRESS_POSTFIX = "-ingress.yaml";
    private static final String AUTOSCALER_POSTFIX = "-hpa.yaml";
    private static final String SVC_TYPE_NODE_PORT = "NodePort";
    private static final String DOCKER_LATEST_TAG = ":latest";
    private static final String INGRESS_CLASS_NGINX = "nginx";
    private static final String INGRESS_HOSTNAME_POSTFIX = ".com";
    private static final String DEFAULT_BASE_IMAGE = "ballerina/ballerina:latest";
    private static Set<Integer> ports = new HashSet<>();

    /**
     * Process deployment annotations for ballerina Service.
     *
     * @param serviceInfo  ServiceInfo Object
     * @param balxFilePath ballerina file name
     * @param outputDir    target output directory
     */
    void processDeploymentAnnotationForService(ServiceInfo serviceInfo, String balxFilePath, String
            outputDir) {
        AnnAttachmentInfo deploymentAnnotationInfo = null;
        if (serviceInfo != null) {
            deploymentAnnotationInfo = serviceInfo.getAnnotationAttachmentInfo
                    (KubeGenConstants.KUBERNETES_ANNOTATION_PACKAGE, KubeGenConstants.DEPLOYMENT_ANNOTATION);
        }
        DeploymentModel deploymentModel;
        if (deploymentAnnotationInfo != null) {
            deploymentModel = getDeploymentModel(serviceInfo, balxFilePath);
            // Process HPA Annotation only if deployment annotation is present
            AnnAttachmentInfo ingressAnnotationInfo = serviceInfo.getAnnotationAttachmentInfo
                    (KubeGenConstants.KUBERNETES_ANNOTATION_PACKAGE, KubeGenConstants.HPA_ANNOTATION);
            if (ingressAnnotationInfo != null) {
                processHPAAnnotationForService(serviceInfo, deploymentModel, balxFilePath, outputDir);
            }
        } else {
            deploymentModel = getDefaultDeploymentModel(balxFilePath);
        }

        //generate dockerfile and docker image
        DockerModel dockerModel = new DockerModel();
        String dockerImage = deploymentModel.getImage();
        String imageTag = dockerImage.substring(dockerImage.lastIndexOf(":") + 1, dockerImage.length());
        dockerModel.setBaseImage(deploymentModel.getBaseImage());
        dockerModel.setName(dockerImage);
        dockerModel.setTag(imageTag);
        dockerModel.setDebugEnable(false);
        dockerModel.setUsername(deploymentModel.getUsername());
        dockerModel.setPassword(deploymentModel.getPassword());
        dockerModel.setPush(deploymentModel.isPush());
        String balxFileName = KubeGenUtils.extractBalxName(balxFilePath) + BALX;
        dockerModel.setBalxFileName(balxFileName);
        dockerModel.setBalxFilePath(balxFileName);
        dockerModel.setPorts(deploymentModel.getPorts());
        dockerModel.setService(true);
        dockerModel.setImageBuild(deploymentModel.isImageBuild());
        createDockerArtifacts(dockerModel, balxFilePath, outputDir + File.separator + KUBERNETES + File
                .separator + DOCKER);
        printDebug(deploymentModel.toString());
        createDeploymentArtifacts(deploymentModel, outputDir, balxFilePath);
        printKubernetesInstructions(outputDir);
    }


    /**
     * Process svc annotations for ballerina Service.
     *
     * @param serviceInfo  ServiceInfo Object
     * @param balxFilePath ballerina file name
     * @param outputDir    target output directory
     */
    void processSvcAnnotationForService(ServiceInfo serviceInfo, String balxFilePath, String
            outputDir) {
        AnnAttachmentInfo svcAnnotationInfo = serviceInfo.getAnnotationAttachmentInfo
                (KubeGenConstants.KUBERNETES_ANNOTATION_PACKAGE, KubeGenConstants.SERVICE_ANNOTATION);
        if (svcAnnotationInfo == null) {
            return;
        }
        ServiceModel serviceModel = new ServiceModel();

        String serviceName = svcAnnotationInfo.getAttributeValue(KubeGenConstants.SVC_NAME)
                != null ?
                svcAnnotationInfo.getAttributeValue(KubeGenConstants.SVC_NAME).getStringValue() :
                serviceInfo.getName();
        //TODO: validate service name with regex.
        serviceModel.setName(serviceName.toLowerCase(Locale.ENGLISH));

        String labels = svcAnnotationInfo.getAttributeValue(KubeGenConstants.SVC_LABELS) != null ?
                svcAnnotationInfo.getAttributeValue(KubeGenConstants.SVC_LABELS).getStringValue() :
                null;
        serviceModel.setLabels(getLabelMap(labels, KubeGenUtils.extractBalxName(balxFilePath)));

        String serviceType = svcAnnotationInfo.getAttributeValue(KubeGenConstants.SVC_SERVICE_TYPE)
                != null ?
                svcAnnotationInfo.getAttributeValue(KubeGenConstants.SVC_SERVICE_TYPE).getStringValue() :
                SVC_TYPE_NODE_PORT;
        serviceModel.setServiceType(serviceType);
        serviceModel.setSelector(KubeGenUtils.extractBalxName(balxFilePath));
        AnnAttachmentInfo annotationInfo = serviceInfo.getAnnotationAttachmentInfo(HttpConstants
                .HTTP_PACKAGE_PATH, HttpConstants.ANN_NAME_CONFIG);
        AnnAttributeValue portAttrVal = annotationInfo.getAttributeValue(HttpConstants.ANN_CONFIG_ATTR_PORT);
        if (portAttrVal != null && portAttrVal.getIntValue() > 0) {
            int port = Math.toIntExact(portAttrVal.getIntValue());
            serviceModel.setPort(port);
            ports.add(port);
        } else {
            //TODO: default port hardcoded.
            serviceModel.setPort(9090);
            ports.add(9090);
        }
        printDebug(serviceModel.toString());
        try {
            String svcContent = new ServiceHandler(serviceModel).generate();
            KubeGenUtils.writeToFile(svcContent, outputDir + File.separator + KUBERNETES + File
                    .separator + serviceInfo.getName() + SVC_POSTFIX);
            printSuccess("Service yaml generated.");
        } catch (IOException e) {
            printError("Unable to write service content to " + outputDir);
        } catch (ArtifactGenerationException e) {
            printError("Unable to generate service  " + e.getMessage());
        }
        // Process Ingress Annotation only if svc annotation is present
        AnnAttachmentInfo ingressAnnotationInfo = serviceInfo.getAnnotationAttachmentInfo
                (KubeGenConstants.KUBERNETES_ANNOTATION_PACKAGE, KubeGenConstants.INGRESS_ANNOTATION);
        if (ingressAnnotationInfo != null) {
            processIngressAnnotationForService(serviceInfo, serviceModel, balxFilePath, outputDir);
        }
    }

    /**
     * Process ingress annotations for ballerina Service.
     *
     * @param serviceInfo  ServiceInfo Object
     * @param balxFilePath ballerina file name
     * @param outputDir    target output directory
     */
    private void processIngressAnnotationForService(ServiceInfo serviceInfo, ServiceModel svc, String
            balxFilePath, String outputDir) {
        AnnAttachmentInfo ingressAnnotationInfo = serviceInfo.getAnnotationAttachmentInfo
                (KubeGenConstants.KUBERNETES_ANNOTATION_PACKAGE, KubeGenConstants.INGRESS_ANNOTATION);
        IngressModel ingressModel = new IngressModel();

        String ingressName = ingressAnnotationInfo.getAttributeValue(KubeGenConstants.INGRESS_NAME)
                != null ?
                ingressAnnotationInfo.getAttributeValue(KubeGenConstants.INGRESS_NAME).getStringValue() :
                serviceInfo.getName();
        //TODO: validate ingress name with regex.
        ingressModel.setName(ingressName.toLowerCase(Locale.ENGLISH));
        String labels = ingressAnnotationInfo.getAttributeValue(KubeGenConstants.INGRESS_LABELS) != null ?
                ingressAnnotationInfo.getAttributeValue(KubeGenConstants.INGRESS_LABELS).getStringValue() :
                null;
        ingressModel.setLabels(getLabelMap(labels, KubeGenUtils.extractBalxName(balxFilePath)));

        String ingressClass = ingressAnnotationInfo.getAttributeValue(KubeGenConstants.INGRESS_CLASS)
                != null ?
                ingressAnnotationInfo.getAttributeValue(KubeGenConstants.INGRESS_CLASS).getStringValue() :
                INGRESS_CLASS_NGINX;
        ingressModel.setIngressClass(ingressClass);

        String hostname = ingressAnnotationInfo.getAttributeValue(KubeGenConstants.INGRESS_HOSTNAME) != null ?
                ingressAnnotationInfo.getAttributeValue(KubeGenConstants.INGRESS_HOSTNAME).getStringValue() :
                serviceInfo.getName() + INGRESS_HOSTNAME_POSTFIX;
        //TODO:validate hostname
        ingressModel.setHostname(hostname.toLowerCase(Locale.ENGLISH));

        String path = ingressAnnotationInfo.getAttributeValue(KubeGenConstants.INGRESS_PATH) != null ?
                ingressAnnotationInfo.getAttributeValue(KubeGenConstants.INGRESS_PATH).getStringValue() :
                "/";
        ingressModel.setPath(path);

        boolean enableTLS = ingressAnnotationInfo.getAttributeValue(KubeGenConstants.INGRESS_ENABLE_TLS) != null
                && ingressAnnotationInfo.getAttributeValue(KubeGenConstants.INGRESS_ENABLE_TLS).getBooleanValue();
        ingressModel.setEnableTLS(enableTLS);

        String targetPath = ingressAnnotationInfo.getAttributeValue(KubeGenConstants.INGRESS_TARGET_PATH) != null ?
                ingressAnnotationInfo.getAttributeValue(KubeGenConstants.INGRESS_TARGET_PATH).getStringValue() :
                null;
        ingressModel.setTargetPath(targetPath);
        ingressModel.setServiceName(svc.getName());
        ingressModel.setServicePort(svc.getPort());

        printDebug(ingressModel.toString());
        try {
            String ingressContext = new IngressHandler(ingressModel).generate();
            KubeGenUtils.writeToFile(ingressContext, outputDir + File.separator + KUBERNETES + File
                    .separator + serviceInfo.getName() + INGRESS_POSTFIX);
            printSuccess("Ingress yaml generated.");
        } catch (IOException e) {
            printError("Unable to write ingress content to " + outputDir);
        } catch (ArtifactGenerationException e) {
            printError("Unable to generate ingress content  " + e.getMessage());
        }
    }

    /**
     * Process HPA annotations for ballerina Service.
     *
     * @param serviceInfo  ServiceInfo Object
     * @param balxFilePath ballerina file name
     * @param outputDir    target output directory
     */
    private void processHPAAnnotationForService(ServiceInfo serviceInfo, DeploymentModel deploymentModel, String
            balxFilePath, String outputDir) {
        AnnAttachmentInfo autoscalerAnnotationInfo = serviceInfo.getAnnotationAttachmentInfo
                (KubeGenConstants.KUBERNETES_ANNOTATION_PACKAGE, KubeGenConstants.HPA_ANNOTATION);
        PodAutoscalerModel podAutoscalerModel = new PodAutoscalerModel();

        String name = autoscalerAnnotationInfo.getAttributeValue(KubeGenConstants.AUTOSCALER_NAME)
                != null ?
                autoscalerAnnotationInfo.getAttributeValue(KubeGenConstants.AUTOSCALER_NAME).getStringValue() :
                serviceInfo.getName();

        podAutoscalerModel.setName(name.toLowerCase(Locale.ENGLISH));
        String labels = autoscalerAnnotationInfo.getAttributeValue(KubeGenConstants.AUTOSCALER_LABELS) != null ?
                autoscalerAnnotationInfo.getAttributeValue(KubeGenConstants.AUTOSCALER_LABELS).getStringValue() :
                null;
        podAutoscalerModel.setLabels(getLabelMap(labels, KubeGenUtils.extractBalxName(balxFilePath)));

        int defaultCPU = 50;
        int cpuPercentage = autoscalerAnnotationInfo.getAttributeValue(KubeGenConstants.AUTOSCALER_CPU_PERCENTAGE)
                != null ?
                Math.toIntExact(autoscalerAnnotationInfo.
                        getAttributeValue(KubeGenConstants.AUTOSCALER_CPU_PERCENTAGE).getIntValue()) : defaultCPU;
        podAutoscalerModel.setCpuPercentage(cpuPercentage);

        int minReplicas = autoscalerAnnotationInfo.getAttributeValue(KubeGenConstants.AUTOSCALER_MIN_REPLICAS)
                != null ?
                Math.toIntExact(autoscalerAnnotationInfo.getAttributeValue(KubeGenConstants.AUTOSCALER_MIN_REPLICAS)
                        .getIntValue()) : deploymentModel.getReplicas();
        podAutoscalerModel.setMinReplicas(minReplicas);

        int maxReplicas = autoscalerAnnotationInfo.getAttributeValue(KubeGenConstants.AUTOSCALER_MAX_REPLICAS)
                != null ?
                Math.toIntExact(autoscalerAnnotationInfo.getAttributeValue(KubeGenConstants.AUTOSCALER_MAX_REPLICAS)
                        .getIntValue()) : deploymentModel.getReplicas() + 1;
        podAutoscalerModel.setMaxReplicas(maxReplicas);

        podAutoscalerModel.setDeployment(deploymentModel.getName());

        printDebug(podAutoscalerModel.toString());
        try {
            String hpaContent = new HPAHandler(podAutoscalerModel).generate();
            KubeGenUtils.writeToFile(hpaContent, outputDir + File.separator + KUBERNETES + File
                    .separator + serviceInfo.getName() + AUTOSCALER_POSTFIX);
            printSuccess("Horizontal pod autoscaler yaml generated.");
        } catch (IOException e) {
            printError("Unable to write HPA content to " + outputDir);
        } catch (ArtifactGenerationException e) {
            printError("Unable to generate HPA content  " + e.getMessage());
        }
    }

    /**
     * Generate label map by splitting the labels string.
     *
     * @param labels         labels string.
     * @param outputFileName output file name parameter added to the selector.
     * @return Map of labels with selector.
     */
    private Map<String, String> getLabelMap(String labels, String outputFileName) {
        Map<String, String> labelMap = new HashMap<>();
        if (labels != null) {
            labelMap = Pattern.compile("\\s*,\\s*")
                    .splitAsStream(labels.trim())
                    .map(s -> s.split(":", 2))
                    .collect(Collectors.toMap(a -> a[0], a -> a.length > 1 ? a[1] : ""));
        }
        labelMap.put(KubeGenConstants.KUBERNETES_SELECTOR_KEY, outputFileName);
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
     * Extract deployment info from Annotation attachment.
     *
     * @param serviceInfo  Service Info object
     * @param balxFilePath ballerina file path
     * @return DeploymentModel for kubernetes
     */
    private DeploymentModel getDeploymentModel(ServiceInfo serviceInfo, String balxFilePath) {
        AnnAttachmentInfo deploymentAnnotationInfo = serviceInfo.getAnnotationAttachmentInfo
                (KubeGenConstants.KUBERNETES_ANNOTATION_PACKAGE, KubeGenConstants.DEPLOYMENT_ANNOTATION);
        DeploymentModel deploymentModel = new DeploymentModel();
        String outputFileName = KubeGenUtils.extractBalxName(balxFilePath);
        String deploymentName = deploymentAnnotationInfo.getAttributeValue(KubeGenConstants.DEPLOYMENT_NAME) !=
                null ?
                deploymentAnnotationInfo.getAttributeValue(KubeGenConstants.DEPLOYMENT_NAME).getStringValue() :
                outputFileName + "-deployment";
        //TODO:Validate deployment name
        deploymentModel.setName(deploymentName.toLowerCase(Locale.ENGLISH));

        List<Integer> portList = new ArrayList<>();
        if (portList.addAll(ports)) {
            deploymentModel.setPorts(portList);
        }
        String namespace = deploymentAnnotationInfo.getAttributeValue(KubeGenConstants.DEPLOYMENT_NAMESPACE) !=
                null ? deploymentAnnotationInfo.getAttributeValue(KubeGenConstants.DEPLOYMENT_NAMESPACE)
                .getStringValue() :
                KubeGenConstants.DEPLOYMENT_NAMESPACE_DEFAULT;
        deploymentModel.setNamespace(namespace);

        String imagePullPolicy = deploymentAnnotationInfo.getAttributeValue(KubeGenConstants
                .DEPLOYMENT_IMAGE_PULL_POLICY)
                != null ? deploymentAnnotationInfo.getAttributeValue(KubeGenConstants
                .DEPLOYMENT_IMAGE_PULL_POLICY).getStringValue() :
                KubeGenConstants.DEPLOYMENT_IMAGE_PULL_POLICY_DEFAULT;
        deploymentModel.setImagePullPolicy(imagePullPolicy);

        String liveness = deploymentAnnotationInfo.getAttributeValue(KubeGenConstants.DEPLOYMENT_LIVENESS)
                != null ?
                deploymentAnnotationInfo.getAttributeValue(KubeGenConstants.DEPLOYMENT_LIVENESS).getStringValue() :
                KubeGenConstants.DEPLOYMENT_LIVENESS_DISABLE;
        deploymentModel.setLiveness(liveness);

        if (DEPLOYMENT_LIVENESS_ENABLE.equals(liveness)) {
            int defaultInitialDelay = 5;
            int defaultPeriodSeconds = 20;
            int initialDelay = deploymentAnnotationInfo.getAttributeValue(KubeGenConstants
                    .DEPLOYMENT_INITIAL_DELAY_SECONDS)
                    != null ?
                    Math.toIntExact(deploymentAnnotationInfo.getAttributeValue(KubeGenConstants
                            .DEPLOYMENT_INITIAL_DELAY_SECONDS).getIntValue()) :
                    defaultInitialDelay;
            deploymentModel.setInitialDelaySeconds(initialDelay);
            int periodSeconds = deploymentAnnotationInfo.getAttributeValue(KubeGenConstants
                    .DEPLOYMENT_PERIOD_SECONDS)
                    != null ?
                    Math.toIntExact(deploymentAnnotationInfo.getAttributeValue(KubeGenConstants
                            .DEPLOYMENT_PERIOD_SECONDS).getIntValue()) :
                    defaultPeriodSeconds;
            deploymentModel.setPeriodSeconds(periodSeconds);
        }

        int defaultReplicas = 1;
        int replicas = deploymentAnnotationInfo.getAttributeValue(KubeGenConstants.DEPLOYMENT_REPLICAS) != null ?
                Math.toIntExact(deploymentAnnotationInfo.getAttributeValue(KubeGenConstants.DEPLOYMENT_REPLICAS)
                        .getIntValue()) : defaultReplicas;
        deploymentModel.setReplicas(replicas);

        String labels = deploymentAnnotationInfo.getAttributeValue(KubeGenConstants.DEPLOYMENT_LABELS) != null ?
                deploymentAnnotationInfo.getAttributeValue(KubeGenConstants.DEPLOYMENT_LABELS).getStringValue() :
                null;
        deploymentModel.setLabels(getLabelMap(labels, KubeGenUtils.extractBalxName(balxFilePath)));

        String envVars = deploymentAnnotationInfo.getAttributeValue(KubeGenConstants.DEPLOYMENT_ENV_VARS) != null ?
                deploymentAnnotationInfo.getAttributeValue(KubeGenConstants.DEPLOYMENT_ENV_VARS).getStringValue() :
                null;
        deploymentModel.setEnv(getEnvVars(envVars));

        int livenessPort = deploymentAnnotationInfo.getAttributeValue(KubeGenConstants
                .DEPLOYMENT_LIVENESS_PORT)
                != null ?
                Math.toIntExact(deploymentAnnotationInfo.getAttributeValue(KubeGenConstants
                        .DEPLOYMENT_LIVENESS_PORT).getIntValue()) : KubeGenUtils.extractPort(serviceInfo);
        deploymentModel.setLivenessPort(livenessPort);

        String baseImage = deploymentAnnotationInfo.getAttributeValue(KubeGenConstants
                .DOCKER_BASE_IMAGE) != null ? deploymentAnnotationInfo.getAttributeValue(KubeGenConstants
                .DOCKER_BASE_IMAGE).getStringValue() : DEFAULT_BASE_IMAGE;
        deploymentModel.setBaseImage(baseImage);

        String image = deploymentAnnotationInfo.getAttributeValue(KubeGenConstants.DEPLOYMENT_IMAGE)
                != null ?
                deploymentAnnotationInfo.getAttributeValue(KubeGenConstants.DEPLOYMENT_IMAGE).getStringValue() :
                KubeGenUtils.extractBalxName(balxFilePath) + DOCKER_LATEST_TAG;
        deploymentModel.setImage(image);

        boolean imageBuild = deploymentAnnotationInfo.getAttributeValue(KubeGenConstants.DEPLOYMENT_IMAGE_BUILD) == null
                || deploymentAnnotationInfo.getAttributeValue(
                KubeGenConstants.DEPLOYMENT_IMAGE_BUILD).getBooleanValue();
        deploymentModel.setImageBuild(imageBuild);

        boolean push = deploymentAnnotationInfo.getAttributeValue(KubeGenConstants.DEPLOYMENT_PUSH)
                != null && deploymentAnnotationInfo.getAttributeValue(KubeGenConstants.DEPLOYMENT_PUSH)
                .getBooleanValue();
        deploymentModel.setPush(push);

        String username = deploymentAnnotationInfo.getAttributeValue(KubeGenConstants.DEPLOYMENT_USERNAME) != null ?
                deploymentAnnotationInfo.getAttributeValue(KubeGenConstants.DEPLOYMENT_USERNAME).getStringValue() :
                null;
        deploymentModel.setUsername(username);

        String password = deploymentAnnotationInfo.getAttributeValue(KubeGenConstants.DEPLOYMENT_PASSWORD) != null ?
                deploymentAnnotationInfo.getAttributeValue(KubeGenConstants
                        .DEPLOYMENT_PASSWORD).getStringValue() : null;
        deploymentModel.setUsername(password);
        return deploymentModel;
    }

    private void createDeploymentArtifacts(DeploymentModel deploymentModel, String outputDir,
                                           String balxFilePath) {
        try {
            String deploymentContent = new DeploymentHandler(deploymentModel).generate();
            KubeGenUtils.writeToFile(deploymentContent, outputDir + File.separator + KUBERNETES + File
                    .separator + KubeGenUtils.extractBalxName(balxFilePath) + DEPLOYMENT_POSTFIX);
            printSuccess("Deployment yaml generated.");
        } catch (IOException e) {
            printError("Unable to write deployment content to " + outputDir);
        } catch (ArtifactGenerationException e) {
            printError("Unable to generate deployment  " + e.getMessage());
        }
    }

    private void printKubernetesInstructions(String outputDir) {
        printInstruction("\nRun following command to deploy kubernetes artifacts: ");
        printInstruction("kubectl create -f " + outputDir + KUBERNETES);
    }

    private void createDockerArtifacts(DockerModel dockerModel, String balxFilePath, String outputDir) {
        DockerHandler dockerArtifactHandler = new DockerHandler(dockerModel);
        String dockerContent = dockerArtifactHandler.generate();
        try {
            printInfo("Creating Dockerfile ...");
            KubeGenUtils.writeToFile(dockerContent, outputDir + File.separator + "Dockerfile");
            printSuccess("Dockerfile generated.");
            String balxDestination = outputDir + File.separator + KubeGenUtils.extractBalxName
                    (balxFilePath) + BALX;
            KubeGenUtils.copyFile(balxFilePath, balxDestination);
            //check image build is enabled.
            if (dockerModel.isImageBuild()) {
                printInfo("Creating docker image ...");
                dockerArtifactHandler.buildImage(dockerModel.getName(), outputDir);
                Files.delete(Paths.get(balxDestination));
                //push only if image build is enabled.
                if (dockerModel.isPush()) {
                    dockerArtifactHandler.pushImage(dockerModel);
                }
            }
        } catch (IOException e) {
            printError("Unable to write Dockerfile content to " + outputDir);
        } catch (InterruptedException e) {
            printError("Unable to create docker images " + e.getMessage());
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

        String deploymentName = KubeGenUtils.extractBalxName(balxFilePath) + "-deployment";
        deploymentModel.setName(deploymentName.toLowerCase(Locale.ENGLISH));

        List<Integer> portList = new ArrayList<>();
        if (portList.addAll(ports)) {
            deploymentModel.setPorts(portList);
        }
        String namespace = KubeGenConstants.DEPLOYMENT_NAMESPACE_DEFAULT;
        deploymentModel.setNamespace(namespace);

        String imagePullPolicy = KubeGenConstants.DEPLOYMENT_IMAGE_PULL_POLICY_DEFAULT;
        deploymentModel.setImagePullPolicy(imagePullPolicy);

        String liveness = KubeGenConstants.DEPLOYMENT_LIVENESS_DISABLE;
        deploymentModel.setLiveness(liveness);

        int defaultReplicas = 1;
        deploymentModel.setReplicas(defaultReplicas);

        deploymentModel.setLabels(getLabelMap(null, KubeGenUtils.extractBalxName(balxFilePath)));
        deploymentModel.setEnv(getEnvVars(null));
        deploymentModel.setBaseImage(DEFAULT_BASE_IMAGE);
        deploymentModel.setImage(KubeGenUtils.extractBalxName(balxFilePath) + DOCKER_LATEST_TAG);
        deploymentModel.setImageBuild(true);
        deploymentModel.setPush(false);

        return deploymentModel;
    }
}
