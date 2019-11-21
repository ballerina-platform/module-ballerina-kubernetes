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

import me.snowdrop.istio.api.networking.v1alpha3.Gateway;
import org.ballerinax.kubernetes.KubernetesConstants;
import org.ballerinax.kubernetes.exceptions.KubernetesPluginException;
import org.ballerinax.kubernetes.test.utils.DockerTestException;
import org.ballerinax.kubernetes.test.utils.KubernetesTestUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.ballerinax.kubernetes.KubernetesConstants.DOCKER;
import static org.ballerinax.kubernetes.KubernetesConstants.KUBERNETES;
import static org.ballerinax.kubernetes.test.utils.KubernetesTestUtils.getExposedPorts;

/**
 * Test cases for generating istio gateway artifacts.
 *
 * @since 0.985.0
 */
public class IstioGatewayTest {
    
    private static final Path BAL_DIRECTORY = Paths.get("src", "test", "resources", "istio", "gateway");
    private static final Path DOCKER_TARGET_PATH = BAL_DIRECTORY.resolve(DOCKER);
    private static final Path KUBERNETES_TARGET_PATH = BAL_DIRECTORY.resolve(KUBERNETES);
    private static final String DOCKER_IMAGE = "pizza-shop:latest";
    
    /**
     * Build bal file with istio gateway annotations.
     *
     * @throws IOException               Error when loading the generated yaml.
     * @throws InterruptedException      Error when compiling the ballerina file.
     * @throws KubernetesPluginException Error when deleting the generated artifacts folder.
     */
    @Test(groups = {"istio"})
    public void allFieldsTest() throws IOException, InterruptedException, KubernetesPluginException,
            DockerTestException {
        Assert.assertEquals(KubernetesTestUtils.compileBallerinaFile(BAL_DIRECTORY, "all_fields.bal"), 0);

        // Check if docker image exists and correct
        validateDockerfile();
        validateDockerImage();

        // Validate gateway yaml
        File gatewayFile = KUBERNETES_TARGET_PATH.resolve("all_fields_istio_gateway.yaml").toFile();
        Assert.assertTrue(gatewayFile.exists());
        Gateway gateway = KubernetesTestUtils.loadYaml(gatewayFile);

        Assert.assertNotNull(gateway.getMetadata());
        Assert.assertEquals(gateway.getMetadata().getName(), "my-gateway", "Invalid gateway name");
        Assert.assertEquals(gateway.getMetadata().getNamespace(), "ballerina", "Invalid gateway namespace");
        
        Assert.assertEquals(gateway.getMetadata().getLabels().size(), 2);
        Assert.assertEquals(gateway.getMetadata().getLabels().get("label1"), "label1", "Invalid label");
        Assert.assertEquals(gateway.getMetadata().getLabels().get("label2"), "label2", "Invalid label");
    
        Assert.assertEquals(gateway.getMetadata().getAnnotations().size(), 2);
        Assert.assertEquals(gateway.getMetadata().getAnnotations().get("anno1"), "anno1Val",
                "Invalid annotation value");
        Assert.assertEquals(gateway.getMetadata().getAnnotations().get("anno2"), "anno2Val",
                "Invalid annotation value");
    
        Assert.assertNotNull(gateway.getSpec());
        Assert.assertEquals(gateway.getSpec().getSelector().get(KubernetesConstants.KUBERNETES_SELECTOR_KEY),
                "my-gateway-controller", "Invalid selector.");
    
        Assert.assertNotNull(gateway.getSpec().getServers());
        Assert.assertEquals(gateway.getSpec().getServers().size(), 1);
        Assert.assertEquals(gateway.getSpec().getServers().get(0).getPort().getNumber().intValue(), 80,
                "Invalid port number.");
        Assert.assertEquals(gateway.getSpec().getServers().get(0).getPort().getName(), "http", "Invalid port name.");
        Assert.assertEquals(gateway.getSpec().getServers().get(0).getPort().getProtocol(), "HTTP",
                "Invalid port protocol.");
    
        Assert.assertNotNull(gateway.getSpec().getServers().get(0).getHosts());
        Assert.assertEquals(gateway.getSpec().getServers().get(0).getHosts().size(), 2);
        Assert.assertTrue(gateway.getSpec().getServers().get(0).getHosts().contains("uk.bookinfo.com"),
                "uk.bookinfo.com host not included");
        Assert.assertTrue(gateway.getSpec().getServers().get(0).getHosts().contains("eu.bookinfo.com"),
                "eu.bookinfo.com host not included");
    
        Assert.assertNotNull(gateway.getSpec().getServers().get(0).getTls());
        Assert.assertTrue(gateway.getSpec().getServers().get(0).getTls().getHttpsRedirect(),
                "Invalid tls httpsRedirect value");

        KubernetesUtils.deleteDirectory(KUBERNETES_TARGET_PATH);
        KubernetesUtils.deleteDirectory(DOCKER_TARGET_PATH);
        KubernetesTestUtils.deleteDockerImage(DOCKER_IMAGE);
    }
    
