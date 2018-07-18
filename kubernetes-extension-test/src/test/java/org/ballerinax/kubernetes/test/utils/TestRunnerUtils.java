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

package org.ballerinax.kubernetes.test.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Kubernetes Integration test utils.
 */
public class TestRunnerUtils {

    private static final Log log = LogFactory.getLog(TestRunnerUtils.class);
    private static final String distributionPath = System.getProperty("ballerina.pack");
    private static final String ballerinaCommand = distributionPath + File.separator + "ballerina";
    private static final String buildCommand = "build";
    private static final String executing = "Executing command: ";

    private static void logOutput(InputStream inputStream) throws IOException {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(inputStream));
            br.lines().forEach(log::info);
        } finally {
            if (br != null) {
                br.close();
            }
        }
    }


    public static int compileBallerinaFile(String directoryPath, String fileName) throws InterruptedException,
            IOException {
        ProcessBuilder pb = new ProcessBuilder
                (ballerinaCommand, buildCommand, fileName);
        log.info(executing + pb.command());
        pb.directory(new File(directoryPath));
        Process process = pb.start();
        int exitCode = process.waitFor();
        log.info("Exit Code:" + exitCode);
        logOutput(process.getInputStream());
        logOutput(process.getErrorStream());
        return exitCode;
    }

    public static int compileBallerinaProject(String directoryPath) throws InterruptedException,
            IOException {
        ProcessBuilder pb = new ProcessBuilder
                (ballerinaCommand, "init");
        log.info(executing + pb.command());
        pb.directory(new File(directoryPath));
        Process process = pb.start();
        int exitCode = process.waitFor();
        log.info("Exit Code:" + exitCode);
        logOutput(process.getInputStream());
        logOutput(process.getErrorStream());

        pb = new ProcessBuilder
                (ballerinaCommand, buildCommand);
        log.info(executing + pb.command());
        pb.directory(new File(directoryPath));
        process = pb.start();
        exitCode = process.waitFor();
        log.info("Exit Code:" + exitCode);
        logOutput(process.getInputStream());
        logOutput(process.getErrorStream());
        return exitCode;
    }


}
