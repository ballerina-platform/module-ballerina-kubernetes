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

import org.ballerinalang.compiler.JarResolver;
import org.ballerinalang.compiler.plugins.AbstractCompilerPlugin;
import org.ballerinalang.compiler.plugins.SupportedAnnotationPackages;
import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.tree.AnnotationAttachmentNode;
import org.ballerinalang.model.tree.FunctionNode;
import org.ballerinalang.model.tree.PackageNode;
import org.ballerinalang.model.tree.ServiceNode;
import org.ballerinalang.model.tree.SimpleVariableNode;
import org.ballerinalang.model.tree.TopLevelNode;
import org.ballerinalang.model.tree.expressions.ExpressionNode;
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
import org.wso2.ballerinalang.compiler.SourceDirectory;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeInit;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.ballerinalang.compiler.JarResolver.JAR_RESOLVER_KEY;
import static org.ballerinax.docker.generator.utils.DockerGenUtils.extractJarName;
import static org.ballerinax.kubernetes.KubernetesConstants.DOCKER;
import static org.ballerinax.kubernetes.KubernetesConstants.KUBERNETES;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.createAnnotation;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.printError;

/**
 * Compiler plugin to generate kubernetes artifacts.
 */
@SupportedAnnotationPackages(
        value = {"ballerina/kubernetes", "ballerina/istio", "ballerina/openshift"}
)
public class KubernetesPlugin extends AbstractCompilerPlugin {
    private static final Logger pluginLog = LoggerFactory.getLogger(KubernetesPlugin.class);
    private DiagnosticLog dlog;
    private SourceDirectory sourceDirectory;

    @Override
    public void setCompilerContext(CompilerContext context) {
        this.sourceDirectory = context.get(SourceDirectory.class);
        if (this.sourceDirectory == null) {
            throw new IllegalArgumentException("source directory has not been initialized");
        }
        KubernetesContext.getInstance().setCompilerContext(context);
    }

    @Override
    public void init(DiagnosticLog diagnosticLog) {
        this.dlog = diagnosticLog;
    }