    /**
     * Build bal file with invalid port istio gateway annotations.
     *
     * @throws IOException          Error when loading the generated yaml.
     * @throws InterruptedException Error when compiling the ballerina file.
     */
    @Test(enabled = false, groups = {"istio"})
    public void invalidPortTest() throws IOException, InterruptedException {
        Assert.assertEquals(KubernetesTestUtils.compileBallerinaFile(BAL_DIRECTORY, "invalid_port.bal"), 1);
    }
    
    /**
     * Build bal file with istio gateway annotation having multiple servers.
     *
     * @throws IOException               Error when loading the generated yaml.
     * @throws InterruptedException      Error when compiling the ballerina file.
     * @throws KubernetesPluginException Error when deleting the generated artifacts folder.
     */
    @Test(groups = {"istio"})
    public void multipleServersTest() throws IOException, InterruptedException, KubernetesPluginException,
            DockerTestException {
        Assert.assertEquals(KubernetesTestUtils.compileBallerinaFile(BAL_DIRECTORY, "multiple_servers.bal"), 0);

        // Check if docker image exists and correct
        validateDockerfile();
        validateDockerImage();

        // Validate gateway yaml
        File gatewayFile = KUBERNETES_TARGET_PATH.resolve("multiple_servers_istio_gateway.yaml").toFile();
        Assert.assertTrue(gatewayFile.exists());
        Gateway gateway = KubernetesTestUtils.loadYaml(gatewayFile);
    
        Assert.assertNotNull(gateway.getMetadata());
        Assert.assertEquals(gateway.getMetadata().getName(), "my-gateway", "Invalid gateway name");
        
        Assert.assertNotNull(gateway.getSpec());
        Assert.assertEquals(gateway.getSpec().getSelector().get(KubernetesConstants.KUBERNETES_SELECTOR_KEY),
                "my-gateway-controller", "Invalid selector.");
    
        Assert.assertNotNull(gateway.getSpec().getServers());
        Assert.assertEquals(gateway.getSpec().getServers().size(), 2);
        Assert.assertEquals(gateway.getSpec().getServers().get(0).getPort().getNumber().intValue(), 80,
                "Invalid port number.");
        Assert.assertEquals(gateway.getSpec().getServers().get(0).getPort().getName(), "http", "Invalid port name.");
        Assert.assertEquals(gateway.getSpec().getServers().get(0).getPort().getProtocol(), "HTTP",
                "Invalid port protocol.");
    
        Assert.assertNotNull(gateway.getSpec().getServers().get(0).getHosts());
        Assert.assertEquals(gateway.getSpec().getServers().get(0).getHosts().size(), 2);
        Assert.assertTrue(gateway.getSpec().getServers().get(0).getHosts().contains("uk.bookinfo.com"),
                "uk.bookinfo.com host not included");
        Assert.assertTrue(gateway.getSpec().getServers().get(0).getHosts().contains("eu.bookinfo.com"),
                "eu.bookinfo.com host not included");
    
        Assert.assertNotNull(gateway.getSpec().getServers().get(0).getTls());
        Assert.assertTrue(gateway.getSpec().getServers().get(0).getTls().getHttpsRedirect(),
                "Invalid tls httpsRedirect value");
    
        Assert.assertEquals(gateway.getSpec().getServers().get(1).getPort().getNumber().intValue(), 443,
                "Invalid port number.");
        Assert.assertEquals(gateway.getSpec().getServers().get(1).getPort().getName(), "https",
                "Invalid port name.");
        Assert.assertEquals(gateway.getSpec().getServers().get(1).getPort().getProtocol(), "HTTPS",
                "Invalid port protocol.");
    
        Assert.assertNotNull(gateway.getSpec().getServers().get(1).getHosts());
        Assert.assertEquals(gateway.getSpec().getServers().get(1).getHosts().size(), 2);
        Assert.assertTrue(gateway.getSpec().getServers().get(1).getHosts().contains("uk.bookinfo.com"),
                "uk.bookinfo.com host not included");
        Assert.assertTrue(gateway.getSpec().getServers().get(1).getHosts().contains("eu.bookinfo.com"),
                "eu.bookinfo.com host not included");
    
        Assert.assertNotNull(gateway.getSpec().getServers().get(1).getTls());
        Assert.assertFalse(gateway.getSpec().getServers().get(1).getTls().getHttpsRedirect(),
                "Invalid tls httpsRedirect value");

        KubernetesUtils.deleteDirectory(KUBERNETES_TARGET_PATH);
        KubernetesUtils.deleteDirectory(DOCKER_TARGET_PATH);
        KubernetesTestUtils.deleteDockerImage(DOCKER_IMAGE);
    }
    
