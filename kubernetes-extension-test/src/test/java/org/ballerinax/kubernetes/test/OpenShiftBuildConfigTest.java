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
import org.ballerinax.kubernetes.exceptions.KubernetesPluginException;
import org.ballerinax.kubernetes.test.utils.KubernetesTestUtils;
import org.ballerinax.kubernetes.utils.KubernetesUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

import static org.ballerinax.kubernetes.KubernetesConstants.KUBERNETES;

/**
 * Test cases for OpenShift Build Configs generation.
 */
public class OpenShiftBuildConfigTest {
    private final String balDirectory = Paths.get("src").resolve("test").resolve("resources").resolve("openshift")
            .resolve("build-config").toAbsolutePath().toString();
    private final String targetPath = Paths.get(balDirectory).resolve(KUBERNETES).toString();
    
    /**
     * Validate generated service yaml.
     */
    @Test(groups = "openshift")
    public void simpleBuildConfig() throws IOException, InterruptedException, KubernetesPluginException {
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
}
