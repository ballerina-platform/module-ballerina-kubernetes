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

import org.ballerinax.docker.generator.DockerArtifactHandler;
import org.ballerinax.docker.generator.exceptions.DockerGenException;
import org.ballerinax.docker.generator.utils.DockerGenUtils;
import org.ballerinax.kubernetes.exceptions.KubernetesPluginException;

import java.io.File;

import static org.ballerinax.kubernetes.KubernetesConstants.DOCKER;

/**
 * Wrapper handler for creating docker artifacts.
 */
public class DockerHandler extends AbstractArtifactHandler {
    @Override
    public void createArtifacts() throws KubernetesPluginException {
        try {
            // Generate docker artifacts
            String dockerOutputDir = dataHolder.getOutputDir();
            if (dockerOutputDir.endsWith("target" + File.separator + "kubernetes" + File.separator)) {
                //Compiling package therefore append balx file dependencies to docker artifact dir path
                dockerOutputDir = dockerOutputDir + File.separator + DockerGenUtils.extractBalxName(dataHolder
                        .getBalxFilePath());
            }
            dockerOutputDir = dockerOutputDir + File.separator + DOCKER;
            DockerArtifactHandler dockerArtifactHandler =
                    new DockerArtifactHandler(dataHolder.getDockerModel());
            dockerArtifactHandler.createArtifacts(OUT, "\t@kubernetes:Docker \t\t\t", dataHolder
                    .getBalxFilePath(), dockerOutputDir);
        } catch (DockerGenException e) {
            throw new KubernetesPluginException("Unable to create/build/push docker image/container.", e);
        }
    }
}
