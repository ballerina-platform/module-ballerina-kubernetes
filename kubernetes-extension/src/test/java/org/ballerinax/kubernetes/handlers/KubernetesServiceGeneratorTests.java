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

package org.ballerinax.kubernetes.handlers;

import io.fabric8.kubernetes.api.KubernetesHelper;
import io.fabric8.kubernetes.api.model.Service;
import org.ballerinax.kubernetes.KubernetesConstants;
import org.ballerinax.kubernetes.exceptions.KubernetesPluginException;
import org.ballerinax.kubernetes.models.KubernetesContext;
import org.ballerinax.kubernetes.models.ServiceModel;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Test kubernetes Service generation.
 */
public class KubernetesServiceGeneratorTests {

    private final String serviceName = "MyService";
    private final String selector = "hello";
    private final String serviceType = "NodePort";
    private final String sessionAffinity = "ClientIP";
    private final int port = 9090;

    @Test
    public void testDeploymentGeneration() {
        ServiceModel serviceModel = new ServiceModel();
        serviceModel.setName(serviceName);
        serviceModel.setPort(port);
        serviceModel.setServiceType(serviceType);
        serviceModel.setSelector(selector);
        serviceModel.setSessionAffinity(sessionAffinity);
        Map<String, String> labels = new HashMap<>();
        labels.put(KubernetesConstants.KUBERNETES_SELECTOR_KEY, selector);
        serviceModel.setLabels(labels);
        KubernetesContext.getInstance().getDataHolder().addBListenerToK8sServiceMap("HelloWorldService", serviceModel);
        try {
            new ServiceHandler().createArtifacts();
            File tempFile = new File("target" + File.separator + "kubernetes" + File.separator + "hello_svc.yaml");
            Assert.assertTrue(tempFile.exists());
            assertGeneratedYAML(tempFile);
            tempFile.deleteOnExit();
        } catch (IOException e) {
            Assert.fail("Unable to read/write service content");
        } catch (KubernetesPluginException e) {
            Assert.fail("Unable to generate yaml from service");
        }
    }

    private void assertGeneratedYAML(File yamlFile) throws IOException {
        Service service = KubernetesHelper.loadYaml(yamlFile);
        Assert.assertEquals(serviceName, service.getMetadata().getName());
        Assert.assertEquals(selector, service.getMetadata().getLabels().get(KubernetesConstants
                .KUBERNETES_SELECTOR_KEY));
        Assert.assertEquals(serviceType, service.getSpec().getType());
        Assert.assertEquals(sessionAffinity, service.getSpec().getSessionAffinity());
        Assert.assertEquals(1, service.getSpec().getPorts().size());
        Assert.assertEquals(port, service.getSpec().getPorts().get(0).getPort().intValue());

    }
}
