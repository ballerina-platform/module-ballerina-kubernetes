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

import com.google.common.base.Optional;
import com.mchange.io.FileUtils;
import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerCertificates;
import com.spotify.docker.client.DockerCertificatesStore;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.ImageInfo;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.glassfish.jersey.internal.RuntimeDelegateImpl;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import javax.ws.rs.ext.RuntimeDelegate;

import static org.ballerinax.docker.generator.DockerGenConstants.UNIX_DEFAULT_DOCKER_HOST;
import static org.ballerinax.docker.generator.DockerGenConstants.WINDOWS_DEFAULT_DOCKER_HOST;

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
    public static ImageInfo getDockerImage(String imageName) throws DockerTestException, InterruptedException {
        try {
            DockerClient client = getDockerClient();
            return client.inspectImage(imageName);
        } catch (DockerException e) {
            throw new DockerTestException(e);
        }
    }
    
    /**
     * Get the list of exposed ports of the docker image.
     *
     * @param imageName The docker image name.
     * @return Exposed ports.
     * @throws DockerTestException      If issue occurs inspecting docker image
     * @throws InterruptedException If issue occurs inspecting docker image
     */
    public static List<String> getExposedPorts(String imageName) throws DockerTestException, InterruptedException {
        ImageInfo dockerImage = getDockerImage(imageName);
        return Objects.requireNonNull(dockerImage.config().exposedPorts()).asList();
    }
    
    /**
     * Get the list of commands of the docker image.
     *
     * @param imageName The docker image name.
     * @return The list of commands.
     * @throws DockerTestException      If issue occurs inspecting docker image
     * @throws InterruptedException If issue occurs inspecting docker image
     */
    public static List<String> getCommand(String imageName) throws DockerTestException, InterruptedException {
        ImageInfo dockerImage = getDockerImage(imageName);
        return dockerImage.config().cmd();
    }
    
    /**
     * Delete a given Docker image and prune
     *
     * @param imageName Docker image Name
     */
    public static void deleteDockerImage(String imageName) throws DockerTestException, InterruptedException {
        try {
            DockerClient client = getDockerClient();
            client.removeImage(imageName, true, false);
        } catch (DockerException e) {
            throw new DockerTestException(e);
        }
    }
    
    public static DockerClient getDockerClient() throws DockerTestException {
        RuntimeDelegate.setInstance(new RuntimeDelegateImpl());
        String operatingSystem = System.getProperty("os.name").toLowerCase(Locale.getDefault());
        String dockerHost = operatingSystem.contains("win") ? WINDOWS_DEFAULT_DOCKER_HOST : UNIX_DEFAULT_DOCKER_HOST;
        DockerClient dockerClient = DefaultDockerClient.builder().uri(dockerHost).build();
        try {
            String dockerCertPath = System.getenv("DOCKER_CERT_PATH");
            if (null != dockerCertPath && !"".equals(dockerCertPath)) {
                if (null != System.getenv("DOCKER_HOST")) {
                    dockerHost = System.getenv("DOCKER_HOST").replace("tcp", "https");
                }
                Optional<DockerCertificatesStore> certOptional =
                        DockerCertificates.builder()
                                .dockerCertPath(Paths.get(dockerCertPath))
                                .build();
                if (certOptional.isPresent()) {
                    dockerClient = DefaultDockerClient.builder()
                            .uri(dockerHost)
                            .dockerCertificates(certOptional.get())
                            .build();
                }
            }
        } catch (DockerCertificateException e) {
            throw new DockerTestException(e);
        }
        return dockerClient;
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
    public static int compileBallerinaFile(String sourceDirectory, String fileName, Map<String, String> envVar)
            throws InterruptedException,
            IOException {
        ProcessBuilder pb = new ProcessBuilder(BALLERINA_COMMAND, BUILD, fileName);
        log.info(COMPILING + sourceDirectory + File.separator + fileName);
        log.debug(EXECUTING_COMMAND + pb.command());
        pb.directory(new File(sourceDirectory));
        Map<String, String> environment = pb.environment();
        addJavaAgents(environment);
        environment.putAll(envVar);
        
        Process process = pb.start();
        int exitCode = process.waitFor();
        log.info(EXIT_CODE + exitCode);
        logOutput(process.getInputStream());
        logOutput(process.getErrorStream());
    
        // log ballerina-internal.log content
        Path ballerinaInternalLog = Paths.get(sourceDirectory, "ballerina-internal.log");
        if (exitCode == 1 && Files.exists(ballerinaInternalLog)) {
            log.info(FileUtils.getContentsAsString(ballerinaInternalLog.toFile()));
        }
        return exitCode;
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
        return compileBallerinaFile(sourceDirectory, fileName, new HashMap<>());
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
