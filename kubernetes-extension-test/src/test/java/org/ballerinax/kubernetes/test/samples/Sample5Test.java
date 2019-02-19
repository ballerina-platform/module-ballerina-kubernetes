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

package org.ballerinax.kubernetes.test.samples;

import io.fabric8.kubernetes.api.KubernetesHelper;
import io.fabric8.kubernetes.api.model.HorizontalPodAutoscaler;
import org.ballerinax.kubernetes.KubernetesConstants;
import org.ballerinax.kubernetes.exceptions.KubernetesPluginException;
import org.ballerinax.kubernetes.test.utils.DockerTestException;
import org.ballerinax.kubernetes.test.utils.KubernetesTestUtils;
import org.ballerinax.kubernetes.utils.KubernetesUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.ballerinax.kubernetes.KubernetesConstants.DOCKER;
import static org.ballerinax.kubernetes.KubernetesConstants.KUBERNETES;
import static org.ballerinax.kubernetes.test.utils.KubernetesTestUtils.getExposedPorts;

public class Sample5Test implements SampleTest {
    
    private final String sourceDirPath = SAMPLE_DIR + File.separator + "sample5";
    private final String targetPath = sourceDirPath + File.separator + KUBERNETES;
    private final String dockerImage = "ballerina.com/pizzashack:2.1.0";
    
    @BeforeClass
    public void compileSample() throws IOException, InterruptedException {
        Assert.assertEquals(KubernetesTestUtils.compileBallerinaFile(sourceDirPath, "pizzashack.bal"), 0);
    }
    
    @Test
    public void validateDockerfile() {
        File dockerFile = new File(targetPath + File.separator + DOCKER + File.separator + "Dockerfile");
        Assert.assertTrue(dockerFile.exists());
    }
    
    @Test
    public void validateDockerImage() throws DockerTestException, InterruptedException {
        List<String> ports = getExposedPorts(this.dockerImage);
        Assert.assertEquals(ports.size(), 2);
        Assert.assertEquals(ports.get(0), "9090/tcp");
        Assert.assertEquals(ports.get(1), "9095/tcp");
    }
    
    @Test
    public void validatePodAutoscaler() throws IOException {
        File hpaYAML = new File(targetPath + File.separator + "pizzashack_hpa.yaml");
        Assert.assertTrue(hpaYAML.exists());
        HorizontalPodAutoscaler podAutoscaler = KubernetesHelper.loadYaml(hpaYAML);
        Assert.assertEquals(podAutoscaler.getMetadata().getName(), "pizzashack-hpa");
        Assert.assertEquals(podAutoscaler.getMetadata().getLabels().get(KubernetesConstants
                .KUBERNETES_SELECTOR_KEY), "pizzashack");
        Assert.assertEquals(podAutoscaler.getSpec().getMaxReplicas().intValue(), 2);
        Assert.assertEquals(podAutoscaler.getSpec().getMinReplicas().intValue(), 1);
        Assert.assertEquals(podAutoscaler.getSpec().getMetrics().size(), 1, "CPU metric is missing.");
        Assert.assertEquals(podAutoscaler.getSpec().getMetrics().get(0).getResource().getName(), "cpu",
                "Invalid resource name.");
        Assert.assertEquals(podAutoscaler.getSpec().getMetrics().get(0).getResource().getTargetAverageUtilization()
                .intValue(), 50);
        Assert.assertEquals(podAutoscaler.getSpec().getScaleTargetRef().getName(), "pizzashack-deployment");
    }
    
    @AfterClass
    public void cleanUp() throws KubernetesPluginException, DockerTestException, InterruptedException {
        KubernetesUtils.deleteDirectory(targetPath);
        KubernetesTestUtils.deleteDockerImage(dockerImage);
    }
}
