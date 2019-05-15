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
import me.snowdrop.istio.api.networking.v1alpha3.Gateway;
import me.snowdrop.istio.api.networking.v1alpha3.NumberPort;
import me.snowdrop.istio.api.networking.v1alpha3.PortSelector;
import me.snowdrop.istio.api.networking.v1alpha3.VirtualService;
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

public class Sample16Test extends SampleTest {

    private static final Path SOURCE_DIR_PATH = SAMPLE_DIR.resolve("sample16");
    private static final Path TARGET_PATH = SOURCE_DIR_PATH.resolve("target").resolve(KUBERNETES);
    private static final Path BOOK_DETAILS_PKG_TARGET_PATH = TARGET_PATH.resolve("book.details");
    private static final Path BOOK_REVIEWS_PKG_TARGET_PATH = TARGET_PATH.resolve("book.reviews");
    private static final Path BOOK_SHOP_PKG_TARGET_PATH = TARGET_PATH.resolve("book.shop");
    private static final String BOOK_DETAILS_DOCKER_IMAGE = "book.details:latest";
    private static final String BOOK_REVIEWS_DOCKER_IMAGE = "book.reviews:latest";
    private static final String BOOK_SHOP_DOCKER_IMAGE = "book.shop:latest";
    private Deployment deployment = null;
    private Service service = null;
    private Gateway gateway = null;
    private VirtualService virtualService = null;

    @BeforeClass
    public void compileSample() throws IOException, InterruptedException {
        Assert.assertEquals(KubernetesTestUtils.compileBallerinaProject(SOURCE_DIR_PATH), 0);
        File yamlFile = BOOK_SHOP_PKG_TARGET_PATH.resolve("book.shop.yaml").toFile();
        Assert.assertTrue(yamlFile.exists());
        List<HasMetadata> k8sItems = KubernetesTestUtils.loadYaml(yamlFile);
        for (HasMetadata data : k8sItems) {
            switch (data.getKind()) {
                case "Deployment":
                    deployment = (Deployment) data;
                    break;
                case "Service":
                    service = (Service) data;
                    break;
                case "Gateway":
                    gateway = (Gateway) data;
                    break;
                case "VirtualService":
                    virtualService = (VirtualService) data;
                    break;
                default:
                    break;
            }
        }
    }

    @Test
    public void validateDockerfile() {
        Assert.assertTrue(BOOK_DETAILS_PKG_TARGET_PATH.resolve(DOCKER).resolve("Dockerfile").toFile().exists());
        Assert.assertTrue(BOOK_REVIEWS_PKG_TARGET_PATH.resolve(DOCKER).resolve("Dockerfile").toFile().exists());
        Assert.assertTrue(BOOK_SHOP_PKG_TARGET_PATH.resolve(DOCKER).resolve("Dockerfile").toFile().exists());
    }

    @Test
    public void validateDockerImageBookDetails() throws DockerTestException, InterruptedException {
        List<String> ports = getExposedPorts(BOOK_DETAILS_DOCKER_IMAGE);
        Assert.assertEquals(ports.size(), 1);
        Assert.assertEquals(ports.get(0), "8080/tcp");
    }

    @Test
    public void validateDockerImageBookReviews() throws DockerTestException, InterruptedException {
        List<String> ports = getExposedPorts(BOOK_REVIEWS_DOCKER_IMAGE);
        Assert.assertEquals(ports.size(), 1);
        Assert.assertEquals(ports.get(0), "7070/tcp");
    }
    
    @Test
    public void validateDockerImageBookShop() throws DockerTestException, InterruptedException {
        List<String> ports = getExposedPorts(BOOK_SHOP_DOCKER_IMAGE);
        Assert.assertEquals(ports.size(), 1);
        Assert.assertEquals(ports.get(0), "9080/tcp");
    }

    @Test
    public void validateShopDeployment() {
        // Assert Deployment
        Assert.assertNotNull(deployment);
        Assert.assertNotNull(deployment.getMetadata());
        Assert.assertEquals(deployment.getMetadata().getName(), "book-shop-deployment");
        Assert.assertEquals(deployment.getSpec().getReplicas().intValue(), 1, "Invalid replica value");
        Assert.assertEquals(deployment.getMetadata().getLabels().get(KubernetesConstants
                .KUBERNETES_SELECTOR_KEY), "book.shop", "Invalid label");
        Assert.assertEquals(deployment.getSpec().getTemplate().getSpec().getContainers().size(), 1,
                "Invalid number of containers.");

        // Assert Containers
        Container container = deployment.getSpec().getTemplate().getSpec().getContainers().get(0);
        Assert.assertEquals(container.getImage(), BOOK_SHOP_DOCKER_IMAGE, "Invalid container image");
        Assert.assertEquals(container.getImagePullPolicy(), KubernetesConstants.ImagePullPolicy.IfNotPresent.name());
        Assert.assertEquals(container.getPorts().size(), 1, "Invalid number of container ports");
        Assert.assertEquals(container.getPorts().get(0).getContainerPort().intValue(), 9080, "Invalid container port");
    }
    
