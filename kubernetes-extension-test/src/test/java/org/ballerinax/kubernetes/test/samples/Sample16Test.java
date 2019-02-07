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
import io.fabric8.kubernetes.api.model.extensions.Deployment;
import org.ballerinax.kubernetes.KubernetesConstants;
import org.ballerinax.kubernetes.exceptions.KubernetesPluginException;
import org.ballerinax.kubernetes.test.utils.DockerTestException;
import org.ballerinax.kubernetes.test.utils.KubernetesTestUtils;
import org.ballerinax.kubernetes.utils.KubernetesUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.andes.util.FileUtils;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.ballerinax.kubernetes.KubernetesConstants.DOCKER;
import static org.ballerinax.kubernetes.KubernetesConstants.KUBERNETES;
import static org.ballerinax.kubernetes.test.utils.KubernetesTestUtils.getExposedPorts;

public class Sample16Test implements SampleTest {

    private final String sourceDirPath = SAMPLE_DIR + File.separator + "sample16";
    private final String targetPath = sourceDirPath + File.separator + "target" + File.separator + KUBERNETES;
    private final String bookDetailsPkgTargetPath = targetPath + File.separator + "book.details";
    private final String bookReviewsPkgTargetPath = targetPath + File.separator + "book.reviews";
    private final String bookShopPkgTargetPath = targetPath + File.separator + "book.shop";
    private final String bookDetailsDockerImage = "book.details:latest";
    private final String bookReviewsDockerImage = "book.reviews:latest";
    private final String bookShopDockerImage = "book.shop:latest";


    @BeforeClass
    public void compileSample() throws IOException, InterruptedException {
        Assert.assertEquals(KubernetesTestUtils.compileBallerinaProject((SAMPLE_DIR + File.separator + "sample16")), 0);
    }

    @Test
    public void validateDockerfile() {
        Assert.assertTrue(new File(bookDetailsPkgTargetPath + File.separator + DOCKER + File.separator + "Dockerfile")
                .exists());
        Assert.assertTrue(new File(bookReviewsPkgTargetPath + File.separator + DOCKER + File.separator + "Dockerfile")
                .exists());
        Assert.assertTrue(new File(bookShopPkgTargetPath + File.separator + DOCKER + File.separator + "Dockerfile")
                .exists());
    }

    @Test
    public void validateDockerImageBookDetails() throws DockerTestException, InterruptedException {
        List<String> ports = getExposedPorts(bookDetailsDockerImage);
        Assert.assertEquals(ports.size(), 1);
        Assert.assertEquals(ports.get(0), "8080/tcp");
    }

    @Test
    public void validateDockerImageBookReviews() throws DockerTestException, InterruptedException {
        List<String> ports = getExposedPorts(bookReviewsDockerImage);
        Assert.assertEquals(ports.size(), 1);
        Assert.assertEquals(ports.get(0), "7070/tcp");
    }
    
    @Test
    public void validateDockerImageBookShop() throws DockerTestException, InterruptedException {
        List<String> ports = getExposedPorts(bookShopDockerImage);
        Assert.assertEquals(ports.size(), 1);
        Assert.assertEquals(ports.get(0), "9080/tcp");
    }

    @Test(enabled = false)
    public void validateShopDeployment() throws IOException {
        File deploymentYAML = new File(bookShopPkgTargetPath + File.separator + "book.shop_deployment.yaml");
        Assert.assertTrue(deploymentYAML.exists(), "Cannot find deployment yaml");
        Deployment deployment = KubernetesHelper.loadYaml(deploymentYAML);
        // Assert Deployment
        Assert.assertEquals(deployment.getMetadata().getName(), "book-shop-deployment");
        Assert.assertEquals(deployment.getSpec().getReplicas().intValue(), 1, "Invalid replica value");
        Assert.assertEquals(deployment.getMetadata().getLabels().get(KubernetesConstants
                .KUBERNETES_SELECTOR_KEY), "book.shop", "Invalid label");
        Assert.assertEquals(deployment.getSpec().getTemplate().getSpec().getContainers().size(), 1,
                "Invalid number of containers.");

        // Assert Containers
        Container container = deployment.getSpec().getTemplate().getSpec().getContainers().get(0);
        Assert.assertEquals(container.getImage(), bookShopDockerImage, "Invalid container image");
        Assert.assertEquals(container.getImagePullPolicy(), KubernetesConstants.ImagePullPolicy.IfNotPresent.name());
        Assert.assertEquals(container.getPorts().size(), 1, "Invalid number of container ports");
        Assert.assertEquals(container.getPorts().get(0).getContainerPort().intValue(), 9080, "Invalid container port");
    }

