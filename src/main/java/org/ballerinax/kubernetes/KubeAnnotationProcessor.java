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

import org.ballerinalang.model.tree.AnnotationAttachmentNode;
import org.ballerinalang.model.tree.Node;
import org.ballerinax.kubernetes.exceptions.KubernetesPluginException;
import org.ballerinax.kubernetes.handlers.DeploymentHandler;
import org.ballerinax.kubernetes.handlers.DockerHandler;
import org.ballerinax.kubernetes.models.DeploymentModel;
import org.ballerinax.kubernetes.models.DockerModel;
import org.ballerinax.kubernetes.models.KubernetesDataHolder;
import org.ballerinax.kubernetes.utils.KubeGenUtils;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangAnnotAttachmentAttribute;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Process Kubernetes Annotations and generate Artifacts.
 */
class KubeAnnotationProcessor {

    private static final String KUBERNETES = "kubernetes";
    private static final String DOCKER = "docker";
    private static final String BALX = ".balx";
    private static final String DEPLOYMENT_POSTFIX = "-deployment.yaml";
    //    private static final String SVC_POSTFIX = "-svc.yaml";
//    private static final String INGRESS_POSTFIX = "-ingress.yaml";
//    private static final String AUTOSCALER_POSTFIX = "-hpa.yaml";
    private static final String DOCKER_LATEST_TAG = ":latest";
    //    private static final String INGRESS_HOSTNAME_POSTFIX = ".com";
    private static final String DEFAULT_BASE_IMAGE = "ballerina/ballerina:latest";

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


    void createArtifacts(KubernetesDataHolder kubernetesData, String balxFilePath,
                         String outputDir) throws KubernetesPluginException {
        generateDeployment(kubernetesData.getDeploymentModel(), balxFilePath, outputDir);
    }


    private void generateDeployment(DeploymentModel deploymentModel, String balxFilePath, String outputDir) throws
            KubernetesPluginException {
        String balxFileName = KubeGenUtils.extractBalxName(balxFilePath);
        if (deploymentModel.getName() == null) {
            deploymentModel.setName(balxFileName);
        }
        if (deploymentModel.getImage() == null) {
            deploymentModel.setImage(balxFileName + ":" + DOCKER_LATEST_TAG);
        }
        deploymentModel.addLabel(KubeGenConstants.KUBERNETES_SELECTOR_KEY, balxFileName);

        String deploymentContent = new DeploymentHandler(deploymentModel).generate();
        try {
            KubeGenUtils.writeToFile(deploymentContent, outputDir + File
                    .separator + KubeGenUtils.extractBalxName(balxFilePath) + DEPLOYMENT_POSTFIX);
            KubeGenUtils.printSuccess("Deployment yaml generated.");
            //generate dockerfile and docker image
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
            dockerModel.setBalxFileName(balxFileName + BALX);
            dockerModel.setPorts(deploymentModel.getPorts());
            dockerModel.setService(true);
            dockerModel.setBuildImage(deploymentModel.isBuildImage());
            createDockerArtifacts(dockerModel, balxFilePath, outputDir + File.separator + KUBERNETES + File
                    .separator + DOCKER);
        } catch (IOException e) {
            throw new KubernetesPluginException("Error while writing deployment content", e);
        }
    }


