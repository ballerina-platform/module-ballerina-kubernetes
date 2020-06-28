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
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.apps.Deployment;
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
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

import static org.ballerinax.kubernetes.KubernetesConstants.DOCKER;
import static org.ballerinax.kubernetes.KubernetesConstants.KUBERNETES;
import static org.ballerinax.kubernetes.test.utils.KubernetesTestUtils.deleteK8s;
import static org.ballerinax.kubernetes.test.utils.KubernetesTestUtils.deployK8s;
import static org.ballerinax.kubernetes.test.utils.KubernetesTestUtils.getExposedPorts;
import static org.ballerinax.kubernetes.test.utils.KubernetesTestUtils.loadImage;
import static org.ballerinax.kubernetes.test.utils.KubernetesTestUtils.validateService;

/**
 * Test cases for sample 10.
 */
public class Sample10Test extends SampleTest {

    private static final Path SOURCE_DIR_PATH = SAMPLE_DIR.resolve("sample10");
    private static final Path DOCKER_TARGET_PATH = SOURCE_DIR_PATH.resolve("target").resolve(DOCKER);
    private static final Path KUBERNETES_TARGET_PATH = SOURCE_DIR_PATH.resolve("target").resolve(KUBERNETES);
    private static final Path BURGER_PKG_DOCKER_TARGET_PATH = DOCKER_TARGET_PATH.resolve("burger");
    private static final Path PIZZA_PKG_DOCKER_TARGET_PATH = DOCKER_TARGET_PATH.resolve("pizza");
    private static final Path BURGER_PKG_K8S_TARGET_PATH = KUBERNETES_TARGET_PATH.resolve("burger");
    private static final Path PIZZA_PKG_K8S_TARGET_PATH = KUBERNETES_TARGET_PATH.resolve("pizza");
    private static final String BURGER_DOCKER_IMAGE = "john-burger-0.0.1:latest";
    private static final String PIZZA_DOCKER_IMAGE = "john-pizza-0.0.1:latest";
    private static final String BURGER_SELECTOR = "john-burger-0.0.1";
    private static final String PIZZA_SELECTOR = "john-pizza-0.0.1";
    private Deployment burgerDeployment;
    private Deployment pizzaDeployment;
    private Service burgerService;
    private Service pizzaService;
    private Ingress burgerIngress;
    private Ingress pizzaIngress;
    private Secret burgerSecret;

    @BeforeClass
    public void compileSample() throws IOException, InterruptedException {
        Assert.assertEquals(KubernetesTestUtils.compileBallerinaProject(SOURCE_DIR_PATH), 0);
        File burgerYamlFile = BURGER_PKG_K8S_TARGET_PATH.resolve("john-burger-0.0.1.yaml").toFile();
        Assert.assertTrue(burgerYamlFile.exists());
        List<HasMetadata> k8sItems = KubernetesTestUtils.loadYaml(burgerYamlFile);
        for (HasMetadata data : k8sItems) {
            switch (data.getKind()) {
                case "Deployment":
                    burgerDeployment = (Deployment) data;
                    break;
                case "Service":
                    burgerService = (Service) data;
                    break;
                case "Ingress":
                    burgerIngress = (Ingress) data;
                    break;
                case "Secret":
                    burgerSecret = (Secret) data;
                    break;
                default:
                    break;
            }
        }

        File pizzaYamlFile = PIZZA_PKG_K8S_TARGET_PATH.resolve("john-pizza-0.0.1.yaml").toFile();
        Assert.assertTrue(pizzaYamlFile.exists());
        k8sItems = KubernetesTestUtils.loadYaml(pizzaYamlFile);
        for (HasMetadata data : k8sItems) {
            switch (data.getKind()) {
                case "Deployment":
                    pizzaDeployment = (Deployment) data;
                    break;
                case "Service":
                    pizzaService = (Service) data;
                    break;
                case "Ingress":
                    pizzaIngress = (Ingress) data;
                    break;
                default:
                    break;
            }
        }
    }

    @Test
    public void validateHelmChartYaml() {
        Assert.assertTrue(BURGER_PKG_K8S_TARGET_PATH.resolve("john-burger-0-0-1-deployment").resolve("Chart.yaml").toFile()
                .exists());
    }

