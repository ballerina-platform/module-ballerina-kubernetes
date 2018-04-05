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
import io.fabric8.kubernetes.api.model.ConfigMap;
import org.ballerinax.kubernetes.exceptions.KubernetesPluginException;
import org.ballerinax.kubernetes.models.ConfigMapModel;
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
 * Generates kubernetes Secret from annotations.
 */
public class KubernetesConfigMapGeneratorTests {

    private final Logger log = LoggerFactory.getLogger(KubernetesConfigMapGeneratorTests.class);
    private final String configMapName = "MyConfigMap";
    private final boolean readOnly = true;
    private final String mountPath = "/user/dir";

    @Test
    public void testConfigMapGenerate() {
        ConfigMapModel configMapModel = new ConfigMapModel();
        configMapModel.setName(configMapName);
        configMapModel.setReadOnly(readOnly);
        configMapModel.setMountPath(mountPath);
        Map<String, String> data = new HashMap<>();
        data.put("hello", "world");
        data.put("test", "test1");
        configMapModel.setData(data);
        try {
            String configMapContent = new ConfigMapHandler(configMapModel).generate();
            Assert.assertNotNull(configMapContent);
            File artifactLocation = new File("target/kubernetes");
            artifactLocation.mkdir();
            File tempFile = File.createTempFile("temp", configMapModel.getName() + ".yaml", artifactLocation);
            KubernetesUtils.writeToFile(configMapContent, tempFile.getPath());
            log.info("Generated YAML: \n" + configMapContent);
            Assert.assertTrue(tempFile.exists());
            assertGeneratedYAML(tempFile);
            tempFile.deleteOnExit();
        } catch (IOException e) {
            Assert.fail("Unable to write to file");
        } catch (KubernetesPluginException e) {
            Assert.fail("Unable to generate yaml from service");
        }
    }

    private void assertGeneratedYAML(File yamlFile) throws IOException {
        ConfigMap configMap = KubernetesHelper.loadYaml(yamlFile);
        Assert.assertEquals(this.configMapName, configMap.getMetadata().getName());
        Assert.assertEquals(2, configMap.getData().size());
        Assert.assertEquals("world", configMap.getData().get("hello"));
        Assert.assertEquals("test1", configMap.getData().get("test"));
    }
}
