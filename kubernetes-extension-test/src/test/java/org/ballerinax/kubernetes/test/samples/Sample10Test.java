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
import java.util.List;

import static org.ballerinax.kubernetes.KubernetesConstants.DOCKER;
import static org.ballerinax.kubernetes.KubernetesConstants.KUBERNETES;
import static org.ballerinax.kubernetes.test.utils.KubernetesTestUtils.getExposedPorts;

public class Sample10Test implements SampleTest {

    private final String sourceDirPath = SAMPLE_DIR + File.separator + "sample10";
    private final String targetPath = sourceDirPath + File.separator + "target" + File.separator + KUBERNETES;
    private final String burgerPkgTargetPath = targetPath + File.separator + "burger";
    private final String pizzaPkgTargetPath = targetPath + File.separator + "pizza";
    private final String burgerDockerImage = "burger:latest";
    private final String pizzaDockerImage = "pizza:latest";
    private final String pizzaSelector = "pizza";
    private final String burgerSelector = "burger";


    @BeforeClass
    public void compileSample() throws IOException, InterruptedException {
        Assert.assertEquals(KubernetesTestUtils.compileBallerinaProject(sourceDirPath), 0);
    }

    @Test
    public void validateHelmChartYaml() {
        Assert.assertTrue(new File(burgerPkgTargetPath + File.separator + 
                "burger-deployment" + File.separator + "Chart.yaml").exists());
    }
    
    @Test
    public void validateHelmChartTemplates() {
        File templateDir = new File(burgerPkgTargetPath + File.separator +
                "burger-deployment" + File.separator + "templates");
        Assert.assertTrue(templateDir.isDirectory());
        Assert.assertTrue(templateDir.list().length > 0);
    }
    
    @Test
    public void validateDockerfile() {
        Assert.assertTrue(new File(burgerPkgTargetPath + File.separator + DOCKER + File.separator + "Dockerfile")
                .exists());
        Assert.assertTrue(new File(pizzaPkgTargetPath + File.separator + DOCKER + File.separator + "Dockerfile")
                .exists());
    }

    @Test
    public void validateDockerImageBurger() throws DockerTestException, InterruptedException {
        List<String> ports = getExposedPorts(burgerDockerImage);
        Assert.assertEquals(ports.size(), 1);
        Assert.assertEquals(ports.get(0), "9096/tcp");
    }

    @Test
    public void validateDockerImagePizza() throws DockerTestException, InterruptedException {
        List<String> ports = getExposedPorts(pizzaDockerImage);
        Assert.assertEquals(ports.size(), 1);
        Assert.assertEquals(ports.get(0), "9099/tcp");
    }

    @Test
    public void validateBurgerDeployment() throws IOException {
        File deploymentYAML = new File(burgerPkgTargetPath + File.separator + "burger_deployment.yaml");
        Assert.assertTrue(deploymentYAML.exists());
        Deployment deployment = KubernetesTestUtils.loadYaml(deploymentYAML);
        // Assert Deployment
        Assert.assertEquals("burger-deployment", deployment.getMetadata().getName());
        Assert.assertEquals(1, deployment.getSpec().getReplicas().intValue());
        Assert.assertEquals(1, deployment.getSpec().getTemplate().getSpec().getVolumes().size());
        Assert.assertEquals(burgerSelector, deployment.getMetadata().getLabels().get(KubernetesConstants
                .KUBERNETES_SELECTOR_KEY));
        Assert.assertEquals(1, deployment.getSpec().getTemplate().getSpec().getContainers().size());

        // Assert Containers
        Container container = deployment.getSpec().getTemplate().getSpec().getContainers().get(0);
        Assert.assertEquals(1, container.getVolumeMounts().size());
        Assert.assertEquals(burgerDockerImage, container.getImage());
        Assert.assertEquals(KubernetesConstants.ImagePullPolicy.IfNotPresent.name(), container.getImagePullPolicy());
        Assert.assertEquals(1, container.getPorts().size());
    }

    @Test
    public void validatePizzaDeployment() throws IOException {
        File deploymentYAML = new File(pizzaPkgTargetPath + File.separator + "pizza_deployment.yaml");
        Assert.assertTrue(deploymentYAML.exists());
        Deployment deployment = KubernetesTestUtils.loadYaml(deploymentYAML);
        // Assert Deployment
        Assert.assertEquals("foodstore", deployment.getMetadata().getName());
        Assert.assertEquals(3, deployment.getSpec().getReplicas().intValue());
        Assert.assertEquals(0, deployment.getSpec().getTemplate().getSpec().getVolumes().size());
        Assert.assertEquals(pizzaSelector, deployment.getMetadata().getLabels().get(KubernetesConstants
                .KUBERNETES_SELECTOR_KEY));
        Assert.assertEquals(1, deployment.getSpec().getTemplate().getSpec().getContainers().size());

        // Assert Containers
        Container container = deployment.getSpec().getTemplate().getSpec().getContainers().get(0);
        Assert.assertEquals(0, container.getVolumeMounts().size());
        Assert.assertEquals(pizzaDockerImage, container.getImage());
        Assert.assertEquals(KubernetesConstants.ImagePullPolicy.IfNotPresent.name(), container.getImagePullPolicy());
        Assert.assertEquals(1, container.getPorts().size());
        Assert.assertEquals(2, container.getEnv().size());
    }

