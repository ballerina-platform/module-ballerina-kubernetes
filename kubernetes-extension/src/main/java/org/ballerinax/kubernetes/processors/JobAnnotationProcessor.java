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
import static org.ballerinax.kubernetes.utils.KubernetesUtils.getEnvVarMap;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.getExternalFileMap;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.getImagePullSecrets;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.getMap;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.getValidName;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.isBlank;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.resolveValue;

/**
 * Job Annotation processor.
 */
public class JobAnnotationProcessor extends AbstractAnnotationProcessor {

    public void processAnnotation(FunctionNode functionNode, AnnotationAttachmentNode attachmentNode) throws
            KubernetesPluginException {
        JobModel jobModel = new JobModel();
        List<BLangRecordLiteral.BLangRecordKeyValue> keyValues =
                ((BLangRecordLiteral) ((BLangAnnotationAttachment) attachmentNode).expr).getKeyValuePairs();
        for (BLangRecordLiteral.BLangRecordKeyValue keyValue : keyValues) {
            JobConfiguration jobConfiguration =
                    JobConfiguration.valueOf(keyValue.getKey().toString());
            switch (jobConfiguration) {
                case name:
                    jobModel.setName(getValidName(resolveValue(keyValue.getValue().toString())));
                    break;
                case namespace:
                    KubernetesContext.getInstance().getDataHolder().setNamespace(resolveValue(
                            keyValue.getValue().toString()));
                    break;
                case labels:
                    jobModel.setLabels(getMap(((BLangRecordLiteral) keyValue.valueExpr).keyValuePairs));
                    break;
                case annotations:
                    jobModel.setAnnotations(getMap(((BLangRecordLiteral) keyValue.valueExpr).keyValuePairs));
                    break;
                case restartPolicy:
                    jobModel.setRestartPolicy(KubernetesConstants.RestartPolicy.valueOf(resolveValue(
                            keyValue.getValue().toString())).name());
                    break;
                case backoffLimit:
                    jobModel.setBackoffLimit(Integer.parseInt(resolveValue(keyValue.getValue().toString())));
                    break;
                case activeDeadlineSeconds:
                    jobModel.setActiveDeadlineSeconds(Integer.parseInt(resolveValue(keyValue.getValue().toString())));
                    break;
                case schedule:
                    jobModel.setSchedule(resolveValue(keyValue.getValue().toString()));
                    break;
                case username:
                    jobModel.setUsername(resolveValue(keyValue.getValue().toString()));
                    break;
                case env:
                    jobModel.setEnv(getEnvVarMap(keyValue.getValue()));
                    break;
                case password:
                    jobModel.setPassword(resolveValue(keyValue.getValue().toString()));
                    break;
                case baseImage:
                    jobModel.setBaseImage(resolveValue(keyValue.getValue().toString()));
                    break;
                case push:
                    jobModel.setPush(Boolean.valueOf(resolveValue(keyValue.getValue().toString())));
                    break;
                case buildImage:
                    jobModel.setBuildImage(Boolean.valueOf(resolveValue(keyValue.getValue().toString())));
                    break;
                case image:
                    jobModel.setImage(resolveValue(keyValue.getValue().toString()));
                    break;
                case dockerHost:
                    jobModel.setDockerHost(resolveValue(keyValue.getValue().toString()));
                    break;
                case dockerCertPath:
                    jobModel.setDockerCertPath(resolveValue(keyValue.getValue().toString()));
                    break;
                case imagePullPolicy:
                    jobModel.setImagePullPolicy(resolveValue(keyValue.getValue().toString()));
                    break;
                case copyFiles:
                    jobModel.setCopyFiles(getExternalFileMap(keyValue));
                    break;
                case singleYAML:
                    jobModel.setSingleYAML(Boolean.valueOf(resolveValue(keyValue.getValue().toString())));
                    break;
                case imagePullSecrets:
                    jobModel.setImagePullSecrets(getImagePullSecrets(keyValue));
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
        dockerCertPath,
        copyFiles,
        singleYAML,
        dependsOn,
        imagePullSecrets
    }
}
