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

import java.util.HashMap;
import java.util.Map;

/**
 * Class to hold Kubernetes data holder against package id.
 */
public class KubernetesContext {
    private static KubernetesContext instance;
    private final Map<String, KubernetesDataHolder> k8sContext;
    private String currentPackage;

    private KubernetesContext() {
        k8sContext = new HashMap<>();
    }

    public static KubernetesContext getInstance() {
        synchronized (KubernetesDataHolder.class) {
            if (instance == null) {
                instance = new KubernetesContext();
            }
        }
        return instance;
    }

    public void addDataHolder(String packageID) {
        this.currentPackage = packageID;
        this.k8sContext.put(packageID, new KubernetesDataHolder());
    }

    public void setCurrentPackage(String packageID) {
        this.currentPackage = packageID;
    }

    public KubernetesDataHolder getDataHolder() {
        return this.k8sContext.get(this.currentPackage);
    }

    public KubernetesDataHolder getDataHolder(String packageID) {
        return this.k8sContext.get(packageID);
    }

}
