/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinax.kubernetes.processors.knative;

import org.ballerinalang.model.tree.AnnotationAttachmentNode;
import org.ballerinalang.model.tree.FunctionNode;
import org.ballerinalang.model.tree.ServiceNode;
import org.ballerinalang.model.tree.SimpleVariableNode;
import org.ballerinax.kubernetes.exceptions.KubernetesPluginException;
import org.ballerinax.kubernetes.models.knative.KnativeContext;
import org.ballerinax.kubernetes.models.knative.PodTolerationModel;
import org.ballerinax.kubernetes.models.knative.ProbeModel;
import org.ballerinax.kubernetes.models.knative.ServiceModel;
import org.ballerinax.kubernetes.processors.AbstractAnnotationProcessor;
import org.ballerinax.kubernetes.utils.KnativeUtils;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangListConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeInit;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static org.ballerinax.kubernetes.KubernetesConstants.DOCKER_CERT_PATH;
import static org.ballerinax.kubernetes.KubernetesConstants.DOCKER_HOST;
import static org.ballerinax.kubernetes.KubernetesConstants.MAIN_FUNCTION_NAME;
import static org.ballerinax.kubernetes.KubernetesConstants.SVC_POSTFIX;
import static org.ballerinax.kubernetes.utils.KnativeUtils.getBooleanValue;
import static org.ballerinax.kubernetes.utils.KnativeUtils.getEnvVarMap;
import static org.ballerinax.kubernetes.utils.KnativeUtils.getExternalFileMap;
import static org.ballerinax.kubernetes.utils.KnativeUtils.getImagePullSecrets;
import static org.ballerinax.kubernetes.utils.KnativeUtils.getIntValue;
import static org.ballerinax.kubernetes.utils.KnativeUtils.getMap;
import static org.ballerinax.kubernetes.utils.KnativeUtils.getStringValue;
import static org.ballerinax.kubernetes.utils.KnativeUtils.getValidName;
import static org.ballerinax.kubernetes.utils.KnativeUtils.isBlank;

/**
 * Deployment Annotation processor.
 */
public class KnativeServiceAnnotationProcessor extends AbstractAnnotationProcessor {

    @Override
    public void processAnnotation(FunctionNode functionNode, AnnotationAttachmentNode attachmentNode) throws
            KubernetesPluginException {
        if (!MAIN_FUNCTION_NAME.equals(functionNode.getName().getValue())) {
            throw new KubernetesPluginException("@kubernetes:Deployment{} annotation cannot be attached to a non " +
                    "main function.");
        }
        processService(attachmentNode);
    }

    @Override
    public void processAnnotation(ServiceNode serviceNode, AnnotationAttachmentNode attachmentNode) throws
            KubernetesPluginException {
        BLangService bService = (BLangService) serviceNode;
        for (BLangExpression attachedExpr : bService.getAttachedExprs()) {
            // If not anonymous endpoint throw error.
            if (attachedExpr instanceof BLangSimpleVarRef) {
                //throw new KubernetesPluginException("adding @knative:Service{} annotation to a service is only " +
                        //"supported when the service has an anonymous listener");
                ServiceModel serviceModelAttched = processService(attachmentNode);
                serviceModelAttched.addPort(serviceModelAttched.getPort());
                if (KnativeUtils.isBlank(serviceModelAttched.getName())) {
                    serviceModelAttched.setName(KnativeUtils.getValidName(serviceNode.getName().getValue())
                            + SVC_POSTFIX);
                }
                return;
            }
        }
        ServiceModel serviceModel = processService(attachmentNode);
        if (KnativeUtils.isBlank(serviceModel.getName())) {
            serviceModel.setName(KnativeUtils.getValidName(serviceNode.getName().getValue()) + SVC_POSTFIX);
        }
        // If service annotation port is not empty, then listener port is used for the k8s svc target port while
        // service annotation port is used for k8s port.
        // If service annotation port is empty, then listener port is used for both port and target port of the k8s
        // svc.
        BLangTypeInit bListener = (BLangTypeInit) bService.getAttachedExprs().get(0);
        if (serviceModel.getPort() == 8080) {
            if (extractPort(bListener) == 9090 || extractPort(bListener) == 9091 || extractPort(bListener) == 8013
            || extractPort(bListener) == 8012) {
                throw new KubernetesPluginException("listner port is conflicts with knative port");
            }
            serviceModel.addPort(extractPort(bListener));
        }
    }

    @Override
    public void processAnnotation(SimpleVariableNode variableNode, AnnotationAttachmentNode attachmentNode)
            throws KubernetesPluginException {
        ServiceModel serviceModel = processService(attachmentNode);
        if (KnativeUtils.isBlank(serviceModel.getName())) {
            serviceModel.setName(KnativeUtils.getValidName(variableNode.getName().getValue()) + SVC_POSTFIX);
        }
        // If service annotation port is not empty, then listener port is used for the k8s svc target port while
        // service annotation port is used for k8s port.
        // If service annotation port is empty, then listener port is used for both port and target port of the k8s
        // svc.
        BLangTypeInit bListener = (BLangTypeInit) ((BLangSimpleVariable) variableNode).expr;
        if (serviceModel.getPort() == 8080) {
            serviceModel.addPort(extractPort(bListener));
        }
    }

