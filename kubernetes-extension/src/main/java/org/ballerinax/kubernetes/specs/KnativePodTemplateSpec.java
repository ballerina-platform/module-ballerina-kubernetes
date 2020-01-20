/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import io.fabric8.kubernetes.api.model.KubernetesResource;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;
import javax.validation.Valid;

/**
 * Knative service annotations model class.
 * Knative PodTemplateSpec
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
        "apiVersion",
        "kind",
        "metadata",
        "spec"
})
@JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
public class KnativePodTemplateSpec implements KubernetesResource {

    private static final long serialVersionUID = 6106269076155338045L;

    /**
     * Knative service annotations model class.
     * Knative PodTemplateSpec
     */
    @JsonProperty("metadata")
    @Valid
    private ObjectMeta metadata;
    /**
     * Knative service annotations model class.
     * Knative PodTemplateSpec
     */
    @JsonProperty("spec")
    @Valid
    private KnativePodSpec spec;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();
    /**
     * Knative service annotations model class.
     * Knative PodTemplateSpec
     */
    public KnativePodTemplateSpec() {
    }
    /**
     * Knative service annotations model class.
     * Knative PodTemplateSpec
     */
    public KnativePodTemplateSpec(ObjectMeta metadata, KnativePodSpec spec) {
        this.metadata = metadata;
        this.spec = spec;
    }

    /**
     * Knative service annotations model class.
     * Knative PodTemplateSpec
     */
    @JsonProperty("metadata")
    public ObjectMeta getMetadata() {
        return metadata;
    }

    /**
     * Knative service annotations model class.
     * Knative PodTemplateSpec
     */
    @JsonProperty("metadata")
    public void setMetadata(ObjectMeta metadata) {
        this.metadata = metadata;
    }

    /**
     * Knative service annotations model class.
     * Knative PodTemplateSpec
     */
    @JsonProperty("spec")
    public KnativePodSpec getSpec() {
        return spec;
    }

    /**
     * Knative service annotations model class.
     * Knative PodTemplateSpec
     */
    @JsonProperty("spec")
    public void setSpec(KnativePodSpec spec) {
        this.spec = spec;
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
