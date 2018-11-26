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
package org.ballerinax.kubernetes.models;

import java.util.HashMap;

/**
 * Model class to hold kubernetes Persistent Volume Claim.
 */
public class PersistentVolumeClaimModel extends KubernetesModel {
    private String mountPath;
    private boolean readOnly;
    private String accessMode;
    private String volumeClaimSize;

    public PersistentVolumeClaimModel() {
        this.accessMode = "ReadWriteOnce";
        this.setAnnotations(new HashMap<>());
    }

    public String getMountPath() {
        return mountPath;
    }

    public void setMountPath(String mountPath) {
        this.mountPath = mountPath;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    public String getAccessMode() {
        return accessMode;
    }

    public void setAccessMode(String accessMode) {
        this.accessMode = accessMode;
    }

    public String getVolumeClaimSize() {
        return volumeClaimSize;
    }

    public void setVolumeClaimSize(String volumeClaimSize) {
        this.volumeClaimSize = volumeClaimSize;
    }
}
