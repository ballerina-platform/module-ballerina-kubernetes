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

import io.fabric8.kubernetes.api.model.ConfigMap;
import org.ballerinax.kubernetes.exceptions.KubernetesPluginException;
import org.ballerinax.kubernetes.models.ConfigMapModel;
import org.ballerinax.kubernetes.models.KubernetesContext;
import org.ballerinax.kubernetes.utils.Utils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Test config map generation.
 */
public class KubernetesConfigMapGeneratorTests {

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
        Set<ConfigMapModel> configMapModels = new HashSet<>();
        configMapModels.add(configMapModel);
        KubernetesContext.getInstance().getDataHolder().addConfigMaps(configMapModels);
        try {
            new ConfigMapHandler().createArtifacts();
            File tempFile = new File("target" + File.separator + "kubernetes" + File.separator + "hello_config_map" +
                    ".yaml");
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
        ConfigMap configMap = Utils.loadYaml(yamlFile);
        Assert.assertEquals(this.configMapName, configMap.getMetadata().getName());
        Assert.assertEquals(2, configMap.getData().size());
        Assert.assertEquals("world", configMap.getData().get("hello"));
        Assert.assertEquals("test1", configMap.getData().get("test"));
    }
}
