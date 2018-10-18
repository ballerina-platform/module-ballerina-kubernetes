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

import org.ballerinalang.model.tree.expressions.ExpressionNode;
import org.ballerinax.kubernetes.KubernetesConstants;
import org.ballerinax.kubernetes.exceptions.KubernetesPluginException;
import org.ballerinax.kubernetes.models.DeploymentModel;
import org.ballerinax.kubernetes.models.KubernetesContext;
import org.ballerinax.kubernetes.models.KubernetesDataHolder;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangArrayLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
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
     * Copy file from source to destination.
     *
     * @param source      source file path
     * @param destination destination file path
     */
    public static void copyFile(String source, String destination) throws KubernetesPluginException {
        File sourceFile = new File(source);
        File destinationFile = new File(destination);
        try (FileInputStream fileInputStream = new FileInputStream(sourceFile);
             FileOutputStream fileOutputStream = new FileOutputStream(destinationFile)) {
            int bufferSize;
            byte[] buffer = new byte[512];
            while ((bufferSize = fileInputStream.read(buffer)) > 0) {
                fileOutputStream.write(buffer, 0, bufferSize);
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
     * Generate set of string using a {@link BLangArrayLiteral}.
     * @param bArrayLiteral Array literal.
     * @return Convert string.
     */
    public static Set<String> getArray(BLangArrayLiteral bArrayLiteral) {
        Set<String> scopeSet = new LinkedHashSet<>();
        for (ExpressionNode bLangExpression : bArrayLiteral.getExpressions()) {
            scopeSet.add(bLangExpression.toString());
        }
        return scopeSet;
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

}
