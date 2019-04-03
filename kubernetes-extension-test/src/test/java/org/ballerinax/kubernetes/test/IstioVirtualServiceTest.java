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

package org.ballerinax.kubernetes.test;

import me.snowdrop.istio.api.networking.v1alpha3.NumberPort;
import me.snowdrop.istio.api.networking.v1alpha3.VirtualService;
import org.apache.commons.io.FileUtils;
import org.ballerinax.kubernetes.exceptions.KubernetesPluginException;
import org.ballerinax.kubernetes.test.utils.DockerTestException;
import org.ballerinax.kubernetes.test.utils.KubernetesTestUtils;
import org.ballerinax.kubernetes.utils.KubernetesUtils;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import static org.ballerinax.kubernetes.KubernetesConstants.DOCKER;
import static org.ballerinax.kubernetes.KubernetesConstants.KUBERNETES;
import static org.ballerinax.kubernetes.test.utils.KubernetesTestUtils.getExposedPorts;

/**
 * Test cases for generating istio virtual service artifacts.
 *
 * @since 0.985.0
 */
public class IstioVirtualServiceTest {
    
    private static final Path BAL_DIRECTORY = Paths.get("src", "test", "resources", "istio", "virtual-service");
    private static final Path TARGET_PATH = BAL_DIRECTORY.resolve(KUBERNETES);
    private static final String DOCKER_IMAGE = "pizza-shop:latest";
    
    /**
     * Build bal file with istio virtual service annotation with http route.
     *
     * @throws IOException               Error when loading the generated yaml.
     * @throws InterruptedException      Error when compiling the ballerina file.
     * @throws KubernetesPluginException Error when deleting the generated artifacts folder.
     */
    @Test(groups = {"istio"}, enabled = false, description = "disabled as its not supported yet.")
    public void httpRouteTest() throws IOException, InterruptedException, KubernetesPluginException,
            DockerTestException {
        Assert.assertEquals(KubernetesTestUtils.compileBallerinaFile(BAL_DIRECTORY, "http_route.bal"), 0);
        
        // Check if docker image exists and correct
        validateDockerfile();
        validateDockerImage();
        
        // Validate virtual service yaml
        File vsFile = TARGET_PATH.resolve("http_route_istio_virtual_service.yaml").toFile();
        Assert.assertTrue(vsFile.exists());
        Yaml yamlProcessor = new Yaml();
        Map<String, Object> virtualSvc = (Map<String, Object>) yamlProcessor.load(FileUtils.readFileToString(vsFile));
        Assert.assertEquals(virtualSvc.get("apiVersion"), "networking.istio.io/v1alpha3", "Invalid apiVersion");
        Assert.assertEquals(virtualSvc.get("kind"), "VirtualService", "Invalid kind.");
        
        Map<String, Object> metadata = (Map<String, Object>) virtualSvc.get("metadata");
        Assert.assertEquals(metadata.get("name"), "my-gateway", "Invalid virtual service name");
        Assert.assertEquals(metadata.get("namespace"), "ballerina", "Invalid virtual service namespace");
        
        Map<String, String> labels = (Map<String, String>) metadata.get("labels");
        Assert.assertEquals(labels.get("label1"), "label1", "Invalid label");
        Assert.assertEquals(labels.get("label2"), "label2", "Invalid label");
        
        Map<String, String> annotations = (Map<String, String>) metadata.get("annotations");
        Assert.assertEquals(annotations.get("anno1"), "anno1Val", "Invalid annotation value");
        Assert.assertEquals(annotations.get("anno2"), "anno2Val", "Invalid annotation value");
        
        Map<String, Object> spec = (Map<String, Object>) virtualSvc.get("spec");
        List<String> hosts = (List<String>) spec.get("hosts");
        Assert.assertEquals(hosts.get(0), "reviews.prod.svc.cluster.local", "Invalid host value.");

        List<Map<String, Object>> http = (List<Map<String, Object>>) spec.get("http");
        Assert.assertEquals(http.size(), 2, "Invalid number of http items");
        
        Map<String, Object> http1 = http.get(0);
        List<Map<String, Map<String, String>>> match1 = (List<Map<String, Map<String, String>>>) http1.get("match");
        Assert.assertEquals(match1.get(0).get("uri").get("prefix"), "/wpcatalog", "Invalid match uri prefix");
        Assert.assertEquals(match1.get(1).get("uri").get("prefix"), "/consumercatalog", "Invalid match uri prefix");
    
        Map<String, String> rewrite = (Map<String, String>) http1.get("rewrite");
        Assert.assertEquals(rewrite.get("uri"), "/newcatalog", "Invalid rewrite uri");
    
        List<Map<String, Map<String, String>>> route1 = (List<Map<String, Map<String, String>>>) http1.get("route");
        Assert.assertEquals(route1.get(0).get("destination").get("host"), "reviews.prod.svc.cluster.local",
                "Invalid route destination host");
        Assert.assertEquals(route1.get(0).get("destination").get("subset"), "v2", "Invalid route destination subset");
    
        Map<String, Object> http2 = http.get(1);
        List<Map<String, Map<String, String>>> route2 = (List<Map<String, Map<String, String>>>) http2.get("route");
        Assert.assertEquals(route2.get(0).get("destination").get("host"), "reviews.prod.svc.cluster.local",
                "Invalid route destination host");
        Assert.assertEquals(route2.get(0).get("destination").get("subset"), "v1", "Invalid route destination subset");
        
        KubernetesUtils.deleteDirectory(TARGET_PATH);
        KubernetesTestUtils.deleteDockerImage(DOCKER_IMAGE);
    }
    
