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
//import io.fabric8.kubernetes.api.model.Doneable;
import io.fabric8.kubernetes.api.model.KubernetesResource;
import io.fabric8.kubernetes.api.model.LabelSelector;
import io.fabric8.kubernetes.api.model.apps.DeploymentStrategy;
//import org.ballerinax.kubernetes.specs.KnativePodTemplateSpec;
/*import io.sundr.builder.annotations.Buildable;
import io.sundr.builder.annotations.Inline;
import lombok.EqualsAndHashCode;
import lombok.ToString;*/
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;
import javax.validation.Valid;

/**
 * Knative service annotations model class.
 * Knative ServiceSpec
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
        "apiVersion",
        "kind",
        "metadata",
        "minReadySeconds",
        "paused",
        "progressDeadlineSeconds",
        "replicas",
        "revisionHistoryLimit",
        "selector",
        "strategy",
        "template"
})
@JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
/*@ToString
@EqualsAndHashCode
@Buildable(editableEnabled = false,
validationEnabled = true, generateBuilderPackage = true, builderPackage = "io.fabric8.kubernetes.api.builder",
inline = @Inline(type = Doneable.class, prefix = "Doneable", value = "done"))*/
public class KnativeServiceSpec implements KubernetesResource {

    private static final long serialVersionUID = 6106269076155338045L;

    /**
     * Knative service annotations model class.
     * Knative ServiceSpec
     */
    @JsonProperty("minReadySeconds")
    private Integer minReadySeconds;
    /**
     * Knative service annotations model class.
     * Knative ServiceSpec
     */
    @JsonProperty("paused")
    private Boolean paused;
    /**
     * Knative service annotations model class.
     * Knative ServiceSpec
     */
    @JsonProperty("progressDeadlineSeconds")
    private Integer progressDeadlineSeconds;
    /**
     * Knative service annotations model class.
     * Knative ServiceSpec
     */
    @JsonProperty("replicas")
    private Integer replicas;
    /**
     * Knative service annotations model class.
     * Knative ServiceSpec
     */
    @JsonProperty("revisionHistoryLimit")
    private Integer revisionHistoryLimit;
    /**
     * Knative service annotations model class.
     * Knative ServiceSpec
     */
    @JsonProperty("selector")
    @Valid
    private LabelSelector selector;
    /**
     * Knative service annotations model class.
     * Knative ServiceSpec
     */
    @JsonProperty("strategy")
    @Valid
    private DeploymentStrategy strategy;
    /**
     * Knative service annotations model class.
     * Knative ServiceSpec
     */
    @JsonProperty("template")
    @Valid
    private KnativePodTemplateSpec template;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * Knative service annotations model class.
     * Knative ServiceSpec
     */
    public KnativeServiceSpec() {
    }

    /**
     * Knative service annotations model class.
     * Knative ServiceSpec
     */
    public KnativeServiceSpec(Integer minReadySeconds, Boolean paused, Integer progressDeadlineSeconds,
                              Integer replicas, Integer revisionHistoryLimit, LabelSelector selector,
                              DeploymentStrategy strategy, KnativePodTemplateSpec template) {
        this.minReadySeconds = minReadySeconds;
        this.paused = paused;
        this.progressDeadlineSeconds = progressDeadlineSeconds;
        this.replicas = replicas;
        this.revisionHistoryLimit = revisionHistoryLimit;
        this.selector = selector;
        this.strategy = strategy;
        this.template = template;
    }

    /**
     * Knative service annotations model class.
     * Knative ServiceSpec
     */
    @JsonProperty("minReadySeconds")
    public Integer getMinReadySeconds() {
        return minReadySeconds;
    }

    /**
     * Knative service annotations model class.
     * Knative ServiceSpec
     */
    @JsonProperty("minReadySeconds")
    public void setMinReadySeconds(Integer minReadySeconds) {
        this.minReadySeconds = minReadySeconds;
    }

    /**
     * Knative service annotations model class.
     * Knative ServiceSpec
     */
    @JsonProperty("paused")
    public Boolean getPaused() {
        return paused;
    }

    /**
     * Knative service annotations model class.
     * Knative ServiceSpec
     */
    @JsonProperty("paused")
    public void setPaused(Boolean paused) {
        this.paused = paused;
    }

    /**
     * Knative service annotations model class.
     * Knative ServiceSpec
     */
    @JsonProperty("progressDeadlineSeconds")
    public Integer getProgressDeadlineSeconds() {
        return progressDeadlineSeconds;
    }

    /**
     * Knative service annotations model class.
     * Knative ServiceSpec
     */
    @JsonProperty("progressDeadlineSeconds")
    public void setProgressDeadlineSeconds(Integer progressDeadlineSeconds) {
        this.progressDeadlineSeconds = progressDeadlineSeconds;
    }

    /**
     * Knative service annotations model class.
     * Knative ServiceSpec
     */
    @JsonProperty("replicas")
    public Integer getReplicas() {
        return replicas;
    }

    /**
     * Knative service annotations model class.
     * Knative ServiceSpec
     */
    @JsonProperty("replicas")
    public void setReplicas(Integer replicas) {
        this.replicas = replicas;
    }

    /**
     * Knative service annotations model class.
     * Knative ServiceSpec
     */
    @JsonProperty("revisionHistoryLimit")
    public Integer getRevisionHistoryLimit() {
        return revisionHistoryLimit;
    }

    /**
     * Knative service annotations model class.
     * Knative ServiceSpec
     */
    @JsonProperty("revisionHistoryLimit")
    public void setRevisionHistoryLimit(Integer revisionHistoryLimit) {
        this.revisionHistoryLimit = revisionHistoryLimit;
    }

    /**
     * Knative service annotations model class.
     * Knative ServiceSpec
     */
    @JsonProperty("selector")
    public LabelSelector getSelector() {
        return selector;
    }

    /**
     * Knative service annotations model class.
     * Knative ServiceSpec
     */
    @JsonProperty("selector")
    public void setSelector(LabelSelector selector) {
        this.selector = selector;
    }

    /**
     * Knative service annotations model class.
     * Knative ServiceSpec
     */
    @JsonProperty("strategy")
    public DeploymentStrategy getStrategy() {
        return strategy;
    }

    /**
     * Knative service annotations model class.
     * Knative ServiceSpec
     */
    @JsonProperty("strategy")
    public void setStrategy(DeploymentStrategy strategy) {
        this.strategy = strategy;
    }

    /**
     * Knative service annotations model class.
     * Knative ServiceSpec
     */
    @JsonProperty("template")
    public KnativePodTemplateSpec getTemplate() {
        return template;
    }

    /**
     * Knative service annotations model class.
     * Knative ServiceSpec
     */
    @JsonProperty("template")
    public void setTemplate(KnativePodTemplateSpec template) {
        this.template = template;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }
}
