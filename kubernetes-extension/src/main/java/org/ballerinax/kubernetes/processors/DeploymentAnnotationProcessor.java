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
import org.ballerinax.kubernetes.models.PodTolerationModel;
import org.ballerinax.kubernetes.models.ProbeModel;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangListConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static org.ballerinax.kubernetes.KubernetesConstants.DOCKER_CERT_PATH;
import static org.ballerinax.kubernetes.KubernetesConstants.DOCKER_HOST;
import static org.ballerinax.kubernetes.KubernetesConstants.MAIN_FUNCTION_NAME;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.convertRecordFields;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.getBooleanValue;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.getEnvVarMap;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.getExternalFileMap;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.getImagePullSecrets;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.getIntValue;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.getMap;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.getStringValue;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.getValidName;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.isBlank;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.parseBuildExtension;

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
        if (!MAIN_FUNCTION_NAME.equals(functionNode.getName().getValue())) {
            throw new KubernetesPluginException("@kubernetes:Deployment{} annotation cannot be attached to a non " +
                                                "main function.");
        }
    
        processDeployment(attachmentNode);
    }
    
    private void processDeployment(AnnotationAttachmentNode attachmentNode) throws KubernetesPluginException {
        DeploymentModel deploymentModel = new DeploymentModel();
        List<BLangRecordLiteral.BLangRecordKeyValueField> keyValues =
            convertRecordFields(((BLangRecordLiteral) ((BLangAnnotationAttachment) attachmentNode).expr).getFields());
        for (BLangRecordLiteral.BLangRecordKeyValueField keyValue : keyValues) {
            DeploymentConfiguration deploymentConfiguration =
                    DeploymentConfiguration.valueOf(keyValue.getKey().toString());
            switch (deploymentConfiguration) {
                case name:
                    deploymentModel.setName(getValidName(getStringValue(keyValue.getValue())));
                    break;
                case labels:
                    deploymentModel.setLabels(getMap(keyValue.getValue()));
                    break;
                case annotations:
                    deploymentModel.setAnnotations(getMap(keyValue.getValue()));
                    break;
                case dockerHost:
                    deploymentModel.setDockerHost(getStringValue(keyValue.getValue()));
                    break;
                case dockerCertPath:
                    deploymentModel.setDockerCertPath(getStringValue(keyValue.getValue()));
                    break;
                case registry:
                    deploymentModel.setRegistry(getStringValue(keyValue.getValue()));
                    break;
                case username:
                    deploymentModel.setUsername(getStringValue(keyValue.getValue()));
                    break;
                case password:
                    deploymentModel.setPassword(getStringValue(keyValue.getValue()));
                    break;
                case baseImage:
                    deploymentModel.setBaseImage(getStringValue(keyValue.getValue()));
                    break;
                case image:
                    deploymentModel.setImage(getStringValue(keyValue.getValue()));
                    break;
                case buildImage:
                    deploymentModel.setBuildImage(getBooleanValue(keyValue.getValue()));
                    break;
                case push:
                    deploymentModel.setPush(getBooleanValue(keyValue.getValue()));
                    break;
                case cmd:
                    deploymentModel.setCmd(getStringValue(keyValue.getValue()));
                    break;
                case copyFiles:
                    deploymentModel.setCopyFiles(getExternalFileMap(keyValue));
                    break;
                case singleYAML:
                    deploymentModel.setSingleYAML(getBooleanValue(keyValue.getValue()));
                    break;
                case namespace:
                    KubernetesContext.getInstance().getDataHolder().setNamespace(getStringValue(keyValue.getValue()));
                    break;
                case replicas:
                    deploymentModel.setReplicas(getIntValue(keyValue.getValue()));
                    break;
                case livenessProbe:
                    deploymentModel.setLivenessProbe(parseProbeConfiguration(keyValue.getValue()));
                    break;
                case readinessProbe:
                    deploymentModel.setReadinessProbe(parseProbeConfiguration(keyValue.getValue()));
                    break;
                case imagePullPolicy:
                    deploymentModel.setImagePullPolicy(getStringValue(keyValue.getValue()));
                    break;
                case env:
                    deploymentModel.setEnv(getEnvVarMap(keyValue.getValue()));
                    break;
                case podAnnotations:
                    deploymentModel.setPodAnnotations(getMap(keyValue.getValue()));
                    break;
                case podTolerations:
                    deploymentModel.setPodTolerations(parsePodTolerationConfiguration(keyValue.getValue()));
                    break;
                case buildExtension:
                    deploymentModel.setBuildExtension(parseBuildExtension(keyValue.getValue()));
                    break;
                case dependsOn:
                    deploymentModel.setDependsOn(getDependsOn(keyValue));
                    break;
                case imagePullSecrets:
                    deploymentModel.setImagePullSecrets(getImagePullSecrets(keyValue));
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
     * Parse pod toleration configurations from a record array.
     *
     * @param podTolerationValues Pod toleration configuration records.
     * @return Pod toleration models.
     * @throws KubernetesPluginException When an unknown field is found.
     */
    private List<PodTolerationModel> parsePodTolerationConfiguration(BLangExpression podTolerationValues)
            throws KubernetesPluginException {
        List<PodTolerationModel> podTolerationModels = new LinkedList<>();
        List<BLangExpression> podTolerations = ((BLangListConstructorExpr) podTolerationValues).exprs;
        for (BLangExpression podTolerationFieldsAsExpression : podTolerations) {
            List<BLangRecordLiteral.BLangRecordKeyValueField> podTolerationFields =
                    convertRecordFields(((BLangRecordLiteral) podTolerationFieldsAsExpression).getFields());
            PodTolerationModel podTolerationModel = new PodTolerationModel();
            for (BLangRecordLiteral.BLangRecordKeyValueField podTolerationField : podTolerationFields) {
                PodTolerationConfiguration podTolerationFieldName =
                        PodTolerationConfiguration.valueOf(podTolerationField.getKey().toString());
                switch (podTolerationFieldName) {
                    case key:
                        podTolerationModel.setKey(getStringValue(podTolerationField.getValue()));
                        break;
                    case operator:
                        podTolerationModel.setOperator(getStringValue(podTolerationField.getValue()));
                        break;
                    case value:
                        podTolerationModel.setValue(getStringValue(podTolerationField.getValue()));
                        break;
                    case effect:
                        podTolerationModel.setEffect(getStringValue(podTolerationField.getValue()));
                        break;
                    case tolerationSeconds:
                        podTolerationModel.setTolerationSeconds(getIntValue(podTolerationField.getValue()));
                        break;
                    default:
                        throw new KubernetesPluginException("unknown pod toleration field found: " +
                                                            podTolerationField.getKey().toString());
                }
            }
            podTolerationModels.add(podTolerationModel);
        }
        
        return podTolerationModels;
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
                List<BLangRecordLiteral.BLangRecordKeyValueField> buildExtensionRecord =
                        convertRecordFields(((BLangRecordLiteral) probeValue).getFields());
                ProbeModel probeModel = new ProbeModel();
                for (BLangRecordLiteral.BLangRecordKeyValueField probeField : buildExtensionRecord) {
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

    private Set<String> getDependsOn(BLangRecordLiteral.BLangRecordKeyValueField keyValue) {
        Set<String> dependsOnList = new HashSet<>();
        List<BLangExpression> configAnnotation = ((BLangListConstructorExpr) keyValue.valueExpr).exprs;
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
        labels,
        annotations,
        dockerHost,
        dockerCertPath,
        registry,
        username,
        password,
        baseImage,
        image,
        buildImage,
        push,
        cmd,
        copyFiles,
        singleYAML,
        namespace,
        replicas,
        livenessProbe,
        readinessProbe,
        imagePullPolicy,
        env,
        podAnnotations,
        podTolerations,
        buildExtension,
        dependsOn,
        imagePullSecrets,
    }
    
    private enum ProbeConfiguration {
        port,
        initialDelaySeconds,
        periodSeconds
    }
    
    private enum PodTolerationConfiguration {
        key,
        operator,
        value,
        effect,
        tolerationSeconds
    }
}