    /**
     * Build bal file with istio virtual service annotation with http match request.
     *
     * @throws IOException               Error when loading the generated yaml.
     * @throws InterruptedException      Error when compiling the ballerina file.
     * @throws KubernetesPluginException Error when deleting the generated artifacts folder.
     */
    @Test(groups = {"istio"}, enabled = false, description = "disabled as its not supported yet.")
    public void httpMatchRequestTest() throws IOException, InterruptedException, KubernetesPluginException,
            DockerTestException {
        Assert.assertEquals(KubernetesTestUtils.compileBallerinaFile(BAL_DIRECTORY, "http_match_request.bal"), 0);
        
        // Check if docker image exists and correct
        validateDockerfile();
        validateDockerImage();
        
        // Validate virtual service yaml
        File vsFile = TARGET_PATH.resolve("http_match_request_istio_virtual_service.yaml").toFile();
        Assert.assertTrue(vsFile.exists());
        Yaml yamlProcessor = new Yaml();
        Map<String, Object> virtualSvc = (Map<String, Object>) yamlProcessor.load(FileUtils.readFileToString(vsFile));
        Assert.assertEquals(virtualSvc.get("apiVersion"), "networking.istio.io/v1alpha3", "Invalid apiVersion");
        Assert.assertEquals(virtualSvc.get("kind"), "VirtualService", "Invalid kind.");
        
        Map<String, Object> metadata = (Map<String, Object>) virtualSvc.get("metadata");
        Assert.assertEquals(metadata.get("name"), "ratings-route", "Invalid virtual service name");
        
        Map<String, Object> spec = (Map<String, Object>) virtualSvc.get("spec");
        List<String> hosts = (List<String>) spec.get("hosts");
        Assert.assertEquals(hosts.get(0), "ratings.prod.svc.cluster.local", "Invalid host value.");
        
        List<Map<String, Object>> http = (List<Map<String, Object>>) spec.get("http");
        Assert.assertEquals(http.size(), 1, "Invalid number of http items");
        
        Map<String, Object> http1 = http.get(0);
        List<Map<String, Object>> match = (List<Map<String, Object>>) http1.get("match");
        Map<String, Map<String, String>> headers = (Map<String, Map<String, String>>) match.get(0).get("headers");
        Assert.assertEquals(headers.get("end-user").get("exact"), "jason", "Invalid match header end-user");
    
        Map<String, String> uri = (Map<String, String>) match.get(0).get("uri");
        Assert.assertEquals(uri.get("prefix"), "/ratings/v2/", "Invalid match uri prefix");
    
        List<Map<String, Map<String, String>>> route = (List<Map<String, Map<String, String>>>) http1.get("route");
        Assert.assertEquals(route.get(0).get("destination").get("host"), "ratings.prod.svc.cluster.local",
                "Invalid route destination host");
        
        KubernetesUtils.deleteDirectory(TARGET_PATH);
        KubernetesTestUtils.deleteDockerImage(DOCKER_IMAGE);
    }
    