    @Test
    public void validateHelmChartTemplates() {
        File templateDir = BURGER_PKG_K8S_TARGET_PATH.resolve("john-burger-0-0-1-deployment").resolve("templates").toFile();
        Assert.assertTrue(templateDir.isDirectory());
        Assert.assertTrue(Objects.requireNonNull(templateDir.list()).length > 0);
    }

    @Test
    public void validateBurgerDeployment() {
        Assert.assertNotNull(burgerDeployment);
        Assert.assertEquals(burgerDeployment.getMetadata().getName(), "john-burger-0-0-1-deployment");
        Assert.assertEquals(burgerDeployment.getSpec().getReplicas().intValue(), 1);
        Assert.assertEquals(burgerDeployment.getMetadata().getLabels().get(KubernetesConstants
                .KUBERNETES_SELECTOR_KEY), BURGER_SELECTOR);
        Assert.assertEquals(burgerDeployment.getSpec().getTemplate().getSpec().getContainers().size(), 1);

        // Assert Containers
        Container container = burgerDeployment.getSpec().getTemplate().getSpec().getContainers().get(0);
        Assert.assertEquals(container.getVolumeMounts().size(), 0);
        Assert.assertEquals(container.getImage(), BURGER_DOCKER_IMAGE);
        Assert.assertEquals(container.getImagePullPolicy(), KubernetesConstants.ImagePullPolicy.IfNotPresent.name());
        Assert.assertEquals(container.getPorts().size(), 1);
    }

    @Test
    public void validatePizzaDeployment() {
        Assert.assertNotNull(pizzaDeployment);
        Assert.assertEquals(pizzaDeployment.getMetadata().getName(), "foodstore");
        Assert.assertEquals(pizzaDeployment.getSpec().getReplicas().intValue(), 3);
        Assert.assertEquals(pizzaDeployment.getSpec().getTemplate().getSpec().getVolumes().size(), 0);
        Assert.assertEquals(pizzaDeployment.getMetadata().getLabels().get(KubernetesConstants
                .KUBERNETES_SELECTOR_KEY), PIZZA_SELECTOR);
        Assert.assertEquals(pizzaDeployment.getSpec().getTemplate().getSpec().getContainers().size(), 1);

        // Assert Containers
        Container container = pizzaDeployment.getSpec().getTemplate().getSpec().getContainers().get(0);
        Assert.assertEquals(container.getVolumeMounts().size(), 0);
        Assert.assertEquals(container.getImage(), PIZZA_DOCKER_IMAGE);
        Assert.assertEquals(container.getImagePullPolicy(), KubernetesConstants.ImagePullPolicy.IfNotPresent.name());
        Assert.assertEquals(container.getPorts().size(), 1);
        Assert.assertEquals(container.getEnv().size(), 2);
    }

    @Test
    public void validateBurgerSVC() {
        Assert.assertNotNull(burgerService);
        Assert.assertEquals(burgerService.getMetadata().getName(), "burgerep-svc");
        Assert.assertEquals(burgerService.getMetadata().getLabels().get(KubernetesConstants
                .KUBERNETES_SELECTOR_KEY), BURGER_SELECTOR);
        Assert.assertEquals(burgerService.getSpec().getType(), KubernetesConstants.ServiceType.ClusterIP.name());
        Assert.assertEquals(burgerService.getSpec().getPorts().size(), 1);
        Assert.assertEquals(burgerService.getSpec().getPorts().get(0).getPort().intValue(), 9096);
    }

    @Test
    public void validatePizzaSVC() {
        Assert.assertNotNull(pizzaService);
        Assert.assertEquals(pizzaService.getMetadata().getName(), "pizzaep-svc");
        Assert.assertEquals(pizzaService.getMetadata().getLabels().get(KubernetesConstants
                .KUBERNETES_SELECTOR_KEY), PIZZA_SELECTOR);
        Assert.assertEquals(pizzaService.getSpec().getType(), KubernetesConstants.ServiceType.ClusterIP.name());
        Assert.assertEquals(pizzaService.getSpec().getPorts().size(), 1);
        Assert.assertEquals(pizzaService.getSpec().getPorts().get(0).getPort().intValue(), 9099);
    }

