/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 *
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

import io.fabric8.kubernetes.api.model.ConfigMapKeySelector;
import io.fabric8.kubernetes.api.model.ConfigMapKeySelectorBuilder;
import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.EnvVarBuilder;
import io.fabric8.kubernetes.api.model.EnvVarSource;
import io.fabric8.kubernetes.api.model.EnvVarSourceBuilder;
import io.fabric8.kubernetes.api.model.ObjectFieldSelector;
import io.fabric8.kubernetes.api.model.ObjectFieldSelectorBuilder;
import io.fabric8.kubernetes.api.model.ResourceFieldSelector;
import io.fabric8.kubernetes.api.model.ResourceFieldSelectorBuilder;
import io.fabric8.kubernetes.api.model.SecretKeySelector;
import io.fabric8.kubernetes.api.model.SecretKeySelectorBuilder;
import org.ballerinalang.model.tree.NodeKind;
import org.apache.commons.io.FileUtils;
import org.ballerinax.kubernetes.KubernetesConstants;
import org.ballerinax.kubernetes.exceptions.KubernetesPluginException;
import org.ballerinax.kubernetes.models.DeploymentModel;
import org.ballerinax.kubernetes.models.KubernetesContext;
import org.ballerinax.kubernetes.models.KubernetesDataHolder;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.ballerinax.kubernetes.KubernetesConstants.YAML;

/**
 * Util methods used for artifact generation.
 */
public class KubernetesUtils {

    private static final boolean debugEnabled = "true".equals(System.getProperty(KubernetesConstants
            .ENABLE_DEBUG_LOGS));
    private static final PrintStream error = System.err;
    private static final PrintStream out = System.out;

