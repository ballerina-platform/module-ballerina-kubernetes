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

import io.fabric8.kubernetes.api.model.Secret;
import org.ballerinax.kubernetes.exceptions.KubernetesPluginException;
import org.ballerinax.kubernetes.models.SecretModel;
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
 * Test kubernetes Secret generation.
 */
public class KubernetesSecretGeneratorTests extends HandlerTestSuite {

    private final String secretName = "MySecret";
    private final boolean readOnly = true;
    private final String mountPath = "/user/dir";

    @Test
    public void testSecretGenerate() {
        SecretModel secretModel = new SecretModel();
        secretModel.setName(secretName);
        secretModel.setReadOnly(readOnly);
        secretModel.setMountPath(mountPath);
        Map<String, String> data = new HashMap<>();
        data.put("hello", "world");
        data.put("test", "test1");
        secretModel.setData(data);
        Set<SecretModel> secretModels = new HashSet<>();
        secretModels.add(secretModel);
        dataHolder.addSecrets(secretModels);
        try {
            new SecretHandler().createArtifacts();
            File tempFile = dataHolder.getK8sArtifactOutputPath().resolve("hello_secret.yaml").toFile();
            Assert.assertTrue(tempFile.exists());
            assertGeneratedYAML(tempFile);
            tempFile.deleteOnExit();
        } catch (KubernetesPluginException e) {
            Assert.fail("Unable to generate yaml from service");
        } catch (IOException e) {
            Assert.fail("Unable to read secret  from yaml");
        }
    }

    private void assertGeneratedYAML(File yamlFile) throws IOException {
        Secret secret = Utils.loadYaml(yamlFile);
        Assert.assertEquals(secretName, secret.getMetadata().getName());
        Assert.assertEquals(2, secret.getData().size());
        Assert.assertEquals("world", secret.getData().get("hello"));
        Assert.assertEquals("test1", secret.getData().get("test"));
    }
}
