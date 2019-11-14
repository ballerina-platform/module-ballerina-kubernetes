package org.ballerinax.kubernetes.models.knative;

/**
 * Probe configuration for knative.
 */
public class ProbeModel {

    private int port;
    private int initialDelaySeconds = -1;
    private int periodSeconds = -1;

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getInitialDelaySeconds() {
        return initialDelaySeconds;
    }

    public void setInitialDelaySeconds(int initialDelaySeconds) {
        this.initialDelaySeconds = initialDelaySeconds;
    }

    public int getPeriodSeconds() {
        return periodSeconds;
    }

    public void setPeriodSeconds(int periodSeconds) {
        this.periodSeconds = periodSeconds;
    }
}
