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

package org.ballerinax.kubernetes.utils;

import io.fabric8.kubernetes.client.utils.Serialization;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;

/**
 * Utilities class for testing purposes.
 */
public class Utils {
    /**
     * Load YAML files to kubernetes resource(s).
     *
     * @param file The path of the file.
     * @param <T>  The type reference of the artifact.
     * @return The refered type.
     * @throws IOException When yaml file could not be loaded.
     */
    public static <T> T loadYaml(File file) throws IOException {
        FileInputStream fileInputStream = FileUtils.openInputStream(file);
        return Serialization.unmarshal(fileInputStream, Collections.emptyMap());
    }
}
