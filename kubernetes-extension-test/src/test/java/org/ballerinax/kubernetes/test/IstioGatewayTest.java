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

import io.fabric8.docker.api.model.ImageInspect;
import org.ballerinax.kubernetes.exceptions.KubernetesPluginException;
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
import static org.ballerinax.kubernetes.test.utils.KubernetesTestUtils.getDockerImage;

/**
 * Test cases for generating istio gateway artifacts.
 */
public class IstioGatewayTest {
    
    private final String balDirectory = Paths.get("src").resolve("test").resolve("resources").resolve("istio")
            .resolve("gateway").toAbsolutePath().toString();
    private final String targetPath = Paths.get(balDirectory).resolve(KUBERNETES).toString();
    private final String dockerImage = "pizza-shop:latest";
    
    /**
     * Build bal file with istio gateway annotations.
     * @throws IOException Error when loading the generated yaml.
     * @throws InterruptedException Error when compiling the ballerina file.
     * @throws KubernetesPluginException Error when deleting the generated artifacts folder.
     */
    @Test
    public void allFieldsTest() throws IOException, InterruptedException, KubernetesPluginException {
        Assert.assertEquals(KubernetesTestUtils.compileBallerinaFile(balDirectory, "all_fields.bal"), 0);
        
        // Check if docker image exists and correct
        validateDockerfile();
        validateDockerImage();
        
        // Validate deployment yaml
        File gatewayFile = Paths.get(targetPath).resolve("all_fields_istio_gateway.yaml").toFile();
        Assert.assertTrue(gatewayFile.exists());
        Yaml yamlProcessor = new Yaml();
        Map<String, Object> gateway = (Map<String, Object>) yamlProcessor.load(FileUtils.readFileAsString(gatewayFile));
        Assert.assertEquals(gateway.get("apiVersion"), "networking.istio.io/v1alpha3", "Invalid apiVersion");
        Assert.assertEquals(gateway.get("kind"), "Gateway", "Invalid kind.");
    
        Map<String, Object> metadata = (Map<String, Object>) gateway.get("metadata");
        Assert.assertEquals(metadata.get("name"), "my-gateway", "Invalid gateway name");
        Assert.assertEquals(metadata.get("namespace"), "ballerina", "Invalid gateway name");
    
        Map<String, String> labels = (Map<String, String>) metadata.get("labels");
        Assert.assertEquals(labels.get("label1"), "label1", "Invalid label");
        Assert.assertEquals(labels.get("label2"), "label2", "Invalid label");
    
        Map<String, String> annotations = (Map<String, String>) metadata.get("annotations");
        Assert.assertEquals(annotations.get("anno1"), "anno1Val", "Invalid annotation value");
        Assert.assertEquals(annotations.get("anno2"), "anno2Val", "Invalid annotation value");
    
        Map<String, Object> spec = (Map<String, Object>) gateway.get("spec");
        Map<String, Object> selector = (Map<String, Object>) spec.get("selector");
        Assert.assertEquals(selector.get("app"), "my-gateway-controller", "Invalid selector.");
    
        List<Map<String, Object>> servers = (List<Map<String, Object>>) spec.get("servers");
        Map<String, Object> server = servers.get(0);
        Map<String, Object> port = (Map<String, Object>) server.get("port");
        Assert.assertEquals(port.get("number"), 80, "Invalid port number.");
        Assert.assertEquals(port.get("name"), "http", "Invalid port name.");
        Assert.assertEquals(port.get("protocol"), "HTTP", "Invalid port protocol.");
    
        List<String> hosts = (List<String>) server.get("hosts");
        Assert.assertTrue(hosts.contains("uk.bookinfo.com"), "uk.bookinfo.com host not included");
        Assert.assertTrue(hosts.contains("eu.bookinfo.com"), "eu.bookinfo.com host not included");
    
        Map<String, Object> tls = (Map<String, Object>) server.get("tls");
        Assert.assertEquals(tls.get("httpsRedirect"), true, "Invalid tls httpsRedirect value");
        
        KubernetesUtils.deleteDirectory(targetPath);
        KubernetesTestUtils.deleteDockerImage(dockerImage);
    }
    
    /**
     * Build bal file with invalid port istio gateway annotations.
     * @throws IOException Error when loading the generated yaml.
     * @throws InterruptedException Error when compiling the ballerina file.
     */
    @Test()
    public void invalidPortTest() throws IOException, InterruptedException {
        Assert.assertEquals(KubernetesTestUtils.compileBallerinaFile(balDirectory, "invalid_port.bal"), 1);
    }
    
