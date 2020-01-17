/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerCertificates;
import com.spotify.docker.client.DockerCertificatesStore;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.DockerHost;
import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.ImageInfo;
import io.fabric8.knative.serving.v1alpha1.Service;
import io.fabric8.knative.serving.v1alpha1.ServiceBuilder;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ResourceHandler;
import io.fabric8.kubernetes.client.Watch;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.utils.Serialization;
import okhttp3.OkHttpClient;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.ballerinax.kubernetes.test.samples.SampleTest;
import org.glassfish.jersey.internal.RuntimeDelegateImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import javax.ws.rs.ext.RuntimeDelegate;

public class KnativeTestUtils {

    private static final Logger log = LoggerFactory.getLogger(SampleTest.class);
    private static final String JAVA_OPTS = "JAVA_OPTS";
    private static final Path DISTRIBUTION_PATH = Paths.get(FilenameUtils.separatorsToSystem(
            System.getProperty("ballerina.pack")));
    private static final String BALLERINA_COMMAND = DISTRIBUTION_PATH +
            File.separator + "bin" +
            File.separator +
            (System.getProperty("os.name").toLowerCase(Locale.getDefault())
                    .contains("win") ? "ballerina.bat" : "ballerina");
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
     * @throws DockerTestException  If issue occurs inspecting docker image
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
     * @throws DockerTestException  If issue occurs inspecting docker image
     * @throws InterruptedException If issue occurs inspecting docker image
     */
    public static List<String> getCommand(String imageName) throws DockerTestException, InterruptedException {
        ImageInfo dockerImage = getDockerImage(imageName);
        return dockerImage.config().cmd();
    }

    /**
     * Delete a given Docker image and prune.
     *
     * @param imageName Docker image Name
     */
    public static void deleteDockerImage(String imageName) {
        // using a thread to make tests run faster
        CompletableFuture.runAsync(() -> {
            try {
                DockerClient client = getDockerClient();
                client.removeImage(imageName, true, false);
            } catch (DockerException | InterruptedException | DockerTestException e) {
                log.error(e.getMessage());
            }
        });
    }

