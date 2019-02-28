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

package org.ballerinax.kubernetes.test;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.openshift.api.model.BuildConfig;
import io.fabric8.openshift.api.model.ImageStream;
import org.apache.commons.io.FileUtils;
import org.ballerinax.kubernetes.exceptions.KubernetesPluginException;
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

import static org.ballerinax.kubernetes.KubernetesConstants.KUBERNETES;

/**
 * Test cases for @kubernetes:OpenShiftBuildConfig{} annotation generated artifacts.
 */
public class OpenShiftBuildConfigTest {
    private final String balDirectory = Paths.get("src").resolve("test").resolve("resources").resolve("openshift")
            .resolve("build-config").toAbsolutePath().toString();
    private final String targetPath = Paths.get(balDirectory).resolve(KUBERNETES).toString();
    
    /**
     * Test case openshift build config annotation with default values.
     */
    @Test(groups = {"openshift"})
    public void simpleBuildConfigTest() throws IOException, InterruptedException, KubernetesPluginException {
        Assert.assertEquals(KubernetesTestUtils.compileBallerinaFile(balDirectory, "simple_bc.bal"), 0);
        File yamlFile = new File(targetPath + File.separator + "simple_bc.yaml");
        Assert.assertTrue(yamlFile.exists());
        KubernetesClient client = new DefaultKubernetesClient();
        List<HasMetadata> k8sItems = client.load(new FileInputStream(yamlFile)).get();
        for (HasMetadata data : k8sItems) {
            switch (data.getKind()) {
                case "Service":
                case "Deployment":
                    break;
                case "BuildConfig":
                    BuildConfig bc = (BuildConfig) data;
                    // metadata
                    Assert.assertNotNull(bc.getMetadata());
                    Assert.assertEquals(bc.getMetadata().getName(), "helloep-openshift-bc", "Invalid name found.");
                    Assert.assertNotNull(bc.getMetadata().getLabels(), "Labels are missing");
                    Assert.assertEquals(bc.getMetadata().getLabels().size(), 1, "Labels are missing");
                    Assert.assertNotNull(bc.getMetadata().getLabels().get("build"), "'build' label is missing");
                    Assert.assertEquals(bc.getMetadata().getLabels().get("build"), "helloep-openshift-bc",
                            "Invalid label 'build' label value.");
                    
                    // spec
                    Assert.assertNotNull(bc.getSpec());
                    Assert.assertNotNull(bc.getSpec().getOutput());
                    Assert.assertNotNull(bc.getSpec().getOutput().getTo());
                    Assert.assertEquals(bc.getSpec().getOutput().getTo().getKind(), "ImageStreamTag",
                            "Invalid output kind.");
                    Assert.assertEquals(bc.getSpec().getOutput().getTo().getName(), "simple_bc:latest",
                            "Invalid image stream name.");
                    Assert.assertNotNull(bc.getSpec().getSource());
                    Assert.assertNotNull(bc.getSpec().getSource().getBinary(), "Binary source is missing");
                    Assert.assertNotNull(bc.getSpec().getStrategy());
                    Assert.assertNotNull(bc.getSpec().getStrategy().getDockerStrategy(), "Docker strategy is missing.");
                    Assert.assertEquals(bc.getSpec().getStrategy().getDockerStrategy().getBuildArgs().size(), 0,
                            "Invalid number of build args.");
                    Assert.assertEquals(bc.getSpec().getStrategy().getDockerStrategy().getDockerfilePath(),
                            "kubernetes/docker/Dockerfile", "Invalid docker path.");
                    Assert.assertFalse(bc.getSpec().getStrategy().getDockerStrategy().getForcePull(),
                            "Force pull image set to false");
                    Assert.assertFalse(bc.getSpec().getStrategy().getDockerStrategy().getNoCache(),
                            "No cache for image build set to false");
                    
                    break;
                case "ImageStream":
                    ImageStream is = (ImageStream) data;
                    Assert.assertNotNull(is.getMetadata());
                    Assert.assertEquals(is.getMetadata().getName(), "simple_bc", "Invalid name found.");
                    Assert.assertEquals(is.getMetadata().getLabels().size(), 1, "Labels are missing");
                    Assert.assertNotNull(is.getMetadata().getLabels().get("build"), "'build' label is missing");
                    Assert.assertEquals(is.getMetadata().getLabels().get("build"), "helloep-openshift-bc",
                            "Invalid label 'build' label value.");
    
                    Assert.assertNull(is.getSpec());
                    break;
                default:
                    Assert.fail("Unknown k8s resource found: " + data.getKind());
                    break;
            }
        }
    
        KubernetesUtils.deleteDirectory(targetPath);
    }
    
