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

package org.ballerinax.kubernetes.test;

import com.github.dockerjava.api.command.InspectImageResponse;
import io.fabric8.kubernetes.api.model.ContainerPort;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServicePort;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import org.ballerinax.kubernetes.exceptions.KubernetesPluginException;
import org.ballerinax.kubernetes.test.utils.DockerTestException;
import org.ballerinax.kubernetes.test.utils.KubernetesTestUtils;
import org.ballerinax.kubernetes.utils.KubernetesUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static org.ballerinax.kubernetes.KubernetesConstants.DOCKER;
import static org.ballerinax.kubernetes.KubernetesConstants.KUBERNETES;
import static org.ballerinax.kubernetes.test.utils.KubernetesTestUtils.getDockerImage;

/**
 * Test cases for Prometheus Deployment.
 */
public class PrometheusTest {
    private static final Path BAL_DIRECTORY = Paths.get("src", "test", "resources", "deployment", "prometheus");
    private static final Path DOCKER_TARGET_PATH = BAL_DIRECTORY.resolve(DOCKER);
    private static final Path KUBERNETES_TARGET_PATH = BAL_DIRECTORY.resolve(KUBERNETES);
    private static final String DOCKER_IMAGE = "pizza-shop:latest";

    /**
     * Build bal file with default prometheus settings.
     *
     * @throws IOException               Error when loading the generated yaml.
     * @throws InterruptedException      Error when compiling the ballerina file.
     * @throws KubernetesPluginException Error when deleting the generated artifacts folder.
     */
    @Test
    public void defaultPrometheusTest() throws IOException, InterruptedException, KubernetesPluginException,
            DockerTestException {
        Assert.assertEquals(KubernetesTestUtils.compileBallerinaFile(BAL_DIRECTORY, "default.bal"), 0);

        // Check if docker image exists and correct
        validateDockerfile();
        validateDockerImage();

        // Validate deployment yaml
        File deploymentYAML = KUBERNETES_TARGET_PATH.resolve("default_deployment.yaml").toFile();
        Assert.assertTrue(deploymentYAML.exists());
        Deployment deployment = KubernetesTestUtils.loadYaml(deploymentYAML);
        Assert.assertNotNull(deployment.getSpec());
        Assert.assertNotNull(deployment.getSpec().getTemplate());
        Assert.assertNotNull(deployment.getSpec().getTemplate().getSpec());
        Assert.assertTrue(deployment.getSpec().getTemplate().getSpec().getContainers().size() > 0);
        final List<ContainerPort> ports = deployment.getSpec().getTemplate().getSpec().getContainers().get(0)
                .getPorts();
        Assert.assertEquals(ports.size(), 2);
        Assert.assertEquals(ports.get(0).getContainerPort().intValue(), 9090);
        Assert.assertEquals(ports.get(1).getContainerPort().intValue(), 9797);

        // Validate svc yaml
        File svcYAML = KUBERNETES_TARGET_PATH.resolve("default_svc.yaml").toFile();
        Assert.assertTrue(svcYAML.exists());
        Service svc = KubernetesTestUtils.loadYaml(svcYAML);
        Assert.assertEquals(svc.getSpec().getPorts().size(), 1);
        final ServicePort servicePort = svc.getSpec().getPorts().get(0);
        Assert.assertEquals(servicePort.getPort().intValue(), 9090);
        Assert.assertEquals(servicePort.getProtocol(), "TCP");
        Assert.assertEquals(servicePort.getTargetPort().getIntVal().intValue(), 9090);
        Assert.assertEquals(svc.getSpec().getType(), "ClusterIP");
        Assert.assertEquals(svc.getMetadata().getName(), "helloep-svc");
        Assert.assertEquals(svc.getSpec().getSelector().get("app"), "default");

        File svcPrometheusYAML = KUBERNETES_TARGET_PATH.resolve("default_prometheus_svc.yaml").toFile();
        Assert.assertTrue(svcPrometheusYAML.exists());
        Service svcPrometheus = KubernetesTestUtils.loadYaml(svcPrometheusYAML);
        Assert.assertEquals(svcPrometheus.getSpec().getPorts().size(), 1);
        final ServicePort prometheusSvcPort = svcPrometheus.getSpec().getPorts().get(0);
        Assert.assertEquals(prometheusSvcPort.getPort().intValue(), 9797);
        Assert.assertEquals(prometheusSvcPort.getProtocol(), "TCP");
        Assert.assertEquals(prometheusSvcPort.getTargetPort().getIntVal().intValue(), 9797);
        Assert.assertEquals(svcPrometheus.getSpec().getType(), "ClusterIP");
        Assert.assertEquals(svcPrometheus.getMetadata().getName(), "helloep-svc-prometheus");
        Assert.assertEquals(svcPrometheus.getSpec().getSelector().get("app"), "default");

        KubernetesUtils.deleteDirectory(KUBERNETES_TARGET_PATH);
        KubernetesUtils.deleteDirectory(DOCKER_TARGET_PATH);
        KubernetesTestUtils.deleteDockerImage(DOCKER_IMAGE);
    }

