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

package org.ballerinax.kubernetes;

/**
 * Constants used in kubernetes extension.
 */
public class KubernetesConstants {
    public static final String ENABLE_DEBUG_LOGS = "debugKubernetes";
    public static final String KUBERNETES = "kubernetes";
    public static final String KUBERNETES_SVC_PROTOCOL = "TCP";
    public static final String KUBERNETES_SELECTOR_KEY = "app";
    public static final String INGRESS_POSTFIX = "-ingress";
    public static final String ANONYMOUS_POSTFIX = "-anonymous";
    public static final String INGRESS_FILE_POSTFIX = "_ingress";
    public static final String INGRESS_HOSTNAME_POSTFIX = ".com";
    public static final String SVC_POSTFIX = "-svc";
    public static final String CONFIG_MAP_POSTFIX = "-config-map";
    public static final String SECRET_POSTFIX = "-secret";
    public static final String DOCKER = "docker";
    public static final String BALX = ".balx";
    public static final String DEPLOYMENT_POSTFIX = "-deployment";
    public static final String JOB_POSTFIX = "-job";
    public static final String HPA_POSTFIX = "-hpa";
    public static final String DEPLOYMENT_FILE_POSTFIX = "_deployment";
    public static final String JOB_FILE_POSTFIX = "_job";
    public static final String SVC_FILE_POSTFIX = "_svc";
    public static final String SECRET_FILE_POSTFIX = "_secret";
    public static final String CONFIG_MAP_FILE_POSTFIX = "_config_map";
    public static final String VOLUME_CLAIM_FILE_POSTFIX = "_volume_claim";
    public static final String HPA_FILE_POSTFIX = "_hpa";
    public static final String YAML = ".yaml";
    public static final String DOCKER_LATEST_TAG = ":latest";
    public static final String BALLERINA_HOME = "/home/ballerina";
    public static final String BALLERINA_RUNTIME = "/ballerina/runtime";
    public static final String BALLERINA_CONF_MOUNT_PATH = "/home/ballerina/conf/";
    public static final String BALLERINA_CONF_FILE_NAME = "ballerina.conf";
    public static final String ENDPOINT_PATH_VARIABLE = "path";
    public static final String UNIX_DEFAULT_DOCKER_HOST = "unix:///var/run/docker.sock";
    public static final String WINDOWS_DEFAULT_DOCKER_HOST = "tcp://localhost:2375";
    public static final String DOCKER_HOST = "DOCKER_HOST";
    public static final String DOCKER_CERT_PATH = "DOCKER_CERT_PATH";
    public static final String NGINX = "nginx";
    public static final String DEFAULT_NAMESPACE = "default";

    /**
     * Restart policy enum.
     */
    public enum RestartPolicy {
        Always,
        Never,
        OnFailure
    }

    /**
     * ImagePullPolicy type enum.
     */
    public enum ImagePullPolicy {
        IfNotPresent,
        Always
    }

    /**
     * Service type enum.
     */
    public enum ServiceType {
        ClusterIP,
        NodePort,
        LoadBalancer,
        ExternalName
    }
}