    /**
     * Test case openshift build config annotation with image stream generation disabled.
     */
    @Test(groups = {"openshift"})
    public void noImageStreamTest() throws IOException, InterruptedException, KubernetesPluginException {
        Assert.assertEquals(KubernetesTestUtils.compileBallerinaFile(balDirectory, "no_image_stream.bal"), 0);
        File yamlFile = new File(targetPath + File.separator + "no_image_stream.yaml");
        Assert.assertTrue(yamlFile.exists());
        KubernetesClient client = new DefaultKubernetesClient();
        List<HasMetadata> k8sItems = client.load(new FileInputStream(yamlFile)).get();
        for (HasMetadata data : k8sItems) {
            switch (data.getKind()) {
                case "Service":
                case "Deployment":
                    break;
                case "BuildConfig":
                    BuildConfig bc = (BuildConfig) data;
                    // metadata
                    Assert.assertNotNull(bc.getMetadata());
                    Assert.assertEquals(bc.getMetadata().getName(), "helloep-openshift-bc", "Invalid name found.");
                    Assert.assertNotNull(bc.getMetadata().getLabels(), "Labels are missing");
                    Assert.assertEquals(bc.getMetadata().getLabels().size(), 1, "Labels are missing");
                    Assert.assertNotNull(bc.getMetadata().getLabels().get("build"), "'build' label is missing");
                    Assert.assertEquals(bc.getMetadata().getLabels().get("build"), "helloep-openshift-bc",
                            "Invalid label 'build' label value.");
                    
                    // spec
                    Assert.assertNotNull(bc.getSpec());
                    Assert.assertNotNull(bc.getSpec().getOutput());
                    Assert.assertNotNull(bc.getSpec().getOutput().getTo());
                    Assert.assertEquals(bc.getSpec().getOutput().getTo().getKind(), "ImageStreamTag",
                            "Invalid output kind.");
                    Assert.assertEquals(bc.getSpec().getOutput().getTo().getName(), "no_image_stream:latest",
                            "Invalid image stream name.");
                    Assert.assertNotNull(bc.getSpec().getSource());
                    Assert.assertNotNull(bc.getSpec().getSource().getBinary(), "Binary source is missing");
                    Assert.assertNotNull(bc.getSpec().getStrategy());
                    Assert.assertNotNull(bc.getSpec().getStrategy().getDockerStrategy(), "Docker strategy is missing.");
                    Assert.assertEquals(bc.getSpec().getStrategy().getDockerStrategy().getBuildArgs().size(), 0,
                            "Invalid number of build args.");
                    Assert.assertEquals(bc.getSpec().getStrategy().getDockerStrategy().getDockerfilePath(),
                            "kubernetes/docker/Dockerfile", "Invalid docker path.");
                    Assert.assertFalse(bc.getSpec().getStrategy().getDockerStrategy().getForcePull(),
                            "Force pull image set to false");
                    Assert.assertFalse(bc.getSpec().getStrategy().getDockerStrategy().getNoCache(),
                            "No cache for image build set to false");
                    
                    break;
                default:
                    Assert.fail("Unknown k8s resource found: " + data.getKind());
                    break;
            }
        }
        
        KubernetesUtils.deleteDirectory(targetPath);
    }
    
