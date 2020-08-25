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
import org.ballerinax.kubernetes.ArtifactManager;
import org.ballerinax.kubernetes.exceptions.KubernetesPluginException;
import org.ballerinax.kubernetes.handlers.AbstractArtifactHandler;
import org.ballerinax.kubernetes.models.openshift.OpenShiftBuildExtensionModel;
import org.ballerinax.kubernetes.utils.DockerImageName;
import org.ballerinax.kubernetes.utils.KubernetesUtils;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.ballerinax.kubernetes.KubernetesConstants.OPENSHIFT;
import static org.ballerinax.kubernetes.KubernetesConstants.OPENSHIFT_BUILD_CONFIG_FILE_POSTFIX;
import static org.ballerinax.kubernetes.KubernetesConstants.YAML;

/**
 * Generates OpenShift's Build Configs.
 */
public class OpenShiftBuildConfigHandler extends AbstractArtifactHandler {
    
    String dockerDir = dataHolder.getSourceRoot().relativize(dataHolder.getDockerArtifactOutputPath()).toString();
    
    @Override
    public void createArtifacts() throws KubernetesPluginException {
        OpenShiftBuildExtensionModel buildConfigModel = dataHolder.getOpenShiftBuildExtensionModel();
        if (null != buildConfigModel) {
            generate(buildConfigModel);
            OUT.println();
            OUT.print("\t@openshift:BuildConfig \t\t\t - complete 1/1");
    
            Map<String, String> instructions = ArtifactManager.getInstructions();
            instructions.put("\tExecute the below command to deploy the OpenShift artifacts: ",
                    "\toc apply -f " + dataHolder.getK8sArtifactOutputPath().resolve(OPENSHIFT).toAbsolutePath());
            instructions.put("\tExecute the below command to start a build: ",
                    "\toc start-build bc/" + buildConfigModel.getName() + " --from-dir=" + dockerDir + " --follow");
            instructions.put("\tExecute the below command to deploy the Kubernetes artifacts: ",
                    "\tkubectl apply -f " + dataHolder.getK8sArtifactOutputPath());
        }
    }
    
    private void generate(OpenShiftBuildExtensionModel buildConfigModel) throws KubernetesPluginException {
        try {
            buildConfigModel.setLabels(new LinkedHashMap<>());
            if (null != buildConfigModel.getLabels() && !buildConfigModel.getLabels().containsKey("build")) {
                buildConfigModel.getLabels().put("build", buildConfigModel.getName());
            }
    
            // Generate docker artifacts
            DockerImageName dockerImageName = new DockerImageName(dataHolder.getDockerModel().getName());
            
            BuildConfig bc = new BuildConfigBuilder()
                    .withNewMetadata()
                    .withName(buildConfigModel.getName())
                    .withLabels(buildConfigModel.getLabels())
                    .withAnnotations(buildConfigModel.getAnnotations())
                    .withNamespace(dataHolder.getNamespace())
                    .endMetadata()
                    .withNewSpec()
                    .withNewOutput()
                    .withNewTo()
                    .withKind("ImageStreamTag")
                    .withName(dockerImageName.getRepository() + ":" + dockerImageName.getTag())
                    .endTo()
                    .endOutput()
                    .withNewSource()
                    .withNewBinary()
                    .endBinary()
                    .endSource()
                    .withNewStrategy()
                    .withNewDockerStrategy()
                    .withDockerfilePath("Dockerfile")
                    .withForcePull(buildConfigModel.isForcePullDockerImage())
                    .withNoCache(buildConfigModel.isBuildDockerWithNoCache())
                    .endDockerStrategy()
                    .endStrategy()
                    .endSpec()
                    .build();
            
            String resourceQuotaContent = SerializationUtils.dumpWithoutRuntimeStateAsYaml(bc);
            KubernetesUtils.writeToFile(dataHolder.getK8sArtifactOutputPath().resolve(OPENSHIFT),
                    resourceQuotaContent, OPENSHIFT_BUILD_CONFIG_FILE_POSTFIX + YAML);
            
            // Modify instructions
        } catch (IOException e) {
            String errorMessage = "error while generating OpenShift Build Config yaml file: " +
                                  buildConfigModel.getName();
            throw new KubernetesPluginException(errorMessage, e);
        }
    }
}
