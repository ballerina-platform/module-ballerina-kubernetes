/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinax.kubernetes.models.knative;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.apps.DeploymentStatus;
import io.fabric8.kubernetes.client.CustomResource;
import io.fabric8.kubernetes.model.annotation.ApiGroup;
import io.fabric8.kubernetes.model.annotation.ApiVersion;
import org.ballerinax.kubernetes.specs.KnativeServiceSpec;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * Generates knative service.
 */

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"apiVersion", "kind", "metadata", "spec", "status"})
@JsonDeserialize(
        using = JsonDeserializer.None.class
)
@ApiVersion("v1")
@ApiGroup("apps")


public class KnativeService extends CustomResource {

    private static final long serialVersionUID = 6106269076155338045L;

    @NotNull
    @JsonProperty("apiVersion")
    private String apiVersion = "serving.knative.dev/v1alpha1";

    @NotNull
    @JsonProperty("kind")
    private String kind = "Service";

    @JsonProperty("metadata")
    private ObjectMeta metadata;

    @JsonProperty("spec")
    @Valid
    private KnativeServiceSpec spec;

    public KnativeService(){
    }

    public KnativeService(String apiVersion, String kind, ObjectMeta metadata,
                          KnativeServiceSpec spec, DeploymentStatus status) {
        this.apiVersion = apiVersion;
        this.kind = kind;
        this.metadata = metadata;
        this.spec = spec;
    }

    @JsonProperty("apiVersion")
    public String getApiVersion() {
        return this.apiVersion;
    }

    @JsonProperty("apiVersion")
    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    @JsonProperty("kind")
    public String getKind() {
        return this.kind;
    }

    @JsonProperty("kind")
    public void setKind(String kind) {
        this.kind = kind;
    }

    @JsonProperty("metadata")
    public ObjectMeta getMetadata() {
        return this.metadata;
    }

    @JsonProperty("metadata")
    public void setMetadata(ObjectMeta metadata) {
        this.metadata = metadata;
    }

    @JsonProperty("spec")
    public KnativeServiceSpec getSpec() {
        return this.spec;
    }

    @JsonProperty("spec")
    public void setSpec(KnativeServiceSpec spec) {
        this.spec = spec;
    }

    public String toString() {
        return "Deployment(apiVersion=" + this.getApiVersion() + ", kind=" + this.getKind() + ", " +
                "metadata=" + this.getMetadata() + ", spec=" + this.getSpec() + ")";
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof KnativeService)) {
            return false;
        } else {
            KnativeService other = (KnativeService) o;
            if (!other.canEqual(this)) {
                return false;
            } else {
                Object apiVersion = this.getApiVersion();
                Object otherApiVersion = other.getApiVersion();
                if (apiVersion == null) {
                    if (otherApiVersion != null) {
                        return false;
                    }
                } else if (!apiVersion.equals(otherApiVersion)) {
                    return false;
                }

                Object kind = this.getKind();
                Object otherKind = other.getKind();
                if (kind == null) {
                    if (otherKind != null) {
                        return false;
                    }
                } else if (!kind.equals(otherKind)) {
                    return false;
                }

                Object metadata = this.getMetadata();
                Object otherMetadata = other.getMetadata();
                if (metadata == null) {
                    if (otherMetadata != null) {
                        return false;
                    }
                } else if (!metadata.equals(otherMetadata)) {
                    return false;
                }

                return true;
            }
        }
    }

    protected boolean canEqual(Object other) {
        return other instanceof KnativeService;
    }

    public int hashCode() {
        int prime = 1;
        int result = 1;
        Object apiVersion = this.getApiVersion();
        result = result * 59 + (apiVersion == null ? 43 : apiVersion.hashCode());
        Object kind = this.getKind();
        result = result * 59 + (kind == null ? 43 : kind.hashCode());
        Object metadata = this.getMetadata();
        result = result * 59 + (metadata == null ? 43 : metadata.hashCode());
        Object spec = this.getSpec();
        result = result * 59 + (spec == null ? 43 : spec.hashCode());
        return result;
    }
}

