/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import io.fabric8.openshift.api.model.BuildConfig;
import io.fabric8.openshift.api.model.ImageStream;
import io.fabric8.openshift.api.model.Route;
import org.ballerinax.kubernetes.exceptions.KubernetesPluginException;
import org.ballerinax.kubernetes.test.utils.KubernetesTestUtils;
import org.ballerinax.kubernetes.utils.KubernetesUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.ballerinax.kubernetes.KubernetesConstants.DOCKER;
import static org.ballerinax.kubernetes.KubernetesConstants.KUBERNETES;
import static org.ballerinax.kubernetes.KubernetesConstants.OPENSHIFT;

/**
 * Test cases for sample 17.
 */
public class Sample17Test extends SampleTest {
    private static final Path SOURCE_DIR_PATH = SAMPLE_DIR.resolve("sample17");
    private static final Path DOCKER_TARGET_PATH = SOURCE_DIR_PATH.resolve(DOCKER);
    private static final Path KUBERNETES_TARGET_PATH = SOURCE_DIR_PATH.resolve(KUBERNETES);
    private BuildConfig buildConfig;
    private ImageStream imageStream;
    private Route route;
    private List<String> originalSourceContent;
    
    @BeforeClass
    public void compileSample() throws IOException, InterruptedException {
        Path sourcePath = SOURCE_DIR_PATH.resolve("hello_world_oc.bal");
        
        // save original source
        Stream<String> lines = Files.lines(sourcePath);
        this.originalSourceContent = lines.collect(Collectors.toList());
        
        // replace placeholders with mocks
        lines = Files.lines(sourcePath);
        List<String> replacedContent = lines.map(line -> line
                .replaceAll("<MINISHIFT_IP>", "192.168.99.131")
                .replaceAll("<MINISHIFT_DOCKER_REGISTRY_IP>", "172.30.1.1:5000"))
                .collect(Collectors.toList());
        Files.write(sourcePath, replacedContent);
        
        Assert.assertEquals(KubernetesTestUtils.compileBallerinaFile(SOURCE_DIR_PATH, "hello_world_oc.bal"), 0);
        File yamlFile = KUBERNETES_TARGET_PATH.resolve(OPENSHIFT).resolve("hello_world_oc.yaml").toFile();
        Assert.assertTrue(yamlFile.exists());
        List<HasMetadata> k8sItems = KubernetesTestUtils.loadYaml(yamlFile);
        for (HasMetadata data : k8sItems) {
            if ("BuildConfig".equals(data.getKind())) {
                buildConfig = (BuildConfig) data;
            } else if ("ImageStream".equals(data.getKind())) {
                imageStream = (ImageStream) data;
            } else if ("Route".equals(data.getKind())) {
                route = (Route) data;
            }
        }
    }
    
    @Test
    public void validateBuildConfig() {
        Assert.assertNotNull(buildConfig);
        Assert.assertNotNull(buildConfig.getMetadata());
        Assert.assertEquals(buildConfig.getMetadata().getName(), "hello-world-oc-openshift-bc");
        Assert.assertEquals(buildConfig.getMetadata().getLabels().size(), 1);
        Assert.assertEquals(buildConfig.getMetadata().getLabels().get("build"), "hello-world-oc-openshift-bc");
        Assert.assertEquals(buildConfig.getMetadata().getNamespace(), "bal-oc");
    
        Assert.assertNotNull(buildConfig.getSpec());
        Assert.assertNotNull(buildConfig.getSpec().getOutput());
        Assert.assertNotNull(buildConfig.getSpec().getOutput().getTo());
        Assert.assertEquals(buildConfig.getSpec().getOutput().getTo().getKind(), "ImageStreamTag",
                "Invalid output kind.");
        Assert.assertEquals(buildConfig.getSpec().getOutput().getTo().getName(),
                "hello_world_oc:latest", "Invalid image stream name.");
        Assert.assertNotNull(buildConfig.getSpec().getSource());
        Assert.assertNotNull(buildConfig.getSpec().getSource().getBinary(), "Binary source is missing");
        Assert.assertNotNull(buildConfig.getSpec().getStrategy());
        Assert.assertNotNull(buildConfig.getSpec().getStrategy().getDockerStrategy(), "Docker strategy is missing.");
        Assert.assertEquals(buildConfig.getSpec().getStrategy().getDockerStrategy().getBuildArgs().size(), 0,
                "Invalid number of build args.");
        Assert.assertEquals(buildConfig.getSpec().getStrategy().getDockerStrategy().getDockerfilePath(),
                "docker/Dockerfile", "Invalid docker path.");
        Assert.assertFalse(buildConfig.getSpec().getStrategy().getDockerStrategy().getForcePull(),
                "Force pull image set to false");
        Assert.assertFalse(buildConfig.getSpec().getStrategy().getDockerStrategy().getNoCache(),
                "No cache for image build set to false");
        
    }
    
    @Test
    public void validateImageStream() {
        Assert.assertNotNull(imageStream.getMetadata());
        Assert.assertEquals(imageStream.getMetadata().getName(), "hello_world_oc", "Invalid name found.");
        Assert.assertEquals(imageStream.getMetadata().getNamespace(), "bal-oc", "Invalid namespace found.");
        Assert.assertEquals(imageStream.getMetadata().getLabels().size(), 1, "Labels are missing");
        Assert.assertNotNull(imageStream.getMetadata().getLabels().get("build"), "'build' label is missing");
        Assert.assertEquals(imageStream.getMetadata().getLabels().get("build"), "hello-world-oc-openshift-bc",
                "Invalid label 'build' label value.");
    
        Assert.assertNull(imageStream.getSpec());
    }
    
    @Test
    public void validateRoute() {
        Assert.assertNotNull(route.getMetadata());
        Assert.assertEquals(route.getMetadata().getName(), "helloep-openshift-route",
                "Invalid name found.");
        Assert.assertEquals(route.getMetadata().getNamespace(), "bal-oc", "Namespace is missing.");
    
        Assert.assertNotNull(route.getSpec(), "Spec is missing.");
        Assert.assertEquals(route.getSpec().getHost(), "helloep-openshift-route-bal-oc.192.168.99.131.nip.io",
                "Invalid host");
        Assert.assertNotNull(route.getSpec().getPort());
        Assert.assertEquals(route.getSpec().getPort().getTargetPort().getIntVal().intValue(), 9090,
                "Invalid port found");
        Assert.assertNotNull(route.getSpec().getTo(), "To is missing.");
        Assert.assertEquals(route.getSpec().getTo().getKind(), "Service", "Kind is missing.");
        Assert.assertEquals(route.getSpec().getTo().getName(), "helloep-svc", "Service name is invalid.");
        Assert.assertEquals(route.getSpec().getTo().getWeight().intValue(), 100, "Invalid route weight.");
    }
    
    @Test
    public void validateDockerfile() {
        File dockerFile = DOCKER_TARGET_PATH.resolve("Dockerfile").toFile();
        Assert.assertTrue(dockerFile.exists());
    }
    
    @AfterClass
    public void cleanUp() throws KubernetesPluginException, IOException {
        KubernetesUtils.deleteDirectory(KUBERNETES_TARGET_PATH);
        KubernetesUtils.deleteDirectory(DOCKER_TARGET_PATH);
    
        // replace with original source
        if (null != this.originalSourceContent) {
            Path sourcePath = SOURCE_DIR_PATH.resolve("hello_world_oc.bal");
            Files.write(sourcePath, this.originalSourceContent);
        }
    }
}