    @Override
    public void process(PackageNode packageNode) {
        BLangPackage bPackage = (BLangPackage) packageNode;
        KubernetesContext.getInstance().addDataHolder(bPackage.packageID, sourceDirectory.getPath());

        //Get dependency jar paths
        JarResolver jarResolver = KubernetesContext.getInstance().getCompilerContext().get(JAR_RESOLVER_KEY);
        Set<Path> dependencyJarPaths = new HashSet<>(jarResolver.allDependencies(bPackage));
        KubernetesContext.getInstance().getDataHolder(bPackage.packageID).getDockerModel()
                .addDependencyJarPaths(dependencyJarPaths);

        // Get the imports with alias _
        List<BLangImportPackage> kubernetesImports = bPackage.getImports().stream()
                .filter(i -> i.symbol.toString().startsWith("ballerina/kubernetes") &&
                        i.getAlias().toString().equals("_"))
                .collect(Collectors.toList());

        if (kubernetesImports.size() > 0) {
            for (BLangImportPackage kubernetesImport : kubernetesImports) {
                // Get the units of the file which has kubernetes import as _
                List<TopLevelNode> topLevelNodes = bPackage.getCompilationUnits().stream()
                        .filter(cu -> cu.getName().equals(kubernetesImport.compUnit.getValue()))
                        .flatMap(cu -> cu.getTopLevelNodes().stream())
                        .collect(Collectors.toList());

                // Filter out the services
                List<ServiceNode> serviceNodes = topLevelNodes.stream()
                        .filter(tln -> tln instanceof ServiceNode)
                        .map(tln -> (ServiceNode) tln)
                        .collect(Collectors.toList());

                // Generate artifacts for services for all services
                serviceNodes.forEach(sn -> process(sn, Collections.singletonList(createAnnotation("Deployment"))));

                // Filter services with 'new Listener()' and generate services
                for (ServiceNode serviceNode : serviceNodes) {
                    Optional<? extends ExpressionNode> initListener = serviceNode.getAttachedExprs().stream()
                            .filter(aex -> aex instanceof BLangTypeInit)
                            .findAny();

                    if (initListener.isPresent()) {
                        serviceNodes.forEach(sn -> process(sn, Collections.singletonList(createAnnotation("Service"))));
                    }
                }

                // Get the variable names of the listeners attached to services
                List<String> listenerNamesToExpose = serviceNodes.stream()
                        .map(ServiceNode::getAttachedExprs)
                        .flatMap(Collection::stream)
                        .filter(aex -> aex instanceof BLangSimpleVarRef)
                        .map(aex -> (BLangSimpleVarRef) aex)
                        .map(BLangSimpleVarRef::toString)
                        .collect(Collectors.toList());

                // Generate artifacts for listeners attached to services
                topLevelNodes.stream()
                        .filter(tln -> tln instanceof SimpleVariableNode)
                        .map(tln -> (SimpleVariableNode) tln)
                        .filter(listener -> listenerNamesToExpose.contains(listener.getName().getValue()))
                        .forEach(listener -> process(listener, Collections.singletonList(createAnnotation("Service"))));

                // Generate artifacts for main functions
                topLevelNodes.stream()
                        .filter(tln -> tln instanceof FunctionNode)
                        .map(tln -> (FunctionNode) tln)
                        .filter(fn -> "main".equals(fn.getName().getValue()))
                        .forEach(fn -> process(fn, Collections.singletonList(createAnnotation("Job"))));
            }
        }
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
    public void codeGenerated(PackageID moduleID, Path executableJarFile) {
        KubernetesContext.getInstance().setCurrentPackage(moduleID);
        KubernetesDataHolder dataHolder = KubernetesContext.getInstance().getDataHolder();
        if (dataHolder.isCanProcess()) {
            executableJarFile = executableJarFile.toAbsolutePath();
            if (null != executableJarFile.getParent() && Files.exists(executableJarFile.getParent())) {
                // artifacts location for a single bal file.
                Path kubernetesOutputPath = executableJarFile.getParent().resolve(KUBERNETES);
                Path dockerOutputPath = executableJarFile.getParent().resolve(DOCKER);
                if (null != executableJarFile.getParent().getParent().getParent() &&
                        Files.exists(executableJarFile.getParent().getParent().getParent())) {
                    // if executable came from a ballerina project
                    Path projectRoot = executableJarFile.getParent().getParent().getParent();
                    if (Files.exists(projectRoot.resolve("Ballerina.toml"))) {
                        dataHolder.setProject(true);
                        kubernetesOutputPath = projectRoot.resolve("target")
                                .resolve(KUBERNETES)
                                .resolve(extractJarName(executableJarFile));
                        dockerOutputPath = projectRoot.resolve("target")
                                .resolve(DOCKER)
                                .resolve(extractJarName(executableJarFile));
                    }
                }
                if (!dataHolder.getDockerModel().isUberJar()) {
                    JarResolver jarResolver =
                            KubernetesContext.getInstance().getCompilerContext().get(JAR_RESOLVER_KEY);
                    executableJarFile = jarResolver.moduleJar(moduleID);
                }
                dataHolder.setUberJarPath(executableJarFile);
                dataHolder.setK8sArtifactOutputPath(kubernetesOutputPath);
                dataHolder.setDockerArtifactOutputPath(dockerOutputPath);
                ArtifactManager artifactManager = new ArtifactManager();
                try {
                    KubernetesUtils.deleteDirectory(kubernetesOutputPath);
                    artifactManager.populateDeploymentModel();
                    validateDeploymentDependencies();
                    artifactManager.createArtifacts();
                } catch (KubernetesPluginException e) {
                    String errorMessage = "module [" + moduleID + "] " + e.getMessage();
                    printError(errorMessage);
                    pluginLog.error(errorMessage, e);
                    try {
                        KubernetesUtils.deleteDirectory(kubernetesOutputPath);
                    } catch (KubernetesPluginException ignored) {
                        //ignored
                    }
                }
            } else {
                printError("error in resolving docker generation location.");
                pluginLog.error("error in resolving docker generation location.");
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