    /**
     * Build bal file with istio gateway annotation having multiple servers.
     * @throws IOException Error when loading the generated yaml.
     * @throws InterruptedException Error when compiling the ballerina file.
     * @throws KubernetesPluginException Error when deleting the generated artifacts folder.
     */
    @Test
    public void multipleServersTest() throws IOException, InterruptedException, KubernetesPluginException {
        Assert.assertEquals(KubernetesTestUtils.compileBallerinaFile(balDirectory, "multiple_servers.bal"), 0);
        
        // Check if docker image exists and correct
        validateDockerfile();
        validateDockerImage();
        
        // Validate deployment yaml
        File gatewayFile = Paths.get(targetPath).resolve("multiple_servers_istio_gateway.yaml").toFile();
        Assert.assertTrue(gatewayFile.exists());
        Yaml yamlProcessor = new Yaml();
        Map<String, Object> gateway = (Map<String, Object>) yamlProcessor.load(FileUtils.readFileAsString(gatewayFile));
        Assert.assertEquals(gateway.get("apiVersion"), "networking.istio.io/v1alpha3", "Invalid apiVersion");
        Assert.assertEquals(gateway.get("kind"), "Gateway", "Invalid kind.");
        
        Map<String, Object> metadata = (Map<String, Object>) gateway.get("metadata");
        Assert.assertEquals(metadata.get("name"), "my-gateway", "Invalid gateway name");
        
        Map<String, Object> spec = (Map<String, Object>) gateway.get("spec");
        Map<String, Object> selector = (Map<String, Object>) spec.get("selector");
        Assert.assertEquals(selector.get("app"), "my-gateway-controller", "Invalid selector.");
        
        List<Map<String, Object>> servers = (List<Map<String, Object>>) spec.get("servers");
        Map<String, Object> server1 = servers.get(0);
        Map<String, Object> port1 = (Map<String, Object>) server1.get("port");
        Assert.assertEquals(port1.get("number"), 80, "Invalid port number.");
        Assert.assertEquals(port1.get("name"), "http", "Invalid port name.");
        Assert.assertEquals(port1.get("protocol"), "HTTP", "Invalid port protocol.");
        
        List<String> hosts1 = (List<String>) server1.get("hosts");
        Assert.assertTrue(hosts1.contains("uk.bookinfo.com"), "uk.bookinfo.com host not included");
        Assert.assertTrue(hosts1.contains("eu.bookinfo.com"), "eu.bookinfo.com host not included");
        
        Map<String, Object> tls1 = (Map<String, Object>) server1.get("tls");
        Assert.assertEquals(tls1.get("httpsRedirect"), true, "Invalid tls httpsRedirect value");
    
        Map<String, Object> server2 = servers.get(1);
        Map<String, Object> port2 = (Map<String, Object>) server2.get("port");
        Assert.assertEquals(port2.get("number"), 443, "Invalid port number.");
        Assert.assertEquals(port2.get("name"), "https", "Invalid port name.");
        Assert.assertEquals(port2.get("protocol"), "HTTPS", "Invalid port protocol.");
    
        List<String> hosts2 = (List<String>) server2.get("hosts");
        Assert.assertTrue(hosts2.contains("uk.bookinfo.com"), "uk.bookinfo.com host not included");
        Assert.assertTrue(hosts2.contains("eu.bookinfo.com"), "eu.bookinfo.com host not included");
    
        Map<String, Object> tls2 = (Map<String, Object>) server2.get("tls");
        Assert.assertEquals(tls2.get("httpsRedirect"), false, "Invalid tls httpsRedirect value");
        
        KubernetesUtils.deleteDirectory(targetPath);
        KubernetesTestUtils.deleteDockerImage(dockerImage);
    }
    
    /**
     * Build bal file with no selector istio gateway annotations.
     * @throws IOException Error when loading the generated yaml.
     * @throws InterruptedException Error when compiling the ballerina file.
     */
    @Test()
    public void noSelectorTest() throws IOException, InterruptedException {
        Assert.assertEquals(KubernetesTestUtils.compileBallerinaFile(balDirectory, "no_selector.bal"), 1);
    }
    
    /**
     * Build bal file with no selector istio gateway annotations.
     * @throws IOException Error when loading the generated yaml.
     * @throws InterruptedException Error when compiling the ballerina file.
     */
    @Test()
    public void noServersTest() throws IOException, InterruptedException {
        Assert.assertEquals(KubernetesTestUtils.compileBallerinaFile(balDirectory, "no_servers.bal"), 1);
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
    public void validateDockerImage() {
        ImageInspect imageInspect = getDockerImage(dockerImage);
        Assert.assertEquals(1, imageInspect.getContainerConfig().getExposedPorts().size());
        Assert.assertTrue(imageInspect.getContainerConfig().getExposedPorts().keySet().contains("9090/tcp"));
    }
}
