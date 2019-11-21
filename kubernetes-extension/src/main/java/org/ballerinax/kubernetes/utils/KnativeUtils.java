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
import org.apache.commons.io.FileUtils;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.expressions.ExpressionNode;
import org.ballerinax.docker.generator.models.CopyFileModel;
import org.ballerinax.kubernetes.KubernetesConstants;
import org.ballerinax.kubernetes.exceptions.KubernetesPluginException;
//import org.ballerinax.kubernetes.models.DeploymentBuildExtension;
//import org.ballerinax.kubernetes.models.DeploymentModel;
//import org.ballerinax.kubernetes.models.JobModel;
//import org.ballerinax.kubernetes.models.KubernetesContext;
//import org.ballerinax.kubernetes.models.KubernetesDataHolder;
import org.ballerinax.kubernetes.models.knative.EnvVarValueModel;
import org.ballerinax.kubernetes.models.knative.KnativeContext;
import org.ballerinax.kubernetes.models.knative.KnativeDataHolder;
//import org.ballerinax.kubernetes.models.knative.ServiceModel;
////import org.ballerinax.kubernetes.models.openshift.OpenShiftBuildExtensionModel;
////import org.ballerinax.kubernetes.processors.openshift.OpenShiftBuildExtensionProcessor;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BConstantSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BFiniteType;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangListConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;

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
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.ballerinax.docker.generator.utils.DockerGenUtils.extractUberJarName;
import static org.ballerinax.kubernetes.KubernetesConstants.YAML;

/**
 * Util methods used for artifact generation.
 */
public class KnativeUtils {

    private static final boolean DEBUG_ENABLED = "true".equals(System.getenv(KubernetesConstants.ENABLE_DEBUG_LOGS));
    private static final PrintStream ERR = System.err;
    private static final PrintStream OUT = System.out;

    /**
     * Write content to a File. Create the required directories if they don't not exists.
     *
     * @param context        context of the file
     * @param outputFileName target file path
     * @throws IOException If an error occurs when writing to a file
     */
    public static void writeToFile(String context, String outputFileName) throws IOException {
        KnativeDataHolder dataHolder = KnativeContext.getInstance().getDataHolder();
        writeToFile(dataHolder.getK8sArtifactOutputPath(), context, outputFileName);
    }

