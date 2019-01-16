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
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.extensions.Deployment;
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
import java.util.List;

import static org.ballerinax.kubernetes.KubernetesConstants.DOCKER;
import static org.ballerinax.kubernetes.KubernetesConstants.KUBERNETES;
import static org.ballerinax.kubernetes.test.utils.KubernetesTestUtils.getExposedPorts;

public class Sample3Test implements SampleTest {

    private final String sourceDirPath = SAMPLE_DIR + File.separator + "sample3";
    private final String targetPath = sourceDirPath + File.separator + KUBERNETES;
    private final String dockerImage = "foodstore:latest";
    private final String selectorApp = "foodstore";
    private Service pizzaSvc;
    private Service burgerSvc;

    @BeforeClass
    public void compileSample() throws IOException, InterruptedException {
        Assert.assertEquals(KubernetesTestUtils.compileBallerinaFile(sourceDirPath, "foodstore.bal"), 0);
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
        Assert.assertEquals(ports.get(0), "9099/tcp");
        Assert.assertEquals(ports.get(1), "9096/tcp");
    }

    @Test
    public void validateDeployment() throws IOException {
        File deploymentYAML = new File(targetPath + File.separator + "foodstore_deployment.yaml");
        Assert.assertTrue(deploymentYAML.exists());
        Deployment deployment = KubernetesHelper.loadYaml(deploymentYAML);
        Assert.assertEquals(selectorApp, deployment.getMetadata().getName());
        Assert.assertEquals(3, deployment.getSpec().getReplicas().intValue());
        Assert.assertEquals(selectorApp, deployment.getMetadata().getLabels().get(KubernetesConstants
                .KUBERNETES_SELECTOR_KEY));
        Assert.assertEquals(1, deployment.getSpec().getTemplate().getSpec().getContainers().size());
        Container container = deployment.getSpec().getTemplate().getSpec().getContainers().get(0);
        Assert.assertEquals(dockerImage, container.getImage());
        Assert.assertEquals(KubernetesConstants.ImagePullPolicy.IfNotPresent.name(), container.getImagePullPolicy());
        Assert.assertEquals(2, container.getPorts().size());
        Assert.assertEquals(0, container.getEnv().size());
        Assert.assertEquals(5, container.getLivenessProbe().getPeriodSeconds().intValue());
        Assert.assertEquals(10, container.getLivenessProbe().getInitialDelaySeconds().intValue());
    }

    @Test
    public void validateK8SService() throws IOException {
        File serviceYAML = new File(targetPath + File.separator + "foodstore_svc.yaml");
        Assert.assertTrue(serviceYAML.exists());
        KubernetesClient client = new DefaultKubernetesClient();
        List<HasMetadata> k8sItems = client.load(new FileInputStream(serviceYAML)).get();
        Assert.assertEquals(2, k8sItems.size());
        for (HasMetadata data : k8sItems) {
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
        }

        // Assert pizzaSvc
        Assert.assertNotNull(pizzaSvc);
        Assert.assertEquals("pizzaep-svc", pizzaSvc.getMetadata().getName());
        Assert.assertEquals(selectorApp, pizzaSvc.getMetadata().getLabels().get(KubernetesConstants
                .KUBERNETES_SELECTOR_KEY));
        Assert.assertEquals(KubernetesConstants.ServiceType.ClusterIP.name(), pizzaSvc.getSpec().getType());
        Assert.assertEquals(1, pizzaSvc.getSpec().getPorts().size());
        Assert.assertEquals(9099, pizzaSvc.getSpec().getPorts().get(0).getPort().intValue());
        Assert.assertEquals("ClientIP", pizzaSvc.getSpec().getSessionAffinity());

        // Assert burgerSvc
        Assert.assertNotNull(burgerSvc);
        Assert.assertEquals("burgerep-svc", burgerSvc.getMetadata().getName());
        Assert.assertEquals(selectorApp, burgerSvc.getMetadata().getLabels().get(KubernetesConstants
                .KUBERNETES_SELECTOR_KEY));
        Assert.assertEquals(KubernetesConstants.ServiceType.ClusterIP.name(), burgerSvc.getSpec().getType());
        Assert.assertEquals(1, burgerSvc.getSpec().getPorts().size());
        Assert.assertEquals(9096, burgerSvc.getSpec().getPorts().get(0).getPort().intValue());
    }

    @Test(dependsOnMethods = {"validateK8SService"})
    public void validateIngress() throws IOException {
        File ingressYAML = new File(targetPath + File.separator + "foodstore_ingress.yaml");
        Assert.assertTrue(ingressYAML.exists());
        KubernetesClient client = new DefaultKubernetesClient();
        List<HasMetadata> k8sItems = client.load(new FileInputStream(ingressYAML)).get();
        Assert.assertEquals(2, k8sItems.size());
        Ingress pizzaIngress = null;
        Ingress burgerIngress = null;
        for (HasMetadata data : k8sItems) {
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
        }
        // Assert Burger ingress
        Assert.assertNotNull(burgerIngress);
        Assert.assertEquals("burgerep-ingress", burgerIngress.getMetadata().getName());
        Assert.assertEquals(selectorApp, burgerIngress.getMetadata().getLabels().get(KubernetesConstants
                .KUBERNETES_SELECTOR_KEY));
        Assert.assertEquals("burger.com", burgerIngress.getSpec().getRules().get(0).getHost());
        Assert.assertEquals("/", burgerIngress.getSpec().getRules().get(0).getHttp().getPaths().get(0).getPath());
        Assert.assertEquals(burgerSvc.getMetadata().getName(), burgerIngress.getSpec().getRules().get(0).getHttp()
                .getPaths()
                .get(0).getBackend()
                .getServiceName());
        Assert.assertEquals(burgerSvc.getSpec().getPorts().get(0).getPort().intValue(), burgerIngress.getSpec()
                .getRules()
                .get(0).getHttp().getPaths().get(0).getBackend()
                .getServicePort().getIntVal().intValue());
        Assert.assertEquals(3, burgerIngress.getMetadata().getAnnotations().size());

        // Assert Pizza ingress
        Assert.assertNotNull(pizzaIngress);
        Assert.assertEquals("pizzaep-ingress", pizzaIngress.getMetadata().getName());
        Assert.assertEquals(selectorApp, pizzaIngress.getMetadata().getLabels().get(KubernetesConstants
                .KUBERNETES_SELECTOR_KEY));
        Assert.assertEquals("pizza.com", pizzaIngress.getSpec().getRules().get(0).getHost());
        Assert.assertEquals("/pizzastore", pizzaIngress.getSpec().getRules().get(0).getHttp().getPaths().get(0)
                .getPath());
        Assert.assertEquals(pizzaSvc.getMetadata().getName(), pizzaIngress.getSpec().getRules().get(0).getHttp()
                .getPaths()
                .get(0).getBackend()
                .getServiceName());
        Assert.assertEquals(pizzaSvc.getSpec().getPorts().get(0).getPort().intValue(), pizzaIngress.getSpec()
                .getRules()
                .get(0).getHttp().getPaths().get(0).getBackend()
                .getServicePort().getIntVal().intValue());
        Assert.assertEquals(3, pizzaIngress.getMetadata().getAnnotations().size());
    }

    @AfterClass
    public void cleanUp() throws KubernetesPluginException, DockerTestException, InterruptedException {
        KubernetesUtils.deleteDirectory(targetPath);
        KubernetesTestUtils.deleteDockerImage(dockerImage);
    }
}