    /**
     * Test case openshift build config annotation with namespace.
     */
    @Test(groups = {"openshift"})
    public void withNamespaceTest() throws IOException, InterruptedException, KubernetesPluginException {
        Assert.assertEquals(KubernetesTestUtils.compileBallerinaFile(balDirectory, "namespace_openshift.bal"), 0);
        File yamlFile = new File(targetPath + File.separator + "namespace_openshift.yaml");
        Assert.assertTrue(yamlFile.exists());
        KubernetesClient client = new DefaultKubernetesClient();
        List<HasMetadata> k8sItems = client.load(new FileInputStream(yamlFile)).get();
        for (HasMetadata data : k8sItems) {
            switch (data.getKind()) {
                case "Service":
                case "Deployment":
                    break;
                case "BuildConfig":
                    BuildConfig bc = (BuildConfig) data;
                    // metadata
                    Assert.assertNotNull(bc.getMetadata());
                    Assert.assertEquals(bc.getMetadata().getName(), "helloep-openshift-bc", "Invalid name found.");
                    Assert.assertEquals(bc.getMetadata().getNamespace(), "ns", "Invalid namespace.");
                    Assert.assertNotNull(bc.getMetadata().getLabels(), "Labels are missing");
                    Assert.assertEquals(bc.getMetadata().getLabels().size(), 1, "Labels are missing");
                    Assert.assertNotNull(bc.getMetadata().getLabels().get("build"), "'build' label is missing");
                    Assert.assertEquals(bc.getMetadata().getLabels().get("build"), "helloep-openshift-bc",
                            "Invalid label 'build' label value.");
                    
                    // spec
                    Assert.assertNotNull(bc.getSpec());
                    Assert.assertNotNull(bc.getSpec().getOutput());
                    Assert.assertNotNull(bc.getSpec().getOutput().getTo());
                    Assert.assertEquals(bc.getSpec().getOutput().getTo().getKind(), "ImageStreamTag",
                            "Invalid output kind.");
                    Assert.assertEquals(bc.getSpec().getOutput().getTo().getName(), "namespace_openshift:latest",
                            "Invalid image stream name.");
                    Assert.assertNotNull(bc.getSpec().getSource());
                    Assert.assertNotNull(bc.getSpec().getSource().getBinary(), "Binary source is missing");
                    Assert.assertNotNull(bc.getSpec().getStrategy());
                    Assert.assertNotNull(bc.getSpec().getStrategy().getDockerStrategy(), "Docker strategy is missing.");
                    Assert.assertEquals(bc.getSpec().getStrategy().getDockerStrategy().getBuildArgs().size(), 0,
                            "Invalid number of build args.");
                    Assert.assertEquals(bc.getSpec().getStrategy().getDockerStrategy().getDockerfilePath(),
                            "kubernetes/docker/Dockerfile", "Invalid docker path.");
                    Assert.assertFalse(bc.getSpec().getStrategy().getDockerStrategy().getForcePull(),
                            "Force pull image set to false");
                    Assert.assertFalse(bc.getSpec().getStrategy().getDockerStrategy().getNoCache(),
                            "No cache for image build set to false");
                    
                    break;
                case "ImageStream":
                    ImageStream is = (ImageStream) data;
                    Assert.assertNotNull(is.getMetadata());
                    Assert.assertEquals(is.getMetadata().getName(), "namespace_openshift", "Invalid name found.");
                    Assert.assertEquals(is.getMetadata().getNamespace(), "ns", "Invalid namespace.");
                    Assert.assertEquals(is.getMetadata().getLabels().size(), 1, "Labels are missing");
                    Assert.assertNotNull(is.getMetadata().getLabels().get("build"), "'build' label is missing");
                    Assert.assertEquals(is.getMetadata().getLabels().get("build"), "helloep-openshift-bc",
                            "Invalid label 'build' label value.");
                    
                    Assert.assertNull(is.getSpec());
                    break;
                default:
                    Assert.fail("Unknown k8s resource found: " + data.getKind());
                    break;
            }
        }
        
        KubernetesUtils.deleteDirectory(targetPath);
    }
    
