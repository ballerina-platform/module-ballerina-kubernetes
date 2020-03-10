/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.batch.Job;
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
 * Generate docker artifacts without @kubernetes annotations.
 */
public class NoAnnotationsTest {
    private static final Path BAL_DIRECTORY = Paths.get("src", "test", "resources", "deployment", "no-annotations");
    private static final Path DOCKER_TARGET_PATH = BAL_DIRECTORY.resolve(DOCKER);
    private static final Path KUBERNETES_TARGET_PATH = BAL_DIRECTORY.resolve(KUBERNETES);
    
    @Test(timeOut = 90000)
    public void serviceWithNoAnnotationTest() throws IOException, InterruptedException, DockerTestException,
            KubernetesPluginException {
        Assert.assertEquals(KubernetesTestUtils.compileBallerinaFile(BAL_DIRECTORY, "no_annotation_service.bal"), 0);
        File yamlFile = KUBERNETES_TARGET_PATH.resolve("no_annotation_service.yaml").toFile();
        Assert.assertTrue(yamlFile.exists());
        KubernetesClient client = new DefaultKubernetesClient();
        List<HasMetadata> k8sItems = client.load(new FileInputStream(yamlFile)).get();
        for (HasMetadata data : k8sItems) {
            if ("Service".equals(data.getKind())) {
                Service service = (Service) data;
                Assert.assertEquals(service.getMetadata().getName(), "helloworld-svc");
                Assert.assertEquals(service.getMetadata().getLabels().get(KubernetesConstants
                        .KUBERNETES_SELECTOR_KEY), "no_annotation_service");
                Assert.assertEquals(service.getSpec().getType(), KubernetesConstants.ServiceType.ClusterIP.name());
                Assert.assertEquals(service.getSpec().getPorts().size(), 1);
                Assert.assertEquals(service.getSpec().getPorts().get(0).getPort().intValue(), 9090);
            }
            
            if ("Deployment".equals(data.getKind())) {
                Deployment deployment = (Deployment) data;
                Assert.assertEquals(deployment.getMetadata().getName(), "no-annotation-service-deployment");
                Assert.assertEquals(deployment.getSpec().getReplicas().intValue(), 1);
                Assert.assertEquals(deployment.getMetadata().getLabels().get(KubernetesConstants
                        .KUBERNETES_SELECTOR_KEY), "no_annotation_service");
                Assert.assertEquals(deployment.getSpec().getTemplate().getSpec().getContainers().size(), 1);
                Container container = deployment.getSpec().getTemplate().getSpec().getContainers().get(0);
                Assert.assertEquals(container.getImage(), "no_annotation_service:latest");
                Assert.assertEquals(container.getImagePullPolicy(),
                        KubernetesConstants.ImagePullPolicy.IfNotPresent.name());
                Assert.assertEquals(container.getPorts().size(), 1);
                Assert.assertEquals(container.getEnv().size(), 0);
            }
        }
    
        validateDockerfile();
        validateDockerImage("no_annotation_service:latest");
    
        KubernetesUtils.deleteDirectory(KUBERNETES_TARGET_PATH);
        KubernetesUtils.deleteDirectory(DOCKER_TARGET_PATH);
        KubernetesTestUtils.deleteDockerImage("no_annotation_service:latest");
    }
    
