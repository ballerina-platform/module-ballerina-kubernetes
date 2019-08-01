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

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.HorizontalPodAutoscaler;
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
import java.nio.file.Path;
import java.util.List;

import static org.ballerinax.kubernetes.KubernetesConstants.DOCKER;
import static org.ballerinax.kubernetes.KubernetesConstants.KUBERNETES;
import static org.ballerinax.kubernetes.test.utils.KubernetesTestUtils.getExposedPorts;

public class Sample5Test extends SampleTest {
    
    private static final Path SOURCE_DIR_PATH = SAMPLE_DIR.resolve("sample5");
    private static final Path DOCKER_TARGET_PATH = SOURCE_DIR_PATH.resolve(DOCKER);
    private static final Path KUBERNETES_TARGET_PATH = SOURCE_DIR_PATH.resolve(KUBERNETES);
    private static final String DOCKER_IMAGE = "ballerina.com/pizzashack:2.1.0";
    private HorizontalPodAutoscaler podAutoscaler;
    
    @BeforeClass
    public void compileSample() throws IOException, InterruptedException {
        Assert.assertEquals(KubernetesTestUtils.compileBallerinaFile(SOURCE_DIR_PATH, "pizzashack.bal"), 0);
        File artifactYaml = KUBERNETES_TARGET_PATH.resolve("pizzashack.yaml").toFile();
        Assert.assertTrue(artifactYaml.exists());
        KubernetesClient client = new DefaultKubernetesClient();
        List<HasMetadata> k8sItems = client.load(new FileInputStream(artifactYaml)).get();
        for (HasMetadata data : k8sItems) {
            switch (data.getKind()) {
                case "HorizontalPodAutoscaler":
                    podAutoscaler = (HorizontalPodAutoscaler) data;
                    break;
                case "Service":
                case "Ingress":
                case "Secret":
                case "Deployment":
                    break;
                default:
                    Assert.fail("Unexpected k8s resource found: " + data.getKind());
                    break;
            }
        }
    }
    
    @Test
    public void validatePodAutoscaler() {
        Assert.assertNotNull(podAutoscaler);
        Assert.assertEquals(podAutoscaler.getMetadata().getName(), "pizzashack-hpa");
        Assert.assertEquals(podAutoscaler.getMetadata().getLabels().get(KubernetesConstants
                .KUBERNETES_SELECTOR_KEY), "pizzashack");
        Assert.assertEquals(podAutoscaler.getSpec().getMaxReplicas().intValue(), 2);
        Assert.assertEquals(podAutoscaler.getSpec().getMinReplicas().intValue(), 1);
        Assert.assertEquals(podAutoscaler.getSpec().getMetrics().size(), 1, "CPU metric is missing.");
        Assert.assertEquals(podAutoscaler.getSpec().getMetrics().get(0).getResource().getName(), "cpu",
                "Invalid resource name.");
        Assert.assertEquals(podAutoscaler.getSpec().getMetrics().get(0).getResource().getTargetAverageUtilization()
                .intValue(), 50);
        Assert.assertEquals(podAutoscaler.getSpec().getScaleTargetRef().getName(), "pizzashack-deployment");
    }
    
    @Test
    public void validateDockerfile() {
        File dockerFile = DOCKER_TARGET_PATH.resolve("Dockerfile").toFile();
        Assert.assertTrue(dockerFile.exists());
    }
    
    @Test
    public void validateDockerImage() throws DockerTestException, InterruptedException {
        List<String> ports = getExposedPorts(DOCKER_IMAGE);
        Assert.assertEquals(ports.size(), 2);
        Assert.assertEquals(ports.get(0), "9090/tcp");
        Assert.assertEquals(ports.get(1), "9095/tcp");
    }
    
    @AfterClass
    public void cleanUp() throws KubernetesPluginException, DockerTestException, InterruptedException {
        KubernetesUtils.deleteDirectory(KUBERNETES_TARGET_PATH);
        KubernetesUtils.deleteDirectory(DOCKER_TARGET_PATH);
        KubernetesTestUtils.deleteDockerImage(DOCKER_IMAGE);
    }
}
