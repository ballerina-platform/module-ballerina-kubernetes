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

import static org.ballerinax.kubernetes.KubernetesConstants.DOCKER;
import static org.ballerinax.kubernetes.KubernetesConstants.KUBERNETES;
import static org.ballerinax.kubernetes.test.utils.KubernetesTestUtils.getExposedPorts;

public class Sample10Test extends SampleTest {

    private static final Path SOURCE_DIR_PATH = SAMPLE_DIR.resolve("sample10");
    private static final Path TARGET_PATH = SOURCE_DIR_PATH.resolve("target").resolve(KUBERNETES);
    private static final Path BURGER_PKG_TARGET_PATH = TARGET_PATH.resolve("burger");
    private static final Path PIZZA_PKG_TARGET_PATH = TARGET_PATH.resolve("pizza");
    private static final String BURGER_DOCKER_IMAGE = "burger:latest";
    private static final String PIZZA_DOCKER_IMAGE = "pizza:latest";
    private static final String BURGER_SELECTOR = "burger";
    private static final String PIZZA_SELECTOR = "pizza";
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
        File burgerYamlFile = BURGER_PKG_TARGET_PATH.resolve("burger.yaml").toFile();
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
        
        File pizzaYamlFile = PIZZA_PKG_TARGET_PATH.resolve("pizza.yaml").toFile();
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
        Assert.assertTrue(BURGER_PKG_TARGET_PATH.resolve("burger-deployment").resolve("Chart.yaml").toFile().exists());
    }
    
    @Test
    public void validateHelmChartTemplates() {
        File templateDir = BURGER_PKG_TARGET_PATH.resolve("burger-deployment").resolve("templates").toFile();
        Assert.assertTrue(templateDir.isDirectory());
        Assert.assertTrue(templateDir.list().length > 0);
    }

    @Test
    public void validateBurgerDeployment() {
        Assert.assertNotNull(burgerDeployment);
        Assert.assertEquals(burgerDeployment.getMetadata().getName(), "burger-deployment");
        Assert.assertEquals(burgerDeployment.getSpec().getReplicas().intValue(), 1);
        Assert.assertEquals(burgerDeployment.getSpec().getTemplate().getSpec().getVolumes().size(), 1);
        Assert.assertEquals(burgerDeployment.getMetadata().getLabels().get(KubernetesConstants
                .KUBERNETES_SELECTOR_KEY), BURGER_SELECTOR);
        Assert.assertEquals(burgerDeployment.getSpec().getTemplate().getSpec().getContainers().size(), 1);

        // Assert Containers
        Container container = burgerDeployment.getSpec().getTemplate().getSpec().getContainers().get(0);
        Assert.assertEquals(container.getVolumeMounts().size(), 1);
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
        Assert.assertEquals(burgerIngress.getSpec().getRules().get(0).getHttp().getPaths().get(0).getPath(), "/");
        Assert.assertTrue(burgerIngress.getMetadata().getAnnotations().containsKey(
                "nginx.ingress.kubernetes.io/ssl-passthrough"));
        Assert.assertTrue(Boolean.valueOf(burgerIngress.getMetadata().getAnnotations().get(
                "nginx.ingress.kubernetes.io/ssl-passthrough")));
        Assert.assertEquals(burgerIngress.getSpec().getTls().size(), 1);
        Assert.assertEquals(burgerIngress.getSpec().getTls().get(0).getHosts().size(), 1);
        Assert.assertEquals(burgerIngress.getSpec().getTls().get(0).getHosts().get(0), "burger.com");
    }

    @Test
    public void validatePizzaIngress() {
        Assert.assertNotNull(pizzaIngress);
        Assert.assertEquals(pizzaIngress.getMetadata().getName(), "pizzaep-ingress");
        Assert.assertEquals(pizzaIngress.getMetadata().getLabels().get(KubernetesConstants
                .KUBERNETES_SELECTOR_KEY), PIZZA_SELECTOR);
        Assert.assertEquals(pizzaIngress.getSpec().getRules().get(0).getHost(), "pizza.com");
        Assert.assertEquals(pizzaIngress.getSpec().getRules().get(0).getHttp().getPaths().get(0).getPath(),
                "/pizzastore");
        Assert.assertTrue(pizzaIngress.getMetadata().getAnnotations().containsKey(
                "nginx.ingress.kubernetes.io/ssl-passthrough"));
        Assert.assertFalse(Boolean.valueOf(pizzaIngress.getMetadata().getAnnotations().get(
                "nginx.ingress.kubernetes.io/ssl-passthrough")));
        Assert.assertEquals(pizzaIngress.getSpec().getTls().size(), 0);
    }

    @Test
    public void validateBurgerSecret() {
        Assert.assertNotNull(burgerSecret);
        Assert.assertEquals(burgerSecret.getMetadata().getName(), "burgerep-keystore");
        Assert.assertEquals(burgerSecret.getData().size(), 1);
    }
    
    @Test
    public void validateDockerfile() {
        Assert.assertTrue(BURGER_PKG_TARGET_PATH.resolve(DOCKER).resolve("Dockerfile").toFile().exists());
        Assert.assertTrue(PIZZA_PKG_TARGET_PATH.resolve(DOCKER).resolve("Dockerfile").toFile().exists());
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

    @AfterClass
    public void cleanUp() throws KubernetesPluginException, DockerTestException, InterruptedException {
        KubernetesUtils.deleteDirectory(TARGET_PATH);
        KubernetesTestUtils.deleteDockerImage(PIZZA_DOCKER_IMAGE);
        KubernetesTestUtils.deleteDockerImage(BURGER_DOCKER_IMAGE);
    }
}
