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
import java.util.Map;

import static org.ballerinax.kubernetes.KubernetesConstants.UNIX_DEFAULT_DOCKER_HOST;
import static org.ballerinax.kubernetes.KubernetesConstants.WINDOWS_DEFAULT_DOCKER_HOST;

/**
 * Kubernetes test utils.
 */
public class KubernetesTestUtils {

    private static final Log log = LogFactory.getLog(KubernetesTestUtils.class);
    private static final String JAVA_OPTS = "JAVA_OPTS";
    private static final String DISTRIBUTION_PATH = System.getProperty("ballerina.pack");
    private static final String BALLERINA_COMMAND = DISTRIBUTION_PATH + File.separator + "ballerina";
    private static final String BUILD = "build";
    private static final String EXECUTING_COMMAND = "Executing command: ";
    private static final String COMPILING = "Compiling: ";
    private static final String EXIT_CODE = "Exit code: ";

    private static void logOutput(InputStream inputStream) throws IOException {
        try (
                BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))
        ) {
            br.lines().forEach(log::info);
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
        client.image().withName(imageName).delete().andPrune();
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
        ProcessBuilder pb = new ProcessBuilder(BALLERINA_COMMAND, BUILD, fileName);
        log.info(COMPILING + sourceDirectory + File.separator + fileName);
        log.debug(EXECUTING_COMMAND + pb.command());
        pb.directory(new File(sourceDirectory));
        Map<String, String> environment = pb.environment();
        addJavaAgents(environment);
        
        Process process = pb.start();
        int exitCode = process.waitFor();
        log.info(EXIT_CODE + exitCode);
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
        ProcessBuilder pb = new ProcessBuilder(BALLERINA_COMMAND, "init");
        log.info(COMPILING + sourceDirectory);
        log.debug(EXECUTING_COMMAND + pb.command());
        pb.directory(new File(sourceDirectory));
        Process process = pb.start();
        int exitCode = process.waitFor();
        log.info(EXIT_CODE + exitCode);
        logOutput(process.getInputStream());
        logOutput(process.getErrorStream());

        pb = new ProcessBuilder
                (BALLERINA_COMMAND, BUILD);
        log.debug(EXECUTING_COMMAND + pb.command());
        pb.directory(new File(sourceDirectory));
        Map<String, String> environment = pb.environment();
        addJavaAgents(environment);
        
        process = pb.start();
        exitCode = process.waitFor();
        log.info(EXIT_CODE + exitCode);
        logOutput(process.getInputStream());
        logOutput(process.getErrorStream());
        return exitCode;
    }

    // Disable fail on unknown properties using reflection to avoid docker client issue.
    // (https://github.com/fabric8io/docker-client/issues/106).
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
    
    private static synchronized void addJavaAgents(Map<String, String> envProperties) {
        String javaOpts = "";
        if (envProperties.containsKey(JAVA_OPTS)) {
            javaOpts = envProperties.get(JAVA_OPTS);
        }
        if (javaOpts.contains("jacoco.agent")) {
            return;
        }
        javaOpts = getJacocoAgentArgs() + javaOpts;
        envProperties.put(JAVA_OPTS, javaOpts);
    }
    
    private static String getJacocoAgentArgs() {
        String jacocoArgLine = System.getProperty("jacoco.agent.argLine");
        if (jacocoArgLine == null || jacocoArgLine.isEmpty()) {
            log.warn("Running integration test without jacoco test coverage");
            return "";
        }
        return jacocoArgLine + " ";
    }
}
