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
import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerCertificates;
import com.spotify.docker.client.DockerCertificatesStore;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.DockerHost;
import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.ImageInfo;
import io.fabric8.kubernetes.client.utils.Serialization;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.DnsResolver;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.http.impl.conn.SystemDefaultDnsResolver;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.glassfish.jersey.internal.RuntimeDelegateImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import javax.ws.rs.ext.RuntimeDelegate;
/**
 * Kubernetes test utils.
 */
public class KubernetesTestUtils {
    private static final Logger log = LoggerFactory.getLogger(KubernetesTestUtils.class);
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
    private static final String KUBECTL = "kubectl";

    private static void logOutput(InputStream inputStream) throws IOException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
            br.lines().forEach(log::info);
        }
    }

    /**
     * Return a ImageInspect object for a given Docker Image name.
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
            throws InterruptedException, IOException {
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
     * Deploys k8s artifacts in a given directory
     *
     * @param sourceDirectory K8s artifacts directory
     * @return Exit code
     * @throws InterruptedException if an error occurs while compiling
     * @throws IOException          if an error occurs while writing file
     */
    public static int deployK8s(Path sourceDirectory) throws InterruptedException, IOException {
        ProcessBuilder pb = new ProcessBuilder(KUBECTL, "apply", "-f", sourceDirectory
                .toAbsolutePath().toString());
        log.info("Deploying artifacts: " + sourceDirectory.normalize());
        log.debug(EXECUTING_COMMAND + pb.command());
        pb.directory(sourceDirectory.toFile());
        Process process = pb.start();
        int exitCode = process.waitFor();
        logOutput(process.getInputStream());
        logOutput(process.getErrorStream());
        Thread.sleep(10000);
        log.info("Deployment " + EXIT_CODE + exitCode);
        return exitCode;
    }

    /**
     * Load docker image to kind
     *
     * @param dockerImage Docker image tag to be exposed
     * @return Exit code
     * @throws InterruptedException if an error occurs while compiling
     * @throws IOException          if an error occurs while writing file
     */
    public static int loadImage(String dockerImage) throws InterruptedException, IOException {
        ProcessBuilder pb = new ProcessBuilder("kind", "load", "docker-image", dockerImage);
        log.info("Loading docker image: " + dockerImage);
        log.debug(EXECUTING_COMMAND + pb.command());
        Process process = pb.start();
        int exitCode = process.waitFor();
        logOutput(process.getInputStream());
        logOutput(process.getErrorStream());
        log.info("Docker image loading " + EXIT_CODE + exitCode);
        return exitCode;
    }

    /**
     * Execute k8s command
     *
     * @param args kubectl arguments
     * @return Exit code
     * @throws InterruptedException if an error occurs while compiling
     * @throws IOException          if an error occurs while writing file
     */
    public static int executeK8sCommand(String... args) throws InterruptedException, IOException {
        List<String> command = new ArrayList<>();
        command.add(KUBECTL);
        command.addAll(Arrays.asList(args));
        ProcessBuilder pb = new ProcessBuilder(command);
        log.debug(EXECUTING_COMMAND + pb.command());
        Process process = pb.start();
        int exitCode = process.waitFor();
        logOutput(process.getInputStream());
        logOutput(process.getErrorStream());
        log.info(EXIT_CODE + exitCode);
        return exitCode;
    }


    /**
     * Send a request to URL and validate the message.
     *
     * @param url     Service URL
     * @param message expected Response message
     * @return true if the message contains expected message
     * @throws IOException if unable to connect to service
     */
    public static boolean readFromURL(String url, String message) throws IOException {
        // Custom DNS resolver
        DnsResolver dnsResolver = new SystemDefaultDnsResolver() {
            @Override
            public InetAddress[] resolve(final String host) throws UnknownHostException {
                if (host.equalsIgnoreCase("abc.com") ||
                        host.equalsIgnoreCase("pizza.com") ||
                        host.equalsIgnoreCase("pizzashack.com") ||
                        host.equalsIgnoreCase("burger.com")) {
                    // If host is matching return the IP address we want, not what is in DNS
                    return new InetAddress[]{InetAddress.getByName("172.17.0.2")};
                } else {
                    // Else, resolve from the DNS
                    return super.resolve(host);
                }
            }
        };

        // HttpClientConnectionManager allows us to use custom DnsResolver
        BasicHttpClientConnectionManager connManager = null;
        try {
            connManager = new BasicHttpClientConnectionManager(
                    // create a SocketFactory with trusting all the certs and ignoring certificate mismatch.
                    RegistryBuilder.<ConnectionSocketFactory>create()
                            .register("http", PlainConnectionSocketFactory.getSocketFactory())
                            .register("https", new SSLConnectionSocketFactory(new SSLContextBuilder()
                                    .loadTrustMaterial(null, (x509CertChain, authType) -> true)
                                    .build(),
                                    NoopHostnameVerifier.INSTANCE))
                            .build(),
                    null, // Default ConnectionFactory
                    null, // Default SchemePortResolver
                    dnsResolver  // Custom DnsResolver
            );
        } catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException e) {
            log.error("error occurred while accession URL: " + url, e);
        }

        // build HttpClient that will use our DnsResolver
        HttpClient httpClient = HttpClientBuilder.create()
                .setConnectionManager(connManager)
                .build();

        HttpGet httpRequest = new HttpGet(url);
        HttpResponse httpResponse = httpClient.execute(httpRequest);
        HttpEntity entity = httpResponse.getEntity();
        if (entity != null) {
            String result = EntityUtils.toString(entity);
            log.info("Response from service: " + result);
            return result.contains(message);
        }
        return false;
    }

    /**
     * Delete k8s artifacts from k8s cluster in a given directory
     *
     * @param sourceDirectory K8s artifacts directory
     * @return Exit code
     * @throws InterruptedException if an error occurs while compiling
     * @throws IOException          if an error occurs while writing file
     */
    public static int deleteK8s(Path sourceDirectory) throws InterruptedException, IOException {
        ProcessBuilder pb = new ProcessBuilder(KUBECTL, "delete", "-f", sourceDirectory
                .toAbsolutePath().toString());
        log.info("Deleting resources" + sourceDirectory.normalize());
        log.debug(EXECUTING_COMMAND + pb.command());
        pb.directory(sourceDirectory.toFile());
        Process process = pb.start();
        int exitCode = process.waitFor();
        log.info(EXIT_CODE + exitCode);
        logOutput(process.getInputStream());
        logOutput(process.getErrorStream());
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
}
