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

import org.ballerinax.kubernetes.exceptions.KubernetesPluginException;
import org.ballerinax.kubernetes.test.utils.DockerTestException;
import org.ballerinax.kubernetes.test.utils.KubernetesTestUtils;
import org.ballerinax.kubernetes.utils.KubernetesUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.ballerinax.kubernetes.KubernetesConstants.DOCKER;
import static org.ballerinax.kubernetes.KubernetesConstants.KUBERNETES;
import static org.ballerinax.kubernetes.test.utils.KubernetesTestUtils.getExposedPorts;

public class Sample13Test implements SampleTest {

    private static final Path SOURCE_DIR_PATH = SAMPLE_DIR.resolve("sample13");
    private static final Path TARGET_PATH = SOURCE_DIR_PATH.resolve("target").resolve(KUBERNETES);
    private static final Path COOL_DRINK_PKG_TARGET_PATH = TARGET_PATH.resolve("cool_drink");
    private static final Path DRINK_STORE_PKG_TARGET_PATH = TARGET_PATH.resolve("drink_store");
    private static final Path HOT_DRINK_PKG_TARGET_PATH = TARGET_PATH.resolve("hot_drink");
    private static final String COOL_DRINK_DOCKER_IMAGE = "cool_drink:latest";
    private static final String DRINK_STORE_DOCKER_IMAGE = "drink_store:latest";
    private static final String HOT_DRINK_DOCKER_IMAGE = "hot_drink:latest";

    @BeforeClass
    public void compileSample() throws IOException, InterruptedException {
        Assert.assertEquals(KubernetesTestUtils.compileBallerinaProject(SOURCE_DIR_PATH), 0);
    }

    @Test
    public void validateDockerfile() {
        Assert.assertTrue(COOL_DRINK_PKG_TARGET_PATH.resolve(DOCKER).resolve("Dockerfile").toFile().exists());
        Assert.assertTrue(DRINK_STORE_PKG_TARGET_PATH.resolve(DOCKER).resolve("Dockerfile").toFile().exists());
        Assert.assertTrue(HOT_DRINK_PKG_TARGET_PATH.resolve(DOCKER).resolve("Dockerfile").toFile().exists());
    }

    @Test
    public void validateDockerImageCoolDrink() throws DockerTestException, InterruptedException {
        List<String> ports = getExposedPorts(COOL_DRINK_DOCKER_IMAGE);
        Assert.assertEquals(ports.size(), 1);
        Assert.assertEquals(ports.get(0), "9090/tcp");
    }

    @Test
    public void validateDockerImageDrinkStore() throws DockerTestException, InterruptedException {
        List<String> ports = getExposedPorts(DRINK_STORE_DOCKER_IMAGE);
        Assert.assertEquals(ports.size(), 1);
        Assert.assertEquals(ports.get(0), "9091/tcp");
    }
    
    @Test
    public void validateDockerImageHotDrink() throws DockerTestException, InterruptedException {
        List<String> ports = getExposedPorts(HOT_DRINK_DOCKER_IMAGE);
        Assert.assertEquals(ports.size(), 1);
        Assert.assertEquals(ports.get(0), "9090/tcp");
    }

    @AfterClass
    public void cleanUp() throws KubernetesPluginException, DockerTestException, InterruptedException {
        KubernetesUtils.deleteDirectory(TARGET_PATH);
        KubernetesTestUtils.deleteDockerImage(DRINK_STORE_DOCKER_IMAGE);
        KubernetesTestUtils.deleteDockerImage(COOL_DRINK_DOCKER_IMAGE);
        KubernetesTestUtils.deleteDockerImage(HOT_DRINK_DOCKER_IMAGE);
    }
}
