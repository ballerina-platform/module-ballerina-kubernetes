/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 *
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinax.kubernetes.processors;

import org.ballerinalang.model.tree.AnnotationAttachmentNode;
import org.ballerinalang.model.tree.FunctionNode;
import org.ballerinax.kubernetes.KubernetesConstants;
import org.ballerinax.kubernetes.exceptions.KubernetesPluginException;
import org.ballerinax.kubernetes.models.JobModel;
import org.ballerinax.kubernetes.models.KubernetesContext;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;

import java.util.List;

import static org.ballerinax.kubernetes.KubernetesConstants.DOCKER_CERT_PATH;
import static org.ballerinax.kubernetes.KubernetesConstants.DOCKER_HOST;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.convertRecordFields;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.getBooleanValue;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.getEnvVarMap;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.getExternalFileMap;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.getImagePullSecrets;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.getIntValue;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.getMap;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.getStringValue;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.getValidName;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.isBlank;

/**
 * Job Annotation processor.
 */
public class JobAnnotationProcessor extends AbstractAnnotationProcessor {

    public void processAnnotation(FunctionNode functionNode, AnnotationAttachmentNode attachmentNode) throws
            KubernetesPluginException {
        JobModel jobModel = new JobModel();
        List<BLangRecordLiteral.BLangRecordKeyValueField> keyValues =
                convertRecordFields(((BLangRecordLiteral) ((BLangAnnotationAttachment) attachmentNode).expr)
                        .getFields());
        for (BLangRecordLiteral.BLangRecordKeyValueField keyValue : keyValues) {
            JobConfiguration jobConfiguration =
                    JobConfiguration.valueOf(keyValue.getKey().toString());
            switch (jobConfiguration) {
                case name:
                    jobModel.setName(getValidName(getStringValue(keyValue.getValue())));
                    break;
                case namespace:
                    KubernetesContext.getInstance().getDataHolder().setNamespace(getStringValue(keyValue.getValue()));
                    break;
                case labels:
                    jobModel.setLabels(getMap(keyValue.getValue()));
                    break;
                case annotations:
                    jobModel.setAnnotations(getMap(keyValue.getValue()));
                    break;
                case restartPolicy:
                    jobModel.setRestartPolicy(KubernetesConstants.RestartPolicy.valueOf(
                            getStringValue(keyValue.getValue())).name());
                    break;
                case backoffLimit:
                    jobModel.setBackoffLimit(getIntValue(keyValue.getValue()));
                    break;
                case activeDeadlineSeconds:
                    jobModel.setActiveDeadlineSeconds(getIntValue(keyValue.getValue()));
                    break;
                case schedule:
                    jobModel.setSchedule(getStringValue(keyValue.getValue()));
                    break;
                case username:
                    jobModel.setUsername(getStringValue(keyValue.getValue()));
                    break;
                case env:
                    jobModel.setEnv(getEnvVarMap(keyValue.getValue()));
                    break;
                case password:
                    jobModel.setPassword(getStringValue(keyValue.getValue()));
                    break;
                case baseImage:
                    jobModel.setBaseImage(getStringValue(keyValue.getValue()));
                    break;
                case push:
                    jobModel.setPush(getBooleanValue(keyValue.getValue()));
                    break;
                case cmd:
                    jobModel.setCmd(getStringValue(keyValue.getValue()));
                    break;
                case buildImage:
                    jobModel.setBuildImage(getBooleanValue(keyValue.getValue()));
                    break;
                case image:
                    jobModel.setImage(getStringValue(keyValue.getValue()));
                    break;
                case dockerHost:
                    jobModel.setDockerHost(getStringValue(keyValue.getValue()));
                    break;
                case dockerCertPath:
                    jobModel.setDockerCertPath(getStringValue(keyValue.getValue()));
                    break;
                case imagePullPolicy:
                    jobModel.setImagePullPolicy(getStringValue(keyValue.getValue()));
                    break;
                case copyFiles:
                    jobModel.setCopyFiles(getExternalFileMap(keyValue));
                    break;
                case singleYAML:
                    jobModel.setSingleYAML(getBooleanValue(keyValue.getValue()));
                    break;
                case imagePullSecrets:
                    jobModel.setImagePullSecrets(getImagePullSecrets(keyValue));
                    break;
                case registry:
                    jobModel.setRegistry(getStringValue(keyValue.getValue()));
                    break;
                case nodeSelector:
                    jobModel.setNodeSelector(getMap(keyValue.getValue()));
                    break;
                case uberJar:
                    jobModel.setUberJar(getBooleanValue(keyValue.getValue()));
                    break;
                case dockerConfigPath:
                    jobModel.setDockerConfigPath(getStringValue(keyValue.getValue()));
                    break;
                default:
                    break;
            }
        }
        String dockerHost = System.getenv(DOCKER_HOST);
        if (!isBlank(dockerHost)) {
            jobModel.setDockerHost(dockerHost);
        }
        String dockerCertPath = System.getenv(DOCKER_CERT_PATH);
        if (!isBlank(dockerCertPath)) {
            jobModel.setDockerCertPath(dockerCertPath);
        }
        KubernetesContext.getInstance().getDataHolder().setJobModel(jobModel);
    }


    /**
     * Enum class for JobConfiguration.
     */
    private enum JobConfiguration {
        name,
        namespace,
        labels,
        annotations,
        restartPolicy,
        backoffLimit,
        activeDeadlineSeconds,
        schedule,
        env,
        buildImage,
        dockerHost,
        username,
        password,
        baseImage,
        image,
        imagePullPolicy,
        push,
        cmd,
        dockerCertPath,
        copyFiles,
        singleYAML,
        dependsOn,
        imagePullSecrets,
        registry,
        nodeSelector,
        uberJar,
        dockerConfigPath
    }
}