    /**
     * Write content to a File. Create the required directories if they don't not exists.
     *
     * @param outputDir  Artifact output path.
     * @param context    Context of the file
     * @param fileSuffix Suffix for artifact.
     * @throws IOException If an error occurs when writing to a file
     */
    public static void writeToFile(Path outputDir, String context, String fileSuffix) throws IOException {
        KnativeDataHolder dataHolder = KnativeContext.getInstance().getDataHolder();
        // Priority given for job, then deployment.
        Path artifactFileName = outputDir.resolve(extractUberJarName(dataHolder.getUberJarPath()) + YAML);

        File newFile = artifactFileName.toFile();
        // append if file exists
        if (newFile.exists()) {
            Files.write(artifactFileName, context.getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.APPEND);
            return;
        }
        //create required directories
        if (newFile.getParentFile().mkdirs()) {
            Files.write(artifactFileName, context.getBytes(StandardCharsets.UTF_8));
            return;
        }
        Files.write(artifactFileName, context.getBytes(StandardCharsets.UTF_8));
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
                throw new KubernetesPluginException("unable to read contents of the file " + targetFilePath);
            }
        }
        throw new KubernetesPluginException("unable to read contents of the file " + targetFilePath);
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
            throw new KubernetesPluginException("error while copying file", e);
        }
    }

    /**
     * Prints an Information message.
     *
     * @param msg message to be printed
     */
    public static void printInfo(String msg) {
        OUT.println(msg);
    }

    /**
     * Prints an Error message.
     *
     * @param msg message to be printed
     */
    public static void printError(String msg) {
        ERR.println("error [k8s plugin]: " + msg);
    }

    /**
     * Prints a debug message.
     *
     * @param msg message to be printed
     */
    public static void printDebug(String msg) {
        if (DEBUG_ENABLED) {
            OUT.println("debug [k8s plugin]: " + msg);
        }
    }

    /**
     * Print warning message.
     *
     * @param message Message content.
     */
    public static void printWarning(String message) {
        OUT.println("warning [k8s plugin]: " + message);
    }

    /**
     * Prints an Instruction message.
     *
     * @param msg message to be printed
     */
    public static void printInstruction(String msg) {
        OUT.println(msg);
    }

    /**
     * Deletes a given directory.
     *
     * @param path path to directory
     * @throws KubernetesPluginException if an error occurs while deleting
     */
    public static void deleteDirectory(Path path) throws KubernetesPluginException {
        Path pathToBeDeleted = path.toAbsolutePath();
        if (!Files.exists(pathToBeDeleted)) {
            return;
        }
        try {
            Files.walk(pathToBeDeleted)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        } catch (IOException e) {
            throw new KubernetesPluginException("unable to delete directory: " + path, e);
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
     * Resolve the given value by processing $env{} place-holders.
     *
     * @param value The user provided value
     * @return The resolved value
     */
    public static String resolveValue(String value) throws KubernetesPluginException {
        int startIndex;
        if ((startIndex = value.indexOf("$env{")) >= 0) {
            int endIndex = value.indexOf("}", startIndex);
            if (endIndex > 0) {
                String varName = value.substring(startIndex + 5, endIndex).trim();
                String resolvedVar = Optional.ofNullable(System.getenv(varName)).orElseThrow(() ->
                        new KubernetesPluginException("error resolving value: " + varName +
                                " is not set in the environment."));
                String rest = (value.length() > endIndex + 1) ? resolveValue(value.substring(endIndex + 1)) : "";
                return value.substring(0, startIndex) + resolvedVar + rest;
            }
        }
        return value;
    }

    /**
     * Generate array of string using a {@link BLangListConstructorExpr}.
     *
     * @param expr Array literal.
     * @return Convert string.
     */
    public static List<String> getList(BLangExpression expr) throws KubernetesPluginException {
        if (expr.getKind() != NodeKind.LIST_CONSTRUCTOR_EXPR) {
            throw new KubernetesPluginException("unable to parse value: " + expr.toString());
        } else {
            List<String> scopeSet = new LinkedList<>();
            if (expr instanceof BLangListConstructorExpr) {
                BLangListConstructorExpr array = (BLangListConstructorExpr) expr;
                for (ExpressionNode bLangExpression : array.getExpressions()) {
                    scopeSet.add(getStringValue((BLangExpression) bLangExpression));
                }
            }
            return scopeSet;
        }
    }

        /**
         * Get a map from a ballerina expression.
         *
         * @param expr Ballerina record value.
         * @return Map of key values.
         * @throws KubernetesPluginException When the expression cannot be parsed.
         */
        public static Map<String, String> getMap(BLangExpression expr) throws KubernetesPluginException {
            if (expr.getKind() != NodeKind.RECORD_LITERAL_EXPR) {
                throw new KubernetesPluginException("unable to parse value: " + expr.toString());
            } else {
                Map<String, String> map = new LinkedHashMap<>();
                if (expr instanceof BLangRecordLiteral) {
                BLangRecordLiteral fields = (BLangRecordLiteral) expr;
                    for (BLangRecordLiteral.BLangRecordKeyValue keyValue : fields.getKeyValuePairs()) {
                        map.put(keyValue.getKey().toString(), getStringValue(keyValue.getValue()));
                    }
                }
                return map;
            }

        }

    /**
     * Get the boolean value from a ballerina expression.
     *
     * @param expr Ballerina boolean value.
     * @return Parsed boolean value.
     * @throws KubernetesPluginException When the expression cannot be parsed.
     */
    public static boolean getBooleanValue(BLangExpression expr) throws KubernetesPluginException {
        return Boolean.parseBoolean(getStringValue(expr));
    }

    /**
     * Get the long value from a ballerina expression.
     *
     * @param expr Ballerina integer value.
     * @return Parsed long value.
     * @throws KubernetesPluginException When the expression cannot be parsed.
     */
    public static long getLongValue(BLangExpression expr) throws KubernetesPluginException {
        return Long.parseLong(getStringValue(expr));
    }

    /**
     * Get the integer value from a ballerina expression.
     *
     * @param expr Ballerina integer value.
     * @return Parsed integer value.
     * @throws KubernetesPluginException When the expression cannot be parsed.
     */
    public static int getIntValue(BLangExpression expr) throws KubernetesPluginException {
        return Integer.parseInt(getStringValue(expr));
    }

    /**
     * Get the string value from a ballerina expression.
     *
     * @param expr Ballerina string value.
     * @return Parsed string value.
     * @throws KubernetesPluginException When the expression cannot be parsed.
     */
    public static String getStringValue(BLangExpression expr) throws KubernetesPluginException {
        if (expr instanceof BLangSimpleVarRef) {
            BLangSimpleVarRef varRef = (BLangSimpleVarRef) expr;
            final BSymbol symbol = varRef.symbol;
            if (symbol instanceof BConstantSymbol) {
                        BConstantSymbol constantSymbol = (BConstantSymbol) symbol;
                        if (constantSymbol.type instanceof BFiniteType) {
                                // Parse compile time constant
                                BFiniteType compileConst = (BFiniteType) constantSymbol.type;
                                if (compileConst.valueSpace.size() > 0) {
                                    return resolveValue(compileConst.valueSpace.iterator().next().toString());
                                }
                        }
                }
        } else if (expr instanceof BLangLiteral) {
            return resolveValue(expr.toString());
        }
        throw new KubernetesPluginException("unable to parse value: " + expr.toString());
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
    public static Map<String, EnvVarValueModel> getEnvVarMap(BLangExpression envVarValues)
            throws KubernetesPluginException {
        Map<String, EnvVarValueModel> envVarMap = new LinkedHashMap<>();
        if (envVarValues.getKind() == NodeKind.RECORD_LITERAL_EXPR) {
            if (envVarValues instanceof BLangRecordLiteral) {
                for (BLangRecordLiteral.BLangRecordKeyValue envVar
                        : ((BLangRecordLiteral) envVarValues).keyValuePairs) {
                    String envVarName = envVar.getKey().toString();
                    EnvVarValueModel envVarValue = null;
                    if (envVar.getValue().getKind() == NodeKind.LITERAL) {
                        // Value is a string
                        envVarValue = new EnvVarValueModel(getStringValue(envVar.getValue()));
                    } else if (envVar.getValue().getKind() == NodeKind.RECORD_LITERAL_EXPR) {
                        BLangRecordLiteral valueFrom = (BLangRecordLiteral) envVar.getValue();
                        BLangRecordLiteral.BLangRecordKeyValue bRefType = valueFrom.getKeyValuePairs().get(0);
                        BLangSimpleVarRef refType = (BLangSimpleVarRef) bRefType.getKey();
                        switch (refType.variableName.toString()) {
                            case "fieldRef":
                                BLangRecordLiteral.BLangRecordKeyValue fieldRefValue =
                                        ((BLangRecordLiteral) bRefType.getValue()).getKeyValuePairs().get(0);
                                EnvVarValueModel.FieldRef fieldRefModel = new EnvVarValueModel.FieldRef();
                                fieldRefModel.setFieldPath(getStringValue(fieldRefValue.getValue()));
                                envVarValue = new EnvVarValueModel(fieldRefModel);
                                break;
                            case "secretKeyRef":
                                EnvVarValueModel.SecretKeyRef secretKeyRefModel = new EnvVarValueModel.SecretKeyRef();
                                for (BLangRecordLiteral.BLangRecordKeyValue secretKeyRefFields :
                                        ((BLangRecordLiteral) bRefType.getValue()).getKeyValuePairs()) {
                                    if (secretKeyRefFields.getKey().toString().equals("key")) {
                                        secretKeyRefModel.setKey(getStringValue(secretKeyRefFields.getValue()));
                                    } else if (secretKeyRefFields.getKey().toString().equals("name")) {
                                        secretKeyRefModel.setName(getStringValue(secretKeyRefFields.getValue()));
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
                                                getStringValue(resourceFieldRefFields.getValue()));
                                    } else if (resourceFieldRefFields.getKey().toString().equals("resource")) {
                                        resourceFieldRefModel.setResource(
                                                getStringValue(resourceFieldRefFields.getValue()));
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
                                        configMapKeyRefModel.setKey(getStringValue(configMapKeyRefFields.getValue()));
                                    } else if (configMapKeyRefFields.getKey().toString().equals("name")) {
                                        configMapKeyRefModel.setName(getStringValue(configMapKeyRefFields.getValue()));
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
        }
        return envVarMap;
    }

    /**
     * Get Image pull secrets.
     *
     * @param keyValue Value of imagePullSecret field of Job annotation.
     * @return A set of image pull secrets.
     */
    public static Set<String> getImagePullSecrets(BLangRecordLiteral.BLangRecordKeyValue keyValue) throws
            KubernetesPluginException {
        Set<String> imagePullSecrets = new HashSet<>();
        List<BLangExpression> configAnnotation = ((BLangListConstructorExpr) keyValue.valueExpr).exprs;
        for (BLangExpression bLangExpression : configAnnotation) {
            imagePullSecrets.add(getStringValue(bLangExpression));
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
    public static Set<CopyFileModel> getExternalFileMap(BLangRecordLiteral.BLangRecordKeyValue keyValue) throws
            KubernetesPluginException {
        Set<CopyFileModel> externalFiles = new HashSet<>();
        List<BLangExpression> configAnnotation = ((BLangListConstructorExpr) keyValue.valueExpr).exprs;
        for (BLangExpression bLangExpression : configAnnotation) {
            List<BLangRecordLiteral.BLangRecordKeyValue> annotationValues =
                    ((BLangRecordLiteral) bLangExpression).getKeyValuePairs();
            CopyFileModel externalFileModel = new CopyFileModel();
            for (BLangRecordLiteral.BLangRecordKeyValue annotation : annotationValues) {
                switch (annotation.getKey().toString()) {
                    case "sourceFile":
                        externalFileModel.setSource(getStringValue(annotation.getValue()));
                        break;
                    case "target":
                        externalFileModel.setTarget(getStringValue(annotation.getValue()));
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