    @Test
    public void validatePizzaSVC() throws IOException {
        File serviceYAML = new File(pizzaPkgTargetPath + File.separator + "pizza_svc.yaml");
        Assert.assertTrue(serviceYAML.exists());
        Service service = KubernetesTestUtils.loadYaml(serviceYAML);
        Assert.assertEquals("pizzaep-svc", service.getMetadata().getName());
        Assert.assertEquals(pizzaSelector, service.getMetadata().getLabels().get(KubernetesConstants
                .KUBERNETES_SELECTOR_KEY));
        Assert.assertEquals(KubernetesConstants.ServiceType.ClusterIP.name(), service.getSpec().getType());
        Assert.assertEquals(1, service.getSpec().getPorts().size());
        Assert.assertEquals(9099, service.getSpec().getPorts().get(0).getPort().intValue());
    }

    @Test
    public void validateBurgerSVC() throws IOException {
        File serviceYAML = new File(burgerPkgTargetPath + File.separator + "burger_svc.yaml");
        Assert.assertTrue(serviceYAML.exists());
        Service service = KubernetesTestUtils.loadYaml(serviceYAML);
        Assert.assertEquals("burgerep-svc", service.getMetadata().getName());
        Assert.assertEquals(burgerSelector, service.getMetadata().getLabels().get(KubernetesConstants
                .KUBERNETES_SELECTOR_KEY));
        Assert.assertEquals(KubernetesConstants.ServiceType.ClusterIP.name(), service.getSpec().getType());
        Assert.assertEquals(1, service.getSpec().getPorts().size());
        Assert.assertEquals(9096, service.getSpec().getPorts().get(0).getPort().intValue());
    }

    @Test
    public void validatePizzaIngress() throws IOException {
        File ingressYAML = new File(pizzaPkgTargetPath + File.separator + "pizza_ingress.yaml");
        Assert.assertNotNull(ingressYAML);
        Ingress ingress = KubernetesTestUtils.loadYaml(ingressYAML);
        Assert.assertEquals("pizzaep-ingress", ingress.getMetadata().getName());
        Assert.assertEquals(pizzaSelector, ingress.getMetadata().getLabels().get(KubernetesConstants
                .KUBERNETES_SELECTOR_KEY));
        Assert.assertEquals("pizza.com", ingress.getSpec().getRules().get(0).getHost());
        Assert.assertEquals("/pizzastore", ingress.getSpec().getRules().get(0).getHttp().getPaths().get(0).getPath());
        Assert.assertTrue(ingress.getMetadata().getAnnotations().containsKey("nginx.ingress.kubernetes" +
                ".io/ssl-passthrough"));
        Assert.assertFalse(Boolean.valueOf(ingress.getMetadata().getAnnotations().get("nginx.ingress.kubernetes" +
                ".io/ssl-passthrough")));
        Assert.assertEquals(0, ingress.getSpec().getTls().size());
    }

    @Test
    public void validateBurgerIngress() throws IOException {
        File ingressYAML = new File(burgerPkgTargetPath + File.separator + "burger_ingress.yaml");
        Assert.assertNotNull(ingressYAML);
        Ingress ingress = KubernetesTestUtils.loadYaml(ingressYAML);
        Assert.assertEquals("burgerep-ingress", ingress.getMetadata().getName());
        Assert.assertEquals(burgerSelector, ingress.getMetadata().getLabels().get(KubernetesConstants
                .KUBERNETES_SELECTOR_KEY));
        Assert.assertEquals("burger.com", ingress.getSpec().getRules().get(0).getHost());
        Assert.assertEquals("/", ingress.getSpec().getRules().get(0).getHttp().getPaths().get(0).getPath());
        Assert.assertTrue(ingress.getMetadata().getAnnotations().containsKey("nginx.ingress.kubernetes" +
                ".io/ssl-passthrough"));
        Assert.assertTrue(Boolean.valueOf(ingress.getMetadata().getAnnotations().get("nginx.ingress.kubernetes" +
                ".io/ssl-passthrough")));
        Assert.assertEquals(1, ingress.getSpec().getTls().size());
        Assert.assertEquals(1, ingress.getSpec().getTls().get(0).getHosts().size());
        Assert.assertEquals("burger.com", ingress.getSpec().getTls().get(0).getHosts().get(0));
    }

    @Test
    public void validateBurgerSecret() throws IOException {
        File secretYAML = new File(burgerPkgTargetPath + File.separator + "burger_secret.yaml");
        Assert.assertTrue(secretYAML.exists());
        Secret secret = KubernetesTestUtils.loadYaml(secretYAML);
        Assert.assertEquals("burgerep-keystore", secret.getMetadata().getName());
        Assert.assertEquals(1, secret.getData().size());
    }

    @AfterClass
    public void cleanUp() throws KubernetesPluginException, DockerTestException, InterruptedException {
        KubernetesUtils.deleteDirectory(targetPath);
        KubernetesTestUtils.deleteDockerImage(pizzaDockerImage);
        KubernetesTestUtils.deleteDockerImage(burgerDockerImage);
    }
}
