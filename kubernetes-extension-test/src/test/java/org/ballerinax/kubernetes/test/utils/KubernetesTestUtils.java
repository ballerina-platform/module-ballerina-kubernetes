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

package org.ballerinax.kubernetes.test.utils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.fabric8.docker.api.model.ImageInspect;
import io.fabric8.docker.client.Config;
import io.fabric8.docker.client.ConfigBuilder;
import io.fabric8.docker.client.DockerClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.Locale;

import static org.ballerinax.kubernetes.KubernetesConstants.UNIX_DEFAULT_DOCKER_HOST;
import static org.ballerinax.kubernetes.KubernetesConstants.WINDOWS_DEFAULT_DOCKER_HOST;

/**
 * Kubernetes Integration test utils.
 */
public class KubernetesTestUtils {

    private static final Log log = LogFactory.getLog(KubernetesTestUtils.class);
    private static final String distributionPath = System.getProperty("ballerina.pack");
    private static final String ballerinaCommand = distributionPath + File.separator + "ballerina";
    private static final String buildCommand = "build";
    private static final String executing = "Executing command: ";

    private static void logOutput(InputStream inputStream) throws IOException {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(inputStream));
            br.lines().forEach(log::info);
        } finally {
            if (br != null) {
                br.close();
            }
        }
    }

    /**
     * Return a ImageInspect object for a given Docker Image name
     *
     * @param imageName Docker image Name
     * @return ImageInspect object
     */
    public static ImageInspect getDockerImage(String imageName) {
        DockerClient client = getDockerClient();
        return client.image().withName(imageName).inspect();
    }

    /**
     * Delete a given Docker image and prune
     *
     * @param imageName Docker image Name
     */
    public static void deleteDockerImage(String imageName) {
        DockerClient client = getDockerClient();
        client.image().withName(imageName).delete().andPrune(true);
    }

    private static DockerClient getDockerClient() {
        disableFailOnUnknownProperties();
        String operatingSystem = System.getProperty("os.name").toLowerCase(Locale.getDefault());
        String dockerHost = operatingSystem.contains("win") ? WINDOWS_DEFAULT_DOCKER_HOST : UNIX_DEFAULT_DOCKER_HOST;
        Config dockerClientConfig = new ConfigBuilder()
                .withDockerUrl(dockerHost)
                .build();
        return new io.fabric8.docker.client.DefaultDockerClient(dockerClientConfig);
    }

    /**
     * Compile a ballerina file in a given directory
     *
     * @param sourceDirectory Ballerina source directory
     * @param fileName        Ballerina source file name
     * @return Exit code
     * @throws InterruptedException if an error occurs while compiling
     * @throws IOException          if an error occurs while writing file
     */
    public static int compileBallerinaFile(String sourceDirectory, String fileName) throws InterruptedException,
            IOException {
        ProcessBuilder pb = new ProcessBuilder(ballerinaCommand, buildCommand, fileName);
        log.info("Compiling " + sourceDirectory);
        log.debug(executing + pb.command());
        pb.directory(new File(sourceDirectory));
        Process process = pb.start();
        int exitCode = process.waitFor();
        log.info("Exit Code:" + exitCode);
        logOutput(process.getInputStream());
        logOutput(process.getErrorStream());
        return exitCode;
    }

    /**
     * Compile a ballerina project in a given directory
     *
     * @param sourceDirectory Ballerina source directory
     * @return Exit code
     * @throws InterruptedException if an error occurs while compiling
     * @throws IOException          if an error occurs while writing file
     */
    public static int compileBallerinaProject(String sourceDirectory) throws InterruptedException,
            IOException {
        ProcessBuilder pb = new ProcessBuilder(ballerinaCommand, "init");
        log.info("Compiling " + sourceDirectory);
        log.debug(executing + pb.command());
        pb.directory(new File(sourceDirectory));
        Process process = pb.start();
        int exitCode = process.waitFor();
        log.info("Exit Code:" + exitCode);
        logOutput(process.getInputStream());
        logOutput(process.getErrorStream());

        pb = new ProcessBuilder
                (ballerinaCommand, buildCommand);
        log.debug(executing + pb.command());
        pb.directory(new File(sourceDirectory));
        process = pb.start();
        exitCode = process.waitFor();
        log.info("Exit Code:" + exitCode);
        logOutput(process.getInputStream());
        logOutput(process.getErrorStream());
        return exitCode;
    }

    private static void disableFailOnUnknownProperties() {
        try {
            final Field jsonMapperField = Config.class.getDeclaredField("JSON_MAPPER");
            assert jsonMapperField != null;
            jsonMapperField.setAccessible(true);
            final ObjectMapper objectMapper = (ObjectMapper) jsonMapperField.get(null);
            assert objectMapper != null;
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        } catch (NoSuchFieldException | IllegalAccessException ignored) {
        }
    }


}
