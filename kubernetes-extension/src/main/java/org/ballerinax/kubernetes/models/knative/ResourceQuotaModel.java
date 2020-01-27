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

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Kubernetes Resource Quota annotations model class.
 */
public class ResourceQuotaModel extends KnativeModel {
    private Map<String, String> hard;
    private Set<String> scopes;

    public ResourceQuotaModel() {
        labels = new LinkedHashMap<>();
        hard = new LinkedHashMap<>();
        scopes = new LinkedHashSet<>();
    }

    public Map<String, String> getHard() {
        return hard;
    }

    public void setHard(Map<String, String> hard) {
        this.hard = hard;
    }

    public Set<String> getScopes() {
        return scopes;
    }

    public void setScopes(Set<String> scopes) {
        this.scopes = scopes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ResourceQuotaModel)) {
            return false;
        }
        ResourceQuotaModel that = (
                ResourceQuotaModel) o;
        return Objects.equals(getLabels(), that.getLabels()) && Objects.equals(getHard(), that.getHard()) &&
                Objects.equals(getScopes(), that.getScopes());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getLabels(), getHard(), getScopes());
    }

    @Override
    public String toString() {
        return "ResourceQuotaModel{" + "labels=" + labels + ", hard=" + hard + ", scopes=" + scopes + '}';
    }
}
