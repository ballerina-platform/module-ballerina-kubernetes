package org.ballerinax.kubernetes.models;

/**
 * Model class to hold kubernetes Persistent Volume Claim.
 */
public class PersistentVolumeClaimModel {
    private String name;
    private String mountPath;
    private boolean readOnly;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
