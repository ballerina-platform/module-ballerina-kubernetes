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

package org.ballerinax.kubernetes.processors.openshift;

import org.ballerinax.kubernetes.exceptions.KubernetesPluginException;
import org.ballerinax.kubernetes.models.openshift.OpenShiftBuildExtensionModel;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;

import java.util.List;

import static org.ballerinax.kubernetes.utils.KubernetesUtils.getBooleanValue;

/**
 * Record processor for OpenShift Build Extension.
 */
public class OpenShiftBuildExtensionProcessor {

    /**
     * Process OpenShift extension fields.
     *
     * @param bcFields Field of OpenShift build extension.
     * @throws KubernetesPluginException Unknown field found.
     */
    public static OpenShiftBuildExtensionModel processBuildExtension(
            List<BLangRecordLiteral.BLangRecordKeyValue> bcFields)
            throws KubernetesPluginException {
        OpenShiftBuildExtensionModel buildExtension = new OpenShiftBuildExtensionModel();
        for (BLangRecordLiteral.BLangRecordKeyValue bcField : bcFields) {
            switch (OpenShiftBuildExtensionFields.valueOf(bcField.getKey().toString())) {
                case forcePullDockerImage:
                    buildExtension.setForcePullDockerImage(getBooleanValue(bcField.getValue()));
                    break;
                case buildDockerWithNoCache:
                    buildExtension.setBuildDockerWithNoCache(getBooleanValue(bcField.getValue()));
                    break;
                default:
                    throw new KubernetesPluginException("unknown field found for OpenShift Build extension: " +
                            bcField.getKey().toString());
            }
        }

        return buildExtension;
    }

    /**
     * Field of OpenShift Build Extension.
     */
    private enum OpenShiftBuildExtensionFields {
        forcePullDockerImage,
        buildDockerWithNoCache
    }
}
