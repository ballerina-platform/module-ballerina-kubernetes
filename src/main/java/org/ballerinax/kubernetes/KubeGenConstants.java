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
 * Constants used in kuberina.
 */
public class KubeGenConstants {
    public static final String ENABLE_DEBUG_LOGS = "debugKuberina";
    public static final String KUBERNETES_SVC_PROTOCOL = "TCP";
    public static final String KUBERNETES_SELECTOR_KEY = "app";

    // Annotation package constants
    public static final String KUBERNETES_ANNOTATION_PACKAGE = "ballerinax.kubernetes";
    public static final String DEPLOYMENT_ANNOTATION = "deployment";
    public static final String HPA_ANNOTATION = "hpa";
    public static final String SERVICE_ANNOTATION = "svc";
    public static final String INGRESS_ANNOTATION = "ingress";

    //Docker annotation constants
    public static final String DOCKER_BASE_IMAGE = "baseImage";

    //Deployment annotation constants
    public static final String DEPLOYMENT_NAME = "name";
    public static final String DEPLOYMENT_LABELS = "labels";
    public static final String DEPLOYMENT_REPLICAS = "replicas";
    public static final String DEPLOYMENT_LIVENESS = "liveness";
    public static final String DEPLOYMENT_INITIAL_DELAY_SECONDS = "initialDelaySeconds";
    public static final String DEPLOYMENT_PERIOD_SECONDS = "periodSeconds";
    public static final String DEPLOYMENT_LIVENESS_PORT = "livenessPort";
    public static final String DEPLOYMENT_IMAGE_PULL_POLICY = "imagePullPolicy";
    public static final String DEPLOYMENT_NAMESPACE = "namespace";
    public static final String DEPLOYMENT_IMAGE = "image";
    public static final String DEPLOYMENT_IMAGE_BUILD = "imageBuild";
    public static final String DEPLOYMENT_NAMESPACE_DEFAULT = "default";
    public static final String DEPLOYMENT_IMAGE_PULL_POLICY_DEFAULT = "IfNotPresent";
    public static final String DEPLOYMENT_LIVENESS_DISABLE = "disable";
    public static final String DEPLOYMENT_LIVENESS_ENABLE = "enable";
    public static final String DEPLOYMENT_ENV_VARS = "env";
    public static final String DEPLOYMENT_USERNAME = "username";
    public static final String DEPLOYMENT_PASSWORD = "password";
    public static final String DEPLOYMENT_PUSH = "push";


    //Kubernetes service constants
    public static final String SVC_NAME = "name";
    public static final String SVC_LABELS = "labels";
    public static final String SVC_SERVICE_TYPE = "serviceType";
    public static final String SVC_PORT = "port";

    //Kubernetes ingress constants
    public static final String INGRESS_NAME = "name";
    public static final String INGRESS_LABELS = "labels";
    public static final String INGRESS_HOSTNAME = "hostname";
    public static final String INGRESS_PATH = "path";
    public static final String INGRESS_TARGET_PATH = "targetPath";
    public static final String INGRESS_CLASS = "ingressClass";
    public static final String INGRESS_ENABLE_TLS = "enableTLS";

    //Kubernetes Pod Autoscaler constants
    public static final String AUTOSCALER_NAME = "name";
    public static final String AUTOSCALER_LABELS = "labels";
    public static final String AUTOSCALER_MIN_REPLICAS = "minReplicas";
    public static final String AUTOSCALER_MAX_REPLICAS = "maxReplicas";
    public static final String AUTOSCALER_CPU_PERCENTAGE = "cpuPercentage";
    public static final String AUTOSCALER_DEPLOYMENT = "deployment";

    /**
     * Enum class for DeploymentConfiguration.
     */
    public enum DeploymentConfiguration {
        name,
        labels,
        replicas,
        enableLiveness,
        livenessPort,
        initialDelaySeconds,
        periodSeconds,
        imagePullPolicy,
        namespace,
        image,
        env,
        buildImage,
        username,
        password,
        baseImage,
        push
    }

}
