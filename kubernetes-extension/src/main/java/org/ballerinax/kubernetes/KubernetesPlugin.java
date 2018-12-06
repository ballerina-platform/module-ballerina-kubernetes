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
import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.tree.AnnotationAttachmentNode;
import org.ballerinalang.model.tree.FunctionNode;
import org.ballerinalang.model.tree.PackageNode;
import org.ballerinalang.model.tree.ServiceNode;
import org.ballerinalang.model.tree.SimpleVariableNode;
import org.ballerinalang.util.diagnostic.Diagnostic;
import org.ballerinalang.util.diagnostic.DiagnosticLog;
import org.ballerinax.kubernetes.exceptions.KubernetesPluginException;
import org.ballerinax.kubernetes.models.KubernetesContext;
import org.ballerinax.kubernetes.models.KubernetesDataHolder;
import org.ballerinax.kubernetes.processors.AnnotationProcessorFactory;
import org.ballerinax.kubernetes.utils.DependencyValidator;
import org.ballerinax.kubernetes.utils.KubernetesUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.ballerinax.kubernetes.KubernetesConstants.KUBERNETES;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.extractBalxName;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.printError;

/**
 * Compiler plugin to generate kubernetes artifacts.
 */
@SupportedAnnotationPackages(
        value = "ballerinax/kubernetes:0.0.0"
)
public class KubernetesPlugin extends AbstractCompilerPlugin {
    private static final Logger pluginLog = LoggerFactory.getLogger(KubernetesPlugin.class);
    private DiagnosticLog dlog;

    @Override
    public void init(DiagnosticLog diagnosticLog) {
        this.dlog = diagnosticLog;
    }
    
    @Override
    public void process(PackageNode packageNode) {
        BLangPackage bPackage = (BLangPackage) packageNode;
        KubernetesContext.getInstance().addDataHolder(bPackage.packageID);
    }
    
    @Override
    public void process(ServiceNode serviceNode, List<AnnotationAttachmentNode> annotations) {
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
    public void process(SimpleVariableNode variableNode, List<AnnotationAttachmentNode> annotations) {
        if (!variableNode.getFlags().contains(Flag.LISTENER)) {
            dlog.logDiagnostic(Diagnostic.Kind.ERROR, variableNode.getPosition(), "@kubernetes annotations are only " +
                    "supported with listeners.");
            return;
        }
        for (AnnotationAttachmentNode attachmentNode : annotations) {
            String annotationKey = attachmentNode.getAnnotationName().getValue();
            try {
                AnnotationProcessorFactory.getAnnotationProcessorInstance(annotationKey).processAnnotation
                        (variableNode, attachmentNode);
            } catch (KubernetesPluginException e) {
                dlog.logDiagnostic(Diagnostic.Kind.ERROR, variableNode.getPosition(), e.getMessage());
            }
        }

    }

    @Override
    public void process(FunctionNode functionNode, List<AnnotationAttachmentNode> annotations) {
        for (AnnotationAttachmentNode attachmentNode : annotations) {
            String annotationKey = attachmentNode.getAnnotationName().getValue();
            try {
                AnnotationProcessorFactory.getAnnotationProcessorInstance(annotationKey).processAnnotation
                        (functionNode, attachmentNode);
            } catch (KubernetesPluginException e) {
                dlog.logDiagnostic(Diagnostic.Kind.ERROR, functionNode.getPosition(), e.getMessage());
            }
        }

    }


    @Override
    public void codeGenerated(PackageID packageID, Path binaryPath) {
        KubernetesContext.getInstance().setCurrentPackage(packageID);
        KubernetesDataHolder dataHolder = KubernetesContext.getInstance().getDataHolder();
        if (dataHolder.isCanProcess()) {
            String filePath = binaryPath.toAbsolutePath().toString();
            String userDir = new File(filePath).getParentFile().getAbsolutePath();
            String targetPath = userDir + File.separator + KUBERNETES + File.separator;
            if (userDir.endsWith("target")) {
                //Compiling package therefore append balx file name to docker artifact dir path
                targetPath = userDir + File.separator + KUBERNETES + File.separator + extractBalxName(filePath);
            }
            dataHolder.setBalxFilePath(filePath);
            dataHolder.setOutputDir(targetPath);
            ArtifactManager artifactManager = new ArtifactManager(targetPath);
            try {
                KubernetesUtils.deleteDirectory(targetPath);
                artifactManager.populateDeploymentModel();
                validateDeploymentDependencies();
                artifactManager.createArtifacts();
            } catch (KubernetesPluginException e) {
                String errorMessage = "package [" + packageID + "] " + e.getMessage();
                printError(errorMessage);
                pluginLog.error(errorMessage, e);
                try {
                    KubernetesUtils.deleteDirectory(targetPath);
                } catch (KubernetesPluginException ignored) {
                    //ignored
                }
            }
        }
    }

    private void validateDeploymentDependencies() throws KubernetesPluginException {
        KubernetesContext context = KubernetesContext.getInstance();
        Map<PackageID, KubernetesDataHolder> packageToDataHolderMap = context.getPackageIDtoDataHolderMap();
        DependencyValidator dependencyValidator = new DependencyValidator();
        for (KubernetesDataHolder dataHolder : packageToDataHolderMap.values()) {
            //add other dependent deployments
            List<String> dependencies = new ArrayList<>();
            //add the current deployment as 0th element
            String currentDeployment = dataHolder.getDeploymentModel().getName();
            if (currentDeployment == null) {
                return;
            }
            dependencies.add(currentDeployment);
            Set<String> dependsOn = dataHolder.getDeploymentModel().getDependsOn();
            for (String listenerName : dependsOn) {
                String dependentDeployment = context.getDeploymentNameFromListener(listenerName);
                if (dependentDeployment == null) {
                    return;
                }
                if (!dependentDeployment.equals(currentDeployment)) {
                    dependencies.add(dependentDeployment);
                } else {
                    // Listener is in the same package.
                    throw new KubernetesPluginException("@kubernetes:Deployment{} contains cyclic dependencies");
                }
            }
            String[] array = dependencies.toArray(new String[0]);
            if (!dependencyValidator.validateDependency(array)) {
                throw new KubernetesPluginException("@kubernetes:Deployment{} contains cyclic dependencies");
            }
        }
    }

}
