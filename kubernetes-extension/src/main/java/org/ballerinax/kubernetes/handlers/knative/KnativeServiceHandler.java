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
package org.ballerinax.kubernetes.handlers.knative;

//import io.fabric8.kubernetes.api.model.apps.Deployment;
//import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;

//import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
//import io.fabric8.kubernetes.api.model.LocalObjectReference;
//import io.fabric8.kubernetes.api.model.LocalObjectReferenceBuilder;
//import io.fabric8.kubernetes.api.model.Toleration;
//import io.fabric8.kubernetes.api.model.TolerationBuilder;
//import io.fabric8.kubernetes.api.model.ServiceSpec;
//import io.fabric8.kubernetes.api.model.ServiceSpecBuilder;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ContainerBuilder;
import io.fabric8.kubernetes.api.model.ContainerPort;
import io.fabric8.kubernetes.api.model.ContainerPortBuilder;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.fabric8.kubernetes.api.model.Probe;
import io.fabric8.kubernetes.api.model.ProbeBuilder;
import io.fabric8.kubernetes.api.model.TCPSocketAction;
import io.fabric8.kubernetes.api.model.TCPSocketActionBuilder;
import io.fabric8.kubernetes.api.model.Volume;
import io.fabric8.kubernetes.api.model.VolumeBuilder;
import io.fabric8.kubernetes.api.model.VolumeMount;
import io.fabric8.kubernetes.api.model.VolumeMountBuilder;
//import io.fabric8.kubernetes.api.model.apps.Deployment;
//import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
//import io.fabric8.kubernetes.api.model.apps.DeploymentSpec;
//import io.fabric8.kubernetes.api.model.apps.DeploymentSpecBuilder;
import io.fabric8.kubernetes.client.internal.SerializationUtils;
import org.ballerinax.docker.generator.exceptions.DockerGenException;
import org.ballerinax.docker.generator.models.DockerModel;
import org.ballerinax.kubernetes.KubernetesConstants;
import org.ballerinax.kubernetes.exceptions.KubernetesPluginException;
import org.ballerinax.kubernetes.models.knative.ConfigMapModel;
//import org.ballerinax.kubernetes.models.knative.KnativeContainerModel;
import org.ballerinax.kubernetes.models.knative.KnativeContext;
import org.ballerinax.kubernetes.models.knative.KnativeService;
import org.ballerinax.kubernetes.models.knative.ProbeModel;
import org.ballerinax.kubernetes.models.knative.SecretModel;
import org.ballerinax.kubernetes.models.knative.ServiceModel;
import org.ballerinax.kubernetes.specs.KnativePodSpec;
import org.ballerinax.kubernetes.specs.KnativePodTemplateSpec;
import org.ballerinax.kubernetes.specs.KnativeServiceSpec;
import org.ballerinax.kubernetes.utils.KnativeUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.ballerinax.docker.generator.DockerGenConstants.REGISTRY_SEPARATOR;
import static org.ballerinax.docker.generator.utils.DockerGenUtils.extractUberJarName;
import static org.ballerinax.kubernetes.KubernetesConstants.DEPLOYMENT_FILE_POSTFIX;
import static org.ballerinax.kubernetes.KubernetesConstants.EXECUTABLE_JAR;
import static org.ballerinax.kubernetes.KubernetesConstants.YAML;
import static org.ballerinax.kubernetes.utils.KnativeUtils.populateEnvVar;

//import org.ballerinax.kubernetes.models.knative.PodTolerationModel;
//import java.util.LinkedList;

//import org.bouncycastle.asn1.cms.MetaData;

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
                    .withName(secretModel.getName() + "-volume")
                    .withReadOnly(secretModel.isReadOnly())
                    .build();
            volumeMounts.add(volumeMount);
        }
        for (ConfigMapModel configMapModel : serviceModel.getConfigMapModels()) {
            VolumeMount volumeMount = new VolumeMountBuilder()
                    .withMountPath(configMapModel.getMountPath())
                    .withName(configMapModel.getName() + "-volume")
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

    private Container generateContainer(ServiceModel serviceModel, List<ContainerPort> containerPorts)
            throws KubernetesPluginException {
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
                    .withName(secretModel.getName() + "-volume")
                    .withNewSecret()
                    .withSecretName(secretModel.getName())
                    .endSecret()
                    .build();
            volumes.add(volume);
        }
        for (ConfigMapModel configMapModel : serviceModel.getConfigMapModels()) {
            Volume volume = new VolumeBuilder()
                    .withName(configMapModel.getName() + "-volume")
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

    /*private List<Toleration> populatePodTolerations(List<PodTolerationModel> podTolerationModels) {
        List<Toleration> tolerations = null;

        if (null != podTolerationModels && podTolerationModels.size() > 0) {
            tolerations = new LinkedList<>();
            for (PodTolerationModel podTolerationModel : podTolerationModels) {
                Toleration toleration = new TolerationBuilder()
                        .withKey(podTolerationModel.getKey())
                        .withOperator(podTolerationModel.getOperator())
                        .withValue(podTolerationModel.getValue())
                        .withEffect(podTolerationModel.getEffect())
                        .withTolerationSeconds((long) podTolerationModel.getTolerationSeconds())
                        .build();

                tolerations.add(toleration);
            }
        }

        return tolerations;
    }*/

    /*private List<LocalObjectReference> getImagePullSecrets(ServiceModel serviceModel) {
        List<LocalObjectReference> imagePullSecrets = new ArrayList<>();
        for (String imagePullSecret : serviceModel.getImagePullSecrets()) {
            imagePullSecrets.add(new LocalObjectReferenceBuilder().withName(imagePullSecret).build());
        }
        return imagePullSecrets;
    }*/

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
        ObjectMeta metaData = new ObjectMetaBuilder()
                .withName(serviceModel.getName())
                .withNamespace(knativeDataHolder.getNamespace())
                .build();
                 KnativeService knativeServiceBuild = new KnativeService();
                 knativeServiceBuild.setMetadata(metaData);
        /*DeploymentSpec deploymentSpec = new DeploymentSpecBuilder()
                .withNewTemplate()
                .withNewSpec()
                .withContainers(container)
                .withInitContainers(generateInitContainer(serviceModel))
                .withVolumes(populateVolume(serviceModel))
                .endSpec()
                .endTemplate()
                .build();*/
        KnativeServiceSpec knativeServiceSpec = new KnativeServiceSpec();
        KnativePodSpec knativePodSpec = new KnativePodSpec();
        knativePodSpec.setContainerConcurrency(100);
        knativePodSpec.setContainers(Collections.singletonList(container));
        knativePodSpec.setInitContainers(generateInitContainer(serviceModel));
        knativePodSpec.setVolumes(populateVolume(serviceModel));
        KnativePodTemplateSpec knativePodTemplateSpec = new KnativePodTemplateSpec();
        knativePodTemplateSpec.setMetadata(null);
        knativePodTemplateSpec.setSpec(knativePodSpec);
        knativeServiceSpec.setTemplate(knativePodTemplateSpec);
        knativeServiceBuild.setSpec(knativeServiceSpec);




        try {
            String deploymentContent = SerializationUtils.dumpWithoutRuntimeStateAsYaml(knativeServiceBuild);
            KnativeUtils.writeToFile(deploymentContent, DEPLOYMENT_FILE_POSTFIX + YAML);
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
            //deploymentModel.setVolumeClaimModels(knativeDataHolder.getVolumeClaimModelSet());
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
