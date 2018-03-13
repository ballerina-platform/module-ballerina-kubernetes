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
import org.ballerinalang.model.tree.Node;
import org.ballerinalang.model.tree.PackageNode;
import org.ballerinalang.model.tree.ServiceNode;
import org.ballerinalang.util.diagnostic.Diagnostic;
import org.ballerinalang.util.diagnostic.DiagnosticLog;
import org.ballerinax.kubernetes.exceptions.KubernetesPluginException;
import org.ballerinax.kubernetes.models.DeploymentModel;
import org.ballerinax.kubernetes.models.IngressModel;
import org.ballerinax.kubernetes.models.KubernetesDataHolder;
import org.ballerinax.kubernetes.models.PodAutoscalerModel;
import org.ballerinax.kubernetes.models.ServiceModel;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangAnnotAttachmentAttribute;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import static org.ballerinax.kubernetes.KubeGenConstants.BALLERINA_NET_HTTP;
import static org.ballerinax.kubernetes.utils.KubeGenUtils.printInfo;
import static org.ballerinax.kubernetes.utils.KubeGenUtils.printInstruction;

/**
 * Compiler plugin to generate kubernetes artifacts.
 */
@SupportedAnnotationPackages(
        value = "ballerinax.kubernetes"
)
public class KubernetesPlugin extends AbstractCompilerPlugin {

    static KubernetesDataHolder kubernetesDataHolder = new KubernetesDataHolder();
    KubeAnnotationProcessor kubeAnnotationProcessor;
    private boolean canProcess = false;
    private DiagnosticLog dlog;

    @Override
    public void init(DiagnosticLog diagnosticLog) {
        this.dlog = diagnosticLog;
        this.kubeAnnotationProcessor = new KubeAnnotationProcessor();
    }

    @Override
    public void process(PackageNode packageNode) {
        // extract port values from services.
        List<? extends ServiceNode> serviceNodes = packageNode.getServices();
        for (ServiceNode serviceNode : serviceNodes) {
            List<? extends AnnotationAttachmentNode> annotationAttachmentNodes = serviceNode.getAnnotationAttachments();
            for (AnnotationAttachmentNode annotationAttachmentNode : annotationAttachmentNodes) {
                String packageID = ((BLangAnnotationAttachment) annotationAttachmentNode).
                        annotationSymbol.pkgID.name.value;
                if (BALLERINA_NET_HTTP.equals(packageID)) {
                    List<BLangAnnotAttachmentAttribute> bLangAnnotationAttachments = ((BLangAnnotationAttachment)
                            annotationAttachmentNode).attributes;
                    for (BLangAnnotAttachmentAttribute annotationAttribute : bLangAnnotationAttachments) {
                        String annotationKey = annotationAttribute.name.value;
                        if ("port".equals(annotationKey)) {
                            Node annotationValue = annotationAttribute.getValue().getValue();
                            kubernetesDataHolder.addPort(Integer.parseInt(annotationValue.toString()));
                        }
                    }
                }
            }
        }
    }


    @Override
    public void process(ServiceNode serviceNode, List<AnnotationAttachmentNode> annotations) {
        printInfo(serviceNode.getName().getValue());
        canProcess = true;
        for (AnnotationAttachmentNode attachmentNode : annotations) {
            printInstruction(attachmentNode.getAnnotationName().getValue());
            String annotationKey = attachmentNode.getAnnotationName().getValue();
            switch (annotationKey) {
                case "svc":
                    ServiceModel serviceModel = new ServiceModel();
                    kubernetesDataHolder.addServiceModel(serviceModel);
                    break;
                case "ingress":
                    IngressModel ingressModel = new IngressModel();
                    kubernetesDataHolder.addIngressModel(ingressModel);
                    break;
                case "hpa":
                    PodAutoscalerModel podAutoscalerModel = new PodAutoscalerModel();
                    kubernetesDataHolder.setPodAutoscalerModel(podAutoscalerModel);
                    break;
                case "deployment":
                    kubernetesDataHolder.setDeploymentModel(kubeAnnotationProcessor.processDeployment(attachmentNode));
                    break;
                default:
                    break;
            }
        }
    }


    @Override
    public void codeGenerated(Path binaryPath) {
        if (canProcess) {
            KubeAnnotationProcessor kubeAnnotationProcessor = new KubeAnnotationProcessor();
            String filePath = binaryPath.toAbsolutePath().toString();
            String userDir = new File(filePath).getParentFile().getAbsolutePath();
            String targetPath = userDir + File.separator + "target" + File.separator + "kubernetes" + File.separator;

            DeploymentModel deploymentModel = kubernetesDataHolder.getDeploymentModel();
            if (deploymentModel == null) {
                deploymentModel = kubeAnnotationProcessor.getDefaultDeploymentModel(filePath);
            }
            //TODO:Fix this
            if (kubernetesDataHolder.getPorts().size() == 0) {
                kubernetesDataHolder.addPort(9090);
            }
            deploymentModel.setPorts(kubernetesDataHolder.getPorts());
            kubernetesDataHolder.setDeploymentModel(deploymentModel);

            try {
                kubeAnnotationProcessor.
                        createArtifacts(kubernetesDataHolder, filePath, targetPath);
            } catch (KubernetesPluginException e) {
                dlog.logDiagnostic(Diagnostic.Kind.ERROR, null, e.getMessage());
            }
        }

    }
}
