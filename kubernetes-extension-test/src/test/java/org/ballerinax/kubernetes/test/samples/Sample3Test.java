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
import io.fabric8.kubernetes.api.model.Service;
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
import java.util.List;

import static org.ballerinax.kubernetes.KubernetesConstants.DOCKER;
import static org.ballerinax.kubernetes.KubernetesConstants.KUBERNETES;
import static org.ballerinax.kubernetes.test.utils.KubernetesTestUtils.deployK8s;
import static org.ballerinax.kubernetes.test.utils.KubernetesTestUtils.getExposedPorts;
import static org.ballerinax.kubernetes.test.utils.KubernetesTestUtils.loadImage;
import static org.ballerinax.kubernetes.test.utils.KubernetesTestUtils.validateService;

/**
 * Test cases for sample 3.
 */
public class Sample3Test extends SampleTest {

    private static final Path SOURCE_DIR_PATH = SAMPLE_DIR.resolve("sample3");
    private static final Path DOCKER_TARGET_PATH = SOURCE_DIR_PATH.resolve(DOCKER);
    private static final Path KUBERNETES_TARGET_PATH = SOURCE_DIR_PATH.resolve(KUBERNETES);
    private static final String DOCKER_IMAGE = "foodstore:latest";
    private static final String SELECTOR_APP = "foodstore";
    private Deployment deployment;
    private Service pizzaSvc;
    private Service burgerSvc;
    private Ingress pizzaIngress;
    private Ingress burgerIngress;