    @Test
    public void validateShopService() {
        // Assert Service
        Assert.assertNotNull(service);
        Assert.assertNotNull(service.getSpec());
        Assert.assertNotNull(service.getSpec().getPorts());
        Assert.assertEquals(service.getSpec().getPorts().size(), 1);
        Assert.assertEquals(service.getSpec().getPorts().get(0).getName(), "http-bookshopep-svc");
    }

    @Test
    public void validateShopGateway() {
        Assert.assertNotNull(gateway);
        Assert.assertNotNull(gateway.getMetadata());
        Assert.assertEquals(gateway.getMetadata().getName(), "bookshopep-istio-gw", "Invalid gateway name");
    
        Assert.assertNotNull(gateway.getSpec());
        Assert.assertNotNull(gateway.getSpec().getSelector());
        Assert.assertEquals(gateway.getSpec().getSelector().get(KubernetesConstants.ISTIO_GATEWAY_SELECTOR),
                "ingressgateway", "Invalid selector.");
    
        Assert.assertNotNull(gateway.getSpec().getServers());
        Assert.assertEquals(gateway.getSpec().getServers().size(), 1);
        Assert.assertNotNull(gateway.getSpec().getServers().get(0).getPort());
        Assert.assertEquals(gateway.getSpec().getServers().get(0).getPort().getNumber().intValue(), 80,
                "Invalid port number.");
        Assert.assertEquals(gateway.getSpec().getServers().get(0).getPort().getName(), "http", "Invalid port name.");
        Assert.assertEquals(gateway.getSpec().getServers().get(0).getPort().getProtocol(), "HTTP",
                "Invalid port protocol.");
    
        Assert.assertTrue(gateway.getSpec().getServers().get(0).getHosts().contains("*"), "* host not included");
    }
    
    @Test
    public void validateShopVirtualService() {
        Assert.assertNotNull(virtualService);
        Assert.assertNotNull(virtualService.getMetadata());
        Assert.assertEquals(virtualService.getMetadata().getName(), "bookshopep-istio-vs",
                "Invalid virtual service name");
    
        Assert.assertNotNull(virtualService.getSpec());
        Assert.assertNotNull(virtualService.getSpec().getHosts());
        Assert.assertEquals(virtualService.getSpec().getHosts().size(), 1);
        Assert.assertEquals(virtualService.getSpec().getHosts().get(0), "*", "Invalid host value.");
    
        Assert.assertNotNull(virtualService.getSpec().getHttp());
        Assert.assertEquals(virtualService.getSpec().getHttp().size(), 1, "Invalid number of http items");
    
        Assert.assertNotNull(virtualService.getSpec().getHttp().get(0).getRoute());
        Assert.assertEquals(virtualService.getSpec().getHttp().get(0).getRoute().size(), 1);
        Assert.assertEquals(virtualService.getSpec().getHttp().get(0).getRoute().get(0).getDestination().getHost(),
                "bookshopep-svc", "Invalid route destination host");
        PortSelector.Port destinationPort =
                virtualService.getSpec().getHttp().get(0).getRoute().get(0).getDestination().getPort().getPort();
        Assert.assertTrue(destinationPort instanceof NumberPort);
        NumberPort destinationPortNumber = (NumberPort) destinationPort;
        Assert.assertEquals(destinationPortNumber.getNumber().intValue(), 9080, "Invalid port");
    
        Assert.assertEquals(virtualService.getSpec().getHttp().get(0).getRoute().get(0).getWeight().intValue(), 100,
                "Invalid weight");
    
    }

    @AfterClass
    public void cleanUp() throws KubernetesPluginException {
        KubernetesUtils.deleteDirectory(TARGET_PATH);
        KubernetesTestUtils.deleteDockerImage(BOOK_REVIEWS_DOCKER_IMAGE);
        KubernetesTestUtils.deleteDockerImage(BOOK_DETAILS_DOCKER_IMAGE);
        KubernetesTestUtils.deleteDockerImage(BOOK_SHOP_DOCKER_IMAGE);
    }
}
