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

import io.fabric8.kubernetes.api.model.PersistentVolumeClaim;
import org.ballerinax.kubernetes.exceptions.KubernetesPluginException;
import org.ballerinax.kubernetes.models.PersistentVolumeClaimModel;
import org.ballerinax.kubernetes.utils.Utils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Test kubernetes volume claim generation.
 */
public class KubernetesVolumeClaimGeneratorTests extends HandlerTestSuite {

    private final String volumeClaimName = "MyVolumeClaim";
    private final boolean readOnly = true;
    private final String mountPath = "/user/dir";
    private final String annotationKey = "volume.beta.kubernetes.io/storage-class";
    private final String annotationValue = "efs";

    @Test
    public void testVolumeClaimGenerate() {
        PersistentVolumeClaimModel volumeClaimModel = new PersistentVolumeClaimModel();
        volumeClaimModel.setName(volumeClaimName);
        volumeClaimModel.setReadOnly(readOnly);
        volumeClaimModel.setMountPath(mountPath);
        volumeClaimModel.setAccessMode("ReadWriteOnce");
        volumeClaimModel.setVolumeClaimSizeAmount("200");
        volumeClaimModel.setVolumeClaimSizeFormat("Mi");
        HashMap<String, String> annotations = new HashMap<>();
        annotations.put(annotationKey, annotationValue);
        volumeClaimModel.setAnnotations(annotations);
        Set<PersistentVolumeClaimModel> claimModles = new HashSet<>();
        claimModles.add(volumeClaimModel);
        dataHolder.addPersistentVolumeClaims(claimModles);
        try {
            new PersistentVolumeClaimHandler().createArtifacts();
            File tempFile = dataHolder.getK8sArtifactOutputPath().resolve("hello_volume_claim.yaml").toFile();
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
        PersistentVolumeClaim volumeClaim = Utils.loadYaml(yamlFile);
        Assert.assertEquals(volumeClaimName, volumeClaim.getMetadata().getName());
        Assert.assertEquals(1, volumeClaim.getMetadata().getAnnotations().size());
        Assert.assertTrue(volumeClaim.getMetadata().getAnnotations()
                .containsKey(annotationKey));
        Assert.assertEquals(annotationValue, volumeClaim.getMetadata().getAnnotations()
                .get(annotationKey));
    }
}
