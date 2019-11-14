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
package org.ballerinax.kubernetes.models.knative;


/**
 * Model class for an environment variable value.
 */
public class EnvVarValueModel {

    private String value;
    private org.ballerinax.kubernetes.models.knative.EnvVarValueModel.RefValue valueFrom;

    public EnvVarValueModel(String value) {
        this.value = value;
    }

    public EnvVarValueModel(org.ballerinax.kubernetes.models.knative.EnvVarValueModel.RefValue valueFrom) {
        this.valueFrom = valueFrom;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
        this.valueFrom = null;
    }

    public org.ballerinax.kubernetes.models.knative.EnvVarValueModel.RefValue getValueFrom() {
        return valueFrom;
    }

    public void setValueFrom(org.ballerinax.kubernetes.models.knative.EnvVarValueModel.RefValue valueFrom) {
        this.value = null;
        this.valueFrom = valueFrom;
    }

    /**
     * Interface for valueFrom type environment variables.
     */
    public interface RefValue {
    }

    /**
     * Model for fieldRef type values for environment variables.
     */
    public static class FieldRef implements org.ballerinax.kubernetes.models.knative.EnvVarValueModel.RefValue {
        private String fieldPath;

        public String getFieldPath() {
            return fieldPath;
        }

        public void setFieldPath(String fieldPath) {
            this.fieldPath = fieldPath;
        }

        @Override
        public String toString() {
            return "FieldRef{" + "fieldPath='" + fieldPath + '\'' + '}';
        }
    }

    @Override
    public String toString() {
        return "EnvVarValueModel{" + "value='" + value + '\'' + ", valueFrom=" + valueFrom + '}';
    }

    /**
     * Model for secretKeyRef type values for environment variables.
     */
    public static class SecretKeyRef implements org.ballerinax.kubernetes.models.knative.EnvVarValueModel.RefValue {
        private String key;
        private String name;

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return "SecrefKeyRef{" + "key='" + key + '\'' + ", name='" + name + '\'' + '}';
        }
    }

    /**
     * Model for resourceFieldRef type values for environment variables.
     */
    public static class ResourceFieldRef implements org.ballerinax.kubernetes.models.knative.EnvVarValueModel.RefValue {
        private String containerName;
        private String resource;

        public String getContainerName() {
            return containerName;
        }

        public void setContainerName(String containerName) {
            this.containerName = containerName;
        }

        public String getResource() {
            return resource;
        }

        public void setResource(String resource) {
            this.resource = resource;
        }

        @Override
        public String toString() {
            return "ResourceFieldRef{" +
                    "containerName='" + containerName + '\'' +
                    ", resource='" + resource + '\'' +
                    '}';
        }
    }

    /**
     * Model for configMapKey type values for environment variables.
     */
    public static class ConfigMapKeyValue implements org.ballerinax.kubernetes.models.knative.
            EnvVarValueModel.RefValue {
        private String key;
        private String name;

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return "ConfigMapKeyValue{" + "key='" + key + '\'' + ", name='" + name + '\'' + '}';
        }
    }

}
