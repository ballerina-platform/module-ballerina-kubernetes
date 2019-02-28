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

package org.ballerinax.kubernetes.handlers;


import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ContainerBuilder;
import io.fabric8.kubernetes.api.model.ContainerPort;
import io.fabric8.kubernetes.api.model.ContainerPortBuilder;
import io.fabric8.kubernetes.api.model.LocalObjectReference;
import io.fabric8.kubernetes.api.model.LocalObjectReferenceBuilder;
import io.fabric8.kubernetes.api.model.Probe;
import io.fabric8.kubernetes.api.model.ProbeBuilder;
import io.fabric8.kubernetes.api.model.TCPSocketAction;
import io.fabric8.kubernetes.api.model.TCPSocketActionBuilder;
import io.fabric8.kubernetes.api.model.Volume;
import io.fabric8.kubernetes.api.model.VolumeBuilder;
import io.fabric8.kubernetes.api.model.VolumeMount;
import io.fabric8.kubernetes.api.model.VolumeMountBuilder;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.fabric8.kubernetes.client.internal.SerializationUtils;
import org.ballerinax.docker.generator.models.DockerModel;
import org.ballerinax.kubernetes.KubernetesConstants;
import org.ballerinax.kubernetes.exceptions.KubernetesPluginException;
import org.ballerinax.kubernetes.models.ConfigMapModel;
import org.ballerinax.kubernetes.models.DeploymentModel;
import org.ballerinax.kubernetes.models.KubernetesContext;
import org.ballerinax.kubernetes.models.PersistentVolumeClaimModel;
import org.ballerinax.kubernetes.models.SecretModel;
import org.ballerinax.kubernetes.models.openshift.OpenShiftBuildExtensionModel;
import org.ballerinax.kubernetes.utils.KubernetesUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.ballerinax.docker.generator.DockerGenConstants.REGISTRY_SEPARATOR;
import static org.ballerinax.kubernetes.KubernetesConstants.BALX;
import static org.ballerinax.kubernetes.KubernetesConstants.DEPLOYMENT_FILE_POSTFIX;
import static org.ballerinax.kubernetes.KubernetesConstants.DEPLOYMENT_POSTFIX;
import static org.ballerinax.kubernetes.KubernetesConstants.OPENSHIFT_BUILD_CONFIG_POSTFIX;
import static org.ballerinax.kubernetes.KubernetesConstants.YAML;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.populateEnvVar;

/**
 * Generates kubernetes deployment from annotations.
 */
public class DeploymentHandler extends AbstractArtifactHandler {

    private List<ContainerPort> populatePorts(Set<Integer> ports) {
        List<ContainerPort> containerPorts = new ArrayList<>();
        for (int port : ports) {
            ContainerPort containerPort = new ContainerPortBuilder()
                    .withContainerPort(port)
                    .withProtocol(KubernetesConstants.KUBERNETES_SVC_PROTOCOL)
                    .build();
            containerPorts.add(containerPort);
        }
        return containerPorts;
    }

    private List<VolumeMount> populateVolumeMounts(DeploymentModel deploymentModel) {
        List<VolumeMount> volumeMounts = new ArrayList<>();
        for (SecretModel secretModel : deploymentModel.getSecretModels()) {
            VolumeMount volumeMount = new VolumeMountBuilder()
                    .withMountPath(secretModel.getMountPath())
                    .withName(secretModel.getName() + "-volume")
                    .withReadOnly(secretModel.isReadOnly())
                    .build();
            volumeMounts.add(volumeMount);
        }
        for (ConfigMapModel configMapModel : deploymentModel.getConfigMapModels()) {
            VolumeMount volumeMount = new VolumeMountBuilder()
                    .withMountPath(configMapModel.getMountPath())
                    .withName(configMapModel.getName() + "-volume")
                    .withReadOnly(configMapModel.isReadOnly())
                    .build();
            volumeMounts.add(volumeMount);
        }
        for (PersistentVolumeClaimModel volumeClaimModel : deploymentModel.getVolumeClaimModels()) {
            VolumeMount volumeMount = new VolumeMountBuilder()
                    .withMountPath(volumeClaimModel.getMountPath())
                    .withName(volumeClaimModel.getName() + "-volume")
                    .withReadOnly(volumeClaimModel.isReadOnly())
                    .build();
            volumeMounts.add(volumeMount);
        }
        return volumeMounts;
    }