    /**
     * Test case openshift build config annotation with multiple build configs in the same module/file.
     */
    @Test(groups = {"openshift"})
    public void multipleBuildAnnotations() throws IOException, InterruptedException {
        Assert.assertEquals(KubernetesTestUtils.compileBallerinaFile(balDirectory, "multiple_build_annotations.bal"),
                1);
    }
    
    /**
     * Test case openshift build config annotation with a main function.
     */
    @Test(groups = {"openshift"})
    public void mainFunctionTest() throws IOException, InterruptedException, KubernetesPluginException {
        Assert.assertEquals(KubernetesTestUtils.compileBallerinaFile(balDirectory, "main_function.bal"), 0);
        File yamlFile = new File(targetPath + File.separator + "main_function.yaml");
        Assert.assertTrue(yamlFile.exists());
        KubernetesClient client = new DefaultKubernetesClient();
        List<HasMetadata> k8sItems = client.load(new FileInputStream(yamlFile)).get();
        for (HasMetadata data : k8sItems) {
            switch (data.getKind()) {
                case "Service":
                case "Deployment":
                    break;
                case "BuildConfig":
                    BuildConfig bc = (BuildConfig) data;
                    // metadata
                    Assert.assertNotNull(bc.getMetadata());
                    Assert.assertEquals(bc.getMetadata().getName(), "main-openshift-bc", "Invalid name found.");
                    Assert.assertNotNull(bc.getMetadata().getLabels(), "Labels are missing");
                    Assert.assertEquals(bc.getMetadata().getLabels().size(), 1, "Labels are missing");
                    Assert.assertNotNull(bc.getMetadata().getLabels().get("build"), "'build' label is missing");
                    Assert.assertEquals(bc.getMetadata().getLabels().get("build"), "main-openshift-bc",
                            "Invalid label 'build' label value.");
                    
                    // spec
                    Assert.assertNotNull(bc.getSpec());
                    Assert.assertNotNull(bc.getSpec().getOutput());
                    Assert.assertNotNull(bc.getSpec().getOutput().getTo());
                    Assert.assertEquals(bc.getSpec().getOutput().getTo().getKind(), "ImageStreamTag",
                            "Invalid output kind.");
                    Assert.assertEquals(bc.getSpec().getOutput().getTo().getName(), "main_function:latest",
                            "Invalid image stream name.");
                    Assert.assertNotNull(bc.getSpec().getSource());
                    Assert.assertNotNull(bc.getSpec().getSource().getBinary(), "Binary source is missing");
                    Assert.assertNotNull(bc.getSpec().getStrategy());
                    Assert.assertNotNull(bc.getSpec().getStrategy().getDockerStrategy(), "Docker strategy is missing.");
                    Assert.assertEquals(bc.getSpec().getStrategy().getDockerStrategy().getBuildArgs().size(), 0,
                            "Invalid number of build args.");
                    Assert.assertEquals(bc.getSpec().getStrategy().getDockerStrategy().getDockerfilePath(),
                            "kubernetes/docker/Dockerfile", "Invalid docker path.");
                    Assert.assertFalse(bc.getSpec().getStrategy().getDockerStrategy().getForcePull(),
                            "Force pull image set to false");
                    Assert.assertFalse(bc.getSpec().getStrategy().getDockerStrategy().getNoCache(),
                            "No cache for image build set to false");
                    
                    break;
                case "ImageStream":
                    ImageStream is = (ImageStream) data;
                    Assert.assertNotNull(is.getMetadata());
                    Assert.assertEquals(is.getMetadata().getName(), "main_function", "Invalid name found.");
                    Assert.assertEquals(is.getMetadata().getLabels().size(), 1, "Labels are missing");
                    Assert.assertNotNull(is.getMetadata().getLabels().get("build"), "'build' label is missing");
                    Assert.assertEquals(is.getMetadata().getLabels().get("build"), "main-openshift-bc",
                            "Invalid label 'build' label value.");
                    
                    Assert.assertNull(is.getSpec());
                    break;
                default:
                    Assert.fail("Unknown k8s resource found: " + data.getKind());
                    break;
            }
        }
        
        KubernetesUtils.deleteDirectory(targetPath);
    }
    