    /**
     * Build bal file with no selector istio gateway annotations.
     *
     * @throws IOException          Error when loading the generated yaml.
     * @throws InterruptedException Error when compiling the ballerina file.
     */
    @Test(groups = {"istio"})
    public void noSelectorTest() throws IOException, InterruptedException, KubernetesPluginException,
            DockerTestException {
        Assert.assertEquals(KubernetesTestUtils.compileBallerinaFile(BAL_DIRECTORY, "no_selector.bal"), 0);
    
        // Check if docker image exists and correct
        validateDockerfile();
        validateDockerImage();
    
        // Validate gateway yaml
        File gatewayFile = KUBERNETES_TARGET_PATH.resolve("no_selector_istio_gateway.yaml").toFile();
        Assert.assertTrue(gatewayFile.exists());
        Gateway gateway = KubernetesTestUtils.loadYaml(gatewayFile);
    
        Assert.assertNotNull(gateway.getSpec());
        Assert.assertNotNull(gateway.getSpec().getSelector());
        Assert.assertEquals(gateway.getSpec().getSelector().get(KubernetesConstants.ISTIO_GATEWAY_SELECTOR),
                "ingressgateway", "Invalid selector.");
    
        KubernetesUtils.deleteDirectory(KUBERNETES_TARGET_PATH);
        KubernetesUtils.deleteDirectory(DOCKER_TARGET_PATH);
        KubernetesTestUtils.deleteDockerImage(DOCKER_IMAGE);
    }
    
