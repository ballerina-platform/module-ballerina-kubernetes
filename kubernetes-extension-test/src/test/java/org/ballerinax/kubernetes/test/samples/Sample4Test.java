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

package org.ballerinax.kubernetes.test.samples;

import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.extensions.Ingress;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.ballerinax.kubernetes.KubernetesConstants;
import org.ballerinax.kubernetes.exceptions.KubernetesPluginException;
import org.ballerinax.kubernetes.test.utils.DockerTestException;
import org.ballerinax.kubernetes.test.utils.KubernetesTestUtils;
import org.ballerinax.kubernetes.utils.KubernetesUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.ballerinax.kubernetes.KubernetesConstants.DOCKER;
import static org.ballerinax.kubernetes.KubernetesConstants.KUBERNETES;
import static org.ballerinax.kubernetes.test.utils.KubernetesTestUtils.deleteK8s;
import static org.ballerinax.kubernetes.test.utils.KubernetesTestUtils.deployK8s;
import static org.ballerinax.kubernetes.test.utils.KubernetesTestUtils.getExposedPorts;
import static org.ballerinax.kubernetes.test.utils.KubernetesTestUtils.readFromURL;

/**
 * Test cases for sample 4.
 */
public class Sample4Test extends SampleTest {

    private static final Path SOURCE_DIR_PATH = SAMPLE_DIR.resolve("sample4");
    private static final Path DOCKER_TARGET_PATH = SOURCE_DIR_PATH.resolve(DOCKER);
    private static final Path KUBERNETES_TARGET_PATH = SOURCE_DIR_PATH.resolve(KUBERNETES);
    private static final String DOCKER_IMAGE = "hello_world_ssl_k8s:latest";
    private static final String SELECTOR_APP = "hello_world_ssl_k8s";
    private Deployment deployment;
    private Secret secret;
    private Ingress ingress;

    @BeforeClass
    public void compileSample() throws IOException, InterruptedException {
        Assert.assertEquals(KubernetesTestUtils.compileBallerinaFile(SOURCE_DIR_PATH, "hello_world_ssl_k8s.bal"), 0);
        File artifactYaml = KUBERNETES_TARGET_PATH.resolve("hello_world_ssl_k8s.yaml").toFile();
        Assert.assertTrue(artifactYaml.exists());
        KubernetesClient client = new DefaultKubernetesClient();
        List<HasMetadata> k8sItems = client.load(new FileInputStream(artifactYaml)).get();
        for (HasMetadata data : k8sItems) {
            switch (data.getKind()) {
                case "Deployment":
                    deployment = (Deployment) data;
                    break;
                case "Secret":
                    secret = (Secret) data;
                    break;
                case "Ingress":
                    ingress = (Ingress) data;
                    break;
                case "Service":
                    break;
                default:
                    Assert.fail("Unexpected k8s resource found: " + data.getKind());
                    break;
            }
        }
    }

    @Test
    public void validateDeployment() {
        Assert.assertNotNull(deployment);
        Assert.assertEquals(deployment.getMetadata().getName(), "hello-world-ssl-k8s-deployment");
        Assert.assertEquals(deployment.getSpec().getReplicas().intValue(), 1);
        Assert.assertEquals(deployment.getSpec().getTemplate().getSpec().getVolumes()
                .get(0).getSecret().getSecretName(), "helloworldsecuredep-keystore");
        Assert.assertEquals(deployment.getMetadata().getLabels().get(KubernetesConstants
                .KUBERNETES_SELECTOR_KEY), SELECTOR_APP);
        Assert.assertEquals(deployment.getSpec().getTemplate().getSpec().getContainers().size(), 1);

        // Assert Containers
        Container container = deployment.getSpec().getTemplate().getSpec().getContainers().get(0);
        Assert.assertEquals(container.getVolumeMounts().size(), 1);
        Assert.assertEquals(container.getVolumeMounts().get(0).getMountPath(), "/home/ballerina/./security");
        Assert.assertEquals(container.getVolumeMounts().get(0).getName(), "helloworldsecuredep-keystore-volume");
        Assert.assertTrue(container.getVolumeMounts().get(0).getReadOnly());
        Assert.assertEquals(container.getImage(), DOCKER_IMAGE);
        Assert.assertEquals(container.getImagePullPolicy(), KubernetesConstants.ImagePullPolicy.IfNotPresent.name());
        Assert.assertEquals(container.getPorts().size(), 1);
    }

    @Test
    public void validateSecret() {
        Assert.assertNotNull(secret);
        Assert.assertEquals(secret.getMetadata().getName(), "helloworldsecuredep-keystore");
        Assert.assertEquals(secret.getData().size(), 1);
    }

    @Test
    public void validateIngress() {
        Assert.assertNotNull(ingress);
        Assert.assertEquals(ingress.getMetadata().getName(), "helloworldsecuredep-ingress");
        Assert.assertEquals(ingress.getMetadata().getLabels().get(KubernetesConstants
                .KUBERNETES_SELECTOR_KEY), SELECTOR_APP);
        Assert.assertEquals(ingress.getSpec().getRules().get(0).getHost(), "abc.com");
        Assert.assertEquals(ingress.getSpec().getRules().get(0).getHttp().getPaths().get(0).getPath(), "/");
        Assert.assertTrue(ingress.getMetadata().getAnnotations().containsKey(
                "nginx.ingress.kubernetes.io/ssl-passthrough"));
        Assert.assertTrue(Boolean.parseBoolean(ingress.getMetadata().getAnnotations().get(
                "nginx.ingress.kubernetes.io/ssl-passthrough")));
        Assert.assertEquals(ingress.getSpec().getTls().size(), 1);
        Assert.assertEquals(ingress.getSpec().getTls().get(0).getHosts().size(), 1);
        Assert.assertEquals(ingress.getSpec().getTls().get(0).getHosts().get(0), "abc.com");
    }

    @Test
    public void validateDockerfile() {
        File dockerFile = DOCKER_TARGET_PATH.resolve("Dockerfile").toFile();
        Assert.assertTrue(dockerFile.exists());
    }

    @Test(groups = {"integration"})
    public void deploySample() throws IOException, InterruptedException {
        Assert.assertEquals(0, deployK8s(KUBERNETES_TARGET_PATH));
        Assert.assertTrue(readFromURL("https://abc.com/helloWorld/sayHello",
                "Hello, World from secured service !"));
        KubernetesTestUtils.deleteK8s(KUBERNETES_TARGET_PATH);
    }

    @Test
    public void validateDockerImage() throws DockerTestException, InterruptedException {
        List<String> ports = getExposedPorts(DOCKER_IMAGE);
        Assert.assertEquals(ports.size(), 1);
        Assert.assertEquals(ports.get(0), "9090/tcp");
    }

    @AfterClass
    public void cleanUp() throws KubernetesPluginException {
        KubernetesUtils.deleteDirectory(KUBERNETES_TARGET_PATH);
        KubernetesUtils.deleteDirectory(DOCKER_TARGET_PATH);
        KubernetesTestUtils.deleteDockerImage(DOCKER_IMAGE);
    }
}
