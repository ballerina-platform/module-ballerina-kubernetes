/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 *
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

package org.ballerinax.kubernetes.models;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.ballerinax.docker.generator.models.CopyFileModel;
import org.ballerinax.kubernetes.KubernetesConstants;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import static org.ballerinax.docker.generator.DockerGenConstants.OPENJDK_8_JRE_ALPINE_BASE_IMAGE;

/**
 * Job model class.
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class JobModel extends KubernetesModel {
    protected Map<String, String> nodeSelector;
    private String restartPolicy;
    private int backoffLimit;
    private int activeDeadlineSeconds;
    private String schedule;
    private Map<String, EnvVarValueModel> env;
    private String imagePullPolicy;
    private String image;
    private boolean buildImage;
    private String dockerHost;
    private String username;
    private String password;
    private String baseImage;
    private boolean push;
    private String cmd;
    private String dockerCertPath;
    private Set<String> imagePullSecrets;
    private Set<CopyFileModel> copyFiles;
    private boolean singleYAML;
    private String registry;

    public JobModel() {
        this.labels = new HashMap<>();
        this.env = new LinkedHashMap<>();
        this.copyFiles = new HashSet<>();
        this.restartPolicy = KubernetesConstants.RestartPolicy.Never.name();
        this.setBaseImage(OPENJDK_8_JRE_ALPINE_BASE_IMAGE);
        this.setPush(false);
        this.buildImage = true;
        this.nodeSelector = new HashMap<>();
        this.setEnv(new HashMap<>());
        this.setImagePullPolicy("IfNotPresent");

        this.activeDeadlineSeconds = 20;
        this.imagePullSecrets = new HashSet<>();
        this.singleYAML = true;
    }

    public void addLabel(String key, String value) {
        this.labels.put(key, value);
    }
}