    /**
     * Build bal file with istio gateway annotation having no tls httpsRedirect field.
     *
     * @throws IOException               Error when loading the generated yaml.
     * @throws InterruptedException      Error when compiling the ballerina file.
     * @throws KubernetesPluginException Error when deleting the generated artifacts folder.
     */
    @Test(groups = {"istio"})
    public void noTLSHttpRedirect() throws IOException, InterruptedException, KubernetesPluginException,
            DockerTestException {
        Assert.assertEquals(KubernetesTestUtils.compileBallerinaFile(BAL_DIRECTORY, "no_tls_https_redirect.bal"), 0);

        // Check if docker image exists and correct
        validateDockerfile();
        validateDockerImage();

        // Validate gateway yaml
        File gatewayFile = KUBERNETES_TARGET_PATH.resolve("no_tls_https_redirect_istio_gateway.yaml").toFile();
        Assert.assertTrue(gatewayFile.exists());
        Gateway gateway = KubernetesTestUtils.loadYaml(gatewayFile);
    
        Assert.assertNotNull(gateway.getMetadata());
        Assert.assertEquals(gateway.getMetadata().getName(), "my-gateway", "Invalid gateway name");
    
        Assert.assertNotNull(gateway.getSpec());
        Assert.assertEquals(gateway.getSpec().getSelector().get(KubernetesConstants.KUBERNETES_SELECTOR_KEY),
                "my-gateway-controller", "Invalid selector.");
    
        Assert.assertNotNull(gateway.getSpec().getServers());
        Assert.assertEquals(gateway.getSpec().getServers().size(), 1);
        Assert.assertEquals(gateway.getSpec().getServers().get(0).getPort().getNumber().intValue(), 80,
                "Invalid port number.");
        Assert.assertEquals(gateway.getSpec().getServers().get(0).getPort().getName(), "http", "Invalid port name.");
        Assert.assertEquals(gateway.getSpec().getServers().get(0).getPort().getProtocol(), "HTTP",
                "Invalid port protocol.");
    
        Assert.assertNotNull(gateway.getSpec().getServers().get(0).getHosts());
        Assert.assertEquals(gateway.getSpec().getServers().get(0).getHosts().size(), 2);
        Assert.assertTrue(gateway.getSpec().getServers().get(0).getHosts().contains("uk.bookinfo.com"),
                "uk.bookinfo.com host not included");
        Assert.assertTrue(gateway.getSpec().getServers().get(0).getHosts().contains("eu.bookinfo.com"),
                "eu.bookinfo.com host not included");
    
        Assert.assertNull(gateway.getSpec().getServers().get(0).getTls(), "tls options should not be available");

        KubernetesUtils.deleteDirectory(KUBERNETES_TARGET_PATH);
        KubernetesUtils.deleteDirectory(DOCKER_TARGET_PATH);
        KubernetesTestUtils.deleteDockerImage(DOCKER_IMAGE);
    }
    
