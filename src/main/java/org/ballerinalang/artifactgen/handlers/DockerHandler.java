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

package org.ballerinalang.artifactgen.handlers;

import io.fabric8.docker.api.model.AuthConfig;
import io.fabric8.docker.api.model.AuthConfigBuilder;
import io.fabric8.docker.client.Config;
import io.fabric8.docker.client.ConfigBuilder;
import io.fabric8.docker.client.DefaultDockerClient;
import io.fabric8.docker.client.DockerClient;
import io.fabric8.docker.client.utils.RegistryUtils;
import io.fabric8.docker.dsl.EventListener;
import io.fabric8.docker.dsl.OutputHandle;
import org.ballerinalang.artifactgen.models.DockerModel;

import java.io.IOException;
import java.util.Calendar;
import java.util.concurrent.CountDownLatch;

import static org.ballerinalang.artifactgen.utils.KubeGenUtils.printDebug;
import static org.ballerinalang.artifactgen.utils.KubeGenUtils.printError;
import static org.ballerinalang.artifactgen.utils.KubeGenUtils.printInfo;
import static org.ballerinalang.artifactgen.utils.KubeGenUtils.printSuccess;

/**
 * Generates Docker artifacts from annotations.
 */
public class DockerHandler implements ArtifactHandler {

    private static final String LOCAL_DOCKER_DAEMON_SOCKET = "unix:///var/run/docker.sock";
    private final CountDownLatch pushDone = new CountDownLatch(1);
    private final CountDownLatch buildDone = new CountDownLatch(1);
    private DockerModel dockerModel;

    public DockerHandler(DockerModel dockerModel) {
        this.dockerModel = dockerModel;
    }

    /**
     * Create docker image.
     *
     * @param imageName docker image name
     * @param dockerDir dockerfile directory
     * @throws InterruptedException When error with docker build process
     * @throws IOException          When error with docker build process
     */
    public void buildImage(String imageName, String dockerDir) throws
            InterruptedException, IOException {
        Config dockerClientConfig = new ConfigBuilder()
                .withDockerUrl(LOCAL_DOCKER_DAEMON_SOCKET)
                .build();
        DockerClient client = new io.fabric8.docker.client.DefaultDockerClient(dockerClientConfig);
        OutputHandle buildHandle = client.image()
                .build()
                .withRepositoryName(imageName)
                .withNoCache()
                .alwaysRemovingIntermediate()
                .usingListener(new EventListener() {
                    @Override
                    public void onSuccess(String message) {
                        printSuccess("Docker image " + imageName + " generated.");
                        buildDone.countDown();
                    }

                    @Override
                    public void onError(String messsage) {
                        printError(messsage);
                        buildDone.countDown();
                    }

                    @Override
                    public void onError(Throwable t) {
                        printError(t.getMessage());
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
    }

    /**
     * Push docker image.
     *
     * @param dockerModel DockerModel
     * @throws InterruptedException When error with docker build process
     * @throws IOException          When error with docker build process
     */
    public void pushImage(DockerModel dockerModel) throws InterruptedException, IOException {

        AuthConfig authConfig = new AuthConfigBuilder().withUsername(dockerModel.getUsername()).withPassword
                (dockerModel.getPassword())
                .build();
        Config config = new ConfigBuilder()
                .withDockerUrl(LOCAL_DOCKER_DAEMON_SOCKET)
                .addToAuthConfigs(RegistryUtils.extractRegistry(dockerModel.getName()), authConfig)
                .build();

        DockerClient client = new DefaultDockerClient(config);
        OutputHandle handle = client.image().withName(dockerModel.getName()).push()
                .usingListener(new EventListener() {
                    @Override
                    public void onSuccess(String message) {
                        printSuccess(message);
                        pushDone.countDown();
                    }

                    @Override
                    public void onError(String message) {
                        printError(message);
                        pushDone.countDown();
                    }

                    @Override
                    public void onError(Throwable t) {
                        printError(t.getMessage());
                        pushDone.countDown();
                    }

                    @Override
                    public void onEvent(String event) {
                        printInfo(event);
                    }
                })
                .toRegistry();

        pushDone.await();
        handle.close();
        client.close();
    }

    /**
     * Generate Dockerfile content.
     *
     * @return Dockerfile content as a string
     */
    public String generate() {
        String dockerBase = "# --------------------------------------------------------------------\n" +
                "# Copyright (c) " + Calendar.getInstance().get(Calendar.YEAR)
                + ", Ballerina (https://ballerina.io/) " +
                "All Rights Reserved.\n" +
                "#\n" +
                "# Licensed under the Apache License, Version 2.0 (the \"License\");\n" +
                "# you may not use this file except in compliance with the License.\n" +
                "# You may obtain a copy of the License at\n" +
                "#\n" +
                "# http://www.apache.org/licenses/LICENSE-2.0\n" +
                "#\n" +
                "# Unless required by applicable law or agreed to in writing, software\n" +
                "# distributed under the License is distributed on an \"AS IS\" BASIS,\n" +
                "# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n" +
                "# See the License for the specific language governing permissions and\n" +
                "# limitations under the License.\n" +
                "# -----------------------------------------------------------------------\n" +
                "\n" +
                "FROM " + dockerModel.getBaseImage() + "\n" +
                "MAINTAINER ballerina Maintainers \"dev@ballerina.io\"\n" +
                "\n" +
                "COPY " + dockerModel.getBalxFileName() + " /home/ballerina \n\n";

        StringBuilder stringBuffer = new StringBuilder(dockerBase);
        if (dockerModel.isService()) {
            stringBuffer.append("EXPOSE ");
            dockerModel.getPorts().forEach(port -> stringBuffer.append(" ").append(port));
            stringBuffer.append("\n\nCMD ballerina run -s ").append(dockerModel.getBalxFileName());
        } else {
            stringBuffer.append("CMD ballerina run ").append(dockerModel.getBalxFileName());
        }
        if (dockerModel.isDebugEnable()) {
            stringBuffer.append(" --debug ").append(dockerModel.getDebugPort());
        }
        return stringBuffer.toString();
    }
}
