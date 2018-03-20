package org.ballerinax.kubernetes.models;

/**
 * Model class to hold kubernetes Persistent Volume Claim.
 */
public class PersistentVolumeClaimModel {
    private String name;
    private String mountPath;
    private boolean readOnly;
    private String accessMode;
    private String volumeClaimSize;

    public PersistentVolumeClaimModel() {
        this.accessMode = "ReadWriteOnce";
    }

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
