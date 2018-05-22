/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 *
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
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.Job;
import org.ballerinax.kubernetes.KubernetesConstants;
import org.ballerinax.kubernetes.exceptions.KubernetesPluginException;
import org.ballerinax.kubernetes.models.JobModel;
import org.ballerinax.kubernetes.models.KubernetesDataHolder;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Test job generation.
 */
public class KubernetesJobGeneratorTests {

    private final String jobName = "MyJOB";
    private final String selector = "TestAPP";
    private final String imageName = "SampleImage:v1.0.0";
    private final String imagePullPolicy = "Always";


    @Test
    public void testDeploymentGeneration() {
        JobModel jobModel = new JobModel();
        jobModel.setName(jobName);
        Map<String, String> labels = new HashMap<>();
        labels.put(KubernetesConstants.KUBERNETES_SELECTOR_KEY, selector);
        jobModel.setLabels(labels);
        jobModel.setImage(imageName);
        jobModel.setImagePullPolicy(imagePullPolicy);
        HashMap<String, String> env = new HashMap<>();
        env.put("ENV_VAR", "ENV");
        jobModel.setEnv(env);
        KubernetesDataHolder.getInstance().setJobModel(jobModel);

        try {
            new JobHandler().createArtifacts();
            File tempFile = new File("target" + File.separator + "kubernetes" + File.separator + "hello_job.yaml");
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
        Job job = KubernetesHelper.loadYaml(yamlFile);
        Assert.assertEquals(jobName, job.getMetadata().getName());
        Assert.assertEquals(1, job.getSpec().getTemplate().getSpec().getContainers().size());
        Container container = job.getSpec().getTemplate().getSpec().getContainers().get(0);
        Assert.assertEquals(imageName, container.getImage());
        Assert.assertEquals(imagePullPolicy, container.getImagePullPolicy());
        Assert.assertEquals(1, container.getEnv().size());
    }
}
