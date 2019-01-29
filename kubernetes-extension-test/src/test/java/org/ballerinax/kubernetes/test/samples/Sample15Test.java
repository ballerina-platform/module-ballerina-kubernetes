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
import java.util.List;

import static org.ballerinax.kubernetes.KubernetesConstants.DOCKER;
import static org.ballerinax.kubernetes.KubernetesConstants.KUBERNETES;
import static org.ballerinax.kubernetes.test.utils.KubernetesTestUtils.getExposedPorts;


public class Sample15Test implements SampleTest {
    
    private final String sourceDirPath = SAMPLE_DIR + File.separator + "sample15";
    private final String targetPath = sourceDirPath + File.separator + KUBERNETES;
    private final String dockerImage = "hello_world_k8s_rq:latest";
    
    @BeforeClass
    public void compileSample() throws IOException, InterruptedException {
        Assert.assertEquals(KubernetesTestUtils.compileBallerinaFile(sourceDirPath, "hello_world_k8s_rq.bal"), 0);
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
        Assert.assertEquals(ports.get(0), "9090/tcp");
    }
    
    @Test
    public void validateResourceQuota() throws IOException {
        File resourceQuotaYAML = new File(targetPath + File.separator + "hello_world_k8s_rq_resource_quota.yaml");
        Assert.assertTrue(resourceQuotaYAML.exists());
        ResourceQuota resourceQuota = KubernetesHelper.loadYaml(resourceQuotaYAML);
        // Assert Resource quota
        Assert.assertEquals("pod-limit", resourceQuota.getMetadata().getName());
        Assert.assertEquals(resourceQuota.getMetadata().getLabels().size(), 0, "Invalid number of labels.");
    
        Assert.assertEquals(resourceQuota.getSpec().getHard().size(), 5, "Invalid number of hard limits.");
        Assert.assertEquals(resourceQuota.getSpec().getHard().get("pods").getAmount(), "2", "Invalid number of pods.");
        Assert.assertEquals(resourceQuota.getSpec().getHard().get("requests.cpu").getAmount(), "1",
                "Invalid number of cpu requests.");
        Assert.assertEquals(resourceQuota.getSpec().getHard().get("requests.memory").getAmount(), "1Gi",
                "Invalid number of memory requests.");
        Assert.assertEquals(resourceQuota.getSpec().getHard().get("limits.cpu").getAmount(), "2",
                "Invalid number of cpu limits");
        Assert.assertEquals(resourceQuota.getSpec().getHard().get("limits.memory").getAmount(), "2Gi",
                "Invalid number of memory limits.");
    
        Assert.assertEquals(resourceQuota.getSpec().getScopes().size(), 0, "Unexpected number of scopes.");
    }
    
    @AfterClass
    public void cleanUp() throws KubernetesPluginException, DockerTestException, InterruptedException {
        KubernetesUtils.deleteDirectory(targetPath);
        KubernetesTestUtils.deleteDockerImage(dockerImage);
    }
}
