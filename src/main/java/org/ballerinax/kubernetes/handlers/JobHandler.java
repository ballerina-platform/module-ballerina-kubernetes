/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinax.kubernetes.handlers;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ContainerBuilder;
import io.fabric8.kubernetes.api.model.CronJob;
import io.fabric8.kubernetes.api.model.CronJobBuilder;
import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.EnvVarBuilder;
import io.fabric8.kubernetes.api.model.Job;
import io.fabric8.kubernetes.api.model.JobBuilder;
import io.fabric8.kubernetes.client.internal.SerializationUtils;
import org.ballerinax.kubernetes.exceptions.KubernetesPluginException;
import org.ballerinax.kubernetes.models.JobModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.ballerinax.kubernetes.utils.KubernetesUtils.isEmpty;

/**
 * Job generator.
 */
public class JobHandler implements ArtifactHandler {

    private JobModel jobModel;

    public JobHandler(JobModel jobModel) {
        this.jobModel = jobModel;
    }

    @Override
    public String generate() throws KubernetesPluginException {
        try {
            if (isEmpty(jobModel.getSchedule())) {
                return SerializationUtils.dumpWithoutRuntimeStateAsYaml(getJob());
            }
            return SerializationUtils.dumpWithoutRuntimeStateAsYaml(getCronJob());
        } catch (JsonProcessingException e) {
            String errorMessage = "Error while generating yaml file for job " + jobModel.getName();
            throw new KubernetesPluginException(errorMessage, e);
        }

    }

    private List<EnvVar> populateEnvVar(Map<String, String> envMap) {
        List<EnvVar> envVars = new ArrayList<>();
        if (envMap == null) {
            return envVars;
        }
        envMap.forEach((k, v) -> {
            EnvVar envVar = new EnvVarBuilder().withName(k).withValue(v).build();
            envVars.add(envVar);
        });
        return envVars;
    }

    private Container generateContainer() {
        return new ContainerBuilder()
                .withName(jobModel.getName())
                .withImage(jobModel.getImage())
                .withImagePullPolicy(jobModel.getImagePullPolicy())
                .withEnv(populateEnvVar(jobModel.getEnv()))
                .build();
    }

    private Job getJob() {
        JobBuilder jobBuilder = new JobBuilder()
                .withNewMetadata()
                .withName(jobModel.getName())
                .endMetadata()
                .withNewSpec()
                .withNewTemplate()
                .withNewSpec()
                .withRestartPolicy(jobModel.getRestartPolicy())
                .withContainers(generateContainer())
                .endSpec()
                .endTemplate()
                .endSpec();
        return jobBuilder.build();
    }

    private CronJob getCronJob() {
        return new CronJobBuilder()
                .withNewMetadata()
                .withName(jobModel.getName())
                .endMetadata()
                .withNewSpec()
                .withNewJobTemplate()
                .withNewSpec()
                .withActiveDeadlineSeconds((long) jobModel.getActiveDeadlineSeconds())
                .endSpec()
                .endJobTemplate()
                .withSchedule(jobModel.getSchedule())
                .endSpec()
                .build();
    }
}