    /**
     * Build bal file with istio virtual service annotation with destination weight.
     *
     * @throws IOException               Error when loading the generated yaml.
     * @throws InterruptedException      Error when compiling the ballerina file.
     * @throws KubernetesPluginException Error when deleting the generated artifacts folder.
     */
    @Test(groups = {"istio"})
    public void destinationWeightTest() throws IOException, InterruptedException, KubernetesPluginException,
            DockerTestException {
        Assert.assertEquals(KubernetesTestUtils.compileBallerinaFile(BAL_DIRECTORY, "destination_weight.bal"), 0);
        
        // Check if docker image exists and correct
        validateDockerfile();
        validateDockerImage();
        
        // Validate virtual service yaml
        File vsFile = TARGET_PATH.resolve("destination_weight_istio_virtual_service.yaml").toFile();
        Assert.assertTrue(vsFile.exists());
        VirtualService virtualService = KubernetesTestUtils.loadYaml(vsFile);
        
        Assert.assertNotNull(virtualService.getMetadata());
        Assert.assertEquals(virtualService.getMetadata().getName(), "reviews-route", "Invalid virtual service name");
    
        Assert.assertNotNull(virtualService.getSpec());
        Assert.assertEquals(virtualService.getSpec().getHosts().size(), 1);
        Assert.assertEquals(virtualService.getSpec().getHosts().get(0), "reviews.prod.svc.cluster.local",
                "Invalid host value.");
        
        Assert.assertEquals(virtualService.getSpec().getHttp().size(), 1, "Invalid number of http items");
        Assert.assertNotNull(virtualService.getSpec().getHttp().get(0).getRoute());
        Assert.assertEquals(virtualService.getSpec().getHttp().get(0).getRoute().size(), 2);
        Assert.assertEquals(virtualService.getSpec().getHttp().get(0).getRoute().get(0).getDestination().getHost(),
                "reviews.prod.svc.cluster.local", "Invalid route destination host");
        Assert.assertEquals(virtualService.getSpec().getHttp().get(0).getRoute().get(0).getDestination().getSubset(),
                "v2", "Invalid route destination subset");
        Assert.assertEquals(virtualService.getSpec().getHttp().get(0).getRoute().get(0).getWeight().intValue(), 25,
                "Invalid route weight");
    
        Assert.assertEquals(virtualService.getSpec().getHttp().get(0).getRoute().get(1).getDestination().getHost(),
                "reviews.prod.svc.cluster.local", "Invalid route destination host");
        Assert.assertEquals(virtualService.getSpec().getHttp().get(0).getRoute().get(1).getDestination().getSubset(),
                "v1", "Invalid route destination subset");
        Assert.assertEquals(virtualService.getSpec().getHttp().get(0).getRoute().get(1).getWeight().intValue(), 75,
                "Invalid route weight");
        
        KubernetesUtils.deleteDirectory(TARGET_PATH);
        KubernetesTestUtils.deleteDockerImage(DOCKER_IMAGE);
    }
    
    /**
     * Build bal file with istio virtual service annotation with destination timeout.
     *
     * @throws IOException               Error when loading the generated yaml.
     * @throws InterruptedException      Error when compiling the ballerina file.
     * @throws KubernetesPluginException Error when deleting the generated artifacts folder.
     */
    @Test(groups = {"istio"})
    public void destinationTest() throws IOException, InterruptedException, KubernetesPluginException,
            DockerTestException {
        Assert.assertEquals(KubernetesTestUtils.compileBallerinaFile(BAL_DIRECTORY, "destination.bal"), 0);
        
        // Check if docker image exists and correct
        validateDockerfile();
        validateDockerImage();
        
        // Validate virtual service yaml
        File vsFile = TARGET_PATH.resolve("destination_istio_virtual_service.yaml").toFile();
        Assert.assertTrue(vsFile.exists());
        VirtualService virtualService = KubernetesTestUtils.loadYaml(vsFile);
        
        Assert.assertNotNull(virtualService.getMetadata());
        Assert.assertEquals(virtualService.getMetadata().getName(), "reviews-route", "Invalid virtual service name");
        
        Assert.assertNotNull(virtualService.getSpec());
        Assert.assertEquals(virtualService.getSpec().getHosts().size(), 1);
        Assert.assertEquals(virtualService.getSpec().getHosts().get(0), "reviews.prod.svc.cluster.local",
                "Invalid host value.");
    
        Assert.assertEquals(virtualService.getSpec().getHttp().size(), 1, "Invalid number of http items");
        Assert.assertEquals(virtualService.getSpec().getHttp().get(0).getTimeout().getSeconds().longValue(), 5L,
                "Invalid timeout value");
    
        Assert.assertNotNull(virtualService.getSpec().getHttp().get(0).getRoute());
        Assert.assertEquals(virtualService.getSpec().getHttp().get(0).getRoute().size(), 1);
        Assert.assertEquals(virtualService.getSpec().getHttp().get(0).getRoute().get(0).getDestination().getHost(),
                "reviews.prod.svc.cluster.local", "Invalid route destination host");
        
        KubernetesUtils.deleteDirectory(TARGET_PATH);
        KubernetesTestUtils.deleteDockerImage(DOCKER_IMAGE);
    }
    
