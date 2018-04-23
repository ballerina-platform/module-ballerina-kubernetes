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
import org.ballerinax.kubernetes.models.KubernetesDataHolder;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;

import java.util.List;
import java.util.Locale;

import static org.ballerinax.kubernetes.KubernetesConstants.DEFAULT_DOCKER_HOST;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.getMap;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.getValidName;
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
            String annotationValue = resolveValue(keyValue.getValue().toString());
            switch (jobConfiguration) {
                case name:
                    jobModel.setName(getValidName(annotationValue));
                    break;
                case labels:
                    jobModel.setLabels(getMap(((BLangRecordLiteral) keyValue.valueExpr).keyValuePairs));
                    break;
                case restartPolicy:
                    jobModel.setRestartPolicy(KubernetesConstants.RestartPolicy.valueOf(annotationValue).name());
                    break;
                case backoffLimit:
                    jobModel.setBackoffLimit(Integer.parseInt(annotationValue));
                    break;
                case activeDeadlineSeconds:
                    jobModel.setActiveDeadlineSeconds(Integer.parseInt(annotationValue));
                    break;
                case schedule:
                    jobModel.setSchedule(annotationValue);
                    break;
                default:
                    break;
            }
        }
        String operatingSystem = System.getProperty("os.name").toLowerCase(Locale.getDefault());
        if (operatingSystem.contains("win") && DEFAULT_DOCKER_HOST.equals(jobModel.getDockerHost())) {
            // Windows users must specify docker host
            throw new KubernetesPluginException("Windows users must specify dockerHost parameter in " +
                    "@kubernetes:Deployment{} annotation.");
        }
        KubernetesDataHolder.getInstance().setJobModel(jobModel);
    }


    /**
     * Enum class for JobConfiguration.
     */
    private enum JobConfiguration {
        name,
        labels,
        restartPolicy,
        backoffLimit,
        activeDeadlineSeconds,
        schedule
    }
}
