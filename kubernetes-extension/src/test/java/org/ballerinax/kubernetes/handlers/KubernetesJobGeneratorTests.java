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

import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.batch.Job;
import org.ballerinax.kubernetes.KubernetesConstants;
import org.ballerinax.kubernetes.exceptions.KubernetesPluginException;
import org.ballerinax.kubernetes.utils.Utils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Test job generation.
 */
public class KubernetesJobGeneratorTests extends HandlerTestSuite {

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
        jobModel.setSingleYAML(false);
        Map<String, EnvVarValueModel> env = new HashMap<>();
        EnvVarValueModel testEnvVar = new EnvVarValueModel("ENV");
        env.put("ENV_VAR", testEnvVar);
        jobModel.setEnv(env);
        KubernetesContext.getInstance().getDataHolder().setJobModel(jobModel);

        try {
            new JobHandler().createArtifacts();
            File tempFile = Paths.get("target", "kubernetes", module.name.toString(), "hello_job.yaml").toFile();
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
        Job job = Utils.loadYaml(yamlFile);
        Assert.assertEquals(jobName, job.getMetadata().getName());
        Assert.assertEquals(1, job.getSpec().getTemplate().getSpec().getContainers().size());
        Container container = job.getSpec().getTemplate().getSpec().getContainers().get(0);
        Assert.assertEquals(imageName, container.getImage());
        Assert.assertEquals(imagePullPolicy, container.getImagePullPolicy());
        Assert.assertEquals(1, container.getEnv().size());
    }
}
