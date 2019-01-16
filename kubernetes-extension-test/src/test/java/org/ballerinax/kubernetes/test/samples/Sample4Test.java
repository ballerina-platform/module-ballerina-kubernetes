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

import io.fabric8.kubernetes.api.KubernetesHelper;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.extensions.Deployment;
import io.fabric8.kubernetes.api.model.extensions.Ingress;
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
import java.io.IOException;
import java.util.List;

import static org.ballerinax.kubernetes.KubernetesConstants.DOCKER;
import static org.ballerinax.kubernetes.KubernetesConstants.KUBERNETES;
import static org.ballerinax.kubernetes.test.utils.KubernetesTestUtils.getExposedPorts;

public class Sample4Test implements SampleTest {

    private final String sourceDirPath = SAMPLE_DIR + File.separator + "sample4";
    private final String targetPath = sourceDirPath + File.separator + KUBERNETES;
    private final String dockerImage = "hello_world_ssl_k8s:latest";
    private final String selectorApp = "hello_world_ssl_k8s";

    @BeforeClass
    public void compileSample() throws IOException, InterruptedException {
        Assert.assertEquals(KubernetesTestUtils.compileBallerinaFile(sourceDirPath, "hello_world_ssl_k8s.bal"), 0);
    }

    @Test
    public void validateDockerfile() {
        File dockerFile = new File(targetPath + File.separator + DOCKER + File.separator + "Dockerfile");
        Assert.assertTrue(dockerFile.exists());
    }

    @Test
    public void validateDockerImage() throws DockerTestException, InterruptedException {
        List<String> ports = getExposedPorts(this.dockerImage);
        Assert.assertEquals(ports.size(), 1);
        Assert.assertEquals(ports.get(0), "9090/tcp");
    }

    @Test
    public void validateDeployment() throws IOException {
        File deploymentYAML = new File(targetPath + File.separator + "hello_world_ssl_k8s_deployment.yaml");
        Assert.assertTrue(deploymentYAML.exists());
        Deployment deployment = KubernetesHelper.loadYaml(deploymentYAML);
        // Assert Deployment
        Assert.assertEquals("hello-world-ssl-k8s-deployment", deployment.getMetadata().getName());
        Assert.assertEquals(1, deployment.getSpec().getReplicas().intValue());
        Assert.assertEquals("helloworldsecuredep-keystore", deployment.getSpec().getTemplate().getSpec().getVolumes()
                .get(0).getSecret().getSecretName());
        Assert.assertEquals(selectorApp, deployment.getMetadata().getLabels().get(KubernetesConstants
                .KUBERNETES_SELECTOR_KEY));
        Assert.assertEquals(1, deployment.getSpec().getTemplate().getSpec().getContainers().size());

        // Assert Containers
        Container container = deployment.getSpec().getTemplate().getSpec().getContainers().get(0);
        Assert.assertEquals(1, container.getVolumeMounts().size());
        Assert.assertEquals("/ballerina/runtime/bre/security", container.getVolumeMounts().get(0).getMountPath());
        Assert.assertEquals("helloworldsecuredep-keystore-volume", container.getVolumeMounts().get(0).getName());
        Assert.assertTrue(container.getVolumeMounts().get(0).getReadOnly().booleanValue());
        Assert.assertEquals(dockerImage, container.getImage());
        Assert.assertEquals(KubernetesConstants.ImagePullPolicy.IfNotPresent.name(), container.getImagePullPolicy());
        Assert.assertEquals(1, container.getPorts().size());
    }

    @Test
    public void validateSecret() throws IOException {
        File secretYAML = new File(targetPath + File.separator + "hello_world_ssl_k8s_secret.yaml");
        Assert.assertTrue(secretYAML.exists());
        Secret secret = KubernetesHelper.loadYaml(secretYAML);
        Assert.assertEquals("helloworldsecuredep-keystore", secret.getMetadata().getName());
        Assert.assertEquals(1, secret.getData().size());
    }

    @Test
    public void validateIngress() throws IOException {
        File ingressYAML = new File(targetPath + File.separator + "hello_world_ssl_k8s_ingress.yaml");
        Assert.assertNotNull(ingressYAML);
        Ingress ingress = KubernetesHelper.loadYaml(ingressYAML);
        Assert.assertEquals("helloworldsecuredep-ingress", ingress.getMetadata().getName());
        Assert.assertEquals(selectorApp, ingress.getMetadata().getLabels().get(KubernetesConstants
                .KUBERNETES_SELECTOR_KEY));
        Assert.assertEquals("abc.com", ingress.getSpec().getRules().get(0).getHost());
        Assert.assertEquals("/", ingress.getSpec().getRules().get(0).getHttp().getPaths().get(0).getPath());
        Assert.assertTrue(ingress.getMetadata().getAnnotations().containsKey("nginx.ingress.kubernetes" +
                ".io/ssl-passthrough"));
        Assert.assertTrue(Boolean.valueOf(ingress.getMetadata().getAnnotations().get("nginx.ingress.kubernetes" +
                ".io/ssl-passthrough")));
        Assert.assertEquals(1, ingress.getSpec().getTls().size());
        Assert.assertEquals(1, ingress.getSpec().getTls().get(0).getHosts().size());
        Assert.assertEquals("abc.com", ingress.getSpec().getTls().get(0).getHosts().get(0));
    }

    @AfterClass
    public void cleanUp() throws KubernetesPluginException, DockerTestException, InterruptedException {
        KubernetesUtils.deleteDirectory(targetPath);
        KubernetesTestUtils.deleteDockerImage(dockerImage);
    }
}