    @Test(timeOut = 90000)
    public void listenerWithNoAnnotationTest() throws IOException, InterruptedException, DockerTestException,
            KubernetesPluginException {
        Assert.assertEquals(KubernetesTestUtils.compileBallerinaFile(BAL_DIRECTORY, "no_annotation_listener.bal"), 0);
        File yamlFile = KUBERNETES_TARGET_PATH.resolve("no_annotation_listener.yaml").toFile();
        Assert.assertTrue(yamlFile.exists());
        KubernetesClient client = new DefaultKubernetesClient();
        List<HasMetadata> k8sItems = client.load(new FileInputStream(yamlFile)).get();
        for (HasMetadata data : k8sItems) {
            if ("Service".equals(data.getKind())) {
                Service service = (Service) data;
                Assert.assertEquals(service.getMetadata().getName(), "helloworldep-svc");
                Assert.assertEquals(service.getMetadata().getLabels().get(KubernetesConstants
                        .KUBERNETES_SELECTOR_KEY), "no_annotation_listener");
                Assert.assertEquals(service.getSpec().getType(), KubernetesConstants.ServiceType.ClusterIP.name());
                Assert.assertEquals(service.getSpec().getPorts().size(), 1);
                Assert.assertEquals(service.getSpec().getPorts().get(0).getPort().intValue(), 9090);
            }
            
            if ("Deployment".equals(data.getKind())) {
                Deployment deployment = (Deployment) data;
                Assert.assertEquals(deployment.getMetadata().getName(), "no-annotation-listener-deployment");
                Assert.assertEquals(deployment.getSpec().getReplicas().intValue(), 1);
                Assert.assertEquals(deployment.getMetadata().getLabels().get(KubernetesConstants
                        .KUBERNETES_SELECTOR_KEY), "no_annotation_listener");
                Assert.assertEquals(deployment.getSpec().getTemplate().getSpec().getContainers().size(), 1);
                Container container = deployment.getSpec().getTemplate().getSpec().getContainers().get(0);
                Assert.assertEquals(container.getImage(), "no_annotation_listener:latest");
                Assert.assertEquals(container.getImagePullPolicy(),
                        KubernetesConstants.ImagePullPolicy.IfNotPresent.name());
                Assert.assertEquals(container.getPorts().size(), 1);
                Assert.assertEquals(container.getEnv().size(), 0);
            }
        }
        
        validateDockerfile();
        validateDockerImage("no_annotation_listener:latest");
        
        KubernetesUtils.deleteDirectory(KUBERNETES_TARGET_PATH);
        KubernetesUtils.deleteDirectory(DOCKER_TARGET_PATH);
        KubernetesTestUtils.deleteDockerImage("no_annotation_listener:latest");
    }
    
    @Test(timeOut = 90000)
    public void mainWithNoAnnotationTest() throws IOException, InterruptedException, DockerTestException,
            KubernetesPluginException {
        Assert.assertEquals(KubernetesTestUtils.compileBallerinaFile(BAL_DIRECTORY, "no_annotations_main.bal"), 0);
        File yamlFile = KUBERNETES_TARGET_PATH.resolve("no_annotations_main.yaml").toFile();
        Assert.assertTrue(yamlFile.exists());
        KubernetesClient client = new DefaultKubernetesClient();
        List<HasMetadata> k8sItems = client.load(new FileInputStream(yamlFile)).get();
        for (HasMetadata data : k8sItems) {
            if ("Job".equals(data.getKind())) {
                Job job = (Job) data;
                Assert.assertEquals(job.getMetadata().getName(), "no-annotations-main-job");
                Assert.assertEquals(job.getSpec().getTemplate().getSpec().getContainers().size(), 1);
    
                Container container = job.getSpec().getTemplate().getSpec().getContainers().get(0);
                Assert.assertEquals(container.getImage(), "no_annotations_main:latest");
                Assert.assertEquals(container.getImagePullPolicy(),
                        KubernetesConstants.ImagePullPolicy.IfNotPresent.name());
                Assert.assertEquals(job.getSpec().getTemplate().getSpec()
                        .getRestartPolicy(), KubernetesConstants.RestartPolicy.Never.name());
            }
        }
        
        validateDockerfile();
        
        KubernetesUtils.deleteDirectory(KUBERNETES_TARGET_PATH);
        KubernetesUtils.deleteDirectory(DOCKER_TARGET_PATH);
        KubernetesTestUtils.deleteDockerImage("no_annotations_main:latest");
    }
    
    public void validateDockerfile() {
        File dockerFile = DOCKER_TARGET_PATH.resolve("Dockerfile").toFile();
        Assert.assertTrue(dockerFile.exists());
    }
    
    public void validateDockerImage(String dockerImage) throws DockerTestException, InterruptedException {
        List<String> ports = getExposedPorts(dockerImage);
        Assert.assertEquals(ports.size(), 1);
        Assert.assertEquals(ports.get(0), "9090/tcp");
    }
}
