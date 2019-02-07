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

import org.ballerinax.kubernetes.exceptions.KubernetesPluginException;
import org.ballerinax.kubernetes.test.utils.DockerTestException;
import org.ballerinax.kubernetes.test.utils.KubernetesTestUtils;
import org.ballerinax.kubernetes.utils.KubernetesUtils;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.andes.util.FileUtils;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
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
    
    private final String balDirectory = Paths.get("src").resolve("test").resolve("resources").resolve("istio")
            .resolve("virtual-service").toAbsolutePath().toString();
    private final String targetPath = Paths.get(balDirectory).resolve(KUBERNETES).toString();
    private final String dockerImage = "pizza-shop:latest";
    
    /**
     * Build bal file with istio virtual service annotation with http route.
     *
     * @throws IOException               Error when loading the generated yaml.
     * @throws InterruptedException      Error when compiling the ballerina file.
     * @throws KubernetesPluginException Error when deleting the generated artifacts folder.
     */
    @Test(groups = {"istio"})
    public void httpRouteTest() throws IOException, InterruptedException, KubernetesPluginException,
            DockerTestException {
        Assert.assertEquals(KubernetesTestUtils.compileBallerinaFile(balDirectory, "http_route.bal"), 0);
        
        // Check if docker image exists and correct
        validateDockerfile();
        validateDockerImage();
        
        // Validate virtual service yaml
        File vsFile = Paths.get(targetPath).resolve("http_route_istio_virtual_service.yaml").toFile();
        Assert.assertTrue(vsFile.exists());
        Yaml yamlProcessor = new Yaml();
        Map<String, Object> virtualSvc = (Map<String, Object>) yamlProcessor.load(FileUtils.readFileAsString(vsFile));
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
        
        KubernetesUtils.deleteDirectory(targetPath);
        KubernetesTestUtils.deleteDockerImage(dockerImage);
    }
    
    /**
     * Build bal file with istio virtual service annotation with http match request.
     *
     * @throws IOException               Error when loading the generated yaml.
     * @throws InterruptedException      Error when compiling the ballerina file.
     * @throws KubernetesPluginException Error when deleting the generated artifacts folder.
     */
    @Test(groups = {"istio"})
    public void httpMatchRequestTest() throws IOException, InterruptedException, KubernetesPluginException,
            DockerTestException {
        Assert.assertEquals(KubernetesTestUtils.compileBallerinaFile(balDirectory, "http_match_request.bal"), 0);
        
        // Check if docker image exists and correct
        validateDockerfile();
        validateDockerImage();
        
        // Validate virtual service yaml
        File vsFile = Paths.get(targetPath).resolve("http_match_request_istio_virtual_service.yaml").toFile();
        Assert.assertTrue(vsFile.exists());
        Yaml yamlProcessor = new Yaml();
        Map<String, Object> virtualSvc = (Map<String, Object>) yamlProcessor.load(FileUtils.readFileAsString(vsFile));
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
        
        KubernetesUtils.deleteDirectory(targetPath);
        KubernetesTestUtils.deleteDockerImage(dockerImage);
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
        Assert.assertEquals(KubernetesTestUtils.compileBallerinaFile(balDirectory, "destination_weight.bal"), 0);
        
        // Check if docker image exists and correct
        validateDockerfile();
        validateDockerImage();
        
        // Validate virtual service yaml
        File vsFile = Paths.get(targetPath).resolve("destination_weight_istio_virtual_service.yaml").toFile();
        Assert.assertTrue(vsFile.exists());
        Yaml yamlProcessor = new Yaml();
        Map<String, Object> virtualSvc = (Map<String, Object>) yamlProcessor.load(FileUtils.readFileAsString(vsFile));
        Assert.assertEquals(virtualSvc.get("apiVersion"), "networking.istio.io/v1alpha3", "Invalid apiVersion");
        Assert.assertEquals(virtualSvc.get("kind"), "VirtualService", "Invalid kind.");
        
        Map<String, Object> metadata = (Map<String, Object>) virtualSvc.get("metadata");
        Assert.assertEquals(metadata.get("name"), "reviews-route", "Invalid virtual service name");
        
        Map<String, Object> spec = (Map<String, Object>) virtualSvc.get("spec");
        List<String> hosts = (List<String>) spec.get("hosts");
        Assert.assertEquals(hosts.get(0), "reviews.prod.svc.cluster.local", "Invalid host value.");
    
        List<Map<String, Object>> http = (List<Map<String, Object>>) spec.get("http");
        Assert.assertEquals(http.size(), 1, "Invalid number of http items");
    
        List<Map<String, Object>> route = (List<Map<String, Object>>) http.get(0).get("route");
        Map<String, Object> destination1 = (Map<String, Object>) route.get(0).get("destination");
        Assert.assertEquals(destination1.get("host"), "reviews.prod.svc.cluster.local",
                "Invalid route destination host");
        Assert.assertEquals(destination1.get("subset"), "v2", "Invalid route destination subset");
        Assert.assertEquals(route.get(0).get("weight"), 25, "Invalid route weight");
    
        Map<String, Object> destination2 = (Map<String, Object>) route.get(1).get("destination");
        Assert.assertEquals(destination2.get("host"), "reviews.prod.svc.cluster.local",
                "Invalid route destination host");
        Assert.assertEquals(destination2.get("subset"), "v1", "Invalid route destination subset");
        Assert.assertEquals(route.get(1).get("weight"), 75, "Invalid route weight");
        
        KubernetesUtils.deleteDirectory(targetPath);
        KubernetesTestUtils.deleteDockerImage(dockerImage);
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
        Assert.assertEquals(KubernetesTestUtils.compileBallerinaFile(balDirectory, "destination.bal"), 0);
        
        // Check if docker image exists and correct
        validateDockerfile();
        validateDockerImage();
        
        // Validate virtual service yaml
        File vsFile = Paths.get(targetPath).resolve("destination_istio_virtual_service.yaml").toFile();
        Assert.assertTrue(vsFile.exists());
        Yaml yamlProcessor = new Yaml();
        Map<String, Object> virtualSvc = (Map<String, Object>) yamlProcessor.load(FileUtils.readFileAsString(vsFile));
        Assert.assertEquals(virtualSvc.get("apiVersion"), "networking.istio.io/v1alpha3", "Invalid apiVersion");
        Assert.assertEquals(virtualSvc.get("kind"), "VirtualService", "Invalid kind.");
        
        Map<String, Object> metadata = (Map<String, Object>) virtualSvc.get("metadata");
        Assert.assertEquals(metadata.get("name"), "reviews-route", "Invalid virtual service name");
        
        Map<String, Object> spec = (Map<String, Object>) virtualSvc.get("spec");
        List<String> hosts = (List<String>) spec.get("hosts");
        Assert.assertEquals(hosts.get(0), "reviews.prod.svc.cluster.local", "Invalid host value.");
        
        List<Map<String, Object>> http = (List<Map<String, Object>>) spec.get("http");
        Assert.assertEquals(http.size(), 1, "Invalid number of http items");
    
        Assert.assertEquals(http.get(0).get("timeout"), "5s", "Invalid timeout value");
        
        List<Map<String, Object>> route = (List<Map<String, Object>>) http.get(0).get("route");
        Map<String, Object> destination1 = (Map<String, Object>) route.get(0).get("destination");
        Assert.assertEquals(destination1.get("host"), "reviews.prod.svc.cluster.local",
                "Invalid route destination host");
        
        KubernetesUtils.deleteDirectory(targetPath);
        KubernetesTestUtils.deleteDockerImage(dockerImage);
    }
    
    /**
     * Build bal file with istio virtual service annotations with http redirect.
     *
     * @throws IOException               Error when loading the generated yaml.
     * @throws InterruptedException      Error when compiling the ballerina file.
     * @throws KubernetesPluginException Error when deleting the generated artifacts folder.
     */
    @Test(groups = {"istio"})
    public void httpRedirectTest() throws IOException, InterruptedException, KubernetesPluginException,
            DockerTestException {
        Assert.assertEquals(KubernetesTestUtils.compileBallerinaFile(balDirectory, "http_redirect.bal"), 0);
        
        // Check if docker image exists and correct
        validateDockerfile();
        validateDockerImage();
        
        // Validate virtual service yaml
        File vsFile = Paths.get(targetPath).resolve("http_redirect_istio_virtual_service.yaml").toFile();
        Assert.assertTrue(vsFile.exists());
        Yaml yamlProcessor = new Yaml();
        Map<String, Object> virtualSvc = (Map<String, Object>) yamlProcessor.load(FileUtils.readFileAsString(vsFile));
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
        
        KubernetesUtils.deleteDirectory(targetPath);
        KubernetesTestUtils.deleteDockerImage(dockerImage);
    }
    
    /**
     * Build bal file with istio virtual service annotation with destination timeout.
     *
     * @throws IOException               Error when loading the generated yaml.
     * @throws InterruptedException      Error when compiling the ballerina file.
     * @throws KubernetesPluginException Error when deleting the generated artifacts folder.
     */
    @Test(groups = {"istio"})
    public void httpRetryTest() throws IOException, InterruptedException, KubernetesPluginException,
            DockerTestException {
        Assert.assertEquals(KubernetesTestUtils.compileBallerinaFile(balDirectory, "http_retry.bal"), 0);
        
        // Check if docker image exists and correct
        validateDockerfile();
        validateDockerImage();
        
        // Validate virtual service yaml
        File vsFile = Paths.get(targetPath).resolve("http_retry_istio_virtual_service.yaml").toFile();
        Assert.assertTrue(vsFile.exists());
        Yaml yamlProcessor = new Yaml();
        Map<String, Object> virtualSvc = (Map<String, Object>) yamlProcessor.load(FileUtils.readFileAsString(vsFile));
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
        
        KubernetesUtils.deleteDirectory(targetPath);
        KubernetesTestUtils.deleteDockerImage(dockerImage);
    }
    
    /**
     * Build bal file with istio virtual service annotation with http fault injection.
     *
     * @throws IOException               Error when loading the generated yaml.
     * @throws InterruptedException      Error when compiling the ballerina file.
     * @throws KubernetesPluginException Error when deleting the generated artifacts folder.
     */
    @Test(groups = {"istio"})
    public void httpFaultInjectionTest() throws IOException, InterruptedException, KubernetesPluginException,
            DockerTestException {
        Assert.assertEquals(KubernetesTestUtils.compileBallerinaFile(balDirectory, "http_fault_injection.bal"), 0);
        
        // Check if docker image exists and correct
        validateDockerfile();
        validateDockerImage();
        
        // Validate virtual service yaml
        File vsFile = Paths.get(targetPath).resolve("http_fault_injection_istio_virtual_service.yaml").toFile();
        Assert.assertTrue(vsFile.exists());
        Yaml yamlProcessor = new Yaml();
        Map<String, Object> virtualSvc = (Map<String, Object>) yamlProcessor.load(FileUtils.readFileAsString(vsFile));
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
        
        KubernetesUtils.deleteDirectory(targetPath);
        KubernetesTestUtils.deleteDockerImage(dockerImage);
    }
    
    /**
     * Build bal file with istio virtual service annotation with CORS policy.
     *
     * @throws IOException               Error when loading the generated yaml.
     * @throws InterruptedException      Error when compiling the ballerina file.
     * @throws KubernetesPluginException Error when deleting the generated artifacts folder.
     */
    @Test(groups = {"istio"})
    public void corsPolicyTest() throws IOException, InterruptedException, KubernetesPluginException,
            DockerTestException {
        Assert.assertEquals(KubernetesTestUtils.compileBallerinaFile(balDirectory, "cors_policy.bal"), 0);
        
        // Check if docker image exists and correct
        validateDockerfile();
        validateDockerImage();
        
        // Validate virtual service yaml
        File vsFile = Paths.get(targetPath).resolve("cors_policy_istio_virtual_service.yaml").toFile();
        Assert.assertTrue(vsFile.exists());
        Yaml yamlProcessor = new Yaml();
        Map<String, Object> virtualSvc = (Map<String, Object>) yamlProcessor.load(FileUtils.readFileAsString(vsFile));
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
        
        KubernetesUtils.deleteDirectory(targetPath);
        KubernetesTestUtils.deleteDockerImage(dockerImage);
    }
    
    /**
     * Build bal file with istio virtual service annotation with tls route.
     *
     * @throws IOException               Error when loading the generated yaml.
     * @throws InterruptedException      Error when compiling the ballerina file.
     * @throws KubernetesPluginException Error when deleting the generated artifacts folder.
     */
    @Test(groups = {"istio"})
    public void tlsRouteTest() throws IOException, InterruptedException, KubernetesPluginException,
            DockerTestException {
        Assert.assertEquals(KubernetesTestUtils.compileBallerinaFile(balDirectory, "tls_route.bal"), 0);
        
        // Check if docker image exists and correct
        validateDockerfile();
        validateDockerImage();
        
        // Validate virtual service yaml
        File vsFile = Paths.get(targetPath).resolve("tls_route_istio_virtual_service.yaml").toFile();
        Assert.assertTrue(vsFile.exists());
        Yaml yamlProcessor = new Yaml();
        Map<String, Object> virtualSvc = (Map<String, Object>) yamlProcessor.load(FileUtils.readFileAsString(vsFile));
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
        
        KubernetesUtils.deleteDirectory(targetPath);
        KubernetesTestUtils.deleteDockerImage(dockerImage);
    }
    
    /**
     * Build bal file with istio virtual service annotation with tcp route.
     *
     * @throws IOException               Error when loading the generated yaml.
     * @throws InterruptedException      Error when compiling the ballerina file.
     * @throws KubernetesPluginException Error when deleting the generated artifacts folder.
     */
    @Test(groups = {"istio"})
    public void tcpRouteTest() throws IOException, InterruptedException, KubernetesPluginException,
            DockerTestException {
        Assert.assertEquals(KubernetesTestUtils.compileBallerinaFile(balDirectory, "tcp_route.bal"), 0);
        
        // Check if docker image exists and correct
        validateDockerfile();
        validateDockerImage();
        
        // Validate virtual service yaml
        File vsFile = Paths.get(targetPath).resolve("tcp_route_istio_virtual_service.yaml").toFile();
        Assert.assertTrue(vsFile.exists());
        Yaml yamlProcessor = new Yaml();
        Map<String, Object> virtualSvc = (Map<String, Object>) yamlProcessor.load(FileUtils.readFileAsString(vsFile));
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
        
        KubernetesUtils.deleteDirectory(targetPath);
        KubernetesTestUtils.deleteDockerImage(dockerImage);
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
        Assert.assertEquals(KubernetesTestUtils.compileBallerinaFile(balDirectory, "empty_annotation.bal"), 0);
        
        // Check if docker image exists and correct
        validateDockerfile();
        validateDockerImage();
        
        // Validate virtual service yaml
        File vsFile = Paths.get(targetPath).resolve("empty_annotation_istio_virtual_service.yaml").toFile();
        Assert.assertTrue(vsFile.exists());
        Yaml yamlProcessor = new Yaml();
        Map<String, Object> virtualSvc = (Map<String, Object>) yamlProcessor.load(FileUtils.readFileAsString(vsFile));
        Assert.assertEquals(virtualSvc.get("apiVersion"), "networking.istio.io/v1alpha3", "Invalid apiVersion");
        Assert.assertEquals(virtualSvc.get("kind"), "VirtualService", "Invalid kind.");
        
        Map<String, Object> metadata = (Map<String, Object>) virtualSvc.get("metadata");
        Assert.assertEquals(metadata.get("name"), "helloep-istio-vs", "Invalid virtual service name");
        
        Map<String, Object> spec = (Map<String, Object>) virtualSvc.get("spec");
        List<String> hosts = (List<String>) spec.get("hosts");
        Assert.assertEquals(hosts.get(0), "*", "Invalid host value.");
        
        List<Map<String, Object>> https = (List<Map<String, Object>>) spec.get("http");
        Assert.assertEquals(https.size(), 1, "Invalid number of http items");
        
        Map<String, Object> http = https.get(0);
    
        List<Map<String, Object>> route1 = (List<Map<String, Object>>) http.get("route");
        Map<String, Object> destination = (Map<String, Object>) route1.get(0).get("destination");
        Assert.assertEquals(destination.get("host"), "hello",
                "Invalid route destination host");
        Map<String, Object> port = (Map<String, Object>) destination.get("port");
        Assert.assertEquals(port.get("number"), 9090, "Invalid port found");
    
        KubernetesUtils.deleteDirectory(targetPath);
        KubernetesTestUtils.deleteDockerImage(dockerImage);
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
        Assert.assertEquals(KubernetesTestUtils.compileBallerinaFile(balDirectory, "svc_port.bal"), 0);
        
        // Check if docker image exists and correct
        validateDockerfile();
        validateDockerImage();
        
        // Validate virtual service yaml
        File gatewayFile = Paths.get(targetPath).resolve("svc_port_istio_virtual_service.yaml").toFile();
        Assert.assertTrue(gatewayFile.exists());
        Yaml yamlProcessor = new Yaml();
        Map<String, Object> gateway = (Map<String, Object>) yamlProcessor.load(FileUtils.readFileAsString(gatewayFile));
        Assert.assertEquals(gateway.get("apiVersion"), "networking.istio.io/v1alpha3", "Invalid apiVersion");
        Assert.assertEquals(gateway.get("kind"), "VirtualService", "Invalid kind.");
        
        Map<String, Object> metadata = (Map<String, Object>) gateway.get("metadata");
        Assert.assertEquals(metadata.get("name"), "helloep-istio-vs", "Invalid virtual service name");
        
        Map<String, Object> spec = (Map<String, Object>) gateway.get("spec");
        List<String> hosts = (List<String>) spec.get("hosts");
        Assert.assertEquals(hosts.get(0), "*", "Invalid host value.");
        
        List<Map<String, Object>> https = (List<Map<String, Object>>) spec.get("http");
        Assert.assertEquals(https.size(), 1, "Invalid number of http items");
        
        Map<String, Object> http = https.get(0);
        
        List<Map<String, Object>> route1 = (List<Map<String, Object>>) http.get("route");
        Map<String, Object> destination = (Map<String, Object>) route1.get(0).get("destination");
        Assert.assertEquals(destination.get("host"), "hello",
                "Invalid route destination host");
        Map<String, Object> port = (Map<String, Object>) destination.get("port");
        Assert.assertEquals(port.get("number"), 8080, "Invalid port found");
        
        KubernetesUtils.deleteDirectory(targetPath);
        KubernetesTestUtils.deleteDockerImage(dockerImage);
    }
    
    /**
     * Validate if Dockerfile is created.
     */
    public void validateDockerfile() {
        File dockerFile = new File(targetPath + File.separator + DOCKER + File.separator + "Dockerfile");
        Assert.assertTrue(dockerFile.exists());
    }
    
    /**
     * Validate contents of the Dockerfile.
     */
    public void validateDockerImage() throws DockerTestException, InterruptedException {
        List<String> ports = getExposedPorts(this.dockerImage);
        Assert.assertEquals(ports.size(), 1);
        Assert.assertEquals(ports.get(0), "9090/tcp");
    }
}
