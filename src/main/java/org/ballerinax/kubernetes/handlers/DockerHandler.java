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

import java.io.IOException;
import java.util.Calendar;
import java.util.concurrent.CountDownLatch;

import static org.ballerinax.kubernetes.utils.KubernetesUtils.printDebug;

/**
 * Generates Docker artifacts from annotations.
 */
public class DockerHandler implements ArtifactHandler {

    private final CountDownLatch pushDone = new CountDownLatch(1);
    private final CountDownLatch buildDone = new CountDownLatch(1);
    private DockerModel dockerModel;

    public DockerHandler(DockerModel dockerModel) {
        this.dockerModel = dockerModel;
    }

    /**
     * Create docker image.
     *
     * @param dockerModel dockerModel object
     * @param dockerDir   dockerfile directory
     * @throws InterruptedException When error with docker build process
     * @throws IOException          When error with docker build process
     */
    public void buildImage(DockerModel dockerModel, String dockerDir) throws
            InterruptedException, IOException, KubernetesPluginException {
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
                        dockerError.setErrorMsg("error building docker image: " + message);
                        buildDone.countDown();
                    }

                    @Override
                    public void onError(Throwable t) {
                        dockerError.setErrorMsg("error building docker image: " + t.getMessage());
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
    public void pushImage(DockerModel dockerModel) throws InterruptedException, IOException, KubernetesPluginException {

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
                        dockerError.setErrorMsg("error pushing docker image: " + message);
                    }

                    @Override
                    public void onError(Throwable t) {
                        pushDone.countDown();
                        dockerError.setErrorMsg("error pushing docker image: " + t.getMessage());
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
                "LABEL maintainer=\"dev@ballerina.io\"\n" +
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
        if (dockerModel.isEnableDebug()) {
            stringBuffer.append(" --debug ").append(dockerModel.getDebugPort());
        }
        return stringBuffer.toString();
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

        public String getErrorMsg() {
            return errorMsg;
        }

        public void setErrorMsg(String errorMsg) {
            this.error = true;
            this.errorMsg = errorMsg;
        }
    }
}
