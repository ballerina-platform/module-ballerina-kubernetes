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
import org.ballerinalang.model.tree.EndpointNode;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.ServiceNode;
import org.ballerinax.kubernetes.exceptions.KubernetesPluginException;
import org.ballerinax.kubernetes.models.DeploymentModel;
import org.ballerinax.kubernetes.models.EnvVarValueModel;
import org.ballerinax.kubernetes.models.ExternalFileModel;
import org.ballerinax.kubernetes.models.KubernetesContext;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangArrayLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.ballerinax.kubernetes.KubernetesConstants.DOCKER_CERT_PATH;
import static org.ballerinax.kubernetes.KubernetesConstants.DOCKER_HOST;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.getMap;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.getValidName;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.isBlank;
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
    public void processAnnotation(EndpointNode endpointNode, AnnotationAttachmentNode attachmentNode)
            throws KubernetesPluginException {
        processDeployment(attachmentNode);
    }

    private void processDeployment(AnnotationAttachmentNode attachmentNode) throws KubernetesPluginException {
        DeploymentModel deploymentModel = new DeploymentModel();
        List<BLangRecordLiteral.BLangRecordKeyValue> keyValues =
                ((BLangRecordLiteral) ((BLangAnnotationAttachment) attachmentNode).expr).getKeyValuePairs();
        for (BLangRecordLiteral.BLangRecordKeyValue keyValue : keyValues) {
            DeploymentConfiguration deploymentConfiguration =
                    DeploymentConfiguration.valueOf(keyValue.getKey().toString());
            String annotationValue = resolveValue(keyValue.getValue().toString());
            switch (deploymentConfiguration) {
                case name:
                    deploymentModel.setName(getValidName(annotationValue));
                    break;
                case namespace:
                    KubernetesContext.getInstance().getDataHolder().setNamespace(annotationValue);
                    break;
                case labels:
                    deploymentModel.setLabels(getMap(((BLangRecordLiteral) keyValue.valueExpr).keyValuePairs));
                    break;
                case enableLiveness:
                    deploymentModel.setEnableLiveness(Boolean.valueOf(annotationValue));
                    break;
                case livenessPort:
                    deploymentModel.setLivenessPort(Integer.parseInt(annotationValue));
                    break;
                case initialDelaySeconds:
                    deploymentModel.setInitialDelaySeconds(Integer.parseInt(annotationValue));
                    break;
                case periodSeconds:
                    deploymentModel.setPeriodSeconds(Integer.parseInt(annotationValue));
                    break;
                case username:
                    deploymentModel.setUsername(annotationValue);
                    break;
                case env:
                    deploymentModel.setEnv(getEnvVarMap(keyValue.getValue()));
                    break;
                case password:
                    deploymentModel.setPassword(annotationValue);
                    break;
                case baseImage:
                    deploymentModel.setBaseImage(annotationValue);
                    break;
                case push:
                    deploymentModel.setPush(Boolean.valueOf(annotationValue));
                    break;
                case buildImage:
                    deploymentModel.setBuildImage(Boolean.valueOf(annotationValue));
                    break;
                case image:
                    deploymentModel.setImage(annotationValue);
                    break;
                case dockerHost:
                    deploymentModel.setDockerHost(annotationValue);
                    break;
                case dockerCertPath:
                    deploymentModel.setDockerCertPath(annotationValue);
                    break;
                case imagePullPolicy:
                    deploymentModel.setImagePullPolicy(annotationValue);
                    break;
                case replicas:
                    deploymentModel.setReplicas(Integer.parseInt(annotationValue));
                    break;
                case copyFiles:
                    deploymentModel.setExternalFiles(getExternalFileMap(keyValue));
                    break;
                case singleYAML:
                    deploymentModel.setSingleYAML(Boolean.valueOf(annotationValue));
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
     * Convert environment variable values into a map for deployment model.
     *
     * @param envVarValues Value of env field of Deployment annotation.
     * @return A map of env var models.
     */
    private Map<String, EnvVarValueModel> getEnvVarMap(BLangExpression envVarValues) {
        Map<String, EnvVarValueModel> envVarMap = new LinkedHashMap<>();
        if (envVarValues.getKind() == NodeKind.RECORD_LITERAL_EXPR) {
            for (BLangRecordLiteral.BLangRecordKeyValue envVar : ((BLangRecordLiteral) envVarValues).keyValuePairs) {
                String envVarName = envVar.getKey().toString();
                EnvVarValueModel envVarValue = null;
                if (envVar.getValue().getKind() == NodeKind.LITERAL) {
                    // Value is a string
                    BLangLiteral value = (BLangLiteral) envVar.getValue();
                    envVarValue = new EnvVarValueModel(value.toString());
                } else if (envVar.getValue().getKind() == NodeKind.RECORD_LITERAL_EXPR) {
                    BLangRecordLiteral valueFrom = (BLangRecordLiteral) envVar.getValue();
                    BLangRecordLiteral.BLangRecordKeyValue bRefType = valueFrom.getKeyValuePairs().get(0);
                    BLangSimpleVarRef refType = (BLangSimpleVarRef) bRefType.getKey();
                    switch (refType.variableName.toString()) {
                        case "fieldRef":
                            BLangRecordLiteral.BLangRecordKeyValue fieldRefValue =
                                    ((BLangRecordLiteral) bRefType.getValue()).getKeyValuePairs().get(0);
                            EnvVarValueModel.FieldRef fieldRefModel = new EnvVarValueModel.FieldRef();
                            fieldRefModel.setFieldPath(fieldRefValue.getValue().toString());
                            envVarValue = new EnvVarValueModel(fieldRefModel);
                            break;
                        case "secretKeyRef":
                            EnvVarValueModel.SecretKeyRef secretKeyRefModel = new EnvVarValueModel.SecretKeyRef();
                            for (BLangRecordLiteral.BLangRecordKeyValue secretKeyRefFields :
                                    ((BLangRecordLiteral) bRefType.getValue()).getKeyValuePairs()) {
                                if (secretKeyRefFields.getKey().toString().equals("key")) {
                                    secretKeyRefModel.setKey(secretKeyRefFields.getValue().toString());
                                } else if (secretKeyRefFields.getKey().toString().equals("name")) {
                                    secretKeyRefModel.setName(secretKeyRefFields.getValue().toString());
                                }
                            }
                            envVarValue = new EnvVarValueModel(secretKeyRefModel);
                            break;
                        case "resourceFieldRef":
                            EnvVarValueModel.ResourceFieldRef resourceFieldRefModel =
                                    new EnvVarValueModel.ResourceFieldRef();
                            for (BLangRecordLiteral.BLangRecordKeyValue resourceFieldRefFields :
                                    ((BLangRecordLiteral) bRefType.getValue()).getKeyValuePairs()) {
                                if (resourceFieldRefFields.getKey().toString().equals("containerName")) {
                                    resourceFieldRefModel.setContainerName(
                                            resourceFieldRefFields.getValue().toString());
                                } else if (resourceFieldRefFields.getKey().toString().equals("resource")) {
                                    resourceFieldRefModel.setResource(resourceFieldRefFields.getValue().toString());
                                }
                            }
                            envVarValue = new EnvVarValueModel(resourceFieldRefModel);
                            break;
                        case "configMapKeyRef":
                            EnvVarValueModel.ConfigMapKeyValue configMapKeyRefModel =
                                    new EnvVarValueModel.ConfigMapKeyValue();
                            for (BLangRecordLiteral.BLangRecordKeyValue configMapKeyRefFields :
                                    ((BLangRecordLiteral) bRefType.getValue()).getKeyValuePairs()) {
                                if (configMapKeyRefFields.getKey().toString().equals("key")) {
                                    configMapKeyRefModel.setKey(configMapKeyRefFields.getValue().toString());
                                } else if (configMapKeyRefFields.getKey().toString().equals("name")) {
                                    configMapKeyRefModel.setName(configMapKeyRefFields.getValue().toString());
                                }
                            }
                            envVarValue = new EnvVarValueModel(configMapKeyRefModel);
                            break;
                        default:
                            break;
                    }
                }
                
                envVarMap.put(envVarName, envVarValue);
            }
        }
        return envVarMap;
    }
    
    private Set<ExternalFileModel> getExternalFileMap(BLangRecordLiteral.BLangRecordKeyValue keyValue) throws
            KubernetesPluginException {
        Set<ExternalFileModel> externalFiles = new HashSet<>();
        List<BLangExpression> configAnnotation = ((BLangArrayLiteral) keyValue.valueExpr).exprs;
        for (BLangExpression bLangExpression : configAnnotation) {
            List<BLangRecordLiteral.BLangRecordKeyValue> annotationValues =
                    ((BLangRecordLiteral) bLangExpression).getKeyValuePairs();
            ExternalFileModel externalFileModel = new ExternalFileModel();
            for (BLangRecordLiteral.BLangRecordKeyValue annotation : annotationValues) {
                String annotationValue = resolveValue(annotation.getValue().toString());
                switch (annotation.getKey().toString()) {
                    case "source":
                        externalFileModel.setSource(annotationValue);
                        break;
                    case "target":
                        externalFileModel.setTarget(annotationValue);
                        break;
                    default:
                        break;
                }
            }
            if (isBlank(externalFileModel.getSource())) {
                throw new KubernetesPluginException("@kubernetes:Deployment copyFiles source cannot be empty.");
            }
            if (isBlank(externalFileModel.getTarget())) {
                throw new KubernetesPluginException("@kubernetes:Deployment copyFiles target cannot be empty.");
            }
            externalFiles.add(externalFileModel);
        }
        return externalFiles;

    }

    private Set<String> getDependsOn(BLangRecordLiteral.BLangRecordKeyValue keyValue) {
        Set<String> dependsOnList = new HashSet<>();
        List<BLangExpression> configAnnotation = ((BLangArrayLiteral) keyValue.valueExpr).exprs;
        for (BLangExpression bLangExpression : configAnnotation) {
            dependsOnList.add(bLangExpression.toString());
        }
        return dependsOnList;
    }

    private Set<String> getImagePullSecrets(BLangRecordLiteral.BLangRecordKeyValue keyValue) {
        Set<String> imagePullSecrets = new HashSet<>();
        List<BLangExpression> configAnnotation = ((BLangArrayLiteral) keyValue.valueExpr).exprs;
        for (BLangExpression bLangExpression : configAnnotation) {
            imagePullSecrets.add(bLangExpression.toString());
        }
        return imagePullSecrets;
    }


    /**
     * Enum class for DeploymentConfiguration.
     */
    private enum DeploymentConfiguration {
        name,
        namespace,
        labels,
        replicas,
        enableLiveness,
        livenessPort,
        initialDelaySeconds,
        periodSeconds,
        imagePullPolicy,
        image,
        env,
        buildImage,
        dockerHost,
        username,
        password,
        baseImage,
        push,
        dockerCertPath,
        copyFiles,
        singleYAML,
        dependsOn,
        imagePullSecrets
    }
}
