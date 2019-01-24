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

package org.ballerinax.kubernetes.handlers.openshift;

import io.fabric8.kubernetes.client.internal.SerializationUtils;
import io.fabric8.openshift.api.model.BuildConfig;
import io.fabric8.openshift.api.model.BuildConfigBuilder;
import org.ballerinax.docker.generator.utils.DockerGenUtils;
import org.ballerinax.kubernetes.exceptions.KubernetesPluginException;
import org.ballerinax.kubernetes.handlers.AbstractArtifactHandler;
import org.ballerinax.kubernetes.models.openshift.OpenShiftBuildConfigModel;
import org.ballerinax.kubernetes.utils.KubernetesUtils;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import static org.ballerinax.kubernetes.KubernetesConstants.DOCKER;
import static org.ballerinax.kubernetes.KubernetesConstants.OPENSHIFT_BUILD_CONFIG_FILE_POSTFIX;
import static org.ballerinax.kubernetes.KubernetesConstants.YAML;

/**
 * Generates OpenShift's Build Configs.
 */
public class OpenShiftBuildConfigHandler extends AbstractArtifactHandler {
    @Override
    public void createArtifacts() throws KubernetesPluginException {
        Map<String, OpenShiftBuildConfigModel> buildConfigModels = dataHolder.getOpenShiftBuildConfigModels();
        int size = buildConfigModels.size();
        if (size > 0) {
            OUT.println();
        }
        int count = 0;
        for (Map.Entry<String, OpenShiftBuildConfigModel> buildConfigModel : buildConfigModels.entrySet()) {
            count++;
            generate(buildConfigModel.getValue());
            OUT.print("\t@kubernetes:OpenShiftBuildConfig \t\t - complete " + count + "/" + buildConfigModels.size() +
                      "\r");
        }
    }
    
    private void generate(OpenShiftBuildConfigModel buildConfigModel) throws KubernetesPluginException {
        try {
            String balxFileName = KubernetesUtils.extractBalxName(dataHolder.getBalxFilePath());
    
            if (!buildConfigModel.getLabels().containsKey("build")) {
                buildConfigModel.getLabels().put("build", balxFileName);
            }
            
            String dockerOutputDir = dataHolder.getOutputDir();
            if (dockerOutputDir.endsWith("target" + File.separator + "kubernetes" + File.separator)) {
                //Compiling package therefore append balx file dependencies to docker artifact dir path
                dockerOutputDir = dockerOutputDir + File.separator + DockerGenUtils.extractBalxName(dataHolder
                        .getBalxFilePath());
            }
            dockerOutputDir = dockerOutputDir + File.separator + DOCKER;
            
            BuildConfig bc = new BuildConfigBuilder()
                    .withNewMetadata()
                    .withName(buildConfigModel.getName())
                    .withLabels(buildConfigModel.getLabels())
                    .withAnnotations(buildConfigModel.getAnnotations())
                    .endMetadata()
                    .withNewSpec()
                    .withNewOutput()
                    .withNewTo()
                    .withKind("ImageStreamTag")
                    .withName(dataHolder.getDockerModel().getName())
                    .endTo()
                    .endOutput()
                    .withNewSource()
                    .withNewBinary()
                    .endBinary()
                    .endSource()
                    .withNewStrategy()
                    .withNewDockerStrategy()
                    .withDockerfilePath(dockerOutputDir)
                    .withNoCache(true)
                    .endDockerStrategy()
                    .endStrategy()
                    .endSpec()
                    .build();
            String resourceQuotaContent = SerializationUtils.dumpWithoutRuntimeStateAsYaml(bc);
            KubernetesUtils.writeToFile(resourceQuotaContent, OPENSHIFT_BUILD_CONFIG_FILE_POSTFIX + YAML);
        } catch (IOException e) {
            String errorMessage = "Error while generating yaml file for openshift build config: " +
                                  buildConfigModel.getName();
            throw new KubernetesPluginException(errorMessage, e);
        }
    }
}