    private ServiceModel processService(AnnotationAttachmentNode attachmentNode) throws KubernetesPluginException {
        ServiceModel serviceModel = new ServiceModel();
        List<BLangRecordLiteral.BLangRecordKeyValue> keyValues =
                ((BLangRecordLiteral) ((BLangAnnotationAttachment) attachmentNode).expr).getKeyValuePairs();
        for (BLangRecordLiteral.BLangRecordKeyValue keyValue : keyValues) {
            ServiceConfiguration
                    serviceConfiguration = ServiceConfiguration.valueOf(keyValue.getKey().toString());
            switch (serviceConfiguration) {
                case name:
                    serviceModel.setName(getValidName(getStringValue(keyValue.getValue())));
                    break;
                case labels:
                    serviceModel.setLabels(getMap(keyValue.getValue()));
                    break;
                case annotations:
                    serviceModel.setAnnotations(getMap(keyValue.getValue()));
                    break;
                case dockerHost:
                    serviceModel.setDockerHost(getStringValue(keyValue.getValue()));
                    break;
                case dockerCertPath:
                    serviceModel.setDockerCertPath(getStringValue(keyValue.getValue()));
                    break;
                case registry:
                    serviceModel.setRegistry(getStringValue(keyValue.getValue()));
                    break;
                case username:
                    serviceModel.setUsername(getStringValue(keyValue.getValue()));
                    break;
                case password:
                    serviceModel.setPassword(getStringValue(keyValue.getValue()));
                    break;
                case baseImage:
                    serviceModel.setBaseImage(getStringValue(keyValue.getValue()));
                    break;
                case image:
                    serviceModel.setImage(getStringValue(keyValue.getValue()));
                    break;
                case buildImage:
                    serviceModel.setBuildImage(getBooleanValue(keyValue.getValue()));
                    break;
                case push:
                    serviceModel.setPush(getBooleanValue(keyValue.getValue()));
                    break;
                case cmd:
                    serviceModel.setCmd(getStringValue(keyValue.getValue()));
                    break;
                case copyFiles:
                    serviceModel.setCopyFiles(getExternalFileMap(keyValue));
                    break;
                case singleYAML:
                    serviceModel.setSingleYAML(getBooleanValue(keyValue.getValue()));
                    break;
                case namespace:
                    KnativeContext.getInstance().getDataHolder().setNamespace(getStringValue(keyValue.getValue()));
                    break;
                case replicas:
                    serviceModel.setReplicas(getIntValue(keyValue.getValue()));
                    break;
                case livenessProbe:
                    serviceModel.setLivenessProbe(parseProbeConfiguration(keyValue.getValue()));
                    break;
                case readinessProbe:
                    serviceModel.setReadinessProbe(parseProbeConfiguration(keyValue.getValue()));
                    break;
                case imagePullPolicy:
                    serviceModel.setImagePullPolicy(getStringValue(keyValue.getValue()));
                    break;
                case env:
                    serviceModel.setEnv(getEnvVarMap(keyValue.getValue()));
                    break;
                case podAnnotations:
                    serviceModel.setPodAnnotations(getMap(keyValue.getValue()));
                    break;
                case podTolerations:
                    serviceModel.setPodTolerations(parsePodTolerationConfiguration(keyValue.getValue()));
                    break;
                case dependsOn:
                    serviceModel.setDependsOn(getDependsOn(keyValue));
                    break;
                case imagePullSecrets:
                    serviceModel.setImagePullSecrets(getImagePullSecrets(keyValue));
                    break;
                case containerConcurrency:
                    serviceModel.setContainerConcurrency(getIntValue(keyValue.getValue()));
                    break;
                case timeoutSeconds:
                    serviceModel.setTimeoutSeconds(getIntValue(keyValue.getValue()));
                    break;
                case port:
                    serviceModel.setPort(getIntValue(keyValue.getValue()));
                    break;
                default:
                    break;
            }
        }

        String dockerHost = System.getenv(DOCKER_HOST);
        if (!isBlank(dockerHost)) {
            serviceModel.setDockerHost(dockerHost);
        }
        String dockerCertPath = System.getenv(DOCKER_CERT_PATH);
        if (!isBlank(dockerCertPath)) {
            serviceModel.setDockerCertPath(dockerCertPath);
        }
        KnativeContext.getInstance().getDataHolder().setServiceModel(serviceModel);
        return serviceModel;
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
            List<BLangRecordLiteral.BLangRecordKeyValue> podTolerationFields =
                    ((BLangRecordLiteral) podTolerationFieldsAsExpression).keyValuePairs;
            PodTolerationModel podTolerationModel = new PodTolerationModel();
            for (BLangRecordLiteral.BLangRecordKeyValue podTolerationField : podTolerationFields) {
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
        List<BLangExpression> configAnnotation = ((BLangListConstructorExpr) keyValue.valueExpr).exprs;
        for (BLangExpression bLangExpression : configAnnotation) {
            dependsOnList.add(bLangExpression.toString());
        }
        return dependsOnList;
    }

    private int extractPort(BLangTypeInit bListener) throws KubernetesPluginException {
        try {
            return Integer.parseInt(bListener.argsExpr.get(0).toString());
        } catch (NumberFormatException e) {
            throw new KubernetesPluginException("unable to parse port/targetPort for the service: " +
                    bListener.argsExpr.get(0).toString());
        }
    }

    /**
     * Enum class for DeploymentConfiguration.
     */
    private enum ServiceConfiguration {
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
        port,
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
        containerConcurrency,
        timeoutSeconds
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