    private List<Container> generateInitContainer(DeploymentModel deploymentModel) throws KubernetesPluginException {
        List<Container> initContainers = new ArrayList<>();
        for (String dependsOn : deploymentModel.getDependsOn()) {
            String serviceName = KubernetesContext.getInstance().getServiceName(dependsOn);
            List<String> commands = new ArrayList<>();
            commands.add("sh");
            commands.add("-c");
            commands.add("until nslookup " + serviceName + "; do echo waiting for " + serviceName + "; sleep 2; done;");
            initContainers.add(new ContainerBuilder()
                    .withName("wait-for-" + serviceName)
                    .withImage("busybox")
                    .withCommand(commands)
                    .build());
        }
        return initContainers;
    }

    private Container generateContainer(DeploymentModel deploymentModel, List<ContainerPort> containerPorts)
            throws KubernetesPluginException {
        String dockerRegistry = deploymentModel.getRegistry();
        String deploymentImageName = deploymentModel.getImage();
        if (null != dockerRegistry && !"".equals(dockerRegistry)) {
            deploymentImageName = dockerRegistry + REGISTRY_SEPARATOR + deploymentImageName;
        }
        
        if (deploymentModel.getBuildExtension() != null) {
            if (deploymentModel.getBuildExtension() instanceof OpenShiftBuildExtensionModel) {
                OpenShiftBuildExtensionModel openShiftBC =
                        (OpenShiftBuildExtensionModel) deploymentModel.getBuildExtension();
                openShiftBC.setName(deploymentModel.getName().replace(DEPLOYMENT_POSTFIX,
                        OPENSHIFT_BUILD_CONFIG_POSTFIX));
                dataHolder.setOpenShiftBuildExtensionModel(openShiftBC);
        
                dockerRegistry = deploymentModel.getRegistry();
                if (dockerRegistry == null || "".equals(dockerRegistry.trim())) {
                    throw new KubernetesPluginException("value for 'registry' field in @kubernetes:Deployment{} " +
                                                        "annotation is required to generate OpenShift Build Configs.");
                }
        
                String namespace = dataHolder.getNamespace();
                if (namespace == null || "".equals(namespace.trim())) {
                    throw new KubernetesPluginException("value for 'namespace' field in @kubernetes:Deployment{} " +
                                                        "annotation is required to generate OpenShift Build Configs. " +
                                                        "use the value of the OpenShift project name.");
                }
        
                deploymentImageName = dockerRegistry + REGISTRY_SEPARATOR + namespace + REGISTRY_SEPARATOR +
                                      deploymentModel.getImage();
            }
        }
    
        return new ContainerBuilder()
                .withName(deploymentModel.getName())
                .withImage(deploymentImageName)
                .withImagePullPolicy(deploymentModel.getImagePullPolicy())
                .withPorts(containerPorts)
                .withEnv(populateEnvVar(deploymentModel.getEnv()))
                .withVolumeMounts(populateVolumeMounts(deploymentModel))
                .withLivenessProbe(generateLivenessProbe(deploymentModel))
                .build();
    }

    private List<Volume> populateVolume(DeploymentModel deploymentModel) {
        List<Volume> volumes = new ArrayList<>();
        for (SecretModel secretModel : deploymentModel.getSecretModels()) {
            Volume volume = new VolumeBuilder()
                    .withName(secretModel.getName() + "-volume")
                    .withNewSecret()
                    .withSecretName(secretModel.getName())
                    .endSecret()
                    .build();
            volumes.add(volume);
        }
        for (ConfigMapModel configMapModel : deploymentModel.getConfigMapModels()) {
            Volume volume = new VolumeBuilder()
                    .withName(configMapModel.getName() + "-volume")
                    .withNewConfigMap()
                    .withName(configMapModel.getName())
                    .endConfigMap()
                    .build();
            volumes.add(volume);
        }
        for (PersistentVolumeClaimModel volumeClaimModel : deploymentModel.getVolumeClaimModels()) {
            Volume volume = new VolumeBuilder()
                    .withName(volumeClaimModel.getName() + "-volume")
                    .withNewPersistentVolumeClaim()
                    .withClaimName(volumeClaimModel.getName())
                    .endPersistentVolumeClaim()
                    .build();
            volumes.add(volume);
        }
        return volumes;
    }

    private Probe generateLivenessProbe(DeploymentModel deploymentModel) {
        if (!deploymentModel.isEnableLiveness()) {
            return null;
        }
        TCPSocketAction tcpSocketAction = new TCPSocketActionBuilder()
                .withNewPort(deploymentModel.getLivenessPort())
                .build();
        return new ProbeBuilder()
                .withInitialDelaySeconds(deploymentModel.getInitialDelaySeconds())
                .withPeriodSeconds(deploymentModel.getPeriodSeconds())
                .withTcpSocket(tcpSocketAction)
                .build();
    }

