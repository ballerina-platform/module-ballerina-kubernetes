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

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.fabric8.docker.api.model.AuthConfig;
import io.fabric8.docker.api.model.AuthConfigBuilder;
import io.fabric8.docker.client.Config;
import io.fabric8.docker.client.ConfigBuilder;
import io.fabric8.docker.client.DefaultDockerClient;
import io.fabric8.docker.client.DockerClient;
import io.fabric8.docker.client.utils.RegistryUtils;
import io.fabric8.docker.dsl.EventListener;
import io.fabric8.docker.dsl.OutputHandle;
import org.ballerinax.kubernetes.exceptions.KubernetesPluginException;
import org.ballerinax.kubernetes.models.DockerModel;
import org.ballerinax.kubernetes.models.ExternalFileModel;
import org.ballerinax.kubernetes.utils.KubernetesUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.CountDownLatch;

import static org.ballerinax.kubernetes.KubernetesConstants.BALX;
import static org.ballerinax.kubernetes.KubernetesConstants.DOCKER;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.copyFileOrDirectory;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.extractBalxName;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.printDebug;

/**
 * Generates Docker artifacts from annotations.
 */
public class DockerHandler extends AbstractArtifactHandler {

    private final CountDownLatch pushDone = new CountDownLatch(1);
    private final CountDownLatch buildDone = new CountDownLatch(1);
    private DockerModel dockerModel;

    private static void disableFailOnUnknownProperties() {
        // Disable fail on unknown properties using reflection to avoid docker client issue.
        // (https://github.com/fabric8io/docker-client/issues/106).
        final Field jsonMapperField;
        try {
            jsonMapperField = Config.class.getDeclaredField("JSON_MAPPER");
            assert jsonMapperField != null;
            jsonMapperField.setAccessible(true);
            final ObjectMapper objectMapper = (ObjectMapper) jsonMapperField.get(null);
            assert objectMapper != null;
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        } catch (NoSuchFieldException | IllegalAccessException ignored) {
        }
    }