    /**
     * Build bal file with istio virtual service annotations with http redirect.
     *
     * @throws IOException               Error when loading the generated yaml.
     * @throws InterruptedException      Error when compiling the ballerina file.
     * @throws KubernetesPluginException Error when deleting the generated artifacts folder.
     */
    @Test(groups = {"istio"}, enabled = false, description = "disabled as its not supported yet.")
    public void httpRedirectTest() throws IOException, InterruptedException, KubernetesPluginException,
            DockerTestException {
        Assert.assertEquals(KubernetesTestUtils.compileBallerinaFile(BAL_DIRECTORY, "http_redirect.bal"), 0);
        
        // Check if docker image exists and correct
        validateDockerfile();
        validateDockerImage();
        
        // Validate virtual service yaml
        File vsFile = TARGET_PATH.resolve("http_redirect_istio_virtual_service.yaml").toFile();
        Assert.assertTrue(vsFile.exists());
        Yaml yamlProcessor = new Yaml();
        Map<String, Object> virtualSvc = (Map<String, Object>) yamlProcessor.load(FileUtils.readFileToString(vsFile));
        Assert.assertEquals(virtualSvc.get("apiVersion"), "networking.istio.io/v1alpha3", "Invalid apiVersion");
        Assert.assertEquals(virtualSvc.get("kind"), "VirtualService", "Invalid kind.");
        
        Map<String, Object> metadata = (Map<String, Object>) virtualSvc.get("metadata");
        Assert.assertEquals(metadata.get("name"), "ratings-route", "Invalid virtual service name");
        
        Map<String, Object> spec = (Map<String, Object>) virtualSvc.get("spec");
        List<String> hosts = (List<String>) spec.get("hosts");
        Assert.assertEquals(hosts.get(0), "ratings.prod.svc.cluster.local", "Invalid host value.");
        
        List<Map<String, Object>> https = (List<Map<String, Object>>) spec.get("http");
        Assert.assertEquals(https.size(), 1, "Invalid number of http items");
        
        Map<String, Object> http = https.get(0);
        List<Map<String, Map<String, String>>> match1 = (List<Map<String, Map<String, String>>>) http.get("match");
        Assert.assertEquals(match1.get(0).get("uri").get("exact"), "/v1/getProductRatings", "Invalid match uri prefix");
        
        Map<String, String> redirect = (Map<String, String>) http.get("redirect");
        Assert.assertEquals(redirect.get("uri"), "/v1/bookRatings", "Invalid redirect uri");
        Assert.assertEquals(redirect.get("authority"), "newratings.default.svc.cluster.local",
                "Invalid redirect authority");
        
        KubernetesUtils.deleteDirectory(TARGET_PATH);
        KubernetesTestUtils.deleteDockerImage(DOCKER_IMAGE);
    }
    
    /**
     * Build bal file with istio virtual service annotation with destination timeout.
     *
     * @throws IOException               Error when loading the generated yaml.
     * @throws InterruptedException      Error when compiling the ballerina file.
     * @throws KubernetesPluginException Error when deleting the generated artifacts folder.
     */
    @Test(groups = {"istio"}, enabled = false, description = "disabled as its not supported yet.")
    public void httpRetryTest() throws IOException, InterruptedException, KubernetesPluginException,
            DockerTestException {
        Assert.assertEquals(KubernetesTestUtils.compileBallerinaFile(BAL_DIRECTORY, "http_retry.bal"), 0);
        
        // Check if docker image exists and correct
        validateDockerfile();
        validateDockerImage();
        
        // Validate virtual service yaml
        File vsFile = TARGET_PATH.resolve("http_retry_istio_virtual_service.yaml").toFile();
        Assert.assertTrue(vsFile.exists());
        Yaml yamlProcessor = new Yaml();
        Map<String, Object> virtualSvc = (Map<String, Object>) yamlProcessor.load(FileUtils.readFileToString(vsFile));
        Assert.assertEquals(virtualSvc.get("apiVersion"), "networking.istio.io/v1alpha3", "Invalid apiVersion");
        Assert.assertEquals(virtualSvc.get("kind"), "VirtualService", "Invalid kind.");
        
        Map<String, Object> metadata = (Map<String, Object>) virtualSvc.get("metadata");
        Assert.assertEquals(metadata.get("name"), "ratings-route", "Invalid virtual service name");
        
        Map<String, Object> spec = (Map<String, Object>) virtualSvc.get("spec");
        List<String> hosts = (List<String>) spec.get("hosts");
        Assert.assertEquals(hosts.get(0), "ratings.prod.svc.cluster.local", "Invalid host value.");
        
        List<Map<String, Object>> http = (List<Map<String, Object>>) spec.get("http");
        Assert.assertEquals(http.size(), 1, "Invalid number of http items");
        
        List<Map<String, Object>> route = (List<Map<String, Object>>) http.get(0).get("route");
        Map<String, Object> destination1 = (Map<String, Object>) route.get(0).get("destination");
        Assert.assertEquals(destination1.get("host"), "ratings.prod.svc.cluster.local",
                "Invalid route destination host");
        Assert.assertEquals(destination1.get("subset"), "v1", "Invalid route destination subset");
    
        Map<String, Object> retries = (Map<String, Object>) http.get(0).get("retries");
        Assert.assertEquals(retries.get("attempts"), 3, "Invalid number of retry attempts");
        Assert.assertEquals(retries.get("perTryTimeout"), "2s", "Invalid number of retry timeout try");
        
        KubernetesUtils.deleteDirectory(TARGET_PATH);
        KubernetesTestUtils.deleteDockerImage(DOCKER_IMAGE);
    }
    
