/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.ballerinalang.net.http.HttpConstants;
import org.ballerinalang.util.codegen.AnnAttachmentInfo;
import org.ballerinalang.util.codegen.AnnAttributeValue;
import org.ballerinalang.util.codegen.ServiceInfo;
import org.ballerinax.kubernetes.KubernetesConstants;
import org.ballerinax.kubernetes.exceptions.KubernetesPluginException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

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
     * @param targetFilePath target file path
     * @throws IOException If an error occurs when writing to a file
     */
    public static void writeToFile(String context, String targetFilePath) throws IOException {
        File newFile = new File(targetFilePath);
        // delete if file exists
        if (newFile.exists() && newFile.delete()) {
            Files.write(Paths.get(targetFilePath), context.getBytes(StandardCharsets.UTF_8));
            return;
        }
        //create required directories
        if (newFile.getParentFile().mkdirs()) {
            Files.write(Paths.get(targetFilePath), context.getBytes(StandardCharsets.UTF_8));
            return;
        }
        Files.write(Paths.get(targetFilePath), context.getBytes(StandardCharsets.UTF_8));
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
     * Extract the ports from a given service.
     *
     * @param serviceInfo service info object
     * @return port exposed by service
     */
    public static int extractPort(ServiceInfo serviceInfo) {
        AnnAttachmentInfo annotationInfo = serviceInfo.getAnnotationAttachmentInfo(HttpConstants
                .HTTP_PACKAGE_PATH, HttpConstants.ANN_NAME_CONFIG);
        AnnAttributeValue portAttrVal = annotationInfo.getAttributeValue(HttpConstants.ANN_CONFIG_ATTR_PORT);
        if (portAttrVal != null && portAttrVal.getIntValue() > 0) {
            return Math.toIntExact(portAttrVal.getIntValue());
        }
        //TODO: remove this with actual port(s)
        return 9090;

    }

    /**
     * Extract the ballerina file name from a given file path
     *
     * @param balxFilePath balx file path.
     * @return output file name of balx
     */
    public static String extractBalxName(String balxFilePath) {
        return balxFilePath.substring(balxFilePath.lastIndexOf(File.separator) + 1, balxFilePath.lastIndexOf("" +
                ".balx"));
    }

    /**
     * Prints an Error message.
     *
     * @param msg message to be printed
     */
    public static void printError(String msg) {
        String ansiReset = "\u001B[0m";
        String ansiRed = "\u001B[31m";
        error.println(ansiRed + "error [Kubernetes plugin]: " + msg + ansiReset);
    }

    /**
     * Prints a debug message.
     *
     * @param msg message to be printed
     */
    public static void printDebug(String msg) {
        String ansiReset = "\u001B[0m";
        String ansiBlue = "\u001B[34m";
        if (debugEnabled) {
            out.println(ansiBlue + "debug: " + msg + ansiReset);
        }
    }

    /**
     * Prints an Instruction message.
     *
     * @param msg message to be printed
     */
    public static void printInstruction(String msg) {
        String ansiReset = "\u001B[0m";
        String ansiCyan = "\u001B[36m";
        out.println(ansiCyan + msg + ansiReset);
    }
}