    /**
     * Build bal file with istio gateway annotation having mutual mode TLS.
     *
     * @throws IOException               Error when loading the generated yaml.
     * @throws InterruptedException      Error when compiling the ballerina file.
     * @throws KubernetesPluginException Error when deleting the generated artifacts folder.
     */
    @Test(groups = {"istio"})
    public void tlsMutualTest() throws IOException, InterruptedException, KubernetesPluginException,
            DockerTestException {
        Assert.assertEquals(KubernetesTestUtils.compileBallerinaFile(BAL_DIRECTORY, "tls_mutual.bal"), 0);

        // Check if docker image exists and correct
        validateDockerfile();
        validateDockerImage();

        // Validate gateway yaml
        File gatewayFile = KUBERNETES_TARGET_PATH.resolve("tls_mutual_istio_gateway.yaml").toFile();
        Assert.assertTrue(gatewayFile.exists());
        Gateway gateway = KubernetesTestUtils.loadYaml(gatewayFile);
    
        Assert.assertNotNull(gateway.getMetadata());
        Assert.assertEquals(gateway.getMetadata().getName(), "my-gateway", "Invalid gateway name");
    
        Assert.assertNotNull(gateway.getSpec());
        Assert.assertEquals(gateway.getSpec().getSelector().get(KubernetesConstants.KUBERNETES_SELECTOR_KEY),
                "my-gateway-controller", "Invalid selector.");
    
        Assert.assertNotNull(gateway.getSpec().getServers());
        Assert.assertEquals(gateway.getSpec().getServers().size(), 1);
        Assert.assertEquals(gateway.getSpec().getServers().get(0).getPort().getNumber().intValue(), 443,
                "Invalid port number.");
        Assert.assertEquals(gateway.getSpec().getServers().get(0).getPort().getName(), "https",
                "Invalid port name.");
        Assert.assertEquals(gateway.getSpec().getServers().get(0).getPort().getProtocol(), "HTTPS",
                "Invalid port protocol.");
    
        Assert.assertNotNull(gateway.getSpec().getServers().get(0).getHosts());
        Assert.assertEquals(gateway.getSpec().getServers().get(0).getHosts().size(), 1);
        Assert.assertTrue(gateway.getSpec().getServers().get(0).getHosts().contains("httpbin.example.com"),
                "httpbin.example.com host not included");
    
        Assert.assertNotNull(gateway.getSpec().getServers().get(0).getTls());
        Assert.assertFalse(gateway.getSpec().getServers().get(0).getTls().getHttpsRedirect(),
                "Invalid tls httpsRedirect value");
        Assert.assertEquals(gateway.getSpec().getServers().get(0).getTls().getMode().name(), "MUTUAL",
                "Invalid tls mode value");
        Assert.assertEquals(gateway.getSpec().getServers().get(0).getTls().getServerCertificate(),
                "/etc/istio/ingressgateway-certs/tls.crt", "Invalid tls serverCertificate value");
        Assert.assertEquals(gateway.getSpec().getServers().get(0).getTls().getPrivateKey(),
                "/etc/istio/ingressgateway-certs/tls.key", "Invalid tls privateKey value");
        Assert.assertEquals(gateway.getSpec().getServers().get(0).getTls().getCaCertificates(),
                "/etc/istio/ingressgateway-ca-certs/ca-chain.cert.pem", "Invalid tls caCertificates value");
        
        KubernetesUtils.deleteDirectory(KUBERNETES_TARGET_PATH);
        KubernetesUtils.deleteDirectory(DOCKER_TARGET_PATH);
        KubernetesTestUtils.deleteDockerImage(DOCKER_IMAGE);
    }
    
    /**
     * Build bal file with istio gateway annotation having invalid mutual mode TLS.
     *
     * @throws IOException          Error when loading the generated yaml.
     * @throws InterruptedException Error when compiling the ballerina file.
     */
    @Test(enabled = false, groups = {"istio"})
    public void invalidTlsMutualTest() throws IOException, InterruptedException {
        Assert.assertEquals(KubernetesTestUtils.compileBallerinaFile(BAL_DIRECTORY, "tls_mutual_invalid.bal"), 1);
    }
    
