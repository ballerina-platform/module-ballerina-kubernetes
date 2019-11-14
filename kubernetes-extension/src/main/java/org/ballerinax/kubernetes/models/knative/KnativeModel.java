package org.ballerinax.kubernetes.models.knative;



import java.util.Map;
import java.util.Objects;

/**
 * Knative Model class.
 */

public abstract class KnativeModel {

    private String version;
    protected String name;
    protected Map<String, String> labels;
    protected Map<String, String> annotations;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Map<String, String> getLabels() {
        return labels;
    }

    public void setLabels(Map<String, String> labels) {
        this.labels = labels;
    }

    public Map<String, String> getAnnotations() {
        return annotations;
    }

    public void setAnnotations(Map<String, String> annotations) {
        this.annotations = annotations;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof KnativeModel)) {
            return false;
        }
        KnativeModel that = (KnativeModel) o;
        return getName().equals(that.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName());
    }
}

