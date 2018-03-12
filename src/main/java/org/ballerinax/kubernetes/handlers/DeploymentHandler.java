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


import com.fasterxml.jackson.core.JsonProcessingException;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ContainerBuilder;
import io.fabric8.kubernetes.api.model.ContainerPort;
import io.fabric8.kubernetes.api.model.ContainerPortBuilder;
import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.EnvVarBuilder;
import io.fabric8.kubernetes.api.model.Probe;
import io.fabric8.kubernetes.api.model.ProbeBuilder;
import io.fabric8.kubernetes.api.model.TCPSocketAction;
import io.fabric8.kubernetes.api.model.TCPSocketActionBuilder;
import io.fabric8.kubernetes.api.model.extensions.Deployment;
import io.fabric8.kubernetes.api.model.extensions.DeploymentBuilder;
import io.fabric8.kubernetes.client.internal.SerializationUtils;
import org.ballerinax.kubernetes.KubeGenConstants;
import org.ballerinax.kubernetes.exceptions.ArtifactGenerationException;
import org.ballerinax.kubernetes.models.DeploymentModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.ballerinax.kubernetes.utils.KubeGenUtils.printError;

/**
 * Generates kubernetes deployment from annotations.
 */
public class DeploymentHandler implements ArtifactHandler {

    private DeploymentModel deploymentModel;

    public DeploymentHandler(DeploymentModel deploymentModel) {
        this.deploymentModel = deploymentModel;
    }

    private List<ContainerPort> populatePorts(List<Integer> ports) {
        List<ContainerPort> containerPorts = new ArrayList<>();
        for (int port : ports) {
            ContainerPort containerPort = new ContainerPortBuilder()
                    .withContainerPort(port)
                    .withProtocol(KubeGenConstants.KUBERNETES_SVC_PROTOCOL)
                    .build();
            containerPorts.add(containerPort);
        }
        return containerPorts;
    }

    private Container generateContainer(DeploymentModel deploymentModel, List<ContainerPort>
            containerPorts) {
        return new ContainerBuilder()
                .withName(deploymentModel.getName())
                .withImage(deploymentModel.getImage())
                .withImagePullPolicy(deploymentModel.getImagePullPolicy())
                .withPorts(containerPorts)
                .withEnv(populateEnvVar(deploymentModel.getEnv()))
                .withLivenessProbe(generateLivenessProbe(deploymentModel))
                .build();
    }

    private List<EnvVar> populateEnvVar(Map<String, String> envMap) {
        List<EnvVar> envVars = new ArrayList<>();
        if (envMap == null) {
            return envVars;
        }
        envMap.forEach((k, v) -> {
            EnvVar envVar = new EnvVarBuilder().withName(k).withValue(v).build();
            envVars.add(envVar);
        });
        return envVars;
    }

    private Probe generateLivenessProbe(DeploymentModel deploymentModel) {
        if (KubeGenConstants.DEPLOYMENT_LIVENESS_DISABLE.equals(deploymentModel.getEnableLiveness())) {
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

    /**
     * Generate kubernetes deployment definition from annotation.
     *
     * @return Generated kubernetes @{@link Deployment} definition
     * @throws ArtifactGenerationException If an error occurs while generating artifact.
     */
    public String generate() throws ArtifactGenerationException {
        List<ContainerPort> containerPorts = null;
        if (deploymentModel.getPorts() != null) {
            containerPorts = populatePorts(deploymentModel.getPorts());
        }
        Container container = generateContainer(deploymentModel, containerPorts);
        Deployment deployment = new DeploymentBuilder()
                .withNewMetadata()
                .withName(deploymentModel.getName())
                .withNamespace(deploymentModel.getNamespace())
                .withLabels(deploymentModel.getLabels())
                .endMetadata()
                .withNewSpec()
                .withReplicas(deploymentModel.getReplicas())
                .withNewTemplate()
                .withNewMetadata()
                .addToLabels(deploymentModel.getLabels())
                .endMetadata()
                .withNewSpec()
                .withContainers(container)
                .endSpec()
                .endTemplate()
                .endSpec()
                .build();
        String deploymentYAML;
        try {
            deploymentYAML = SerializationUtils.dumpWithoutRuntimeStateAsYaml(deployment);
        } catch (JsonProcessingException e) {
            String errorMessage = "Error while parsing yaml file for deployment: " + deploymentModel.getName();
            printError(errorMessage);
            throw new ArtifactGenerationException(errorMessage, e);
        }
        return deploymentYAML;
    }

}

