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

import com.spotify.docker.client.messages.ImageInfo;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.batch.Job;
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
import static org.ballerinax.kubernetes.test.utils.KubernetesTestUtils.deleteK8s;
import static org.ballerinax.kubernetes.test.utils.KubernetesTestUtils.deployK8s;
import static org.ballerinax.kubernetes.test.utils.KubernetesTestUtils.getDockerImage;
import static org.ballerinax.kubernetes.test.utils.KubernetesTestUtils.loadImage;

/**
 * Test cases for sample 11.
 */
public class Sample11Test extends SampleTest {

    private static final Path SOURCE_DIR_PATH = SAMPLE_DIR.resolve("sample11");
    private static final Path DOCKER_TARGET_PATH = SOURCE_DIR_PATH.resolve(DOCKER);
    private static final Path KUBERNETES_TARGET_PATH = SOURCE_DIR_PATH.resolve(KUBERNETES);
    private static final String DOCKER_IMAGE = "hello_world_job:latest";
    private Job job;

    @BeforeClass
    public void compileSample() throws IOException, InterruptedException {
        Assert.assertEquals(KubernetesTestUtils.compileBallerinaFile(SOURCE_DIR_PATH, "hello_world_job.bal"), 0);
        File artifactYaml = KUBERNETES_TARGET_PATH.resolve("hello_world_job.yaml").toFile();
        Assert.assertTrue(artifactYaml.exists());
        KubernetesClient client = new DefaultKubernetesClient();
        List<HasMetadata> k8sItems = client.load(new FileInputStream(artifactYaml)).get();
        for (HasMetadata data : k8sItems) {
            if ("Job".equals(data.getKind())) {
                job = (Job) data;
            } else {
                Assert.fail("Unexpected k8s resource found: " + data.getKind());
            }
        }
    }

    @Test
    public void validateJob() {
        Assert.assertNotNull(job);
        Assert.assertEquals("hello-world-job-job", job.getMetadata().getName());
        Assert.assertEquals(job.getSpec().getTemplate().getSpec().getContainers().size(), 1);

        Container container = job.getSpec().getTemplate().getSpec().getContainers().get(0);
        Assert.assertEquals(container.getImage(), DOCKER_IMAGE);
        Assert.assertEquals(container.getImagePullPolicy(), KubernetesConstants.ImagePullPolicy.IfNotPresent.name());
        Assert.assertEquals(job.getSpec().getTemplate().getSpec()
                .getRestartPolicy(), KubernetesConstants.RestartPolicy.Never.name());
    }

    @Test
    public void validateDockerfile() {
        File dockerFile = DOCKER_TARGET_PATH.resolve("Dockerfile").toFile();
        Assert.assertTrue(dockerFile.exists());
    }

    @Test
    public void validateDockerImage() throws DockerTestException, InterruptedException {
        ImageInfo imageInspect = getDockerImage(DOCKER_IMAGE);
        Assert.assertNotNull(imageInspect.config());
    }

    @Test(groups = {"integration"})
    public void deploySample() throws IOException, InterruptedException {
        Assert.assertEquals(0, loadImage(DOCKER_IMAGE));
        Assert.assertEquals(0, deployK8s(KUBERNETES_TARGET_PATH));
        deleteK8s(KUBERNETES_TARGET_PATH);
    }

    @AfterClass
    public void cleanUp() throws KubernetesPluginException {
        KubernetesUtils.deleteDirectory(KUBERNETES_TARGET_PATH);
        KubernetesUtils.deleteDirectory(DOCKER_TARGET_PATH);
        KubernetesTestUtils.deleteDockerImage(DOCKER_IMAGE);
    }
}
