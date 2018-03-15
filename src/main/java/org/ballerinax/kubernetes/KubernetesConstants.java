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
public class KubernetesConstants {
    public static final String ENABLE_DEBUG_LOGS = "debugKubernetes";
    public static final String KUBERNETES_SVC_PROTOCOL = "TCP";
    public static final String KUBERNETES_SELECTOR_KEY = "app";
    public static final String DEPLOYMENT_NAMESPACE_DEFAULT = "default";
    public static final String DEPLOYMENT_IMAGE_PULL_POLICY_DEFAULT = "IfNotPresent";
    public static final String DEPLOYMENT_LIVENESS_DISABLE = "disable";
}