    @Test
    public void validateBurgerIngress() {
        Assert.assertNotNull(burgerIngress);
        Assert.assertEquals(burgerIngress.getMetadata().getName(), "burgerep-ingress");
        Assert.assertEquals(burgerIngress.getMetadata().getLabels().get(KubernetesConstants
                .KUBERNETES_SELECTOR_KEY), BURGER_SELECTOR);
        Assert.assertEquals(burgerIngress.getSpec().getRules().get(0).getHost(), "burger.com");
        Assert.assertEquals(burgerIngress.getSpec().getRules().get(0).getHttp().getPaths().get(0).getPath(), "/(.*)");
    }

    @Test
    public void validatePizzaIngress() {
        Assert.assertNotNull(pizzaIngress);
        Assert.assertEquals(pizzaIngress.getMetadata().getName(), "pizzaep-ingress");
        Assert.assertEquals(pizzaIngress.getMetadata().getLabels().get(KubernetesConstants
                .KUBERNETES_SELECTOR_KEY), PIZZA_SELECTOR);
        Assert.assertEquals(pizzaIngress.getSpec().getRules().get(0).getHost(), "pizza.com");
        Assert.assertEquals(pizzaIngress.getSpec().getRules().get(0).getHttp().getPaths().get(0).getPath(),
                "/pizzastore(/|$)(.*)");
        Assert.assertTrue(pizzaIngress.getMetadata().getAnnotations().containsKey(
                "nginx.ingress.kubernetes.io/ssl-passthrough"));
        Assert.assertFalse(Boolean.parseBoolean(pizzaIngress.getMetadata().getAnnotations().get(
                "nginx.ingress.kubernetes.io/ssl-passthrough")));
        Assert.assertEquals(pizzaIngress.getSpec().getTls().size(), 0);
    }


    @Test
    public void validateDockerfile() {
        Assert.assertTrue(BURGER_PKG_DOCKER_TARGET_PATH.resolve("Dockerfile").toFile().exists());
        Assert.assertTrue(PIZZA_PKG_DOCKER_TARGET_PATH.resolve("Dockerfile").toFile().exists());
    }

    @Test
    public void validateDockerImageBurger() throws DockerTestException, InterruptedException {
        List<String> ports = getExposedPorts(BURGER_DOCKER_IMAGE);
        Assert.assertEquals(ports.size(), 1);
        Assert.assertEquals(ports.get(0), "9096/tcp");
    }

    @Test
    public void validateDockerImagePizza() throws DockerTestException, InterruptedException {
        List<String> ports = getExposedPorts(PIZZA_DOCKER_IMAGE);
        Assert.assertEquals(ports.size(), 1);
        Assert.assertEquals(ports.get(0), "9099/tcp");
    }

//    @Test(groups = {"integration"})
//    public void deploySample() throws IOException, InterruptedException {
//        Assert.assertEquals(0, loadImage(BURGER_DOCKER_IMAGE));
//        Assert.assertEquals(0, loadImage(PIZZA_DOCKER_IMAGE));
//        Assert.assertEquals(0, deployK8s(BURGER_PKG_K8S_TARGET_PATH));
//        Assert.assertEquals(0, deployK8s(PIZZA_PKG_K8S_TARGET_PATH));
//        Assert.assertTrue(validateService("http://pizza.com/pizzastore/pizza/menu", "Pizza menu"));
//        Assert.assertTrue(validateService("http://burger.com/menu", "Burger menu"));
//        deleteK8s(BURGER_PKG_K8S_TARGET_PATH);
//        deleteK8s(PIZZA_PKG_K8S_TARGET_PATH);
//    }

    @AfterClass
    public void cleanUp() throws KubernetesPluginException {
        KubernetesUtils.deleteDirectory(KUBERNETES_TARGET_PATH);
        KubernetesUtils.deleteDirectory(DOCKER_TARGET_PATH);
        KubernetesTestUtils.deleteDockerImage(PIZZA_DOCKER_IMAGE);
        KubernetesTestUtils.deleteDockerImage(BURGER_DOCKER_IMAGE);
    }
}
