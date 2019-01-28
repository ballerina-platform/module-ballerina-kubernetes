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
import io.fabric8.openshift.api.model.ImageStream;
import io.fabric8.openshift.api.model.ImageStreamBuilder;
import org.ballerinax.kubernetes.exceptions.KubernetesPluginException;
import org.ballerinax.kubernetes.handlers.AbstractArtifactHandler;
import org.ballerinax.kubernetes.models.openshift.OpenShiftBuildConfigModel;
import org.ballerinax.kubernetes.utils.KubernetesUtils;

import java.io.IOException;
import java.util.LinkedHashMap;

import static org.ballerinax.kubernetes.KubernetesConstants.OPENSHIFT_IMAGE_STREAM_TAG_FILE_POSTFIX;
import static org.ballerinax.kubernetes.KubernetesConstants.YAML;

/**
 * Generates OpenShift's Image Streams using build configs.
 */
public class OpenShiftImageStreamHandler extends AbstractArtifactHandler {
    @Override
    public void createArtifacts() throws KubernetesPluginException {
        OpenShiftBuildConfigModel buildConfigModel = dataHolder.getOpenShiftBuildConfigModel();
        if (buildConfigModel.isGenerateImageStream()) {
            generate(buildConfigModel);
            OUT.println("\t@kubernetes:OpenShiftImageStream \t - complete 1/1");
        }
    }
    
    private void generate(OpenShiftBuildConfigModel buildConfigModel) throws KubernetesPluginException {
        try {
            buildConfigModel.setLabels(new LinkedHashMap<>());
            if (null != buildConfigModel.getLabels() && !buildConfigModel.getLabels().containsKey("build")) {
                buildConfigModel.getLabels().put("build", buildConfigModel.getName());
            }
            
            String dockerImageName = dataHolder.getDockerModel().getName().substring(0,
                    dataHolder.getDockerModel().getName().indexOf(":"));
            
            ImageStream is = new ImageStreamBuilder()
                    .withNewMetadata()
                    .withName(dockerImageName)
                    .withLabels(buildConfigModel.getLabels())
                    .withAnnotations(buildConfigModel.getAnnotations())
                    .withNamespace(buildConfigModel.getNamespace())
                    .endMetadata()
                    .build();
            
            String resourceQuotaContent = SerializationUtils.dumpWithoutRuntimeStateAsYaml(is);
            KubernetesUtils.writeToFile(resourceQuotaContent, OPENSHIFT_IMAGE_STREAM_TAG_FILE_POSTFIX + YAML);
        } catch (IOException e) {
            String errorMessage = "Error while generating OpenShift Image Stream yaml file: " +
                                  buildConfigModel.getName();
            throw new KubernetesPluginException(errorMessage, e);
        }
    }
}