    /**
     * Build bal file with istio virtual service annotation with http fault injection.
     *
     * @throws IOException               Error when loading the generated yaml.
     * @throws InterruptedException      Error when compiling the ballerina file.
     * @throws KubernetesPluginException Error when deleting the generated artifacts folder.
     */
    @Test(groups = {"istio"}, enabled = false, description = "disabled as its not supported yet.")
    public void httpFaultInjectionTest() throws IOException, InterruptedException, KubernetesPluginException,
            DockerTestException {
        Assert.assertEquals(KubernetesTestUtils.compileBallerinaFile(BAL_DIRECTORY, "http_fault_injection.bal"), 0);
        
        // Check if docker image exists and correct
        validateDockerfile();
        validateDockerImage();
        
        // Validate virtual service yaml
        File vsFile = TARGET_PATH.resolve("http_fault_injection_istio_virtual_service.yaml").toFile();
        Assert.assertTrue(vsFile.exists());
        Yaml yamlProcessor = new Yaml();
        Map<String, Object> virtualSvc = (Map<String, Object>) yamlProcessor.load(FileUtils.readFileToString(vsFile));
        Assert.assertEquals(virtualSvc.get("apiVersion"), "networking.istio.io/v1alpha3", "Invalid apiVersion");
        Assert.assertEquals(virtualSvc.get("kind"), "VirtualService", "Invalid kind.");
        
        Map<String, Object> metadata = (Map<String, Object>) virtualSvc.get("metadata");
        Assert.assertEquals(metadata.get("name"), "ratings-route", "Invalid virtual service name");
        
        Map<String, Object> spec = (Map<String, Object>) virtualSvc.get("spec");
        List<String> hosts = (List<String>) spec.get("hosts");
        Assert.assertEquals(hosts.get(0), "ratings.prod.svc.cluster.local", "Invalid host value.");
        
        List<Map<String, Object>> http = (List<Map<String, Object>>) spec.get("http");
        Assert.assertEquals(http.size(), 1, "Invalid number of http items");
        
        List<Map<String, Object>> route = (List<Map<String, Object>>) http.get(0).get("route");
        Map<String, Object> destination1 = (Map<String, Object>) route.get(0).get("destination");
        Assert.assertEquals(destination1.get("host"), "ratings.prod.svc.cluster.local",
                "Invalid route destination host");
        Assert.assertEquals(destination1.get("subset"), "v1", "Invalid route destination subset");
        
        Map<String, Map<String, Object>> fault = (Map<String, Map<String, Object>>) http.get(0).get("fault");
        Assert.assertEquals(fault.get("abort").get("percent"), 10, "Invalid fault abort percent");
        Assert.assertEquals(fault.get("abort").get("httpStatus"), 400, "Invalid fault abort http status code");
        
        KubernetesUtils.deleteDirectory(TARGET_PATH);
        KubernetesTestUtils.deleteDockerImage(DOCKER_IMAGE);
    }
    
