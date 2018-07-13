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

import org.ballerinalang.model.elements.PackageID;
import org.ballerinax.kubernetes.exceptions.KubernetesPluginException;
import org.wso2.ballerinalang.compiler.util.Names;

import java.util.HashMap;
import java.util.Map;

import static org.ballerinax.kubernetes.utils.KubernetesUtils.isBlank;

/**
 * Class to hold Kubernetes data holder against package id.
 */
public class KubernetesContext {
    private static KubernetesContext instance;
    private final Map<PackageID, KubernetesDataHolder> packageIDtoDataHolderMap;
    private PackageID currentPackage;

    private KubernetesContext() {
        packageIDtoDataHolderMap = new HashMap<>();
    }

    public static KubernetesContext getInstance() {
        synchronized (KubernetesDataHolder.class) {
            if (instance == null) {
                instance = new KubernetesContext();
            }
        }
        return instance;
    }

    public void addDataHolder(PackageID packageID) {
        this.currentPackage = packageID;
        this.packageIDtoDataHolderMap.put(packageID, new KubernetesDataHolder());
    }

    public void setCurrentPackage(PackageID packageID) {
        this.currentPackage = packageID;
    }

    public KubernetesDataHolder getDataHolder() {
        return this.packageIDtoDataHolderMap.get(this.currentPackage);
    }

    public KubernetesDataHolder getDataHolder(PackageID packageID) {
        return this.packageIDtoDataHolderMap.get(packageID);
    }

    public Map<PackageID, KubernetesDataHolder> getPackageIDtoDataHolderMap() {
        return packageIDtoDataHolderMap;
    }

    public String getServiceName(String dependsOn) throws KubernetesPluginException {
        String packageName = dependsOn.substring(0, dependsOn.indexOf(Names.VERSION_SEPARATOR.value));
        String endpoint = dependsOn.substring(dependsOn.indexOf(Names.VERSION_SEPARATOR.value) + 1, dependsOn.length());
        for (PackageID packageID : packageIDtoDataHolderMap.keySet()) {
            if (packageName.equals(packageID.name.value)) {
                return getDataHolder(packageID).getbEndpointToK8sServiceMap().get(endpoint).getName();
            }
        }
        throw new KubernetesPluginException("Dependent endpoint " + dependsOn + " is not annotated with " +
                "@kubernetes:Service{}");
    }

    public String getDeploymentNameFromEndpoint(String dependsOn) throws KubernetesPluginException {
        if (isBlank(dependsOn) || !dependsOn.contains(Names.VERSION_SEPARATOR.value) || !(dependsOn.indexOf
                (Names.VERSION_SEPARATOR.value) > 1)) {
            throw new KubernetesPluginException("@kubernetes:Deployment{} Invalid dependsOn format specified " +
                    dependsOn);
        }
        String packageName = dependsOn.substring(0, dependsOn.indexOf(Names.VERSION_SEPARATOR.value));
        for (PackageID packageID : packageIDtoDataHolderMap.keySet()) {
            if (packageName.equals(packageID.name.value)) {
                return getDataHolder(packageID).getDeploymentModel().getName();
            }
        }
        throw new KubernetesPluginException("Dependent endpoint " + dependsOn + " not found.");
    }
}