    private List<LocalObjectReference> getImagePullSecrets(DeploymentModel deploymentModel) {
        List<LocalObjectReference> imagePullSecrets = new ArrayList<>();
        for (String imagePullSecret : deploymentModel.getImagePullSecrets()) {
            imagePullSecrets.add(new LocalObjectReferenceBuilder().withName(imagePullSecret).build());
        }
        return imagePullSecrets;
    }

    /**
     * Generate kubernetes deployment definition from annotation.
     *
     * @param deploymentModel @{@link DeploymentModel} definition
     * @throws KubernetesPluginException If an error occurs while generating artifact.
     */
    private void generate(DeploymentModel deploymentModel) throws KubernetesPluginException {
        List<ContainerPort> containerPorts = null;
        if (deploymentModel.getPorts() != null) {
            containerPorts = populatePorts(deploymentModel.getPorts());
        }
        Container container = generateContainer(deploymentModel, containerPorts);
        Deployment deployment = new DeploymentBuilder()
                .withNewMetadata()
                .withName(deploymentModel.getName())
                .withLabels(deploymentModel.getLabels())
                .withAnnotations(deploymentModel.getAnnotations())
                .withNamespace(dataHolder.getNamespace())
                .endMetadata()
                .withNewSpec()
                .withNewSelector()
                .withMatchLabels(deploymentModel.getLabels())
                .endSelector()
                .withReplicas(deploymentModel.getReplicas())
                .withNewTemplate()
                .withNewMetadata()
                .addToLabels(deploymentModel.getLabels())
                .addToAnnotations(deploymentModel.getPodAnnotations())
                .endMetadata()
                .withNewSpec()
                .withContainers(container)
                .withImagePullSecrets(getImagePullSecrets(deploymentModel))
                .withInitContainers(generateInitContainer(deploymentModel))
                .withVolumes(populateVolume(deploymentModel))
                .endSpec()
                .endTemplate()
                .endSpec()
                .build();

        try {
            String deploymentContent = SerializationUtils.dumpWithoutRuntimeStateAsYaml(deployment);
            KubernetesUtils.writeToFile(deploymentContent, DEPLOYMENT_FILE_POSTFIX + YAML);
        } catch (IOException e) {
            String errorMessage = "Error while generating yaml file for deployment: " + deploymentModel.getName();
            throw new KubernetesPluginException(errorMessage, e);
        }
    }

    @Override
    public void createArtifacts() throws KubernetesPluginException {
        DeploymentModel deploymentModel = dataHolder.getDeploymentModel();
        deploymentModel.setPodAutoscalerModel(dataHolder.getPodAutoscalerModel());
        deploymentModel.setSecretModels(dataHolder.getSecretModelSet());
        deploymentModel.setConfigMapModels(dataHolder.getConfigMapModelSet());
        deploymentModel.setVolumeClaimModels(dataHolder.getVolumeClaimModelSet());
        if (deploymentModel.isEnableLiveness() && deploymentModel.getLivenessPort() == 0) {
            //set first port as liveness port
            deploymentModel.setLivenessPort(deploymentModel.getPorts().iterator().next());
        }
        generate(deploymentModel);
        OUT.println();
        OUT.println("\t@kubernetes:Deployment \t\t\t - complete 1/1");
        dataHolder.setDockerModel(getDockerModel(deploymentModel));
    }


    /**
     * Create docker artifacts.
     *
     * @param deploymentModel Deployment model
     */
    private DockerModel getDockerModel(DeploymentModel deploymentModel) {
        DockerModel dockerModel = new DockerModel();
        String dockerImage = deploymentModel.getImage();
        String imageTag = dockerImage.substring(dockerImage.lastIndexOf(":") + 1);
        dockerModel.setBaseImage(deploymentModel.getBaseImage());
        dockerModel.setName(dockerImage);
        dockerModel.setTag(imageTag);
        dockerModel.setEnableDebug(false);
        dockerModel.setUsername(deploymentModel.getUsername());
        dockerModel.setPassword(deploymentModel.getPassword());
        dockerModel.setPush(deploymentModel.isPush());
        dockerModel.setBalxFileName(KubernetesUtils.extractBalxName(dataHolder.getBalxFilePath()) + BALX);
        dockerModel.setPorts(deploymentModel.getPorts());
        dockerModel.setService(true);
        dockerModel.setDockerHost(deploymentModel.getDockerHost());
        dockerModel.setDockerCertPath(deploymentModel.getDockerCertPath());
        dockerModel.setBuildImage(deploymentModel.isBuildImage());
        dockerModel.addCommandArg(deploymentModel.getCommandArgs());
        dockerModel.setCopyFiles(deploymentModel.getCopyFiles());
        return dockerModel;
    }
}

