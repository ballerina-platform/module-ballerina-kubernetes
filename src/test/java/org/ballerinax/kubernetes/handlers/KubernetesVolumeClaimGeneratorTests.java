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
import io.fabric8.kubernetes.api.model.PersistentVolumeClaim;
import org.ballerinax.kubernetes.exceptions.KubernetesPluginException;
import org.ballerinax.kubernetes.models.PersistentVolumeClaimModel;
import org.ballerinax.kubernetes.utils.KubernetesUtils;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * Generates kubernetes Secret from annotations.
 */
public class KubernetesVolumeClaimGeneratorTests {

    private final Logger log = LoggerFactory.getLogger(KubernetesVolumeClaimGeneratorTests.class);
    private final String volumeClaimName = "MyVolumeClaim";
    private final boolean readOnly = true;
    private final String mountPath = "/user/dir";

    @Test
    public void testVolumeClaimGenerate() {
        PersistentVolumeClaimModel volumeClaimModel = new PersistentVolumeClaimModel();
        volumeClaimModel.setName(volumeClaimName);
        volumeClaimModel.setReadOnly(readOnly);
        volumeClaimModel.setMountPath(mountPath);
        volumeClaimModel.setAccessMode("ReadWriteOnce");
        try {
            String configMapContent = new PersistentVolumeClaimHandler(volumeClaimModel).generate();
            Assert.assertNotNull(configMapContent);
            File artifactLocation = new File("target/kubernetes");
            artifactLocation.mkdir();
            File tempFile = File.createTempFile("temp", volumeClaimModel.getName() + ".yaml", artifactLocation);
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
        PersistentVolumeClaim volumeClaim = KubernetesHelper.loadYaml(yamlFile);
        Assert.assertEquals(volumeClaimName, volumeClaim.getMetadata().getName());
    }
}
