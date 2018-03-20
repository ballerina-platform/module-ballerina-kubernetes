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
import org.ballerinax.kubernetes.models.KubernetesDataHolder;
import org.ballerinax.kubernetes.models.SecretModel;
import org.ballerinax.kubernetes.models.ServiceModel;
import org.ballerinax.kubernetes.utils.KubernetesUtils;
import org.wso2.ballerinalang.compiler.tree.BLangEndpoint;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;

import java.io.File;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.ballerinax.kubernetes.utils.KubernetesUtils.printError;

/**
 * Compiler plugin to generate kubernetes artifacts.
 */
@SupportedAnnotationPackages(
        value = "ballerinax.kubernetes"
)
public class KubernetesPlugin extends AbstractCompilerPlugin {

    private static KubernetesDataHolder kubernetesDataHolder = new KubernetesDataHolder();
    private static boolean canProcess;
    private KubernetesAnnotationProcessor kubernetesAnnotationProcessor;
    private DiagnosticLog dlog;
    private PrintStream out = System.out;

    private static synchronized void setCanProcess(boolean val) {
        canProcess = val;
    }

    @Override
    public void init(DiagnosticLog diagnosticLog) {
        this.dlog = diagnosticLog;
        setCanProcess(false);
        this.kubernetesAnnotationProcessor = new KubernetesAnnotationProcessor();
    }

    @Override
    public void process(ServiceNode serviceNode, List<AnnotationAttachmentNode> annotations) {
        setCanProcess(true);
        Set<String> endpoints = extractEndpointName(serviceNode);
        for (AnnotationAttachmentNode attachmentNode : annotations) {
            String annotationKey = attachmentNode.getAnnotationName().getValue();
            switch (annotationKey) {
                case "ingress":
                    kubernetesDataHolder.addIngressModel(kubernetesAnnotationProcessor
                            .processIngressAnnotation
                                    (serviceNode.getName().getValue(), attachmentNode), endpoints);
                    break;
                case "hpa":
                    kubernetesDataHolder.setPodAutoscalerModel(kubernetesAnnotationProcessor
                            .processPodAutoscalerAnnotation(attachmentNode));
                    break;
                case "deployment":
                    kubernetesDataHolder.setDeploymentModel(kubernetesAnnotationProcessor.processDeployment
                            (attachmentNode));
                    break;
                case "secret":
                    try {
                        kubernetesDataHolder.addSecrets(kubernetesAnnotationProcessor.processSecrets(attachmentNode));
                    } catch (KubernetesPluginException e) {
                        dlog.logDiagnostic(Diagnostic.Kind.ERROR, serviceNode.getPosition(), e.getMessage());
                    }
                    break;
                case "configMap":
                    try {
                        kubernetesDataHolder.addConfigMaps(kubernetesAnnotationProcessor.processConfigMap
                                (attachmentNode));
                    } catch (KubernetesPluginException e) {
                        dlog.logDiagnostic(Diagnostic.Kind.ERROR, serviceNode.getPosition(), e.getMessage());
                    }
                    break;
                case "persistentVolumeClaim":
                    try {
                        kubernetesDataHolder.addPersistentVolumeClaims(
                                kubernetesAnnotationProcessor.processPersistentVolumeClaim(attachmentNode));
                    } catch (KubernetesPluginException e) {
                        dlog.logDiagnostic(Diagnostic.Kind.ERROR, serviceNode.getPosition(), e.getMessage());
                    }
                    break;
                default:
                    break;
            }
        }
    }


    private Set<String> extractEndpointName(ServiceNode serviceNode) {
        Set<String> endpoints = new HashSet<>();
        List<BLangSimpleVarRef> endpointList = ((BLangService) serviceNode).boundEndpoints;
        for (BLangSimpleVarRef var : endpointList) {
            endpoints.add(var.variableName.getValue());
        }
        return endpoints;
    }

    @Override
    public void process(EndpointNode endpointNode, List<AnnotationAttachmentNode> annotations) {
        String endpointName = endpointNode.getName().getValue();
        ServiceModel serviceModel = null;
        setCanProcess(true);
        for (AnnotationAttachmentNode attachmentNode : annotations) {
            String annotationKey = attachmentNode.getAnnotationName().getValue();
            switch (annotationKey) {
                case "svc":
                    serviceModel = kubernetesAnnotationProcessor.processServiceAnnotation(endpointName,
                            attachmentNode);
                    kubernetesDataHolder.addServiceModel(endpointName, serviceModel);
                    break;
                default:
                    break;
            }
        }
        List<BLangRecordLiteral.BLangRecordKeyValue> keyValues =
                ((BLangRecordLiteral) ((BLangEndpoint) endpointNode).configurationExpr).getKeyValuePairs();
        for (BLangRecordLiteral.BLangRecordKeyValue keyValue : keyValues) {
            String key = keyValue.getKey().toString();
            switch (key) {
                case "port":
                    int port = Integer.parseInt(keyValue.getValue().toString());
                    kubernetesDataHolder.addPort(port);
                    if (serviceModel != null) {
                        serviceModel.setPort(port);
                    }
                    break;
                case "ssl":
                    List<BLangRecordLiteral.BLangRecordKeyValue> sslKeyValues = ((BLangRecordLiteral) keyValue
                            .valueExpr).getKeyValuePairs();
                    try {
                        Set<SecretModel> secretModels = kubernetesAnnotationProcessor
                                .processSSLAnnotation(endpointName, sslKeyValues);
                        kubernetesDataHolder.addEndpointSecret(endpointName, secretModels);
                        kubernetesDataHolder.addSecrets(secretModels);
                    } catch (KubernetesPluginException e) {
                        dlog.logDiagnostic(Diagnostic.Kind.ERROR, endpointNode.getPosition(), e.getMessage());
                    }
                    break;
                default:
                    break;

            }
        }
    }


    @Override
    public void codeGenerated(Path binaryPath) {
        if (canProcess) {
            KubernetesAnnotationProcessor kubernetesAnnotationProcessor = new KubernetesAnnotationProcessor();
            String filePath = binaryPath.toAbsolutePath().toString();
            String userDir = new File(filePath).getParentFile().getAbsolutePath();
            String targetPath = userDir + File.separator + "kubernetes" + File
                    .separator;
            try {
                KubernetesUtils.deleteDirectory(targetPath);
                kubernetesAnnotationProcessor.
                        createArtifacts(kubernetesDataHolder, filePath, targetPath);
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