    public static DockerClient getDockerClient() throws DockerTestException {
        RuntimeDelegate.setInstance(new RuntimeDelegateImpl());
        URI dockerURI = DockerHost.fromEnv().uri();
        DockerClient dockerClient = DefaultDockerClient.builder().uri(dockerURI).build();

        try {
            String dockerCertPath = DockerHost.fromEnv().dockerCertPath();
            if (null != dockerCertPath && !"".equals(dockerCertPath)) {
                Optional<DockerCertificatesStore> certOptional =
                        DockerCertificates.builder()
                                .dockerCertPath(Paths.get(dockerCertPath))
                                .build();
                if (certOptional.isPresent()) {
                    dockerClient = DefaultDockerClient.builder()
                            .uri(dockerURI)
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
     * Compile a ballerina file in a given directory.
     *
     * @param sourceDirectory Ballerina source directory
     * @param fileName        Ballerina source file name
     * @return Exit code
     * @throws InterruptedException if an error occurs while compiling
     * @throws IOException          if an error occurs while writing file
     */
    public static int compileBallerinaFile(Path sourceDirectory, String fileName, Map<String, String> envVar)
            throws InterruptedException,
            IOException {
        Path ballerinaInternalLog = Paths.get(sourceDirectory.toAbsolutePath().toString(), "ballerina-internal.log");
        if (ballerinaInternalLog.toFile().exists()) {
            log.warn("Deleting already existing ballerina-internal.log file.");
            FileUtils.deleteQuietly(ballerinaInternalLog.toFile());
        }

        ProcessBuilder pb = new ProcessBuilder(BALLERINA_COMMAND, BUILD, fileName);
        log.info(COMPILING + sourceDirectory.resolve(fileName).normalize());
        log.debug(EXECUTING_COMMAND + pb.command());
        pb.directory(sourceDirectory.toFile());
        Map<String, String> environment = pb.environment();
        addJavaAgents(environment);
        environment.putAll(envVar);

        Process process = pb.start();
        int exitCode = process.waitFor();
        log.info(EXIT_CODE + exitCode);
        logOutput(process.getInputStream());
        logOutput(process.getErrorStream());

        // log ballerina-internal.log content
        if (Files.exists(ballerinaInternalLog)) {
            log.error("ballerina-internal.log file found. content: ");
            log.error(FileUtils.readFileToString(ballerinaInternalLog.toFile(), Charset.defaultCharset()));
        }
        return exitCode;
    }

    /**
     * Compile a ballerina file in a given directory.
     *
     * @param sourceDirectory Ballerina source directory
     * @param fileName        Ballerina source file name
     * @return Exit code
     * @throws InterruptedException if an error occurs while compiling
     * @throws IOException          if an error occurs while writing file
     */
    public static int compileBallerinaFile(Path sourceDirectory, String fileName) throws InterruptedException,
            IOException {
        return compileBallerinaFile(sourceDirectory, fileName, new HashMap<>());
    }

    /**
     * Compile a ballerina project in a given directory.
     *
     * @param sourceDirectory Ballerina source directory
     * @return Exit code
     * @throws InterruptedException if an error occurs while compiling
     * @throws IOException          if an error occurs while writing file
     */
    public static int compileBallerinaProject(Path sourceDirectory, boolean skipTests) throws InterruptedException,
            IOException {
        Path ballerinaInternalLog = Paths.get(sourceDirectory.toAbsolutePath().toString(), "ballerina-internal.log");
        if (ballerinaInternalLog.toFile().exists()) {
            log.warn("Deleting already existing ballerina-internal.log file.");
            FileUtils.deleteQuietly(ballerinaInternalLog.toFile());
        }

        ProcessBuilder pb;
        if (skipTests) {
            pb = new ProcessBuilder(BALLERINA_COMMAND, BUILD, "-a", "--skip-tests");
        } else {
            pb = new ProcessBuilder(BALLERINA_COMMAND, BUILD, "-a");
        }

        log.info(COMPILING + sourceDirectory.normalize());
        log.debug(EXECUTING_COMMAND + pb.command());
        pb.directory(sourceDirectory.toFile());
        Map<String, String> environment = pb.environment();
        addJavaAgents(environment);

        Process process = pb.start();
        int exitCode = process.waitFor();
        log.info(EXIT_CODE + exitCode);
        logOutput(process.getInputStream());
        logOutput(process.getErrorStream());

        // log ballerina-internal.log content
        if (Files.exists(ballerinaInternalLog)) {
            log.info("ballerina-internal.log file found. content: ");
            log.info(FileUtils.readFileToString(ballerinaInternalLog.toFile(), Charset.defaultCharset()));
        }

        return exitCode;
    }

    /**
     * Compile a ballerina project in a given directory.
     *
     * @param sourceDirectory Ballerina source directory
     * @return Exit code
     * @throws InterruptedException if an error occurs while compiling
     * @throws IOException          if an error occurs while writing file
     */
    public static int compileBallerinaProject(Path sourceDirectory) throws InterruptedException, IOException {
        return compileBallerinaProject(sourceDirectory, false);
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

    /**
     * Load YAML files to kubernetes resource(s).
     *
     * @param file The path of the file.
     * @param <T>  The type reference of the artifact.
     * @return The refered type.
     * @throws IOException When yaml file could not be loaded.
     */
    public static <T> T loadYaml(File file) throws IOException {
        FileInputStream fileInputStream = FileUtils.openInputStream(file);
        return Serialization.unmarshal(fileInputStream, Collections.emptyMap());
    }
    
    /**
     * Knative service handler for parsing yamls.
     */
    public static class ServiceHandler implements ResourceHandler<Service, ServiceBuilder> {

        @Override
        public String getKind() {
            return "Service";
        }

        @Override
        public String getApiVersion() {
            return "serving.knative.dev/v1alpha1";
        }

        @Override
        public Service create(OkHttpClient okHttpClient, Config config, String s, Service service) {
            return null;
        }

        @Override
        public Service replace(OkHttpClient okHttpClient, Config config, String s, Service service) {
            return null;
        }

        @Override
        public Service reload(OkHttpClient okHttpClient, Config config, String s, Service service) {
            return null;
        }

        @Override
        public ServiceBuilder edit(Service service) {
            return new ServiceBuilder(service);
        }

        @Override
        public Boolean delete(OkHttpClient okHttpClient, Config config, String s, Boolean aBoolean, Service service) {
            return null;
        }

        @Override
        public Watch watch(OkHttpClient okHttpClient, Config config, String s, Service service,
                           Watcher<Service> watcher) {
            return null;
        }

        @Override
        public Watch watch(OkHttpClient okHttpClient, Config config, String s, Service service, String s1,
                           Watcher<Service> watcher) {
            return null;
        }

        @Override
        public Service waitUntilReady(OkHttpClient okHttpClient, Config config, String s, Service service, long l,
                                      TimeUnit timeUnit) {
            return null;
        }

        @Override
        public Service waitUntilCondition(OkHttpClient okHttpClient, Config config, String s, Service service,
                                          Predicate<Service> predicate, long l, TimeUnit timeUnit) {
            return null;
        }
    }
}
