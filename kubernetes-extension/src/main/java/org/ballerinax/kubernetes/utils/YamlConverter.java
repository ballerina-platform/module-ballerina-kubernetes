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

package org.ballerinax.kubernetes.utils;

import org.yaml.snakeyaml.Yaml;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 */
public class YamlConverter {
    public static String toYaml(Object obj, Class clazz) throws IllegalAccessException {
        Map<String, Object> yamlValues = new LinkedHashMap<>();
        for (Field declaredField : clazz.getDeclaredFields()) {
            declaredField.setAccessible(true);
            if (declaredField.getType().equals(String.class)) {
                if (declaredField.get(obj) != null) {
                    yamlValues.put(declaredField.getName(), declaredField.get(obj));
                }
                
            }
        }
        Yaml yamlProcessor = new Yaml();
        return yamlProcessor.dump(yamlValues);
    }
}
