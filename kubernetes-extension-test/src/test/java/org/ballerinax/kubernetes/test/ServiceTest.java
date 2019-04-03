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

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.extensions.Ingress;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.ballerinax.kubernetes.KubernetesConstants;
import org.ballerinax.kubernetes.exceptions.KubernetesPluginException;
import org.ballerinax.kubernetes.test.utils.DockerTestException;
import org.ballerinax.kubernetes.test.utils.KubernetesTestUtils;
import org.ballerinax.kubernetes.utils.KubernetesUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.ballerinax.kubernetes.KubernetesConstants.DOCKER;
import static org.ballerinax.kubernetes.KubernetesConstants.KUBERNETES;
import static org.ballerinax.kubernetes.test.utils.KubernetesTestUtils.getExposedPorts;

/**
 * Test generating service artifacts.
 */
public class ServiceTest {
    private static final Path BAL_DIRECTORY = Paths.get("src", "test", "resources", "svc");
    private static final Path TARGET_PATH = BAL_DIRECTORY.resolve(KUBERNETES);
    private static final String DOCKER_IMAGE = "pizza-shop:latest";
    
    @Test
    public void differentSvcPortTest() throws IOException, InterruptedException, DockerTestException,
            KubernetesPluginException {
        Assert.assertEquals(KubernetesTestUtils.compileBallerinaFile(BAL_DIRECTORY, "different_svc_ports.bal"), 0);
        File yamlFile = TARGET_PATH.resolve("different_svc_ports.yaml").toFile();
        Assert.assertTrue(yamlFile.exists());
        List<HasMetadata> k8sItems = KubernetesTestUtils.loadYaml(yamlFile);
        Service service = null;
        Ingress ingress = null;
        for (HasMetadata data : k8sItems) {
            switch (data.getKind()) {
                case "Service":
                    service = (Service) data;
                    break;
                case "Ingress":
                    ingress = (Ingress) data;
                    break;
                default:
                    break;
            }
        }
    
        validateDockerfile();
        validateDockerImage(DOCKER_IMAGE);
    
        Assert.assertNotNull(service);
        Assert.assertEquals("hello", service.getMetadata().getName());
        Assert.assertEquals("different_svc_ports", service.getMetadata().getLabels().get(KubernetesConstants
                .KUBERNETES_SELECTOR_KEY));
        Assert.assertEquals(KubernetesConstants.ServiceType.ClusterIP.name(), service.getSpec().getType());
        Assert.assertEquals(1, service.getSpec().getPorts().size());
        Assert.assertEquals(8080, service.getSpec().getPorts().get(0).getPort().intValue());
        Assert.assertEquals(9090, service.getSpec().getPorts().get(0).getTargetPort().getIntVal().intValue());
    
        Assert.assertNotNull(ingress);
        Assert.assertEquals("helloep-ingress", ingress.getMetadata().getName());
        Assert.assertEquals("different_svc_ports", ingress.getMetadata().getLabels().get(KubernetesConstants
                .KUBERNETES_SELECTOR_KEY));
        Assert.assertEquals("abc.com", ingress.getSpec().getRules().get(0).getHost());
        Assert.assertEquals("/", ingress.getSpec().getRules().get(0).getHttp().getPaths().get(0).getPath());
        Assert.assertEquals(service.getMetadata().getName(), ingress.getSpec().getRules().get(0).getHttp().getPaths()
                .get(0).getBackend()
                .getServiceName());
        Assert.assertEquals(service.getSpec().getPorts().get(0).getPort().intValue(), ingress.getSpec().getRules()
                .get(0).getHttp().getPaths().get(0).getBackend()
                .getServicePort().getIntVal().intValue());
        Assert.assertEquals(2, ingress.getMetadata().getAnnotations().size());
    
        KubernetesUtils.deleteDirectory(TARGET_PATH);
        KubernetesTestUtils.deleteDockerImage(DOCKER_IMAGE);
    }
    
    /**
     * <pre>@kubernetes:Service</pre> annotation cannot be attached to a non anonymous endpoint of a service.
     * @throws IOException Error when loading the generated yaml.
     * @throws InterruptedException Error when compiling the ballerina file.
     */
    @Test
    public void serviceAnnotationOnNonAnonymousEndpointTest() throws IOException, InterruptedException {
        Assert.assertEquals(KubernetesTestUtils.compileBallerinaFile(BAL_DIRECTORY, "invalid_svc_annotation.bal"), 1);
    }
    
    @Test
    public void portAndTargetPortTest() throws IOException, InterruptedException, KubernetesPluginException,
            DockerTestException {
        Assert.assertEquals(KubernetesTestUtils.compileBallerinaFile(BAL_DIRECTORY, "port_and_target_port.bal"), 0);
        File yamlFile = TARGET_PATH.resolve("port_and_target_port.yaml").toFile();
        Assert.assertTrue(yamlFile.exists());
        KubernetesClient client = new DefaultKubernetesClient();
        List<HasMetadata> k8sItems = client.load(new FileInputStream(yamlFile)).get();
        for (HasMetadata data : k8sItems) {
            if ("Service".equals(data.getKind())) {
                Service service = (Service) data;
                Assert.assertNotNull(service);
                Assert.assertEquals("hello", service.getMetadata().getName());
                Assert.assertEquals("port_and_target_port", service.getMetadata().getLabels().get(KubernetesConstants
                        .KUBERNETES_SELECTOR_KEY));
                Assert.assertEquals(KubernetesConstants.ServiceType.ClusterIP.name(), service.getSpec().getType());
                Assert.assertEquals(1, service.getSpec().getPorts().size());
                Assert.assertEquals(8080, service.getSpec().getPorts().get(0).getPort().intValue());
                Assert.assertEquals(9090, service.getSpec().getPorts().get(0).getTargetPort().getIntVal().intValue());
            }
        }
    
        validateDockerfile();
        validateDockerImage(DOCKER_IMAGE);
    
        KubernetesUtils.deleteDirectory(TARGET_PATH);
        KubernetesTestUtils.deleteDockerImage(DOCKER_IMAGE);
    }
    
    public void validateDockerfile() {
        File dockerFile = TARGET_PATH.resolve(DOCKER).resolve("Dockerfile").toFile();
        Assert.assertTrue(dockerFile.exists());
    }
    
    public void validateDockerImage(String dockerImage) throws DockerTestException, InterruptedException {
        List<String> ports = getExposedPorts(dockerImage);
        Assert.assertEquals(ports.size(), 1);
        Assert.assertEquals(ports.get(0), "9090/tcp");
    }
}
