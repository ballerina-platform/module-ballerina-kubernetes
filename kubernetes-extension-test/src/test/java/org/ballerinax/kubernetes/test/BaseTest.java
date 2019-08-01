/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinax.kubernetes.test;

import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerException;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ballerinax.kubernetes.test.samples.SampleTest;
import org.ballerinax.kubernetes.test.utils.DockerTestException;
import org.ballerinax.kubernetes.test.utils.KubernetesTestUtils;
import org.testng.Assert;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Base test class
 */
public class BaseTest {
    private static final String TAG_SEPARATOR = ":";
    private static final String BALLERINA_BASE_IMAGE = "ballerina/ballerina-runtime";

    private static final Log log = LogFactory.getLog(SampleTest.class);
    
    /**
     * Location of the ballerina docker base image.
     */
    private static final Path DOCKER_FILE = Paths.get("src", "test", "resources", "docker-base", "Dockerfile");
    
    /**
     * Location of the extracted ballerina pack.
     */
    private static final Path BALLERINA_RUNTIME_DIR = Paths.get(FilenameUtils.separatorsToSystem(
            System.getProperty("ballerina.pack"))).toAbsolutePath();
    
    /**
     * Location where the base image is copied in order to build the image.
     */
    private static final Path DOCKER_FILE_COPY = BALLERINA_RUNTIME_DIR.getParent().resolve("Dockerfile");
    
    /**
     * The name of the ballerina zip file.
     */
    private static final String BALLERINA_RUNTIME_ZIP_NAME = BALLERINA_RUNTIME_DIR.getFileName() + ".zip";
    
    /**
     * The docker base image name.
     */
    private static final String DOCKER_IMAGE = BALLERINA_BASE_IMAGE + TAG_SEPARATOR +
                                               System.getProperty("docker.image.version");
    
    
    private String builtImageID = null;
    
    @BeforeSuite
    public void buildDockerImage() throws IOException, DockerTestException, DockerException, InterruptedException {
        // copy extracted ballerina distribution to the /docker/base directory.
        FileUtils.copyFile(DOCKER_FILE.toFile(), DOCKER_FILE_COPY.toFile());
        
        // Passing build argument.
        String ballerinaDistBuildArg = "{\"BALLERINA_DIST\":\"" + BALLERINA_RUNTIME_ZIP_NAME + "\"}";
        
        CountDownLatch buildDone = new CountDownLatch(1);
        final AtomicReference<String> errorAtomicReference = new AtomicReference<>();
        builtImageID = KubernetesTestUtils.getDockerClient().build(DOCKER_FILE_COPY.getParent(), DOCKER_IMAGE,
                message -> {
                    String buildImageId = message.buildImageId();
                    String error = message.error();
                    String stream = message.stream();
                    
                    if (stream != null) {
                        log.info(stream.replaceAll("\\n", ". "));
                    }
                    
                    // when an image is built successfully.
                    if (null != buildImageId) {
                        buildDone.countDown();
                    }
                    
                    if (error != null) {
                        errorAtomicReference.set(error);
                        buildDone.countDown();
                    }
                }, DockerClient.BuildParam.noCache(),
                DockerClient.BuildParam.forceRm(),
                DockerClient.BuildParam.create("buildargs", URLEncoder.encode(ballerinaDistBuildArg,
                        Charsets.UTF_8.displayName())));
        
        buildDone.await();
        String dockerErrorMsg = errorAtomicReference.get();
        if (null != dockerErrorMsg) {
            log.error(dockerErrorMsg);
            Assert.fail();
        }
        
        log.info("Ballerina base image built: " + builtImageID);
        Assert.assertNotNull(KubernetesTestUtils.getDockerClient().inspectImage(DOCKER_IMAGE));
    }
    
    
    @AfterSuite
    public void deleteDockerImage() {
        if (null != builtImageID) {
            log.info("Removing built ballerina base image:" + builtImageID);
            KubernetesTestUtils.deleteDockerImage(DOCKER_IMAGE);
        }
    }
}
