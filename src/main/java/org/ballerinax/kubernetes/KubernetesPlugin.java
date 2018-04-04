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

package org.ballerinax.kubernetes;

import org.ballerinalang.compiler.plugins.AbstractCompilerPlugin;
import org.ballerinalang.compiler.plugins.SupportedAnnotationPackages;
import org.ballerinalang.model.tree.AnnotationAttachmentNode;
import org.ballerinalang.model.tree.EndpointNode;
import org.ballerinalang.model.tree.ServiceNode;
import org.ballerinalang.util.diagnostic.Diagnostic;
import org.ballerinalang.util.diagnostic.DiagnosticLog;
import org.ballerinax.kubernetes.exceptions.KubernetesPluginException;
import org.ballerinax.kubernetes.processors.AnnotationProcessorFactory;
import org.ballerinax.kubernetes.utils.KubernetesUtils;

import java.io.File;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.List;

import static org.ballerinax.kubernetes.utils.KubernetesUtils.extractBalxName;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.printError;

/**
 * Compiler plugin to generate kubernetes artifacts.
 */
@SupportedAnnotationPackages(
        value = "ballerinax.kubernetes"
)
public class KubernetesPlugin extends AbstractCompilerPlugin {

    private static boolean canProcess;
    private DiagnosticLog dlog;
    private PrintStream out;

    private static synchronized void setCanProcess(boolean val) {
        canProcess = val;
    }

    @Override
    public void init(DiagnosticLog diagnosticLog) {
        this.dlog = diagnosticLog;
        setCanProcess(false);
        out = System.out;
    }

    @Override
    public void process(ServiceNode serviceNode, List<AnnotationAttachmentNode> annotations) {
        setCanProcess(true);
        for (AnnotationAttachmentNode attachmentNode : annotations) {
            String annotationKey = attachmentNode.getAnnotationName().getValue();
            try {
                AnnotationProcessorFactory.getAnnotationProcessorInstance(annotationKey).processAnnotation
                        (serviceNode, attachmentNode);
            } catch (KubernetesPluginException e) {
                dlog.logDiagnostic(Diagnostic.Kind.ERROR, serviceNode.getPosition(), e.getMessage());
            }
        }
    }


    @Override
    public void process(EndpointNode endpointNode, List<AnnotationAttachmentNode> annotations) {
        setCanProcess(true);
        for (AnnotationAttachmentNode attachmentNode : annotations) {
            String annotationKey = attachmentNode.getAnnotationName().getValue();
            try {
                AnnotationProcessorFactory.getAnnotationProcessorInstance(annotationKey).processAnnotation
                        (endpointNode, attachmentNode);
            } catch (KubernetesPluginException e) {
                dlog.logDiagnostic(Diagnostic.Kind.ERROR, endpointNode.getPosition(), e.getMessage());
            }
        }

    }


    @Override
    public void codeGenerated(Path binaryPath) {
        if (canProcess) {

            String filePath = binaryPath.toAbsolutePath().toString();
            String userDir = new File(filePath).getParentFile().getAbsolutePath();
            if (userDir.endsWith("target")) {
                //Compiling package therefore append balx file name to kubernetes artifact dir path
                userDir = userDir + File.separator + extractBalxName(filePath);
            }
            String targetPath = userDir + File.separator + "kubernetes" + File
                    .separator;
            ArtifactManager artifactManager = new ArtifactManager(filePath, targetPath);
            try {
                KubernetesUtils.deleteDirectory(targetPath);
                artifactManager.createArtifacts();
            } catch (KubernetesPluginException e) {
                out.println();
                printError(e.getMessage());
                dlog.logDiagnostic(Diagnostic.Kind.ERROR, null, e.getMessage());
                try {
                    KubernetesUtils.deleteDirectory(targetPath);
                } catch (KubernetesPluginException ignored) {
                }
            }
        }
    }
}
