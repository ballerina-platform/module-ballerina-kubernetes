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
 *//*


package org.ballerinax.kubernetes.models.knative;

import io.fabric8.kubernetes.api.builder.ValidationUtils;
import io.fabric8.kubernetes.api.builder.VisitableBuilder;
import io.fabric8.kubernetes.api.model.apps.DeploymentFluent;
import io.fabric8.kubernetes.api.model.apps.DeploymentFluentImpl;
//import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;

import javax.validation.Validator;

*/
/**
 * Generates knative service from annotations.
 *//*


public class KnativeServiceBuilder extends DeploymentFluentImpl<KnativeServiceBuilder> implements
        VisitableBuilder<KnativeService, KnativeServiceBuilder> {

    DeploymentFluent<?> fluent;
    Boolean validationEnabled;
    Validator validator;

    public KnativeServiceBuilder() {
        this(true);
    }

    public KnativeServiceBuilder(Boolean validationEnabled) {
        this(new KnativeService(), validationEnabled);
    }

    public KnativeServiceBuilder(DeploymentFluent<?> fluent) {
        this(fluent, true);
    }

    public KnativeServiceBuilder(DeploymentFluent<?> fluent, Boolean validationEnabled) {
        this(fluent, new KnativeService(), validationEnabled);
    }

    public KnativeServiceBuilder(DeploymentFluent<?> fluent, KnativeService instance) {
        this(fluent, instance, true);
    }

    public KnativeServiceBuilder(DeploymentFluent<?> fluent, KnativeService instance, Boolean validationEnabled) {
        this.fluent = fluent;
        fluent.withApiVersion(instance.getApiVersion());
        fluent.withKind(instance.getKind());
        fluent.withMetadata(instance.getMetadata());
        fluent.withSpec(instance.getSpec());
        this.validationEnabled = validationEnabled;
    }

    public KnativeServiceBuilder(KnativeService instance) {
        this(instance, true);
    }

    public KnativeServiceBuilder(KnativeService instance, Boolean validationEnabled) {
        this.fluent = this;
        this.withApiVersion(instance.getApiVersion());
        this.withKind(instance.getKind());
        this.withMetadata(instance.getMetadata());
        this.withSpec(instance.getSpec());
        this.validationEnabled = validationEnabled;
    }

    public KnativeServiceBuilder(Validator validator) {
        this(new KnativeService(), true);
    }

    public KnativeServiceBuilder(DeploymentFluent<?> fluent, KnativeService instance, Validator validator) {
        this.fluent = fluent;
        fluent.withApiVersion(instance.getApiVersion());
        fluent.withKind(instance.getKind());
        fluent.withMetadata(instance.getMetadata());
        fluent.withSpec(instance.getSpec());
        this.validator = validator;
        this.validationEnabled = validator != null;
    }

    public KnativeServiceBuilder(KnativeService instance, Validator validator) {
        this.fluent = this;
        this.withApiVersion(instance.getApiVersion());
        this.withKind(instance.getKind());
        this.withMetadata(instance.getMetadata());
        this.withSpec(instance.getSpec());
        this.validator = validator;
        this.validationEnabled = validator != null;
    }

    public KnativeService build() {
        KnativeService buildable = new KnativeService(this.fluent.getApiVersion(),
                this.fluent.getKind(), this.fluent.getMetadata(), this.fluent.getSpec(), this.fluent.getStatus());
        if (this.validationEnabled) {
            ValidationUtils.validate(buildable, this.validator);
        }

        return buildable;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && this.getClass() == o.getClass()) {
            if (!super.equals(o)) {
                return false;
            } else {
                KnativeServiceBuilder that;
                label55: {
                    that = (KnativeServiceBuilder) o;
                    if (this.fluent != null && this.fluent != this) {
                        if (this.fluent.equals(that.fluent)) {
                            break label55;
                        }
                    } else if (that.fluent == null || this.fluent == this) {
                        break label55;
                    }

                    return false;
                }

                if (this.validationEnabled != null) {
                    if (!this.validationEnabled.equals(that.validationEnabled)) {
                        return false;
                    }
                } else if (that.validationEnabled != null) {
                    return false;
                }

                return true;
            }
        } else {
            return false;
        }
    }


}
*/