    /**
     * Test case openshift build config annotation with force pull and caching disabled when docker image building.
     */
    @Test(groups = {"openshift"})
    public void noCacheAndForcePullTest() throws IOException, InterruptedException, KubernetesPluginException {
        Assert.assertEquals(KubernetesTestUtils.compileBallerinaFile(balDirectory, "cache_and_force_pull.bal"), 0);
        File yamlFile = new File(targetPath + File.separator + "cache_and_force_pull.yaml");
        Assert.assertTrue(yamlFile.exists());
        KubernetesClient client = new DefaultKubernetesClient();
        List<HasMetadata> k8sItems = client.load(new FileInputStream(yamlFile)).get();
        for (HasMetadata data : k8sItems) {
            switch (data.getKind()) {
                case "Service":
                case "Deployment":
                    break;
                case "BuildConfig":
                    BuildConfig bc = (BuildConfig) data;
                    // metadata
                    Assert.assertNotNull(bc.getMetadata());
                    Assert.assertEquals(bc.getMetadata().getName(), "helloep-openshift-bc", "Invalid name found.");
                    Assert.assertNotNull(bc.getMetadata().getLabels(), "Labels are missing");
                    Assert.assertEquals(bc.getMetadata().getLabels().size(), 1, "Labels are missing");
                    Assert.assertNotNull(bc.getMetadata().getLabels().get("build"), "'build' label is missing");
                    Assert.assertEquals(bc.getMetadata().getLabels().get("build"), "helloep-openshift-bc",
                            "Invalid label 'build' label value.");
                    
                    // spec
                    Assert.assertNotNull(bc.getSpec());
                    Assert.assertNotNull(bc.getSpec().getOutput());
                    Assert.assertNotNull(bc.getSpec().getOutput().getTo());
                    Assert.assertEquals(bc.getSpec().getOutput().getTo().getKind(), "ImageStreamTag",
                            "Invalid output kind.");
                    Assert.assertEquals(bc.getSpec().getOutput().getTo().getName(), "cache_and_force_pull:latest",
                            "Invalid image stream name.");
                    Assert.assertNotNull(bc.getSpec().getSource());
                    Assert.assertNotNull(bc.getSpec().getSource().getBinary(), "Binary source is missing");
                    Assert.assertNotNull(bc.getSpec().getStrategy());
                    Assert.assertNotNull(bc.getSpec().getStrategy().getDockerStrategy(), "Docker strategy is missing.");
                    Assert.assertEquals(bc.getSpec().getStrategy().getDockerStrategy().getBuildArgs().size(), 0,
                            "Invalid number of build args.");
                    Assert.assertEquals(bc.getSpec().getStrategy().getDockerStrategy().getDockerfilePath(),
                            "kubernetes/docker/Dockerfile", "Invalid docker path.");
                    Assert.assertTrue(bc.getSpec().getStrategy().getDockerStrategy().getForcePull(),
                            "Force pull image set to false");
                    Assert.assertTrue(bc.getSpec().getStrategy().getDockerStrategy().getNoCache(),
                            "No cache for image build set to false");
                    
                    break;
                case "ImageStream":
                    ImageStream is = (ImageStream) data;
                    Assert.assertNotNull(is.getMetadata());
                    Assert.assertEquals(is.getMetadata().getName(), "cache_and_force_pull", "Invalid name found.");
                    Assert.assertEquals(is.getMetadata().getLabels().size(), 1, "Labels are missing");
                    Assert.assertNotNull(is.getMetadata().getLabels().get("build"), "'build' label is missing");
                    Assert.assertEquals(is.getMetadata().getLabels().get("build"), "helloep-openshift-bc",
                            "Invalid label 'build' label value.");
                    
                    Assert.assertNull(is.getSpec());
                    break;
                default:
                    Assert.fail("Unknown k8s resource found: " + data.getKind());
                    break;
            }
        }
        
        KubernetesUtils.deleteDirectory(targetPath);
    }
    
