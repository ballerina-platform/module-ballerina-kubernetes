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

package org.ballerinax.kubernetes.handlers.knative;

import io.fabric8.knative.serving.v1alpha1.Service;
import io.fabric8.knative.serving.v1alpha1.ServiceBuilder;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ContainerBuilder;
import io.fabric8.kubernetes.api.model.ContainerPort;
import io.fabric8.kubernetes.api.model.ContainerPortBuilder;
import io.fabric8.kubernetes.api.model.Probe;
import io.fabric8.kubernetes.api.model.ProbeBuilder;
import io.fabric8.kubernetes.api.model.TCPSocketAction;
import io.fabric8.kubernetes.api.model.TCPSocketActionBuilder;
import io.fabric8.kubernetes.api.model.Volume;
import io.fabric8.kubernetes.api.model.VolumeBuilder;
import io.fabric8.kubernetes.api.model.VolumeMount;
import io.fabric8.kubernetes.api.model.VolumeMountBuilder;
import io.fabric8.kubernetes.client.internal.SerializationUtils;
import org.ballerinax.docker.generator.exceptions.DockerGenException;
import org.ballerinax.docker.generator.models.DockerModel;
import org.ballerinax.kubernetes.KubernetesConstants;
import org.ballerinax.kubernetes.exceptions.KubernetesPluginException;
import org.ballerinax.kubernetes.models.knative.ConfigMapModel;
import org.ballerinax.kubernetes.models.knative.KnativeContext;
import org.ballerinax.kubernetes.models.knative.ProbeModel;
import org.ballerinax.kubernetes.models.knative.SecretModel;
import org.ballerinax.kubernetes.models.knative.ServiceModel;
import org.ballerinax.kubernetes.utils.KnativeUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.ballerinax.docker.generator.DockerGenConstants.REGISTRY_SEPARATOR;
import static org.ballerinax.docker.generator.utils.DockerGenUtils.extractUberJarName;
import static org.ballerinax.kubernetes.KubernetesConstants.EXECUTABLE_JAR;
import static org.ballerinax.kubernetes.KubernetesConstants.KNATIVE_SVC_FILE_POSTFIX;
import static org.ballerinax.kubernetes.KubernetesConstants.VOLUME_DEFINE;
import static org.ballerinax.kubernetes.KubernetesConstants.YAML;
import static org.ballerinax.kubernetes.utils.KnativeUtils.populateEnvVar;

/**
 * Generates knative service from annotations.
 */
public class KnativeServiceHandler extends KnativeAbstractArtifactHandler {

    private List<ContainerPort> populatePorts(Set<Integer> ports) {
        List<ContainerPort> containerPorts = new ArrayList<>();
        for (int port :ports) {
            ContainerPort containerPort = new ContainerPortBuilder()
                    .withContainerPort(port)
                    .withProtocol(KubernetesConstants.KUBERNETES_SVC_PROTOCOL)
                    .build();
            containerPorts.add(containerPort);
        }
        return containerPorts;
    }

    private List<VolumeMount> populateVolumeMounts(ServiceModel serviceModel) {
        List<VolumeMount> volumeMounts = new ArrayList<>();
        for (SecretModel secretModel : serviceModel.getSecretModels()) {
            VolumeMount volumeMount = new VolumeMountBuilder()
                    .withMountPath(secretModel.getMountPath())
                    .withName(secretModel.getName() + VOLUME_DEFINE)
                    .withReadOnly(secretModel.isReadOnly())
                    .build();
            volumeMounts.add(volumeMount);
        }
        for (ConfigMapModel configMapModel : serviceModel.getConfigMapModels()) {
            VolumeMount volumeMount = new VolumeMountBuilder()
                    .withMountPath(configMapModel.getMountPath())
                    .withName(configMapModel.getName() + VOLUME_DEFINE)
                    .withReadOnly(configMapModel.isReadOnly())
                    .build();
            volumeMounts.add(volumeMount);
        }
        return volumeMounts;
    }

