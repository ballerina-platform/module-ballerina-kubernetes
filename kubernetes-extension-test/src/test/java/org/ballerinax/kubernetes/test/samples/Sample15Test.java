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
import io.fabric8.kubernetes.api.model.ResourceQuota;
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

/**
 * Test cases for sample 15.
 */
public class Sample15Test extends SampleTest {
    private static final Path SOURCE_DIR_PATH = SAMPLE_DIR.resolve("sample15");
    private static final Path DOCKER_TARGET_PATH = SOURCE_DIR_PATH.resolve(DOCKER);
    private static final Path KUBERNETES_TARGET_PATH = SOURCE_DIR_PATH.resolve(KUBERNETES);
    private static final String DOCKER_IMAGE = "hello_world_k8s_rq:latest";
    private ResourceQuota resourceQuota;
    
    @BeforeClass
    public void compileSample() throws IOException, InterruptedException {
        Assert.assertEquals(KubernetesTestUtils.compileBallerinaFile(SOURCE_DIR_PATH, "hello_world_k8s_rq.bal"), 0);
        File yamlFile = KUBERNETES_TARGET_PATH.resolve("hello_world_k8s_rq.yaml").toFile();
        Assert.assertTrue(yamlFile.exists());
        List<HasMetadata> k8sItems = KubernetesTestUtils.loadYaml(yamlFile);
        for (HasMetadata data : k8sItems) {
            if ("ResourceQuota".equals(data.getKind())) {
                resourceQuota = (ResourceQuota) data;
            }
        }
    }
    
    @Test
    public void validateResourceQuota() {
        Assert.assertNotNull(resourceQuota);
        Assert.assertEquals("pod-limit", resourceQuota.getMetadata().getName());
        Assert.assertEquals(resourceQuota.getMetadata().getLabels().size(), 0, "Invalid number of labels.");
    
        Assert.assertEquals(resourceQuota.getSpec().getHard().size(), 5, "Invalid number of hard limits.");
        Assert.assertEquals(resourceQuota.getSpec().getHard().get("pods").getAmount(), "2", "Invalid number of pods.");
        Assert.assertEquals(resourceQuota.getSpec().getHard().get("requests.cpu").getAmount(), "1",
                "Invalid number of cpu requests.");
        Assert.assertEquals(resourceQuota.getSpec().getHard().get("requests.memory").getAmount(), "1",
                "Invalid number of memory requests.");
        Assert.assertEquals(resourceQuota.getSpec().getHard().get("requests.memory").getFormat(), "Gi",
                "Invalid number of memory requests.");
        Assert.assertEquals(resourceQuota.getSpec().getHard().get("limits.cpu").getAmount(), "2",
                "Invalid number of cpu limits");
        Assert.assertEquals(resourceQuota.getSpec().getHard().get("limits.memory").getAmount(), "2",
                "Invalid number of memory limits.");
        Assert.assertEquals(resourceQuota.getSpec().getHard().get("limits.memory").getFormat(), "Gi",
                "Invalid number of memory limits.");
    
        Assert.assertEquals(resourceQuota.getSpec().getScopes().size(), 0, "Unexpected number of scopes.");
    }
    
    @Test
    public void validateDockerfile() {
        File dockerFile = DOCKER_TARGET_PATH.resolve("Dockerfile").toFile();
        Assert.assertTrue(dockerFile.exists());
    }
    
    @Test
    public void validateDockerImage() throws DockerTestException, InterruptedException {
        List<String> ports = getExposedPorts(DOCKER_IMAGE);
        Assert.assertEquals(ports.size(), 1);
        Assert.assertEquals(ports.get(0), "9090/tcp");
    }
    
    @AfterClass
    public void cleanUp() throws KubernetesPluginException {
        KubernetesUtils.deleteDirectory(KUBERNETES_TARGET_PATH);
        KubernetesUtils.deleteDirectory(DOCKER_TARGET_PATH);
        KubernetesTestUtils.deleteDockerImage(DOCKER_IMAGE);
    }
}
