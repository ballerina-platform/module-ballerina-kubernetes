package org.ballerinax.kubernetes.models;

import java.util.Map;

/**
 * Model class to hold kubernetes config map data.
 */
public class ConfigMapModel {
    private String name;
    private Map<String, String> data;
    private String mountPath;
    private boolean readOnly;

    public ConfigMapModel() {
        this.readOnly = true;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, String> getData() {
        return data;
    }

    public void setData(Map<String, String> data) {
        this.data = data;
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
}
