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
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
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
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import static org.ballerinax.kubernetes.KubernetesConstants.DOCKER;
import static org.ballerinax.kubernetes.KubernetesConstants.KUBERNETES;
import static org.ballerinax.kubernetes.test.utils.KubernetesTestUtils.getCommand;
import static org.ballerinax.kubernetes.test.utils.KubernetesTestUtils.getExposedPorts;

public class Sample8Test implements SampleTest {

    private final String sourceDirPath = SAMPLE_DIR + File.separator + "sample8";
    private final String targetPath = sourceDirPath + File.separator + KUBERNETES;
    private final String dockerImage = "hello_world_config_map_k8s:latest";

    @BeforeClass
    public void compileSample() throws IOException, InterruptedException {
        Assert.assertEquals(KubernetesTestUtils.compileBallerinaFile(sourceDirPath, "hello_world_config_map_k8s.bal")
                , 0);
    }

    @Test
    public void validateDeployment() throws IOException {
        File deploymentYAML = new File(targetPath + File.separator + "hello_world_config_map_k8s_deployment.yaml");
        Assert.assertTrue(deploymentYAML.exists());
        Deployment deployment = KubernetesHelper.loadYaml(deploymentYAML);
        // Assert Deployment
        Assert.assertEquals("hello-world-config-map-k8s-deployment", deployment.getMetadata().getName());
        Assert.assertEquals(1, deployment.getSpec().getReplicas().intValue());
        Assert.assertEquals(3, deployment.getSpec().getTemplate().getSpec().getVolumes().size());
        Assert.assertEquals("hello_world_config_map_k8s", deployment.getMetadata().getLabels().get(KubernetesConstants
                .KUBERNETES_SELECTOR_KEY));
        Assert.assertEquals(1, deployment.getSpec().getTemplate().getSpec().getContainers().size());

        // Assert Containers
        Container container = deployment.getSpec().getTemplate().getSpec().getContainers().get(0);
        Assert.assertEquals(3, container.getVolumeMounts().size());
        Assert.assertEquals(dockerImage, container.getImage());
        Assert.assertEquals(KubernetesConstants.ImagePullPolicy.IfNotPresent.name(), container.getImagePullPolicy());
        Assert.assertEquals(1, container.getPorts().size());
        Assert.assertEquals(1, container.getEnv().size());

        //Validate config file
        Assert.assertEquals("CONFIG_FILE", container.getEnv().get(0).getName());
        Assert.assertEquals("/home/ballerina/conf/ballerina.conf", container.getEnv().get(0).getValue());
    }

    @Test
    public void validateConfigMap() throws IOException {
        File secretYAML = new File(targetPath + File.separator + "hello_world_config_map_k8s_config_map.yaml");
        Assert.assertTrue(secretYAML.exists());
        KubernetesClient client = new DefaultKubernetesClient();
        List<HasMetadata> k8sItems = client.load(new FileInputStream(secretYAML)).get();
        Assert.assertEquals(2, k8sItems.size());
        ConfigMap ballerinaConf = null;
        ConfigMap dataMap = null;
        for (HasMetadata data : k8sItems) {
            switch (data.getMetadata().getName()) {
                case "helloworld-ballerina-conf-config-map":
                    ballerinaConf = (ConfigMap) data;
                    break;
                case "helloworld-config-map":
                    dataMap = (ConfigMap) data;
                    break;
                default:
                    break;
            }
        }
        // Assert ballerina.conf config map
        Assert.assertNotNull(ballerinaConf);
        Assert.assertEquals(1, ballerinaConf.getData().size());

        // Assert Data config map
        Assert.assertNotNull(dataMap);
        Assert.assertEquals(1, dataMap.getData().size());
    }

    @Test
    public void validateDockerfile() {
        File dockerFile = new File(targetPath + File.separator + DOCKER + File.separator + "Dockerfile");
        Assert.assertTrue(dockerFile.exists());
    }

    @Test
    public void validateDockerImage() throws DockerTestException, InterruptedException {
        List<String> ports = getExposedPorts(this.dockerImage);
        Assert.assertEquals(ports.size(), 1);
        Assert.assertEquals(ports.get(0), "9090/tcp");
        // Validate ballerina.conf in run command
        Assert.assertEquals(getCommand(this.dockerImage).toString(),
                            "[/bin/sh, -c, ballerina run --config ${CONFIG_FILE} hello_world_config_map_k8s.balx]");
    }

    @AfterClass
    public void cleanUp() throws KubernetesPluginException, DockerTestException, InterruptedException {
        KubernetesUtils.deleteDirectory(targetPath);
        KubernetesTestUtils.deleteDockerImage(dockerImage);
    }

}
