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
import org.ballerinax.kubernetes.models.ServiceModel;
import org.ballerinax.kubernetes.utils.KubernetesUtils;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.BLangEndpoint;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangArrayLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;

import java.io.File;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

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
        List<String> endpoints = extractEndpointName(serviceNode);
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
                default:
                    break;
            }
        }
    }

    //TODO: Remove this after new ServiceNode implementation
    private List<String> extractEndpointName(ServiceNode serviceNode) {
        List<String> endpoints = new ArrayList<>();
        List<? extends AnnotationAttachmentNode> attachmentNodes = serviceNode.getAnnotationAttachments();
        for (AnnotationAttachmentNode attachmentNode : attachmentNodes) {
            String annotationType = attachmentNode.getAnnotationName().getValue();
            if ("serviceConfig".equals(annotationType)) {
                List<BLangRecordLiteral.BLangRecordKeyValue> keyValues =
                        ((BLangRecordLiteral) ((BLangAnnotationAttachment) attachmentNode).expr).getKeyValuePairs();
                for (BLangRecordLiteral.BLangRecordKeyValue keyValue : keyValues) {
                    final String key = ((BLangSimpleVarRef) keyValue.getKey()).variableName.value;
                    if (!key.equals("endpoints")) {
                        continue;
                    }
                    final List<BLangExpression> endpointExp = ((BLangArrayLiteral) keyValue.getValue()).exprs;
                    for (BLangExpression endpoint : endpointExp) {
                        if (endpoint instanceof BLangSimpleVarRef) {
                            endpoints.add(endpoint.toString());
                        }
                    }
                }
            }
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
                    kubernetesAnnotationProcessor.extractSSLConfigurations(endpointName, sslKeyValues);
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
                dlog.logDiagnostic(Diagnostic.Kind.ERROR, null, e.getMessage());
            }
        }
    }
}