    /**
     * Test case openshift build config annotation with a service.
     */
    @Test(groups = {"openshift"})
    public void serviceAnnotationTest() throws IOException, InterruptedException, KubernetesPluginException {
        Assert.assertEquals(KubernetesTestUtils.compileBallerinaFile(balDirectory, "annotation_on_service.bal"), 0);
        File yamlFile = new File(targetPath + File.separator + "annotation_on_service.yaml");
        Assert.assertTrue(yamlFile.exists());
        KubernetesClient client = new DefaultKubernetesClient();
        List<HasMetadata> k8sItems = client.load(new FileInputStream(yamlFile)).get();
        for (HasMetadata data : k8sItems) {
            switch (data.getKind()) {
                case "Service":
                case "Deployment":
                    break;
                case "BuildConfig":
                    BuildConfig bc = (BuildConfig) data;
                    // metadata
                    Assert.assertNotNull(bc.getMetadata());
                    Assert.assertEquals(bc.getMetadata().getName(), "helloworld-openshift-bc", "Invalid name found.");
                    Assert.assertNotNull(bc.getMetadata().getLabels(), "Labels are missing");
                    Assert.assertEquals(bc.getMetadata().getLabels().size(), 1, "Labels are missing");
                    Assert.assertNotNull(bc.getMetadata().getLabels().get("build"), "'build' label is missing");
                    Assert.assertEquals(bc.getMetadata().getLabels().get("build"), "helloworld-openshift-bc",
                            "Invalid label 'build' label value.");
                    
                    // spec
                    Assert.assertNotNull(bc.getSpec());
                    Assert.assertNotNull(bc.getSpec().getOutput());
                    Assert.assertNotNull(bc.getSpec().getOutput().getTo());
                    Assert.assertEquals(bc.getSpec().getOutput().getTo().getKind(), "ImageStreamTag",
                            "Invalid output kind.");
                    Assert.assertEquals(bc.getSpec().getOutput().getTo().getName(), "annotation_on_service:latest",
                            "Invalid image stream name.");
                    Assert.assertNotNull(bc.getSpec().getSource());
                    Assert.assertNotNull(bc.getSpec().getSource().getBinary(), "Binary source is missing");
                    Assert.assertNotNull(bc.getSpec().getStrategy());
                    Assert.assertNotNull(bc.getSpec().getStrategy().getDockerStrategy(), "Docker strategy is missing.");
                    Assert.assertEquals(bc.getSpec().getStrategy().getDockerStrategy().getBuildArgs().size(), 0,
                            "Invalid number of build args.");
                    Assert.assertEquals(bc.getSpec().getStrategy().getDockerStrategy().getDockerfilePath(),
                            "kubernetes/docker/Dockerfile", "Invalid docker path.");
                    Assert.assertFalse(bc.getSpec().getStrategy().getDockerStrategy().getForcePull(),
                            "Force pull image set to false");
                    Assert.assertFalse(bc.getSpec().getStrategy().getDockerStrategy().getNoCache(),
                            "No cache for image build set to false");
                    
                    break;
                case "ImageStream":
                    ImageStream is = (ImageStream) data;
                    Assert.assertNotNull(is.getMetadata());
                    Assert.assertEquals(is.getMetadata().getName(), "annotation_on_service", "Invalid name found.");
                    Assert.assertEquals(is.getMetadata().getLabels().size(), 1, "Labels are missing");
                    Assert.assertNotNull(is.getMetadata().getLabels().get("build"), "'build' label is missing");
                    Assert.assertEquals(is.getMetadata().getLabels().get("build"), "helloworld-openshift-bc",
                            "Invalid label 'build' label value.");
                    
                    Assert.assertNull(is.getSpec());
                    break;
                default:
                    Assert.fail("Unknown k8s resource found: " + data.getKind());
                    break;
            }
        }
        
        KubernetesUtils.deleteDirectory(targetPath);
    }
    
