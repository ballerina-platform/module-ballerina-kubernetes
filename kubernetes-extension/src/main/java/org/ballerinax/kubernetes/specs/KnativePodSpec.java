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


package org.ballerinax.kubernetes.specs;


import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.fabric8.kubernetes.api.model.Affinity;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.HostAlias;
import io.fabric8.kubernetes.api.model.KubernetesResource;
import io.fabric8.kubernetes.api.model.LocalObjectReference;
import io.fabric8.kubernetes.api.model.PodDNSConfig;
import io.fabric8.kubernetes.api.model.PodReadinessGate;
import io.fabric8.kubernetes.api.model.PodSecurityContext;
import io.fabric8.kubernetes.api.model.Toleration;
import io.fabric8.kubernetes.api.model.Volume;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Generated;
import javax.validation.Valid;

/**
 * Kubernetes service annotations model class.
 * Knative PodSpec
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
        "apiVersion",
        "kind",
        "metadata",
        "containerConcurrency",
        "containers",
        "imagePullSecrets",
        "initContainers",
        "readinessGates",
        "securityContext",
        "serviceAccount",
        "serviceAccountName",
        "tolerations",
        "volumes"
})
@JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
public class KnativePodSpec implements KubernetesResource {

    private static final long serialVersionUID = 6106269076155338045L;

    /**
     * Kubernetes service annotations model class.
     * Knative PodSpec
     */
    @JsonProperty("containerConcurrency")
    @Valid
    private java.lang.Integer containerConcurrency;

    /**
     * Kubernetes service annotations model class.
     * Knative PodSpec
     */
    @JsonProperty("containers")
    @Valid
    private List<Container> containers = new ArrayList<Container>();


    /**
     * Kubernetes service annotations model class.
     * Knative PodSpec
     */
    @JsonProperty("imagePullSecrets")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @Valid
    private List<LocalObjectReference> imagePullSecrets = new ArrayList<LocalObjectReference>();

    /**
     * Kubernetes service annotations model class.
     * Knative PodSpec
     */
    @JsonProperty("initContainers")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @Valid
    private List<Container> initContainers = new ArrayList<Container>();

    /**
     * Kubernetes service annotations model class.
     * Knative PodSpec
     */
    @JsonProperty("readinessGates")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @Valid
    private List<PodReadinessGate> readinessGates = new ArrayList<PodReadinessGate>();

    /**
     * Kubernetes service annotations model class.
     * Knative PodSpec
     */
    @JsonProperty("securityContext")
    @Valid
    private PodSecurityContext securityContext;

    /**
     * Kubernetes service annotations model class.
     * Knative PodSpec
     */
    @JsonProperty("serviceAccount")
    private java.lang.String serviceAccount;

    /**
     * Kubernetes service annotations model class.
     * Knative PodSpec
     */
    @JsonProperty("serviceAccountName")
    private java.lang.String serviceAccountName;

    /**
     * Kubernetes service annotations model class.
     * Knative PodSpec
     */
    @JsonProperty("tolerations")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @Valid
    private List<Toleration> tolerations = new ArrayList<Toleration>();

    /**
     * Kubernetes service annotations model class.
     * Knative PodSpec
     */
    @JsonProperty("volumes")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @Valid
    private List<Volume> volumes = new ArrayList<Volume>();
    @JsonIgnore
    private Map<java.lang.String, Object> additionalProperties = new HashMap<java.lang.String, Object>();

    /**
     * Kubernetes service annotations model class.
     * Knative PodSpec
     */
    public KnativePodSpec() {
    }

    /**
     * Knative  model class.
     * Knative PodSpec
     */
    public KnativePodSpec(Long activeDeadlineSeconds, Affinity affinity,
                          Boolean automountServiceAccountToken, java.lang.Integer containerConcurrency,
                          List<Container> containers,
                          PodDNSConfig dnsConfig, java.lang.String dnsPolicy, Boolean enableServiceLinks,
                          List<HostAlias> hostAliases, Boolean hostIPC, Boolean hostNetwork, Boolean hostPID,
                          java.lang.String hostname, List<LocalObjectReference>
                                  imagePullSecrets, List<Container> initContainers,
                          java.lang.String nodeName, Map<String, String>
                                  nodeSelector, Integer priority,
                          java.lang.String priorityClassName,
                          List<PodReadinessGate> readinessGates,
                          java.lang.String restartPolicy, java.lang.String runtimeClassName,
                          java.lang.String schedulerName, PodSecurityContext securityContext,
                          java.lang.String serviceAccount, java.lang.String serviceAccountName,
                          Boolean shareProcessNamespace, java.lang.String subdomain,
                          Long terminationGracePeriodSeconds, List<Toleration> tolerations, List<Volume> volumes) {
        this.containerConcurrency = containerConcurrency;
        this.containers = containers;
        this.imagePullSecrets = imagePullSecrets;
        this.initContainers = initContainers;
        this.readinessGates = readinessGates;
        this.securityContext = securityContext;
        this.serviceAccount = serviceAccount;
        this.serviceAccountName = serviceAccountName;
        this.tolerations = tolerations;
        this.volumes = volumes;
    }

    /**
     * Knative  model class.
     * Knative PodSpec
     */
    @JsonProperty("containerConcurrency")
    public java.lang.Integer getContainerConcurrency() {
        return containerConcurrency;
    }

    /**
     * Knative  model class.
     * Knative PodSpec
     */
    @JsonProperty("containerConcurrency")
    public void setContainerConcurrency(java.lang.Integer containerConcurrency) {
        this.containerConcurrency = containerConcurrency;
    }

    /**
     * Knative  model class.
     * Knative PodSpec
     */
    @JsonProperty("containers")
    public List<Container> getContainers() {
        return containers;
    }

    /**
     * Knative  model class.
     * Knative PodSpec
     */
    @JsonProperty("containers")
    public void setContainers(List<Container> containers) {
        this.containers = containers;
    }

    /**
     * Knative  model class.
     * Knative PodSpec
     */
    @JsonProperty("imagePullSecrets")
    public List<LocalObjectReference> getImagePullSecrets() {
        return imagePullSecrets;
    }

    /**
     * Knative  model class.
     * Knative PodSpec
     */
    @JsonProperty("imagePullSecrets")
    public void setImagePullSecrets(List<LocalObjectReference> imagePullSecrets) {
        this.imagePullSecrets = imagePullSecrets;
    }

    /**
     * Knative  model class.
     * Knative PodSpec
     */
    @JsonProperty("initContainers")
    public List<Container> getInitContainers() {
        return initContainers;
    }

    /**
     * Knative  model class.
     * Knative PodSpec
     */
    @JsonProperty("initContainers")
    public void setInitContainers(List<Container> initContainers) {
        this.initContainers = initContainers;
    }

    /**
     * Knative  model class.
     * Knative PodSpec
     */
    @JsonProperty("readinessGates")
    public List<PodReadinessGate> getReadinessGates() {
        return readinessGates;
    }

    /**
     * Knative  model class.
     * Knative PodSpec
     */
    @JsonProperty("readinessGates")
    public void setReadinessGates(List<PodReadinessGate> readinessGates) {
        this.readinessGates = readinessGates;
    }

    /**
     * Knative  model class.
     * Knative PodSpec
     */
    @JsonProperty("securityContext")
    public PodSecurityContext getSecurityContext() {
        return securityContext;
    }

    /**
     * Knative  model class.
     * Knative PodSpec
     */
    @JsonProperty("securityContext")
    public void setSecurityContext(PodSecurityContext securityContext) {
        this.securityContext = securityContext;
    }

    /**
     * Knative  model class.
     * Knative PodSpec
     */
    @JsonProperty("serviceAccount")
    public java.lang.String getServiceAccount() {
        return serviceAccount;
    }

    /**
     * Knative  model class.
     * Knative PodSpec
     */
    @JsonProperty("serviceAccount")
    public void setServiceAccount(java.lang.String serviceAccount) {
        this.serviceAccount = serviceAccount;
    }

    /**
     * Knative  model class.
     * Knative PodSpec
     */
    @JsonProperty("serviceAccountName")
    public java.lang.String getServiceAccountName() {
        return serviceAccountName;
    }

    /**
     * Knative  model class.
     * Knative PodSpec
     */
    @JsonProperty("serviceAccountName")
    public void setServiceAccountName(java.lang.String serviceAccountName) {
        this.serviceAccountName = serviceAccountName;
    }

    /**
     * Knative  model class.
     * Knative PodSpec
     */
    @JsonProperty("tolerations")
    public List<Toleration> getTolerations() {
        return tolerations;
    }

    /**
     * Knative  model class.
     * Knative PodSpec
     */
    @JsonProperty("tolerations")
    public void setTolerations(List<Toleration> tolerations) {
        this.tolerations = tolerations;
    }

    /**
     * Knative  model class.
     * Knative PodSpec
     */
    @JsonProperty("volumes")
    public List<Volume> getVolumes() {
        return volumes;
    }

    /**
     * Knative  model class.
     * Knative PodSpec
     */
    @JsonProperty("volumes")
    public void setVolumes(List<Volume> volumes) {
        this.volumes = volumes;
    }

    @JsonAnyGetter
    public Map<java.lang.String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(java.lang.String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