    /**
     * Build bal file with istio gateway annotation having simple mode TLS.
     *
     * @throws IOException               Error when loading the generated yaml.
     * @throws InterruptedException      Error when compiling the ballerina file.
     * @throws KubernetesPluginException Error when deleting the generated artifacts folder.
     */
    @Test(groups = {"istio"})
    public void tlsSimpleTest() throws IOException, InterruptedException, KubernetesPluginException,
            DockerTestException {
        Assert.assertEquals(KubernetesTestUtils.compileBallerinaFile(BAL_DIRECTORY, "tls_simple.bal"), 0);
        
        // Check if docker image exists and correct
        validateDockerfile();
        validateDockerImage();
        
        // Validate gateway yaml
        File gatewayFile = KUBERNETES_TARGET_PATH.resolve("tls_simple_istio_gateway.yaml").toFile();
        Assert.assertTrue(gatewayFile.exists());
        Gateway gateway = KubernetesTestUtils.loadYaml(gatewayFile);
    
        Assert.assertNotNull(gateway.getMetadata());
        Assert.assertEquals(gateway.getMetadata().getName(), "my-gateway", "Invalid gateway name");
    
        Assert.assertNotNull(gateway.getSpec());
        Assert.assertEquals(gateway.getSpec().getSelector().get(KubernetesConstants.KUBERNETES_SELECTOR_KEY),
                "my-gateway-controller", "Invalid selector.");
    
        Assert.assertNotNull(gateway.getSpec().getServers());
        Assert.assertEquals(gateway.getSpec().getServers().size(), 1);
        Assert.assertEquals(gateway.getSpec().getServers().get(0).getPort().getNumber().intValue(), 443,
                "Invalid port number.");
        Assert.assertEquals(gateway.getSpec().getServers().get(0).getPort().getName(), "https",
                "Invalid port name.");
        Assert.assertEquals(gateway.getSpec().getServers().get(0).getPort().getProtocol(), "HTTPS",
                "Invalid port protocol.");
    
        Assert.assertNotNull(gateway.getSpec().getServers().get(0).getHosts());
        Assert.assertEquals(gateway.getSpec().getServers().get(0).getHosts().size(), 1);
        Assert.assertTrue(gateway.getSpec().getServers().get(0).getHosts().contains("httpbin.example.com"),
                "httpbin.example.com host not included");
    
        Assert.assertNotNull(gateway.getSpec().getServers().get(0).getTls());
        Assert.assertFalse(gateway.getSpec().getServers().get(0).getTls().getHttpsRedirect(),
                "Invalid tls httpsRedirect value");
        Assert.assertEquals(gateway.getSpec().getServers().get(0).getTls().getMode().name(), "SIMPLE",
                "Invalid tls mode value");
        Assert.assertEquals(gateway.getSpec().getServers().get(0).getTls().getServerCertificate(),
                "/etc/istio/ingressgateway-certs/tls.crt", "Invalid tls serverCertificate value");
        Assert.assertEquals(gateway.getSpec().getServers().get(0).getTls().getPrivateKey(),
                "/etc/istio/ingressgateway-certs/tls.key", "Invalid tls privateKey value");
        Assert.assertNull(gateway.getSpec().getServers().get(0).getTls().getCaCertificates(),
                "Unexpected tls caCertificates value found.");
        
        KubernetesUtils.deleteDirectory(KUBERNETES_TARGET_PATH);
        KubernetesUtils.deleteDirectory(DOCKER_TARGET_PATH);
        KubernetesTestUtils.deleteDockerImage(DOCKER_IMAGE);
    }
    
    /**
     * Build bal file with istio gateway annotation with no values for endpoint.
     *
     * @throws IOException               Error when loading the generated yaml.
     * @throws InterruptedException      Error when compiling the ballerina file.
     * @throws KubernetesPluginException Error when deleting the generated artifacts folder.
     */
    @Test(groups = {"istio"})
    public void emptyAnnoForEndpointTest() throws IOException, InterruptedException, KubernetesPluginException,
            DockerTestException {
        Assert.assertEquals(KubernetesTestUtils.compileBallerinaFile(BAL_DIRECTORY, "empty_annotation_ep.bal"), 0);
        
        // Check if docker image exists and correct
        validateDockerfile();
        validateDockerImage();
        
        // Validate gateway yaml
        File gatewayFile = KUBERNETES_TARGET_PATH.resolve("empty_annotation_ep_istio_gateway.yaml").toFile();
        Assert.assertTrue(gatewayFile.exists());
        Gateway gateway = KubernetesTestUtils.loadYaml(gatewayFile);
    
        Assert.assertNotNull(gateway.getMetadata());
        Assert.assertEquals(gateway.getMetadata().getName(), "helloep-istio-gw", "Invalid gateway name");
    
        Assert.assertNotNull(gateway.getSpec());
        Assert.assertEquals(gateway.getSpec().getSelector().get(KubernetesConstants.ISTIO_GATEWAY_SELECTOR),
                "ingressgateway", "Invalid selector.");
    
        Assert.assertNotNull(gateway.getSpec().getServers());
        Assert.assertEquals(gateway.getSpec().getServers().size(), 1);
        Assert.assertEquals(gateway.getSpec().getServers().get(0).getPort().getNumber().intValue(), 80,
                "Invalid port number.");
        Assert.assertEquals(gateway.getSpec().getServers().get(0).getPort().getName(), "http", "Invalid port name.");
        Assert.assertEquals(gateway.getSpec().getServers().get(0).getPort().getProtocol(), "HTTP",
                "Invalid port protocol.");
    
        Assert.assertNotNull(gateway.getSpec().getServers().get(0).getHosts());
        Assert.assertEquals(gateway.getSpec().getServers().get(0).getHosts().size(), 1);
        Assert.assertEquals(gateway.getSpec().getServers().get(0).getHosts().get(0), "*",
                "* host not included");
        
        KubernetesUtils.deleteDirectory(KUBERNETES_TARGET_PATH);
        KubernetesUtils.deleteDirectory(DOCKER_TARGET_PATH);
        KubernetesTestUtils.deleteDockerImage(DOCKER_IMAGE);
    }
    
