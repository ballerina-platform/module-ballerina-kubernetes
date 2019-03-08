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
package org.ballerinax.kubernetes.processors;

import org.ballerinalang.model.tree.AnnotationAttachmentNode;
import org.ballerinalang.model.tree.FunctionNode;
import org.ballerinalang.model.tree.ServiceNode;
import org.ballerinalang.model.tree.SimpleVariableNode;
import org.ballerinax.kubernetes.exceptions.KubernetesPluginException;
import org.ballerinax.kubernetes.models.DeploymentModel;
import org.ballerinax.kubernetes.models.KubernetesContext;
import org.ballerinax.kubernetes.models.ProbeModel;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangArrayLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.ballerinax.kubernetes.KubernetesConstants.DOCKER_CERT_PATH;
import static org.ballerinax.kubernetes.KubernetesConstants.DOCKER_HOST;
import static org.ballerinax.kubernetes.KubernetesConstants.MAIN_FUNCTION_NAME;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.getBooleanValue;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.getEnvVarMap;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.getExternalFileMap;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.getImagePullSecrets;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.getIntValue;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.getMap;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.getValidName;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.isBlank;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.parseBuildExtension;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.resolveValue;

/**
 * Deployment Annotation processor.
 */
public class DeploymentAnnotationProcessor extends AbstractAnnotationProcessor {

    @Override
    public void processAnnotation(ServiceNode entityName, AnnotationAttachmentNode attachmentNode) throws
            KubernetesPluginException {
        processDeployment(attachmentNode);
    }
    
    @Override
    public void processAnnotation(SimpleVariableNode variableNode, AnnotationAttachmentNode attachmentNode) throws
            KubernetesPluginException {
        processDeployment(attachmentNode);
    }
    
    @Override
    public void processAnnotation(FunctionNode functionNode, AnnotationAttachmentNode attachmentNode) throws
            KubernetesPluginException {
        if (MAIN_FUNCTION_NAME.equals(functionNode.getName().getValue())) {
            processDeployment(attachmentNode);
        }
    }
    
    private void processDeployment(AnnotationAttachmentNode attachmentNode) throws KubernetesPluginException {
        DeploymentModel deploymentModel = new DeploymentModel();
        List<BLangRecordLiteral.BLangRecordKeyValue> keyValues =
                ((BLangRecordLiteral) ((BLangAnnotationAttachment) attachmentNode).expr).getKeyValuePairs();
        for (BLangRecordLiteral.BLangRecordKeyValue keyValue : keyValues) {
            DeploymentConfiguration deploymentConfiguration =
                    DeploymentConfiguration.valueOf(keyValue.getKey().toString());
            switch (deploymentConfiguration) {
                case name:
                    deploymentModel.setName(getValidName(resolveValue(keyValue.getValue().toString())));
                    break;
                case namespace:
                    KubernetesContext.getInstance().getDataHolder().setNamespace(resolveValue(
                            keyValue.getValue().toString()));
                    break;
                case labels:
                    deploymentModel.setLabels(getMap(((BLangRecordLiteral) keyValue.valueExpr).keyValuePairs));
                    break;
                case annotations:
                    deploymentModel.setAnnotations(getMap(((BLangRecordLiteral) keyValue.valueExpr).keyValuePairs));
                    break;
                case podAnnotations:
                    deploymentModel.setPodAnnotations(getMap(((BLangRecordLiteral) keyValue.valueExpr).keyValuePairs));
                    break;
                case livenessProbe:
                    deploymentModel.setLivenessProbe(parseProbeConfiguration(keyValue.getValue()));
                    break;
                case readinessProbe:
                    deploymentModel.setReadinessProbe(parseProbeConfiguration(keyValue.getValue()));
                    break;
                case username:
                    deploymentModel.setUsername(resolveValue(keyValue.getValue().toString()));
                    break;
                case env:
                    deploymentModel.setEnv(getEnvVarMap(keyValue.getValue()));
                    break;
                case password:
                    deploymentModel.setPassword(resolveValue(keyValue.getValue().toString()));
                    break;
                case baseImage:
                    deploymentModel.setBaseImage(resolveValue(keyValue.getValue().toString()));
                    break;
                case push:
                    deploymentModel.setPush(Boolean.valueOf(resolveValue(keyValue.getValue().toString())));
                    break;
                case buildImage:
                    deploymentModel.setBuildImage(Boolean.valueOf(resolveValue(keyValue.getValue().toString())));
                    break;
                case image:
                    deploymentModel.setImage(resolveValue(keyValue.getValue().toString()));
                    break;
                case dockerHost:
                    deploymentModel.setDockerHost(resolveValue(keyValue.getValue().toString()));
                    break;
                case dockerCertPath:
                    deploymentModel.setDockerCertPath(resolveValue(keyValue.getValue().toString()));
                    break;
                case imagePullPolicy:
                    deploymentModel.setImagePullPolicy(resolveValue(keyValue.getValue().toString()));
                    break;
                case replicas:
                    deploymentModel.setReplicas(Integer.parseInt(resolveValue(keyValue.getValue().toString())));
                    break;
                case copyFiles:
                    deploymentModel.setCopyFiles(getExternalFileMap(keyValue));
                    break;
                case singleYAML:
                    deploymentModel.setSingleYAML(Boolean.valueOf(resolveValue(keyValue.getValue().toString())));
                    break;
                case dependsOn:
                    deploymentModel.setDependsOn(getDependsOn(keyValue));
                    break;
                case imagePullSecrets:
                    deploymentModel.setImagePullSecrets(getImagePullSecrets(keyValue));
                    break;
                case registry:
                    deploymentModel.setRegistry(resolveValue(keyValue.getValue().toString()));
                    break;
                case buildExtension:
                    deploymentModel.setBuildExtension(parseBuildExtension(keyValue.getValue()));
                    break;
                default:
                    break;
            }
        }

        String dockerHost = System.getenv(DOCKER_HOST);
        if (!isBlank(dockerHost)) {
            deploymentModel.setDockerHost(dockerHost);
        }
        String dockerCertPath = System.getenv(DOCKER_CERT_PATH);
        if (!isBlank(dockerCertPath)) {
            deploymentModel.setDockerCertPath(dockerCertPath);
        }
        KubernetesContext.getInstance().getDataHolder().setDeploymentModel(deploymentModel);
    }
    