    /**
     * Build bal file with istio virtual service annotation with CORS policy.
     *
     * @throws IOException               Error when loading the generated yaml.
     * @throws InterruptedException      Error when compiling the ballerina file.
     * @throws KubernetesPluginException Error when deleting the generated artifacts folder.
     */
    @Test(groups = {"istio"}, enabled = false, description = "disabled as its not supported yet.")
    public void corsPolicyTest() throws IOException, InterruptedException, KubernetesPluginException,
            DockerTestException {
        Assert.assertEquals(KubernetesTestUtils.compileBallerinaFile(BAL_DIRECTORY, "cors_policy.bal"), 0);
        
        // Check if docker image exists and correct
        validateDockerfile();
        validateDockerImage();
        
        // Validate virtual service yaml
        File vsFile = TARGET_PATH.resolve("cors_policy_istio_virtual_service.yaml").toFile();
        Assert.assertTrue(vsFile.exists());
        Yaml yamlProcessor = new Yaml();
        Map<String, Object> virtualSvc = (Map<String, Object>) yamlProcessor.load(FileUtils.readFileToString(vsFile));
        Assert.assertEquals(virtualSvc.get("apiVersion"), "networking.istio.io/v1alpha3", "Invalid apiVersion");
        Assert.assertEquals(virtualSvc.get("kind"), "VirtualService", "Invalid kind.");
        
        Map<String, Object> metadata = (Map<String, Object>) virtualSvc.get("metadata");
        Assert.assertEquals(metadata.get("name"), "ratings-route", "Invalid virtual service name");
        
        Map<String, Object> spec = (Map<String, Object>) virtualSvc.get("spec");
        List<String> hosts = (List<String>) spec.get("hosts");
        Assert.assertEquals(hosts.get(0), "ratings.prod.svc.cluster.local", "Invalid host value.");
        
        List<Map<String, Object>> http = (List<Map<String, Object>>) spec.get("http");
        Assert.assertEquals(http.size(), 1, "Invalid number of http items");
        
        List<Map<String, Object>> route = (List<Map<String, Object>>) http.get(0).get("route");
        Map<String, Object> destination1 = (Map<String, Object>) route.get(0).get("destination");
        Assert.assertEquals(destination1.get("host"), "ratings.prod.svc.cluster.local",
                "Invalid route destination host");
        Assert.assertEquals(destination1.get("subset"), "v1", "Invalid route destination subset");
    
        Map<String, Object> corsPolicy = (Map<String, Object>) http.get(0).get("corsPolicy");
        Assert.assertEquals(((List<String>) corsPolicy.get("allowOrigin")).get(0), "example.com",
                "Invalid cors allowOrigin");
        Assert.assertEquals(((List<String>) corsPolicy.get("allowMethods")).get(0), "POST",
                "Invalid cors allowMethods");
        Assert.assertEquals(((List<String>) corsPolicy.get("allowMethods")).get(1), "GET", "Invalid cors allowMethods");
        Assert.assertEquals(corsPolicy.get("allowCredentials"), false, "Invalid cors allowCredentials");
        Assert.assertEquals(((List<String>) corsPolicy.get("allowHeaders")).get(0), "X-Foo-Bar",
                "Invalid cors allowHeaders");
        
        KubernetesUtils.deleteDirectory(TARGET_PATH);
        KubernetesTestUtils.deleteDockerImage(DOCKER_IMAGE);
    }
    
    /**
     * Build bal file with istio virtual service annotation with tls route.
     *
     * @throws IOException               Error when loading the generated yaml.
     * @throws InterruptedException      Error when compiling the ballerina file.
     * @throws KubernetesPluginException Error when deleting the generated artifacts folder.
     */
    @Test(groups = {"istio"}, enabled = false, description = "disabled as its not supported yet.")
    public void tlsRouteTest() throws IOException, InterruptedException, KubernetesPluginException,
            DockerTestException {
        Assert.assertEquals(KubernetesTestUtils.compileBallerinaFile(BAL_DIRECTORY, "tls_route.bal"), 0);
        
        // Check if docker image exists and correct
        validateDockerfile();
        validateDockerImage();
        
        // Validate virtual service yaml
        File vsFile = TARGET_PATH.resolve("tls_route_istio_virtual_service.yaml").toFile();
        Assert.assertTrue(vsFile.exists());
        Yaml yamlProcessor = new Yaml();
        Map<String, Object> virtualSvc = (Map<String, Object>) yamlProcessor.load(FileUtils.readFileToString(vsFile));
        Assert.assertEquals(virtualSvc.get("apiVersion"), "networking.istio.io/v1alpha3", "Invalid apiVersion");
        Assert.assertEquals(virtualSvc.get("kind"), "VirtualService", "Invalid kind.");
        
        Map<String, Object> metadata = (Map<String, Object>) virtualSvc.get("metadata");
        Assert.assertEquals(metadata.get("name"), "bookinfo-sni", "Invalid virtual service name");
        
        Map<String, Object> spec = (Map<String, Object>) virtualSvc.get("spec");
        List<String> hosts = (List<String>) spec.get("hosts");
        Assert.assertEquals(hosts.get(0), "*.bookinfo.com", "Invalid host value.");
        List<String> gateways = (List<String>) spec.get("gateways");
        Assert.assertEquals(gateways.get(0), "mygateway", "Invalid gateways value.");
        
        List<Map<String, Object>> tls = (List<Map<String, Object>>) spec.get("tls");
        Assert.assertEquals(tls.size(), 2, "Invalid number of tls items");
        
        Map<String, Object> tls1 = tls.get(0);
        List<Map<String, Object>> match1 = (List<Map<String, Object>>) tls1.get("match");
        Assert.assertEquals(match1.get(0).get("port"), 443, "Invalid match port");
        Assert.assertEquals(((List<String>) match1.get(0).get("sniHosts")).get(0), "login.bookinfo.com",
                "Invalid match sniHosts");
        
        List<Map<String, Map<String, String>>> route1 = (List<Map<String, Map<String, String>>>) tls1.get("route");
        Assert.assertEquals(route1.get(0).get("destination").get("host"), "login.prod.svc.cluster.local",
                "Invalid route destination host");
        
        Map<String, Object> tls2 = tls.get(1);
        List<Map<String, Object>> match2 = (List<Map<String, Object>>) tls2.get("match");
        Assert.assertEquals(match2.get(0).get("port"), 443, "Invalid match port");
        Assert.assertEquals(((List<String>) match2.get(0).get("sniHosts")).get(0), "reviews.bookinfo.com",
                "Invalid match sniHosts");
    
        List<Map<String, Map<String, String>>> route2 = (List<Map<String, Map<String, String>>>) tls2.get("route");
        Assert.assertEquals(route2.get(0).get("destination").get("host"), "reviews.prod.svc.cluster.local",
                "Invalid route destination host");
        
        KubernetesUtils.deleteDirectory(TARGET_PATH);
        KubernetesTestUtils.deleteDockerImage(DOCKER_IMAGE);
    }
    