    //    private void printKubernetesInstructions(String outputDir) {
//        KubeGenUtils.printInstruction("\nRun following command to deploy kubernetes artifacts: ");
//        KubeGenUtils.printInstruction("kubectl create -f " + outputDir + KUBERNETES);
//    }
//
    private void createDockerArtifacts(DockerModel dockerModel, String balxFilePath, String outputDir)
            throws KubernetesPluginException {
        DockerHandler dockerArtifactHandler = new DockerHandler(dockerModel);
        String dockerContent = dockerArtifactHandler.generate();
        try {
            KubeGenUtils.printInfo("Creating Dockerfile ...");
            KubeGenUtils.writeToFile(dockerContent, outputDir + File.separator + "Dockerfile");
            KubeGenUtils.printSuccess("Dockerfile generated.");
            String balxDestination = outputDir + File.separator + KubeGenUtils.extractBalxName
                    (balxFilePath) + BALX;
            KubeGenUtils.copyFile(balxFilePath, balxDestination);
            //check image build is enabled.
            if (dockerModel.isBuildImage()) {
                KubeGenUtils.printInfo("Creating docker image ...");
                dockerArtifactHandler.buildImage(dockerModel.getName(), outputDir);
                Files.delete(Paths.get(balxDestination));
                //push only if image build is enabled.
                if (dockerModel.isPush()) {
                    KubeGenUtils.printInfo("Pushing docker image ...");
                    dockerArtifactHandler.pushImage(dockerModel);
                }
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
    DeploymentModel getDefaultDeploymentModel(String balxFilePath) {
        DeploymentModel deploymentModel = new DeploymentModel();
        String balxName = KubeGenUtils.extractBalxName(balxFilePath);
        String deploymentName = balxName + "-deployment";
        deploymentModel.setName(deploymentName.toLowerCase(Locale.ENGLISH).replace("_", "-"));
        String namespace = KubeGenConstants.DEPLOYMENT_NAMESPACE_DEFAULT;
        deploymentModel.setNamespace(namespace);

        String imagePullPolicy = KubeGenConstants.DEPLOYMENT_IMAGE_PULL_POLICY_DEFAULT;
        deploymentModel.setImagePullPolicy(imagePullPolicy);

        String liveness = KubeGenConstants.DEPLOYMENT_LIVENESS_DISABLE;
        deploymentModel.setEnableLiveness(liveness);

        int defaultReplicas = 1;
        deploymentModel.setReplicas(defaultReplicas);

        deploymentModel.addLabel(KubeGenConstants.KUBERNETES_SELECTOR_KEY, balxName);
        deploymentModel.setEnv(getEnvVars(null));
        deploymentModel.setBaseImage(DEFAULT_BASE_IMAGE);
        deploymentModel.setImage(balxName + DOCKER_LATEST_TAG);
        deploymentModel.setBuildImage(true);
        deploymentModel.setPush(false);

        return deploymentModel;
    }

    DeploymentModel processDeployment(AnnotationAttachmentNode attachmentNode) {
        DeploymentModel deploymentModel = new DeploymentModel();
        List<BLangAnnotAttachmentAttribute> bLangAnnotationAttachments = ((BLangAnnotationAttachment)
                attachmentNode).attributes;
        for (BLangAnnotAttachmentAttribute annotationAttribute : bLangAnnotationAttachments) {
            DeploymentConfiguration deploymentConfiguration =
                    DeploymentConfiguration.valueOf(annotationAttribute.name.value);
            Node annotationValue = annotationAttribute.getValue().getValue();
            if (annotationValue == null) {
                continue;
            }
            switch (deploymentConfiguration) {
                case name:
                    deploymentModel.setName(annotationValue.toString());
                    break;
                case labels:
                    deploymentModel.setLabels(getLabelMap(annotationValue.toString()));
                    break;
                case enableLiveness:
                    deploymentModel.setEnableLiveness(annotationValue.toString());
                    break;
                case livenessPort:
                    deploymentModel.setLivenessPort(Integer.parseInt(annotationValue.toString()));
                    break;
                case initialDelaySeconds:
                    deploymentModel.setInitialDelaySeconds(Integer.parseInt(annotationValue.toString()));
                    break;
                case periodSeconds:
                    deploymentModel.setPeriodSeconds(Integer.parseInt(annotationValue.toString()));
                    break;
                case username:
                    deploymentModel.setUsername(annotationValue.toString());
                    break;
                case env:
                    deploymentModel.setEnv(getEnvVars(annotationValue.toString()));
                    break;
                case password:
                    deploymentModel.setPassword(annotationValue.toString());
                    break;
                case baseImage:
                    deploymentModel.setBaseImage(annotationValue.toString());
                    break;
                case push:
                    deploymentModel.setPush(Boolean.valueOf(annotationValue.toString()));
                    break;
                case buildImage:
                    deploymentModel.setBuildImage(Boolean.valueOf(annotationValue.toString()));
                    break;
                case imagePullPolicy:
                    deploymentModel.setImagePullPolicy(annotationValue.toString());
                    break;
                case replicas:
                    deploymentModel.setReplicas(Integer.parseInt(annotationValue.toString()));
                    break;
                default:
                    break;
            }
        }
        return deploymentModel;
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
        username,
        password,
        baseImage,
        push
    }
}
