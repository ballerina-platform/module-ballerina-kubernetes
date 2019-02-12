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
import io.fabric8.openshift.api.model.Route;
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
 * Test cases for @kubernetes:OpenShiftRoute{} annotation generated artifacts.
 */
public class OpenShiftRouteTest {
    private final String balDirectory = Paths.get("src").resolve("test").resolve("resources").resolve("openshift")
            .resolve("route").toAbsolutePath().toString();
    private final String targetPath = Paths.get(balDirectory).resolve(KUBERNETES).toString();
    
    /**
     * Test case openshift route with host domain.
     */
    @Test(groups = {"openshift"})
    public void noDomainTest() throws IOException, InterruptedException, KubernetesPluginException {
        Assert.assertEquals(KubernetesTestUtils.compileBallerinaFile(balDirectory, "with_domain.bal"), 0);
        File yamlFile = new File(targetPath + File.separator + "with_domain.yaml");
        Assert.assertTrue(yamlFile.exists());
        KubernetesClient client = new DefaultKubernetesClient();
        List<HasMetadata> k8sItems = client.load(new FileInputStream(yamlFile)).get();
        for (HasMetadata data : k8sItems) {
            switch (data.getKind()) {
                case "Service":
                case "Deployment":
                    break;
                case "Route":
                    Route route = (Route) data;
                    // metadata
                    Assert.assertNotNull(route.getMetadata());
                    Assert.assertEquals(route.getMetadata().getName(), "helloep-openshift-route",
                            "Invalid name found.");
                    Assert.assertEquals(route.getMetadata().getNamespace(), "ns", "Namespace is missing.");
                    
                    // spec
                    Assert.assertNotNull(route.getSpec(), "Spec is missing.");
                    Assert.assertEquals(route.getSpec().getHost(), "helloep-openshift-route-ns.abc.com",
                            "Invalid host");
                    Assert.assertNotNull(route.getSpec().getPort());
                    Assert.assertEquals(route.getSpec().getPort().getTargetPort().getIntVal().intValue(), 9090,
                            "Invalid port found");
                    Assert.assertNotNull(route.getSpec().getTo(), "To is missing.");
                    Assert.assertEquals(route.getSpec().getTo().getKind(), "Service", "Kind is missing.");
                    Assert.assertEquals(route.getSpec().getTo().getName(), "helloep-svc", "Service name is invalid.");
                    Assert.assertEquals(route.getSpec().getTo().getWeight().intValue(), 100, "Invalid route weight.");
                    
                    break;
                default:
                    Assert.fail("Unknown k8s resource found: " + data.getKind());
                    break;
            }
        }
        
        KubernetesUtils.deleteDirectory(targetPath);
    }
    
    /**
     * Test case openshift route with host.
     */
    @Test(groups = {"openshift"})
    public void simpleRoute() throws IOException, InterruptedException, KubernetesPluginException {
        Assert.assertEquals(KubernetesTestUtils.compileBallerinaFile(balDirectory, "simple_route.bal"), 0);
        File yamlFile = new File(targetPath + File.separator + "simple_route.yaml");
        Assert.assertTrue(yamlFile.exists());
        KubernetesClient client = new DefaultKubernetesClient();
        List<HasMetadata> k8sItems = client.load(new FileInputStream(yamlFile)).get();
        for (HasMetadata data : k8sItems) {
            switch (data.getKind()) {
                case "Service":
                case "Deployment":
                    break;
                case "Route":
                    Route route = (Route) data;
                    // metadata
                    Assert.assertNotNull(route.getMetadata());
                    Assert.assertEquals(route.getMetadata().getName(), "helloep-openshift-route",
                            "Invalid name found.");
                    
                    // spec
                    Assert.assertNotNull(route.getSpec(), "Spec is missing.");
                    Assert.assertEquals(route.getSpec().getHost(), "www.bxoc.com", "Invalid host");
                    Assert.assertNotNull(route.getSpec().getPort());
                    Assert.assertEquals(route.getSpec().getPort().getTargetPort().getIntVal().intValue(), 9090,
                            "Invalid port found");
                    Assert.assertNotNull(route.getSpec().getTo(), "To is missing.");
                    Assert.assertEquals(route.getSpec().getTo().getKind(), "Service", "Kind is missing.");
                    Assert.assertEquals(route.getSpec().getTo().getName(), "helloep-svc", "Service name is invalid.");
                    Assert.assertEquals(route.getSpec().getTo().getWeight().intValue(), 100, "Invalid route weight.");
                    
                    break;
                default:
                    Assert.fail("Unknown k8s resource found: " + data.getKind());
                    break;
            }
        }
        
        KubernetesUtils.deleteDirectory(targetPath);
    }
    
    /**
     * Test case to check that namespace is required.
     */
    @Test(groups = {"openshift"})
    public void noNamespace() throws IOException, InterruptedException {
        Assert.assertEquals(KubernetesTestUtils.compileBallerinaFile(balDirectory, "domain_with_no_namespace.bal"), 1);
    }
}