    /**
     * Write content to a File. Create the required directories if they don't not exists.
     *
     * @param context        context of the file
     * @param outputFileName target file path
     * @throws IOException If an error occurs when writing to a file
     */
    private static void writeDockerfile(String context, String outputFileName) throws IOException {
        File newFile = new File(outputFileName);
        // append if file exists
        if (newFile.exists()) {
            Files.write(Paths.get(outputFileName), context.getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
            return;
        }
        //create required directories
        if (newFile.getParentFile().mkdirs()) {
            Files.write(Paths.get(outputFileName), context.getBytes(StandardCharsets.UTF_8));
            return;
        }
        Files.write(Paths.get(outputFileName), context.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Create docker image.
     *
     * @param dockerModel dockerModel object
     * @param dockerDir   dockerfile directory
     * @throws InterruptedException When error with docker build process
     * @throws IOException          When error with docker build process
     */
    private void buildImage(DockerModel dockerModel, String dockerDir) throws
            InterruptedException, IOException, KubernetesPluginException {
        disableFailOnUnknownProperties();
        Config dockerClientConfig = new ConfigBuilder()
                .withDockerUrl(dockerModel.getDockerHost())
                .build();
        DockerClient client = new io.fabric8.docker.client.DefaultDockerClient(dockerClientConfig);
        final DockerError dockerError = new DockerError();
        OutputHandle buildHandle = client.image()
                .build()
                .withRepositoryName(dockerModel.getName())
                .withNoCache()
                .alwaysRemovingIntermediate()
                .usingListener(new EventListener() {
                    @Override
                    public void onSuccess(String message) {
                        buildDone.countDown();
                    }

                    @Override
                    public void onError(String message) {
                        dockerError.setErrorMsg("Unable to build Docker image: " + message);
                        buildDone.countDown();
                    }

                    @Override
                    public void onError(Throwable t) {
                        dockerError.setErrorMsg("Unable to build Docker image: " + t.getMessage());
                        buildDone.countDown();
                    }

                    @Override
                    public void onEvent(String event) {
                        printDebug(event);
                    }
                })
                .fromFolder(dockerDir);
        buildDone.await();
        buildHandle.close();
        client.close();
        handleError(dockerError);
    }

    private void handleError(DockerError dockerError) throws KubernetesPluginException {
        if (dockerError.isError()) {
            throw new KubernetesPluginException(dockerError.getErrorMsg());
        }
    }

    /**
     * Push docker image.
     *
     * @param dockerModel DockerModel
     * @throws InterruptedException When error with docker build process
     * @throws IOException          When error with docker build process
     */
    private void pushImage(DockerModel dockerModel) throws InterruptedException, IOException,
            KubernetesPluginException {
        disableFailOnUnknownProperties();
        AuthConfig authConfig = new AuthConfigBuilder().withUsername(dockerModel.getUsername()).withPassword
                (dockerModel.getPassword())
                .build();
        Config config = new ConfigBuilder()
                .withDockerUrl(dockerModel.getDockerHost())
                .addToAuthConfigs(RegistryUtils.extractRegistry(dockerModel.getName()), authConfig)
                .build();

        DockerClient client = new DefaultDockerClient(config);
        final DockerError dockerError = new DockerError();
        OutputHandle handle = client.image().withName(dockerModel.getName()).push()
                .usingListener(new EventListener() {
                    @Override
                    public void onSuccess(String message) {
                        pushDone.countDown();
                    }

                    @Override
                    public void onError(String message) {
                        pushDone.countDown();
                        dockerError.setErrorMsg("Unable to push Docker image: " + message);
                    }

                    @Override
                    public void onError(Throwable t) {
                        pushDone.countDown();
                        dockerError.setErrorMsg("Unable to push Docker image: " + t.getMessage());
                    }

                    @Override
                    public void onEvent(String event) {
                        printDebug(event);
                    }
                })
                .toRegistry();

        pushDone.await();
        handle.close();
        client.close();
        handleError(dockerError);
    }

    /**
     * Generate Dockerfile content.
     *
     * @return Dockerfile content as a string
     */
    public String generate() {
        String dockerBase = "# Auto Generated Dockerfile\n" +
                "\n" +
                "FROM " + dockerModel.getBaseImage() + "\n" +
                "LABEL maintainer=\"dev@ballerina.io\"\n" +
                "\n" +
                "COPY " + dockerModel.getBalxFileName() + " /home/ballerina \n\n";

        StringBuilder stringBuffer = new StringBuilder(dockerBase);
        dockerModel.getExternalFiles().forEach(file -> {
            // Extract the source filename relative to docker folder.
            String sourceFileName = String.valueOf(Paths.get(file.getSource()).getFileName());
            stringBuffer.append("COPY ")
                    .append(sourceFileName)
                    .append(" ")
                    .append(file.getTarget())
                    .append("\n");
        });

        if (dockerModel.getPorts() != null && dockerModel.getPorts().size() > 0) {
            stringBuffer.append("EXPOSE ");
            dockerModel.getPorts().forEach(port -> stringBuffer.append(" ").append(port));
        }

        stringBuffer.append("\nCMD ballerina run ");

        if (!KubernetesUtils.isBlank(dockerModel.getCommandArg())) {
            stringBuffer.append(dockerModel.getCommandArg());
        }

        if (dockerModel.isEnableDebug()) {
            stringBuffer.append("--debug ").append(dockerModel.getDebugPort()).append(" ");
        }
        stringBuffer.append(dockerModel.getBalxFileName()).append("\n");
        return stringBuffer.toString();
    }

    @Override
    public void createArtifacts() throws KubernetesPluginException {
        dockerModel = dataHolder.getDockerModel();
        if (dockerModel.getDockerCertPath() != null) {
            System.setProperty("docker.cert.path", dockerModel.getDockerCertPath());
        }
        String dockerContent = generate();
        try {
            OUT.print("\t@kubernetes:Docker \t\t\t - complete 0/3 \r");
            String dockerOutputDir = dataHolder.getOutputDir();
            if (dockerOutputDir.endsWith("target" + File.separator + "kubernetes" + File.separator)) {
                //Compiling package therefore append balx file dependencies to docker artifact dir path
                dockerOutputDir = dockerOutputDir + File.separator + extractBalxName(dataHolder
                        .getBalxFilePath());
            }
            dockerOutputDir = dockerOutputDir + File.separator + DOCKER;
            writeDockerfile(dockerContent, dockerOutputDir + File.separator + "Dockerfile");
            OUT.print("\t@kubernetes:Docker \t\t\t - complete 1/3 \r");
            String balxDestination = dockerOutputDir + File.separator + KubernetesUtils.extractBalxName
                    (dataHolder
                            .getBalxFilePath()) + BALX;
            copyFileOrDirectory(dataHolder
                    .getBalxFilePath(), balxDestination);
            for (ExternalFileModel copyFileModel : dockerModel.getExternalFiles()) {
                // Copy external files to docker folder
                String target = dockerOutputDir + File.separator + String.valueOf(Paths.get(copyFileModel.getSource())
                        .getFileName());
                Path sourcePath = Paths.get(copyFileModel.getSource());
                if (!sourcePath.isAbsolute()) {
                    sourcePath = sourcePath.toAbsolutePath();
                }
                copyFileOrDirectory(sourcePath.toString(), target);
            }
            //check image build is enabled.
            if (dockerModel.isBuildImage()) {
                buildImage(dockerModel, dockerOutputDir);
                OUT.print("\t@kubernetes:Docker \t\t\t - complete 2/3 \r");
                Files.delete(Paths.get(balxDestination));
                //push only if image build is enabled.
                if (dockerModel.isPush()) {
                    pushImage(dockerModel);
                }
                OUT.print("\t@kubernetes:Docker \t\t\t - complete 3/3 \r");
            }
            OUT.print("\t@kubernetes:Docker \t\t\t - complete 3/3");
        } catch (IOException e) {
            throw new KubernetesPluginException("Unable to write Dockerfile content");
        } catch (InterruptedException e) {
            throw new KubernetesPluginException("Unable to create Docker images " + e.getMessage());
        }
    }

    /**
     * Class to hold docker errors.
     */
    private static class DockerError {
        private boolean error;
        private String errorMsg;

        DockerError() {
            this.error = false;
        }

        public boolean isError() {
            return error;
        }

        String getErrorMsg() {
            return errorMsg;
        }

        public void setErrorMsg(String errorMsg) {
            this.error = true;
            this.errorMsg = errorMsg;
        }
    }
}