    /**
     * Write content to a File. Create the required directories if they don't not exists.
     *
     * @param context        context of the file
     * @param outputFileName target file path
     * @throws IOException If an error occurs when writing to a file
     */
    public static void writeToFile(String context, String outputFileName) throws IOException {
        KubernetesDataHolder dataHolder = KubernetesContext.getInstance().getDataHolder();
        outputFileName = dataHolder.getOutputDir() + File
                .separator + extractBalxName(dataHolder.getBalxFilePath()) + outputFileName;
        DeploymentModel deploymentModel = KubernetesContext.getInstance().getDataHolder().getDeploymentModel();
        if (deploymentModel != null && deploymentModel.isSingleYAML()) {
            outputFileName = dataHolder.getOutputDir() + File
                    .separator + extractBalxName(dataHolder.getBalxFilePath()) + YAML;
        }
        File newFile = new File(outputFileName);
        // append if file exists
        if (newFile.exists()) {
            Files.write(Paths.get(outputFileName), context.getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
            return;
        }
        //create required directories
        if (newFile.getParentFile().mkdirs()) {
            Files.write(Paths.get(outputFileName), context.getBytes(StandardCharsets.UTF_8));
            return;
        }
        Files.write(Paths.get(outputFileName), context.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Read contents of a File.
     *
     * @param targetFilePath target file path
     * @throws KubernetesPluginException If an error occurs when reading file
     */
    public static byte[] readFileContent(Path targetFilePath) throws KubernetesPluginException {
        File file = targetFilePath.toFile();
        // append if file exists
        if (file.exists() && !file.isDirectory()) {
            try {
                return Files.readAllBytes(targetFilePath);
            } catch (IOException e) {
                throw new KubernetesPluginException("Unable to read contents of the file " + targetFilePath);
            }
        }
        throw new KubernetesPluginException("Unable to read contents of the file " + targetFilePath);
    }

    /**
     * Copy file or directory.
     *
     * @param source      source file/directory path
     * @param destination destination file/directory path
     */
    public static void copyFileOrDirectory(String source, String destination) throws KubernetesPluginException {
        File src = new File(source);
        File dst = new File(destination);
        try {
            // if source is file
            if (Files.isRegularFile(Paths.get(source))) {
                if (Files.isDirectory(dst.toPath())) {
                    // if destination is directory
                    FileUtils.copyFileToDirectory(src, dst);
                } else {
                    // if destination is file
                    FileUtils.copyFile(src, dst);
                }
            } else if (Files.isDirectory(Paths.get(source))) {
                FileUtils.copyDirectory(src, dst);
            }
        } catch (IOException e) {
            throw new KubernetesPluginException("Error while copying file", e);
        }

    }

    /**
     * Extract the ballerina file name from a given file path
     *
     * @param balxFilePath balx file path.
     * @return output file name of balx
     */
    public static String extractBalxName(String balxFilePath) {
        if (balxFilePath.contains(".balx")) {
            return balxFilePath.substring(balxFilePath.lastIndexOf(File.separator) + 1, balxFilePath.lastIndexOf(
                    ".balx"));
        }
        return null;
    }

    /**
     * Prints an Error message.
     *
     * @param msg message to be printed
     */
    public static void printError(String msg) {
        error.println("error [k8s plugin]: " + msg);
    }

    /**
     * Prints a debug message.
     *
     * @param msg message to be printed
     */
    public static void printDebug(String msg) {
        if (debugEnabled) {
            out.println("debug [k8s plugin]: " + msg);
        }
    }

    /**
     * Prints an Instruction message.
     *
     * @param msg message to be printed
     */
    public static void printInstruction(String msg) {
        out.println(msg);
    }

    /**
     * Deletes a given directory.
     *
     * @param path path to directory
     * @throws KubernetesPluginException if an error occurs while deleting
     */
    public static void deleteDirectory(String path) throws KubernetesPluginException {
        Path pathToBeDeleted = Paths.get(path);
        if (!Files.exists(pathToBeDeleted)) {
            return;
        }
        try {
            Files.walk(pathToBeDeleted)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        } catch (IOException e) {
            throw new KubernetesPluginException("Unable to delete directory: " + path, e);
        }

    }

    /* Checks if a String is empty ("") or null.
     *
     * @param str the String to check, may be null
     * @return true if the String is empty or null
     */
    public static boolean isBlank(String str) {
        int strLen;
        if (str != null && (strLen = str.length()) != 0) {
            for (int i = 0; i < strLen; ++i) {
                if (!Character.isWhitespace(str.charAt(i))) {
                    return false;
                }
            }
            return true;
        } else {
            return true;
        }
    }

    /**
     * Resolve variable value from environment variable if $env{} is used. Else return the value.
     *
     * @param variable variable value
     * @return Resolved variable
     */
    public static String resolveValue(String variable) throws KubernetesPluginException {
        if (variable.contains("$env{")) {
            //remove white spaces
            variable = variable.replace(" ", "");
            //extract variable name
            final String envVariable = variable.substring(variable.lastIndexOf("$env{") + 5,
                    variable.lastIndexOf("}"));
            //resolve value
            String value = Optional.ofNullable(System.getenv(envVariable)).orElseThrow(
                    () -> new KubernetesPluginException("error resolving value: " + envVariable + " is not set in " +
                            "the environment."));
            // substitute value
            return variable.replace("$env{" + envVariable + "}", value);
        }
        return variable;
    }

    /**
     * Generate map by splitting keyValues.
     *
     * @param keyValues key value paris.
     * @return Map of key values.
     */
    public static Map<String, String> getMap(List<BLangRecordLiteral.BLangRecordKeyValue> keyValues) {
        Map<String, String> map = new LinkedHashMap<>();
        if (keyValues != null) {
            keyValues.forEach(keyValue -> map.put(keyValue.getKey().toString(), keyValue.getValue().toString()));
        }
        return map;
    }

    /**
     * Returns valid kubernetes name.
     *
     * @param name actual value
     * @return valid name
     */
    public static String getValidName(String name) {
        return name.toLowerCase(Locale.getDefault()).replace("_", "-").replace(".", "-");
    }

    /**
     * Convert environment variable values into a map for deployment model.
     *
     * @param envVarValues Value of env field of Deployment annotation.
     * @return A map of env var models.
     */
    public static Map<String, EnvVarValueModel> getEnvVarMap(BLangExpression envVarValues) {
        Map<String, EnvVarValueModel> envVarMap = new LinkedHashMap<>();
        if (envVarValues.getKind() == NodeKind.RECORD_LITERAL_EXPR && envVarValues instanceof BLangRecordLiteral) {
            for (BLangRecordLiteral.BLangRecordKeyValue envVar : ((BLangRecordLiteral) envVarValues).keyValuePairs) {
                String envVarName = envVar.getKey().toString();
                EnvVarValueModel envVarValue = null;
                if (envVar.getValue().getKind() == NodeKind.LITERAL) {
                    // Value is a string
                    BLangLiteral value = (BLangLiteral) envVar.getValue();
                    envVarValue = new EnvVarValueModel(value.toString());
                } else if (envVar.getValue().getKind() == NodeKind.RECORD_LITERAL_EXPR) {
                    BLangRecordLiteral valueFrom = (BLangRecordLiteral) envVar.getValue();
                    BLangRecordLiteral.BLangRecordKeyValue bRefType = valueFrom.getKeyValuePairs().get(0);
                    BLangSimpleVarRef refType = (BLangSimpleVarRef) bRefType.getKey();
                    switch (refType.variableName.toString()) {
                        case "fieldRef":
                            BLangRecordLiteral.BLangRecordKeyValue fieldRefValue =
                                    ((BLangRecordLiteral) bRefType.getValue()).getKeyValuePairs().get(0);
                            EnvVarValueModel.FieldRef fieldRefModel = new EnvVarValueModel.FieldRef();
                            fieldRefModel.setFieldPath(fieldRefValue.getValue().toString());
                            envVarValue = new EnvVarValueModel(fieldRefModel);
                            break;
                        case "secretKeyRef":
                            EnvVarValueModel.SecretKeyRef secretKeyRefModel = new EnvVarValueModel.SecretKeyRef();
                            for (BLangRecordLiteral.BLangRecordKeyValue secretKeyRefFields :
                                    ((BLangRecordLiteral) bRefType.getValue()).getKeyValuePairs()) {
                                if (secretKeyRefFields.getKey().toString().equals("key")) {
                                    secretKeyRefModel.setKey(secretKeyRefFields.getValue().toString());
                                } else if (secretKeyRefFields.getKey().toString().equals("name")) {
                                    secretKeyRefModel.setName(secretKeyRefFields.getValue().toString());
                                }
                            }
                            envVarValue = new EnvVarValueModel(secretKeyRefModel);
                            break;
                        case "resourceFieldRef":
                            EnvVarValueModel.ResourceFieldRef resourceFieldRefModel =
                                    new EnvVarValueModel.ResourceFieldRef();
                            for (BLangRecordLiteral.BLangRecordKeyValue resourceFieldRefFields :
                                    ((BLangRecordLiteral) bRefType.getValue()).getKeyValuePairs()) {
                                if (resourceFieldRefFields.getKey().toString().equals("containerName")) {
                                    resourceFieldRefModel.setContainerName(
                                            resourceFieldRefFields.getValue().toString());
                                } else if (resourceFieldRefFields.getKey().toString().equals("resource")) {
                                    resourceFieldRefModel.setResource(resourceFieldRefFields.getValue().toString());
                                }
                            }
                            envVarValue = new EnvVarValueModel(resourceFieldRefModel);
                            break;
                        case "configMapKeyRef":
                            EnvVarValueModel.ConfigMapKeyValue configMapKeyRefModel =
                                    new EnvVarValueModel.ConfigMapKeyValue();
                            for (BLangRecordLiteral.BLangRecordKeyValue configMapKeyRefFields :
                                    ((BLangRecordLiteral) bRefType.getValue()).getKeyValuePairs()) {
                                if (configMapKeyRefFields.getKey().toString().equals("key")) {
                                    configMapKeyRefModel.setKey(configMapKeyRefFields.getValue().toString());
                                } else if (configMapKeyRefFields.getKey().toString().equals("name")) {
                                    configMapKeyRefModel.setName(configMapKeyRefFields.getValue().toString());
                                }
                            }
                            envVarValue = new EnvVarValueModel(configMapKeyRefModel);
                            break;
                        default:
                            break;
                    }
                }

                envVarMap.put(envVarName, envVarValue);
            }
        }
        return envVarMap;
    }

    /**
     * Get Image pull secrets.
     *
     * @param keyValue Value of imagePullSecret field of Job annotation.
     * @return A set of image pull secrets.
     */
    public static Set<String> getImagePullSecrets(BLangRecordLiteral.BLangRecordKeyValue keyValue) {
        Set<String> imagePullSecrets = new HashSet<>();
        List<BLangExpression> configAnnotation = ((BLangArrayLiteral) keyValue.valueExpr).exprs;
        for (BLangExpression bLangExpression : configAnnotation) {
            imagePullSecrets.add(bLangExpression.toString());
        }
        return imagePullSecrets;
    }


    /**
     * Get the set of external files to copy to docker image.
     *
     * @param keyValue Value of copyFiles field of Job annotation.
     * @return A set of external files
     * @throws KubernetesPluginException if an error occur while getting the paths
     */
    public static Set<ExternalFileModel> getExternalFileMap(BLangRecordLiteral.BLangRecordKeyValue keyValue) throws
            KubernetesPluginException {
        Set<ExternalFileModel> externalFiles = new HashSet<>();
        List<BLangExpression> configAnnotation = ((BLangArrayLiteral) keyValue.valueExpr).exprs;
        for (BLangExpression bLangExpression : configAnnotation) {
            List<BLangRecordLiteral.BLangRecordKeyValue> annotationValues =
                    ((BLangRecordLiteral) bLangExpression).getKeyValuePairs();
            ExternalFileModel externalFileModel = new ExternalFileModel();
            for (BLangRecordLiteral.BLangRecordKeyValue annotation : annotationValues) {
                String annotationValue = resolveValue(annotation.getValue().toString());
                switch (annotation.getKey().toString()) {
                    case "source":
                        externalFileModel.setSource(annotationValue);
                        break;
                    case "target":
                        externalFileModel.setTarget(annotationValue);
                        break;
                    default:
                        break;
                }
            }
            if (isBlank(externalFileModel.getSource())) {
                throw new KubernetesPluginException("@kubernetes:Deployment copyFiles source cannot be empty.");
            }
            if (isBlank(externalFileModel.getTarget())) {
                throw new KubernetesPluginException("@kubernetes:Deployment copyFiles target cannot be empty.");
            }
            externalFiles.add(externalFileModel);
        }
        return externalFiles;
    }

    /**
     * Get a list of environment variables.
     *
     * @param envMap Map of Environment variables
     * @return List of env vars
     */
    public static List<EnvVar> populateEnvVar(Map<String, EnvVarValueModel> envMap) {
        List<EnvVar> envVars = new ArrayList<>();
        if (envMap == null) {
            return envVars;
        }
        envMap.forEach((k, v) -> {
            EnvVar envVar = null;
            if (v.getValue() != null) {
                envVar = new EnvVarBuilder().withName(k).withValue(v.getValue()).build();
            } else if (v.getValueFrom() instanceof EnvVarValueModel.FieldRef) {
                EnvVarValueModel.FieldRef fieldRefModel = (EnvVarValueModel.FieldRef) v.getValueFrom();

                ObjectFieldSelector fieldRef =
                        new ObjectFieldSelectorBuilder().withFieldPath(fieldRefModel.getFieldPath()).build();
                EnvVarSource envVarSource = new EnvVarSourceBuilder().withFieldRef(fieldRef).build();
                envVar = new EnvVarBuilder().withName(k).withValueFrom(envVarSource).build();
            } else if (v.getValueFrom() instanceof EnvVarValueModel.SecretKeyRef) {
                EnvVarValueModel.SecretKeyRef secretKeyRefModel = (EnvVarValueModel.SecretKeyRef) v.getValueFrom();

                SecretKeySelector secretRef = new SecretKeySelectorBuilder()
                        .withName(secretKeyRefModel.getName())
                        .withKey(secretKeyRefModel.getKey())
                        .build();
                EnvVarSource envVarSource = new EnvVarSourceBuilder().withSecretKeyRef(secretRef).build();
                envVar = new EnvVarBuilder().withName(k).withValueFrom(envVarSource).build();
            } else if (v.getValueFrom() instanceof EnvVarValueModel.ResourceFieldRef) {
                EnvVarValueModel.ResourceFieldRef resourceFieldRefModel =
                        (EnvVarValueModel.ResourceFieldRef) v.getValueFrom();

                ResourceFieldSelector resourceFieldRef = new ResourceFieldSelectorBuilder()
                        .withContainerName(resourceFieldRefModel.getContainerName())
                        .withResource(resourceFieldRefModel.getResource())
                        .build();
                EnvVarSource envVarSource = new EnvVarSourceBuilder().withResourceFieldRef(resourceFieldRef).build();
                envVar = new EnvVarBuilder().withName(k).withValueFrom(envVarSource).build();
            } else if (v.getValueFrom() instanceof EnvVarValueModel.ConfigMapKeyValue) {
                EnvVarValueModel.ConfigMapKeyValue configMapKeyValue =
                        (EnvVarValueModel.ConfigMapKeyValue) v.getValueFrom();

                ConfigMapKeySelector configMapKey = new ConfigMapKeySelectorBuilder()
                        .withKey(configMapKeyValue.getKey())
                        .withName(configMapKeyValue.getName())
                        .build();
                EnvVarSource envVarSource = new EnvVarSourceBuilder().withConfigMapKeyRef(configMapKey).build();
                envVar = new EnvVarBuilder().withName(k).withValueFrom(envVarSource).build();
            }

            if (envVar != null) {
                envVars.add(envVar);
            }
        });
        return envVars;
    }
}