    /**
     * Parse probe configuration from a record.
     *
     * @param probeValue Probe configuration record.
     * @return Parse probe model.
     * @throws KubernetesPluginException When an unknown field is found.
     */
    private ProbeModel parseProbeConfiguration(BLangExpression probeValue) throws KubernetesPluginException {
        if ((probeValue instanceof BLangSimpleVarRef || probeValue instanceof BLangLiteral) &&
            getBooleanValue(probeValue)) {
            return new ProbeModel();
        } else {
            if (probeValue instanceof BLangRecordLiteral) {
                List<BLangRecordLiteral.BLangRecordKeyValue> buildExtensionRecord =
                        ((BLangRecordLiteral) probeValue).keyValuePairs;
                ProbeModel probeModel = new ProbeModel();
                for (BLangRecordLiteral.BLangRecordKeyValue probeField : buildExtensionRecord) {
                    ProbeConfiguration probeConfiguration =
                            ProbeConfiguration.valueOf(probeField.getKey().toString());
                    switch (probeConfiguration) {
                        case port:
                            probeModel.setPort(getIntValue(probeField.getValue()));
                            break;
                        case initialDelaySeconds:
                            probeModel.setInitialDelaySeconds(getIntValue(probeField.getValue()));
                            break;
                        case periodSeconds:
                            probeModel.setPeriodSeconds(getIntValue(probeField.getValue()));
                            break;
                        default:
                            throw new KubernetesPluginException("unknown probe field found: " +
                                                                probeField.getKey().toString());
                    }
                }
                return probeModel;
            }
        }
        return null;
    }

    private Set<String> getDependsOn(BLangRecordLiteral.BLangRecordKeyValue keyValue) {
        Set<String> dependsOnList = new HashSet<>();
        List<BLangExpression> configAnnotation = ((BLangArrayLiteral) keyValue.valueExpr).exprs;
        for (BLangExpression bLangExpression : configAnnotation) {
            dependsOnList.add(bLangExpression.toString());
        }
        return dependsOnList;
    }


    /**
     * Enum class for DeploymentConfiguration.
     */
    private enum DeploymentConfiguration {
        name,
        namespace,
        labels,
        annotations,
        podAnnotations,
        replicas,
        livenessProbe,
        readinessProbe,
        imagePullPolicy,
        image,
        env,
        buildImage,
        dockerHost,
        registry,
        username,
        password,
        baseImage,
        push,
        dockerCertPath,
        copyFiles,
        singleYAML,
        dependsOn,
        imagePullSecrets,
        buildExtension
    }
    
    private enum ProbeConfiguration {
        port,
        initialDelaySeconds,
        periodSeconds
    }
}
