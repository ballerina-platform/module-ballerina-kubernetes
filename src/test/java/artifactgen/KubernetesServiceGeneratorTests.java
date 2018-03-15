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

package org.ballerinalang.artifactgen;

import org.ballerinax.kubernetes.KubernetesConstants;
import org.ballerinax.kubernetes.exceptions.KubernetesPluginException;
import org.ballerinax.kubernetes.handlers.ServiceHandler;
import org.ballerinax.kubernetes.models.ServiceModel;
import org.ballerinax.kubernetes.utils.KubernetesUtils;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Generates kubernetes Service from annotations.
 */
public class KubernetesServiceGeneratorTests {

    private final Logger log = LoggerFactory.getLogger(KubernetesServiceGeneratorTests.class);

    @Test
    public void testServiceGenerate() {
        ServiceModel serviceModel = new ServiceModel();
        serviceModel.setName("MyService");
        serviceModel.setPort(9090);
        serviceModel.setServiceType("NodePort");
        serviceModel.setSelector("MyAPP");
        Map<String, String> labels = new HashMap<>();
        labels.put(KubernetesConstants.KUBERNETES_SELECTOR_KEY, "TestAPP");
        serviceModel.setLabels(labels);
        ServiceHandler kubernetesServiceGenerator = new ServiceHandler(serviceModel);
        try {
            String serviceYAML = kubernetesServiceGenerator.generate();
            Assert.assertNotNull(serviceYAML);
            File artifactLocation = new File("target/kubernetes");
            artifactLocation.mkdir();
            File tempFile = File.createTempFile("temp", serviceModel.getName() + ".yaml", artifactLocation);
            KubernetesUtils.writeToFile(serviceYAML, tempFile.getPath());
            log.info("Generated YAML: \n" + serviceYAML);
            Assert.assertTrue(tempFile.exists());
            //tempFile.deleteOnExit();
        } catch (IOException e) {
            Assert.fail("Unable to write to file");
        } catch (KubernetesPluginException e) {
            Assert.fail("Unable to generate yaml from service");
        }
    }
}
