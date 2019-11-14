package org.ballerinax.kubernetes.models.knative;

import java.util.Map;
import java.util.Objects;

/**
 * Model class to hold Knative config map data.
 */
public class ConfigMapModel extends KnativeModel {

    private Map<String, String> data;
    private String mountPath;
    private boolean readOnly;
    private String ballerinaConf;

    public ConfigMapModel() {
        this.readOnly = true;
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

    public String getBallerinaConf() {
        return ballerinaConf;
    }

    public void setBallerinaConf(String ballerinaConf) {
        this.ballerinaConf = ballerinaConf;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof org.ballerinax.kubernetes.models.knative.ConfigMapModel)) {
            return false;
        }
        org.ballerinax.kubernetes.models.knative.ConfigMapModel that = (org.ballerinax.kubernetes.models.knative.
                ConfigMapModel) o;
        return Objects.equals(getMountPath(), that.getMountPath());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getMountPath());
    }



}