    /**
     * Build bal file with istio virtual service annotation with tcp route.
     *
     * @throws IOException               Error when loading the generated yaml.
     * @throws InterruptedException      Error when compiling the ballerina file.
     * @throws KubernetesPluginException Error when deleting the generated artifacts folder.
     */
    @Test(groups = {"istio"}, enabled = false, description = "disabled as its not supported yet.")
    public void tcpRouteTest() throws IOException, InterruptedException, KubernetesPluginException,
            DockerTestException {
        Assert.assertEquals(KubernetesTestUtils.compileBallerinaFile(BAL_DIRECTORY, "tcp_route.bal"), 0);
        
        // Check if docker image exists and correct
        validateDockerfile();
        validateDockerImage();
        
        // Validate virtual service yaml
        File vsFile = TARGET_PATH.resolve("tcp_route_istio_virtual_service.yaml").toFile();
        Assert.assertTrue(vsFile.exists());
        Yaml yamlProcessor = new Yaml();
        Map<String, Object> virtualSvc = (Map<String, Object>) yamlProcessor.load(FileUtils.readFileToString(vsFile));
        Assert.assertEquals(virtualSvc.get("apiVersion"), "networking.istio.io/v1alpha3", "Invalid apiVersion");
        Assert.assertEquals(virtualSvc.get("kind"), "VirtualService", "Invalid kind.");
        
        Map<String, Object> metadata = (Map<String, Object>) virtualSvc.get("metadata");
        Assert.assertEquals(metadata.get("name"), "bookinfo-mongo", "Invalid virtual service name");
        
        Map<String, Object> spec = (Map<String, Object>) virtualSvc.get("spec");
        List<String> hosts = (List<String>) spec.get("hosts");
        Assert.assertEquals(hosts.get(0), "mongo.prod.svc.cluster.local", "Invalid host value.");
        
        List<Map<String, Object>> tcpList = (List<Map<String, Object>>) spec.get("tcp");
        Assert.assertEquals(tcpList.size(), 1, "Invalid number of tls items");
        
        Map<String, Object> tcp = tcpList.get(0);
        List<Map<String, Object>> match1 = (List<Map<String, Object>>) tcp.get("match");
        Assert.assertEquals(match1.get(0).get("port"), 27017, "Invalid match port");
        
        List<Map<String, Object>> route = (List<Map<String, Object>>) tcp.get("route");
        Map<String, Object> destination = (Map<String, Object>) route.get(0).get("destination");
        Assert.assertEquals(destination.get("host"), "mongo.backup.svc.cluster.local",
                "Invalid route destination host");
        Assert.assertEquals(((Map<String, Object>) destination.get("port")).get("number"), 5555,
                "Invalid destination port");
        
        KubernetesUtils.deleteDirectory(TARGET_PATH);
        KubernetesTestUtils.deleteDockerImage(DOCKER_IMAGE);
    }
    