    private List<Container> generateInitContainer(ServiceModel serviceModel) throws KubernetesPluginException {
        List<Container> initContainers = new ArrayList<>();
        for (String dependsOn : serviceModel.getDependsOn()) {
            String serviceName = KnativeContext.getInstance().getServiceName(dependsOn);
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

    private Container generateContainer(ServiceModel serviceModel, List<ContainerPort> containerPorts) {
        String dockerRegistry = serviceModel.getRegistry();
        String deploymentImageName = serviceModel.getImage();
        if (null != dockerRegistry && !"".equals(dockerRegistry)) {
            deploymentImageName = dockerRegistry + REGISTRY_SEPARATOR + deploymentImageName;
        }
        return new ContainerBuilder()
                .withName(serviceModel.getName())
                .withImage(deploymentImageName)
                .withPorts(containerPorts)
                .withEnv(populateEnvVar(serviceModel.getEnv()))
                .withVolumeMounts(populateVolumeMounts(serviceModel))
                .withLivenessProbe(generateProbe(serviceModel.getLivenessProbe()))
                .withReadinessProbe(generateProbe(serviceModel.getReadinessProbe()))
                .build();
    }

    private List<Volume> populateVolume(ServiceModel serviceModel) {
        List<Volume> volumes = new ArrayList<>();
        for (SecretModel secretModel : serviceModel.getSecretModels()) {
            Volume volume = new VolumeBuilder()
                    .withName(secretModel.getName() + VOLUME_DEFINE)
                    .withNewSecret()
                    .withSecretName(secretModel.getName())
                    .endSecret()
                    .build();
            volumes.add(volume);
        }
        for (ConfigMapModel configMapModel : serviceModel.getConfigMapModels()) {
            Volume volume = new VolumeBuilder()
                    .withName(configMapModel.getName() + VOLUME_DEFINE)
                    .withNewConfigMap()
                    .withName(configMapModel.getName())
                    .endConfigMap()
                    .build();
            volumes.add(volume);
        }
        return volumes;
    }

    private Probe generateProbe(ProbeModel probeModel) {
        if (null == probeModel) {
            return null;
        }
        TCPSocketAction tcpSocketAction = new TCPSocketActionBuilder()
                .withNewPort(probeModel.getPort())
                .build();
        return new ProbeBuilder()
                .withInitialDelaySeconds(probeModel.getInitialDelaySeconds())
                .withPeriodSeconds(probeModel.getPeriodSeconds())
                .withTcpSocket(tcpSocketAction)
                .build();
    }

    /**
     * Generate kubernetes deployment definition from annotation.
     *
     * @param serviceModel @{@link ServiceModel} definition
     * @throws KubernetesPluginException If an error occurs while generating artifact.
     */
    private void generate(ServiceModel serviceModel) throws KubernetesPluginException {
        List<ContainerPort> containerPorts = null;
        if (serviceModel.getPorts() != null) {
            containerPorts = populatePorts(serviceModel.getPorts());
        }
        Container container = generateContainer(serviceModel, containerPorts);
        Service knativeSvc = new ServiceBuilder()
                .withNewMetadata()
                .withName(serviceModel.getName())
                .withNamespace(knativeDataHolder.getNamespace())
                .withAnnotations(serviceModel.getAnnotations())
                .withLabels(serviceModel.getLabels())
                .endMetadata()
                .withNewSpec()
                .withNewTemplate()
                .withNewSpec()
                .withContainerConcurrency((long) serviceModel.getContainerConcurrency())
                .withTimeoutSeconds((long) serviceModel.getTimeoutSeconds())
                .withContainer(container)
                .withInitContainers(generateInitContainer(serviceModel))
                .withVolumes(populateVolume(serviceModel))
                .endSpec()
                .endTemplate()
                .endSpec()
                .build();
                
        try {
            String knativeSvcContent = SerializationUtils.dumpWithoutRuntimeStateAsYaml(knativeSvc);
            KnativeUtils.writeToFile(knativeSvcContent, KNATIVE_SVC_FILE_POSTFIX + YAML);
        } catch (IOException e) {
            String errorMessage = "error while generating yaml file for deployment: " + serviceModel.getName();
            throw new KubernetesPluginException(errorMessage, e);
        }
    }

    @Override
    public void createArtifacts() throws KubernetesPluginException {
        try {
            ServiceModel serviceModel = knativeDataHolder.getServiceModel();
            serviceModel.setPodAutoscalerModel(knativeDataHolder.getPodAutoscalerModel());
            serviceModel.setSecretModels(knativeDataHolder.getSecretModelSet());
            serviceModel.setConfigMapModels(knativeDataHolder.getConfigMapModelSet());
            if (null != serviceModel.getLivenessProbe() && serviceModel.getLivenessProbe().getPort() == 0) {
                //set first port as liveness port
                serviceModel.getLivenessProbe().setPort(serviceModel.getPorts().iterator().next());
            }

            if (null != serviceModel.getReadinessProbe() && serviceModel.getReadinessProbe().getPort() == 0) {
                //set first port as readiness port
                serviceModel.getReadinessProbe().setPort(serviceModel.getPorts().iterator().next());
            }
            generate(serviceModel);
            OUT.println();
            OUT.print("\t@knative:Service \t\t\t - complete 1/1");
            knativeDataHolder.setDockerModel(getDockerModel(serviceModel));
        } catch (DockerGenException e) {
            throw new KubernetesPluginException("error occurred creating docker image.", e);
        }
    }

    /**
     * Create docker artifacts.
     *
     * @param serviceModel Service model
     */
    private DockerModel getDockerModel(ServiceModel serviceModel) throws DockerGenException {
        DockerModel dockerModel = new DockerModel();
        String dockerImage = serviceModel.getImage();
        String imageTag = "latest";
        if (dockerImage.contains(":")) {
            imageTag = dockerImage.substring(dockerImage.lastIndexOf(":") + 1);
            dockerImage = dockerImage.substring(0, dockerImage.lastIndexOf(":"));
        }
        dockerModel.setBaseImage(serviceModel.getBaseImage());
        dockerModel.setRegistry(serviceModel.getRegistry());
        dockerModel.setName(dockerImage);
        dockerModel.setTag(imageTag);
        dockerModel.setEnableDebug(false);
        dockerModel.setUsername(serviceModel.getUsername());
        dockerModel.setPassword(serviceModel.getPassword());
        dockerModel.setPush(serviceModel.isPush());
        dockerModel.setUberJarFileName(extractUberJarName(knativeDataHolder.getUberJarPath()) + EXECUTABLE_JAR);
        dockerModel.setPorts(serviceModel.getPorts());
        dockerModel.setService(true);
        dockerModel.setDockerHost(serviceModel.getDockerHost());
        dockerModel.setDockerCertPath(serviceModel.getDockerCertPath());
        dockerModel.setBuildImage(serviceModel.isBuildImage());
        dockerModel.addCommandArg(serviceModel.getCommandArgs());
        dockerModel.setCopyFiles(serviceModel.getCopyFiles());
        return dockerModel;
    }
}