    /**
     * Build bal file with NodePort prometheus settings.
     *
     * @throws IOException               Error when loading the generated yaml.
     * @throws InterruptedException      Error when compiling the ballerina file.
     * @throws KubernetesPluginException Error when deleting the generated artifacts folder.
     */
    @Test
    public void nodePortPrometheusTest() throws IOException, InterruptedException, KubernetesPluginException,
            DockerTestException {
        Assert.assertEquals(KubernetesTestUtils.compileBallerinaFile(BAL_DIRECTORY, "nodeport.bal"), 0);

        // Check if docker image exists and correct
        validateDockerfile();
        validateDockerImage();

        // Validate deployment yaml
        File deploymentYAML = KUBERNETES_TARGET_PATH.resolve("nodeport_deployment.yaml").toFile();
        Assert.assertTrue(deploymentYAML.exists());
        Deployment deployment = KubernetesTestUtils.loadYaml(deploymentYAML);
        Assert.assertNotNull(deployment.getSpec());
        Assert.assertNotNull(deployment.getSpec().getTemplate());
        Assert.assertNotNull(deployment.getSpec().getTemplate().getSpec());
        Assert.assertTrue(deployment.getSpec().getTemplate().getSpec().getContainers().size() > 0);
        final List<ContainerPort> ports = deployment.getSpec().getTemplate().getSpec().getContainers().get(0)
                .getPorts();
        Assert.assertEquals(ports.size(), 2);
        Assert.assertEquals(ports.get(0).getContainerPort().intValue(), 9090);
        Assert.assertEquals(ports.get(1).getContainerPort().intValue(), 9898);

        // Validate svc yaml
        File svcYAML = KUBERNETES_TARGET_PATH.resolve("nodeport_svc.yaml").toFile();
        Assert.assertTrue(svcYAML.exists());
        Service svc = KubernetesTestUtils.loadYaml(svcYAML);
        Assert.assertEquals(svc.getSpec().getPorts().size(), 1);
        final ServicePort servicePort = svc.getSpec().getPorts().get(0);
        Assert.assertEquals(servicePort.getPort().intValue(), 9090);
        Assert.assertEquals(servicePort.getProtocol(), "TCP");
        Assert.assertEquals(servicePort.getTargetPort().getIntVal().intValue(), 9090);
        Assert.assertEquals(svc.getSpec().getType(), "ClusterIP");
        Assert.assertEquals(svc.getMetadata().getName(), "helloep-svc");
        Assert.assertEquals(svc.getSpec().getSelector().get("app"), "nodeport");

        // Validate prometheus svc yaml
        File svcPrometheusYAML = KUBERNETES_TARGET_PATH.resolve("nodeport_prometheus_svc.yaml").toFile();
        Assert.assertTrue(svcPrometheusYAML.exists());
        Service svcPrometheus = KubernetesTestUtils.loadYaml(svcPrometheusYAML);
        Assert.assertEquals(svcPrometheus.getSpec().getPorts().size(), 1);
        final ServicePort prometheusSvcPort = svcPrometheus.getSpec().getPorts().get(0);
        Assert.assertEquals(prometheusSvcPort.getPort().intValue(), 9898);
        Assert.assertEquals(prometheusSvcPort.getProtocol(), "TCP");
        Assert.assertEquals(prometheusSvcPort.getTargetPort().getIntVal().intValue(), 9898);
        Assert.assertEquals(svcPrometheus.getSpec().getType(), "NodePort");
        Assert.assertEquals(svcPrometheus.getMetadata().getName(), "helloep-svc-prometheus");
        Assert.assertEquals(svcPrometheus.getSpec().getSelector().get("app"), "nodeport");

        KubernetesUtils.deleteDirectory(KUBERNETES_TARGET_PATH);
        KubernetesUtils.deleteDirectory(DOCKER_TARGET_PATH);
        KubernetesTestUtils.deleteDockerImage(DOCKER_IMAGE);
    }

    /**
     * Validate if Dockerfile is created.
     */
    public void validateDockerfile() {
        File dockerFile = DOCKER_TARGET_PATH.resolve("Dockerfile").toFile();
        Assert.assertTrue(dockerFile.exists());
    }

    /**
     * Validate contents of the Dockerfile.
     */
    public void validateDockerImage() throws DockerTestException, InterruptedException {
        InspectImageResponse imageInspect = getDockerImage(DOCKER_IMAGE);
        Assert.assertTrue(Arrays.toString(Objects.requireNonNull(imageInspect.getConfig())
                .getCmd()).contains("--b7a.observability.enabled=true"));
        Assert.assertNotNull(imageInspect.getConfig(), "Image not found");
    }
}
