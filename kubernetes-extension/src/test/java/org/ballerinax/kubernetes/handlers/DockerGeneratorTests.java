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

import org.ballerinax.docker.generator.models.DockerModel;
import org.ballerinax.kubernetes.exceptions.KubernetesPluginException;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.util.HashSet;
import java.util.Set;


/**
 * Docker generator tests.
 */
public class DockerGeneratorTests extends HandlerTestSuite {

    @Test
    public void testDockerGenerate() throws KubernetesPluginException {
        DockerModel dockerModel = new DockerModel();
        Set<Integer> ports = new HashSet<>();
        ports.add(9090);
        ports.add(9091);
        ports.add(9092);
        dockerModel.setPorts(ports);
        dockerModel.setUberJar(true);
        dockerModel.setService(true);
        dockerModel.setJarFileName("hello.jar");
        dockerModel.setEnableDebug(true);
        dockerModel.setBaseImage("ballerina/ballerina-runtime" + ":latest");
        dockerModel.setDebugPort(5005);
        dockerModel.setBuildImage(false);
        dataHolder.setDockerModel(dockerModel);
        new DockerHandler().createArtifacts();
        File dockerfile = dataHolder.getDockerArtifactOutputPath().resolve("Dockerfile").toFile();
        Assert.assertTrue(dockerfile.exists());
        dockerfile.deleteOnExit();
    }
}