    /**
     * Build bal file with istio gateway annotation with no values for service.
     *
     * @throws IOException               Error when loading the generated yaml.
     * @throws InterruptedException      Error when compiling the ballerina file.
     * @throws KubernetesPluginException Error when deleting the generated artifacts folder.
     */
    @Test(groups = {"istio"})
    public void emptyAnnotationForSvcTest() throws IOException, InterruptedException, KubernetesPluginException,
            DockerTestException {
        Assert.assertEquals(KubernetesTestUtils.compileBallerinaFile(BAL_DIRECTORY, "empty_annotation_svc.bal"), 0);
        
        // Check if docker image exists and correct
        validateDockerfile();
        validateDockerImage();
        
        // Validate gateway yaml
        File gatewayFile = KUBERNETES_TARGET_PATH.resolve("empty_annotation_svc_istio_gateway.yaml").toFile();
        Assert.assertTrue(gatewayFile.exists());
        Gateway gateway = KubernetesTestUtils.loadYaml(gatewayFile);
    
        Assert.assertNotNull(gateway.getMetadata());
        Assert.assertEquals(gateway.getMetadata().getName(), "helloworld-istio-gw", "Invalid gateway name");
    
        Assert.assertNotNull(gateway.getSpec());
        Assert.assertEquals(gateway.getSpec().getSelector().get(KubernetesConstants.ISTIO_GATEWAY_SELECTOR),
                "ingressgateway", "Invalid selector.");
    
        Assert.assertNotNull(gateway.getSpec().getServers());
        Assert.assertEquals(gateway.getSpec().getServers().size(), 1);
        Assert.assertEquals(gateway.getSpec().getServers().get(0).getPort().getNumber().intValue(), 80,
                "Invalid port number.");
        Assert.assertEquals(gateway.getSpec().getServers().get(0).getPort().getName(), "http", "Invalid port name.");
        Assert.assertEquals(gateway.getSpec().getServers().get(0).getPort().getProtocol(), "HTTP",
                "Invalid port protocol.");
    
        Assert.assertNotNull(gateway.getSpec().getServers().get(0).getHosts());
        Assert.assertEquals(gateway.getSpec().getServers().get(0).getHosts().size(), 1);
        Assert.assertEquals(gateway.getSpec().getServers().get(0).getHosts().get(0), "*",
                "* host not included");
        
        KubernetesUtils.deleteDirectory(KUBERNETES_TARGET_PATH);
        KubernetesUtils.deleteDirectory(DOCKER_TARGET_PATH);
        KubernetesTestUtils.deleteDockerImage(DOCKER_IMAGE);
    }
    
    /**
     * Build bal file with istio gateway annotation having invalid simple mode TLS.
     *
     * @throws IOException          Error when loading the generated yaml.
     * @throws InterruptedException Error when compiling the ballerina file.
     */
    @Test(enabled = false, groups = {"istio"})
    public void invalidTlsSimpleTest() throws IOException, InterruptedException {
        Assert.assertEquals(KubernetesTestUtils.compileBallerinaFile(BAL_DIRECTORY, "tls_simple_invalid.bal"), 1);
    }
    
    /**
     * Validate if Dockerfile is created.
     */
    public void validateDockerfile() {
        File dockerFile = DOCKER_TARGET_PATH.resolve("Dockerfile").toFile();
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