    /**
     * Test case openshift build config annotation with a ballerina project.
     */
    @Test(groups = {"openshift"})
    public void buildProject() throws IOException, InterruptedException, KubernetesPluginException {
        Path projectPath = Paths.get(balDirectory).resolve("print-project");
        Path targetPath = projectPath.resolve("target");
        Path projectTarget = targetPath.resolve("kubernetes").resolve("printer");
        
        FileUtils.deleteQuietly(targetPath.toFile());
    
        Assert.assertEquals(KubernetesTestUtils.compileBallerinaProject(projectPath.toAbsolutePath()), 0);
        File yamlFile = targetPath.resolve(projectTarget).resolve("printer.yaml").toAbsolutePath().toFile();
        Assert.assertTrue(yamlFile.exists());
        KubernetesClient client = new DefaultKubernetesClient();
        List<HasMetadata> k8sItems = client.load(new FileInputStream(yamlFile)).get();
        for (HasMetadata data : k8sItems) {
            switch (data.getKind()) {
                case "Service":
                case "Deployment":
                    break;
                case "BuildConfig":
                    BuildConfig bc = (BuildConfig) data;
                    // metadata
                    Assert.assertNotNull(bc.getMetadata());
                    Assert.assertEquals(bc.getMetadata().getName(), "printep-openshift-bc", "Invalid name found.");
                    Assert.assertNotNull(bc.getMetadata().getLabels(), "Labels are missing");
                    Assert.assertEquals(bc.getMetadata().getLabels().size(), 1, "Labels are missing");
                    Assert.assertNotNull(bc.getMetadata().getLabels().get("build"), "'build' label is missing");
                    Assert.assertEquals(bc.getMetadata().getLabels().get("build"), "printep-openshift-bc",
                            "Invalid label 'build' label value.");
                    
                    // spec
                    Assert.assertNotNull(bc.getSpec());
                    Assert.assertNotNull(bc.getSpec().getOutput());
                    Assert.assertNotNull(bc.getSpec().getOutput().getTo());
                    Assert.assertEquals(bc.getSpec().getOutput().getTo().getKind(), "ImageStreamTag",
                            "Invalid output kind.");
                    Assert.assertEquals(bc.getSpec().getOutput().getTo().getName(), "printer:latest",
                            "Invalid image stream name.");
                    Assert.assertNotNull(bc.getSpec().getSource());
                    Assert.assertNotNull(bc.getSpec().getSource().getBinary(), "Binary source is missing");
                    Assert.assertNotNull(bc.getSpec().getStrategy());
                    Assert.assertNotNull(bc.getSpec().getStrategy().getDockerStrategy(), "Docker strategy is missing.");
                    Assert.assertEquals(bc.getSpec().getStrategy().getDockerStrategy().getBuildArgs().size(), 0,
                            "Invalid number of build args.");
                    Assert.assertEquals(bc.getSpec().getStrategy().getDockerStrategy().getDockerfilePath(),
                            "kubernetes/docker/Dockerfile", "Invalid docker path.");
                    Assert.assertFalse(bc.getSpec().getStrategy().getDockerStrategy().getForcePull(),
                            "Force pull image set to false");
                    Assert.assertFalse(bc.getSpec().getStrategy().getDockerStrategy().getNoCache(),
                            "No cache for image build set to false");
                    
                    break;
                case "ImageStream":
                    ImageStream is = (ImageStream) data;
                    Assert.assertNotNull(is.getMetadata());
                    Assert.assertEquals(is.getMetadata().getName(), "printer", "Invalid name found.");
                    Assert.assertEquals(is.getMetadata().getLabels().size(), 1, "Labels are missing");
                    Assert.assertNotNull(is.getMetadata().getLabels().get("build"), "'build' label is missing");
                    Assert.assertEquals(is.getMetadata().getLabels().get("build"), "printep-openshift-bc",
                            "Invalid label 'build' label value.");
                    
                    Assert.assertNull(is.getSpec());
                    break;
                default:
                    Assert.fail("Unknown k8s resource found: " + data.getKind());
                    break;
            }
        }
        
        KubernetesUtils.deleteDirectory(targetPath);
    }
}