    /**
     * Build bal file with istio virtual service annotations having no fields.
     *
     * @throws IOException               Error when loading the generated yaml.
     * @throws InterruptedException      Error when compiling the ballerina file.
     * @throws KubernetesPluginException Error when deleting the generated artifacts folder.
     */
    @Test(groups = {"istio"})
    public void emptyAnnotationTest() throws IOException, InterruptedException, KubernetesPluginException,
            DockerTestException {
        Assert.assertEquals(KubernetesTestUtils.compileBallerinaFile(BAL_DIRECTORY, "empty_annotation.bal"), 0);
        
        // Check if docker image exists and correct
        validateDockerfile();
        validateDockerImage();
        
        // Validate virtual service yaml
        File vsFile = TARGET_PATH.resolve("empty_annotation_istio_virtual_service.yaml").toFile();
        Assert.assertTrue(vsFile.exists());
        VirtualService virtualService = KubernetesTestUtils.loadYaml(vsFile);
    
        Assert.assertNotNull(virtualService.getMetadata());
        Assert.assertEquals(virtualService.getMetadata().getName(), "helloep-istio-vs", "Invalid virtual service name");
    
        Assert.assertNotNull(virtualService.getSpec());
        Assert.assertEquals(virtualService.getSpec().getHosts().size(), 1);
        Assert.assertEquals(virtualService.getSpec().getHosts().get(0), "*", "Invalid host value.");
    
        Assert.assertEquals(virtualService.getSpec().getHttp().size(), 1, "Invalid number of http items");
        Assert.assertNotNull(virtualService.getSpec().getHttp().get(0).getRoute());
        Assert.assertEquals(virtualService.getSpec().getHttp().get(0).getRoute().size(), 1);
        Assert.assertEquals(virtualService.getSpec().getHttp().get(0).getRoute().get(0).getDestination().getHost(),
                "hello", "Invalid route destination host");
        Assert.assertTrue(virtualService.getSpec().getHttp().get(0).getRoute().get(0).getDestination().getPort()
                .getPort() instanceof NumberPort);
        NumberPort numberPort =
                (NumberPort) virtualService.getSpec().getHttp().get(0).getRoute().get(0).getDestination().getPort()
                        .getPort();
        Assert.assertEquals(numberPort.getNumber().intValue(), 9090, "Invalid port found");
    
        KubernetesUtils.deleteDirectory(TARGET_PATH);
        KubernetesTestUtils.deleteDockerImage(DOCKER_IMAGE);
    }
    
    /**
     * Build bal file with istio virtual service annotations. Check if service annotation port is used.
     *
     * @throws IOException               Error when loading the generated yaml.
     * @throws InterruptedException      Error when compiling the ballerina file.
     * @throws KubernetesPluginException Error when deleting the generated artifacts folder.
     */
    @Test(groups = {"istio"})
    public void useServiceAnnotationPortTest() throws IOException, InterruptedException, KubernetesPluginException,
            DockerTestException {
        Assert.assertEquals(KubernetesTestUtils.compileBallerinaFile(BAL_DIRECTORY, "svc_port.bal"), 0);
        
        // Check if docker image exists and correct
        validateDockerfile();
        validateDockerImage();
        
        // Validate virtual service yaml
        File vsFile = TARGET_PATH.resolve("svc_port_istio_virtual_service.yaml").toFile();
        Assert.assertTrue(vsFile.exists());
        VirtualService virtualService = KubernetesTestUtils.loadYaml(vsFile);
    
        Assert.assertNotNull(virtualService.getMetadata());
        Assert.assertEquals(virtualService.getMetadata().getName(), "helloep-istio-vs", "Invalid virtual service name");
    
        Assert.assertNotNull(virtualService.getSpec());
        Assert.assertEquals(virtualService.getSpec().getHosts().size(), 1);
        Assert.assertEquals(virtualService.getSpec().getHosts().get(0), "*", "Invalid host value.");
    
        Assert.assertEquals(virtualService.getSpec().getHttp().size(), 1, "Invalid number of http items");
        Assert.assertNotNull(virtualService.getSpec().getHttp().get(0).getRoute());
        Assert.assertEquals(virtualService.getSpec().getHttp().get(0).getRoute().size(), 1);
        Assert.assertEquals(virtualService.getSpec().getHttp().get(0).getRoute().get(0).getDestination().getHost(),
                "hello", "Invalid route destination host");
        Assert.assertTrue(virtualService.getSpec().getHttp().get(0).getRoute().get(0).getDestination().getPort()
                .getPort() instanceof NumberPort);
        NumberPort numberPort =
                (NumberPort) virtualService.getSpec().getHttp().get(0).getRoute().get(0).getDestination().getPort()
                        .getPort();
        Assert.assertEquals(numberPort.getNumber().intValue(), 8080, "Invalid port found");
        
        KubernetesUtils.deleteDirectory(TARGET_PATH);
        KubernetesTestUtils.deleteDockerImage(DOCKER_IMAGE);
    }
    
    /**
     * Validate if Dockerfile is created.
     */
    public void validateDockerfile() {
        File dockerFile = TARGET_PATH.resolve(DOCKER).resolve("Dockerfile").toFile();
        Assert.assertTrue(dockerFile.exists());
    }
    
    /**
     * Validate contents of the Dockerfile.
     */
    public void validateDockerImage() throws DockerTestException, InterruptedException {
        List<String> ports = getExposedPorts(DOCKER_IMAGE);
        Assert.assertEquals(ports.size(), 1);
        Assert.assertEquals(ports.get(0), "9090/tcp");
    }
}