    @Test(enabled = false)
    public void validateShopGateway() {
        File gatewayYAML = new File(bookShopPkgTargetPath + File.separator + "book.shop_istio_gateway.yaml");
        Assert.assertTrue(gatewayYAML.exists(), "Cannot find istio gateway");
        // Validate gateway yaml
        
        Yaml yamlProcessor = new Yaml();
        Map<String, Object> gateway = (Map<String, Object>) yamlProcessor.load(FileUtils.readFileAsString(gatewayYAML));
        Assert.assertEquals(gateway.get("apiVersion"), "networking.istio.io/v1alpha3", "Invalid apiVersion");
        Assert.assertEquals(gateway.get("kind"), "Gateway", "Invalid kind.");
    
        Map<String, Object> metadata = (Map<String, Object>) gateway.get("metadata");
        Assert.assertEquals(metadata.get("name"), "bookshopep-istio-gw", "Invalid gateway name");
    
        Map<String, Object> spec = (Map<String, Object>) gateway.get("spec");
        Map<String, Object> selector = (Map<String, Object>) spec.get("selector");
        Assert.assertEquals(selector.get(KubernetesConstants.ISTIO_GATEWAY_SELECTOR), "ingressgateway",
                "Invalid selector.");
    
        List<Map<String, Object>> servers = (List<Map<String, Object>>) spec.get("servers");
        Map<String, Object> server = servers.get(0);
        Map<String, Object> port = (Map<String, Object>) server.get("port");
        Assert.assertEquals(port.get("number"), 80, "Invalid port number.");
        Assert.assertEquals(port.get("name"), "http", "Invalid port name.");
        Assert.assertEquals(port.get("protocol"), "HTTP", "Invalid port protocol.");
    
        List<String> hosts = (List<String>) server.get("hosts");
        Assert.assertTrue(hosts.contains("*"), "* host not included");
    }
    
    @Test(enabled = false)
    public void validateShopVirtualService() {
        File vsFile = new File(bookShopPkgTargetPath + File.separator + "book.shop_istio_virtual_service.yaml");
        Assert.assertTrue(vsFile.exists(), "Cannot find istio virtual service");
        Yaml yamlProcessor = new Yaml();
        Map<String, Object> virtualSvc = (Map<String, Object>) yamlProcessor.load(FileUtils.readFileAsString(vsFile));
        Assert.assertEquals(virtualSvc.get("apiVersion"), "networking.istio.io/v1alpha3", "Invalid apiVersion");
        Assert.assertEquals(virtualSvc.get("kind"), "VirtualService", "Invalid kind.");
    
        Map<String, Object> metadata = (Map<String, Object>) virtualSvc.get("metadata");
        Assert.assertEquals(metadata.get("name"), "bookshopep-istio-vs", "Invalid virtual service name");
    
        Map<String, Object> spec = (Map<String, Object>) virtualSvc.get("spec");
        List<String> hosts = (List<String>) spec.get("hosts");
        Assert.assertEquals(hosts.get(0), "*", "Invalid host value.");
    
        List<Map<String, Object>> http = (List<Map<String, Object>>) spec.get("http");
        Assert.assertEquals(http.size(), 1, "Invalid number of http items");
    
        Map<String, Object> httpMap = http.get(0);
    
        List<Map<String, Map<String, Object>>> route = (List<Map<String, Map<String, Object>>>) httpMap.get("route");
        Assert.assertEquals(route.get(0).get("destination").get("host"), "bookshopep-svc",
                "Invalid route destination host");
        Map<String, Integer> port = (Map<String, Integer>) route.get(0).get("destination").get("port");
        Assert.assertEquals(port.get("number").intValue(), 9080, "Invalid port");
    
    }

    @AfterClass
    public void cleanUp() throws KubernetesPluginException, DockerTestException, InterruptedException {
        KubernetesUtils.deleteDirectory(targetPath);
        KubernetesTestUtils.deleteDockerImage(bookReviewsDockerImage);
        KubernetesTestUtils.deleteDockerImage(bookDetailsDockerImage);
        KubernetesTestUtils.deleteDockerImage(bookShopDockerImage);
    }
}