    @BeforeClass
    public void compileSample() throws IOException, InterruptedException {
        Assert.assertEquals(KubernetesTestUtils.compileBallerinaFile(SOURCE_DIR_PATH, "foodstore.bal"), 0);
        File artifactYaml = KUBERNETES_TARGET_PATH.resolve("foodstore.yaml").toFile();
        Assert.assertTrue(artifactYaml.exists());
        KubernetesClient client = new DefaultKubernetesClient();
        List<HasMetadata> k8sItems = client.load(new FileInputStream(artifactYaml)).get();
        for (HasMetadata data : k8sItems) {
            switch (data.getKind()) {
                case "Deployment":
                    deployment = (Deployment) data;
                    break;
                case "Service":
                    switch (data.getMetadata().getName()) {
                        case "pizzaep-svc":
                            pizzaSvc = (Service) data;
                            break;
                        case "burgerep-svc":
                            burgerSvc = (Service) data;
                            break;
                        default:
                            break;
                    }
                    break;
                case "Ingress":
                    switch (data.getMetadata().getName()) {
                        case "pizzaep-ingress":
                            pizzaIngress = (Ingress) data;
                            break;
                        case "burgerep-ingress":
                            burgerIngress = (Ingress) data;
                            break;
                        default:
                            break;
                    }
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
        Assert.assertEquals(deployment.getMetadata().getName(), SELECTOR_APP);
        Assert.assertEquals(deployment.getSpec().getReplicas().intValue(), 3);
        Assert.assertEquals(deployment.getMetadata().getLabels().get(KubernetesConstants
                .KUBERNETES_SELECTOR_KEY), SELECTOR_APP);
        Assert.assertEquals(deployment.getSpec().getTemplate().getSpec().getContainers().size(), 1);
        Container container = deployment.getSpec().getTemplate().getSpec().getContainers().get(0);
        Assert.assertEquals(container.getImage(), DOCKER_IMAGE);
        Assert.assertEquals(container.getImagePullPolicy(), KubernetesConstants.ImagePullPolicy.IfNotPresent.name());
        Assert.assertEquals(container.getPorts().size(), 2);
        Assert.assertEquals(container.getEnv().size(), 0);
        Assert.assertEquals(container.getLivenessProbe().getPeriodSeconds().intValue(), 5);
        Assert.assertEquals(container.getLivenessProbe().getInitialDelaySeconds().intValue(), 10);
    }

    @Test
    public void validateK8SService() {
        Assert.assertNotNull(pizzaSvc);
        Assert.assertEquals(pizzaSvc.getMetadata().getName(), "pizzaep-svc");
        Assert.assertEquals(pizzaSvc.getMetadata().getLabels().get(KubernetesConstants
                .KUBERNETES_SELECTOR_KEY), SELECTOR_APP);
        Assert.assertEquals(pizzaSvc.getSpec().getType(), KubernetesConstants.ServiceType.ClusterIP.name());
        Assert.assertEquals(pizzaSvc.getSpec().getPorts().size(), 1);
        Assert.assertEquals(pizzaSvc.getSpec().getPorts().get(0).getPort().intValue(), 9099);
        Assert.assertEquals(pizzaSvc.getSpec().getSessionAffinity(), "ClientIP");

        // Assert burgerSvc
        Assert.assertNotNull(burgerSvc);
        Assert.assertEquals(burgerSvc.getMetadata().getName(), "burgerep-svc");
        Assert.assertEquals(burgerSvc.getMetadata().getLabels().get(KubernetesConstants
                .KUBERNETES_SELECTOR_KEY), SELECTOR_APP);
        Assert.assertEquals(burgerSvc.getSpec().getType(), KubernetesConstants.ServiceType.ClusterIP.name());
        Assert.assertEquals(burgerSvc.getSpec().getPorts().size(), 1);
        Assert.assertEquals(burgerSvc.getSpec().getPorts().get(0).getPort().intValue(), 9096);
    }

    @Test(dependsOnMethods = {"validateK8SService"})
    public void validateIngress() {
        // Assert Burger ingress
        Assert.assertNotNull(burgerIngress);
        Assert.assertEquals(burgerIngress.getMetadata().getName(), "burgerep-ingress");
        Assert.assertEquals(burgerIngress.getMetadata().getLabels().get(KubernetesConstants
                .KUBERNETES_SELECTOR_KEY), SELECTOR_APP);
        Assert.assertEquals(burgerIngress.getSpec().getRules().get(0).getHost(), "burger.com");
        Assert.assertEquals(burgerIngress.getSpec().getRules().get(0).getHttp().getPaths().get(0).getPath(), "/(.*)");
        Assert.assertEquals(burgerSvc.getMetadata().getName(), burgerIngress.getSpec().getRules().get(0).getHttp()
                .getPaths()
                .get(0).getBackend()
                .getServiceName());
        Assert.assertEquals(burgerSvc.getSpec().getPorts().get(0).getPort().intValue(), burgerIngress.getSpec()
                .getRules()
                .get(0).getHttp().getPaths().get(0).getBackend()
                .getServicePort().getIntVal().intValue());
        Assert.assertEquals(burgerIngress.getMetadata().getAnnotations().size(), 3);

        // Assert Pizza ingress
        Assert.assertNotNull(pizzaIngress);
        Assert.assertEquals(pizzaIngress.getMetadata().getName(), "pizzaep-ingress");
        Assert.assertEquals(pizzaIngress.getMetadata().getLabels().get(KubernetesConstants
                .KUBERNETES_SELECTOR_KEY), SELECTOR_APP);
        Assert.assertEquals(pizzaIngress.getSpec().getRules().get(0).getHost(), "pizza.com");
        Assert.assertEquals(pizzaIngress.getSpec().getRules().get(0).getHttp().getPaths().get(0).getPath(),
                "/pizzastore(/|$)(.*)");
        Assert.assertEquals(pizzaSvc.getMetadata().getName(), pizzaIngress.getSpec().getRules().get(0).getHttp()
                .getPaths()
                .get(0).getBackend()
                .getServiceName());
        Assert.assertEquals(pizzaSvc.getSpec().getPorts().get(0).getPort().intValue(), pizzaIngress.getSpec()
                .getRules()
                .get(0).getHttp().getPaths().get(0).getBackend()
                .getServicePort().getIntVal().intValue());
        Assert.assertEquals(pizzaIngress.getMetadata().getAnnotations().size(), 3);
    }

    @Test
    public void validateDockerfile() {
        File dockerFile = DOCKER_TARGET_PATH.resolve("Dockerfile").toFile();
        Assert.assertTrue(dockerFile.exists());
    }

    @Test
    public void validateDockerImage() throws DockerTestException, InterruptedException {
        List<String> ports = getExposedPorts(DOCKER_IMAGE);
        Assert.assertEquals(ports.size(), 2);
        Assert.assertEquals(ports.get(0), "9096/tcp");
        Assert.assertEquals(ports.get(1), "9099/tcp");
    }

    @Test(groups = {"integration"})
    public void deploySample() throws IOException, InterruptedException {
        Assert.assertEquals(0, loadImage(DOCKER_IMAGE));
        Assert.assertEquals(0, deployK8s(KUBERNETES_TARGET_PATH));
        Assert.assertTrue(validateService("http://pizza.com/pizzastore/pizza/menu", "Pizza menu"));
        Assert.assertTrue(validateService("http://burger.com/menu", "Burger menu"));
        KubernetesTestUtils.deleteK8s(KUBERNETES_TARGET_PATH);
    }

    @AfterClass
    public void cleanUp() throws KubernetesPluginException {
        KubernetesUtils.deleteDirectory(KUBERNETES_TARGET_PATH);
        KubernetesUtils.deleteDirectory(DOCKER_TARGET_PATH);
        KubernetesTestUtils.deleteDockerImage(DOCKER_IMAGE);
    }
}
