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

package org.ballerinax.kubernetes.processors.istio;

import org.ballerinalang.model.tree.AnnotationAttachmentNode;
import org.ballerinalang.model.tree.ServiceNode;
import org.ballerinalang.model.tree.SimpleVariableNode;
import org.ballerinalang.model.tree.expressions.ExpressionNode;
import org.ballerinax.kubernetes.exceptions.KubernetesPluginException;
import org.ballerinax.kubernetes.models.KubernetesContext;
import org.ballerinax.kubernetes.models.istio.IstioDestination;
import org.ballerinax.kubernetes.models.istio.IstioDestinationWeight;
import org.ballerinax.kubernetes.models.istio.IstioHttpRoute;
import org.ballerinax.kubernetes.models.istio.IstioVirtualServiceModel;
import org.ballerinax.kubernetes.processors.AbstractAnnotationProcessor;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangArrayLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static org.ballerinax.kubernetes.KubernetesConstants.ISTIO_VIRTUAL_SERVICE_POSTFIX;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.getArray;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.getIntValue;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.getLongValue;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.getMap;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.getValidName;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.isBlank;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.resolveValue;

/**
 * Istio virtual service annotation processor.
 *
 * @since 0.985.0
 */
public class IstioVirtualServiceAnnotationProcessor extends AbstractAnnotationProcessor {
    
    @Override
    public void processAnnotation(ServiceNode serviceNode, AnnotationAttachmentNode attachmentNode)
            throws KubernetesPluginException {
        List<BLangRecordLiteral.BLangRecordKeyValue> keyValues =
                ((BLangRecordLiteral) ((BLangAnnotationAttachment) attachmentNode).expr).getKeyValuePairs();
    
        IstioVirtualServiceModel vsModel = this.processIstioVSAnnotation(keyValues);
    
        if (isBlank(vsModel.getName())) {
            vsModel.setName(getValidName(serviceNode.getName().getValue()) + ISTIO_VIRTUAL_SERVICE_POSTFIX);
        }
        
        setDefaultValues(vsModel);
        KubernetesContext.getInstance().getDataHolder().addIstioVirtualServiceModel(serviceNode.getName().getValue(),
                vsModel);
    }
    
    @Override
    public void processAnnotation(SimpleVariableNode variableNode, AnnotationAttachmentNode attachmentNode)
            throws KubernetesPluginException {
        List<BLangRecordLiteral.BLangRecordKeyValue> keyValues =
                ((BLangRecordLiteral) ((BLangAnnotationAttachment) attachmentNode).expr).getKeyValuePairs();
    
        IstioVirtualServiceModel vsModel = this.processIstioVSAnnotation(keyValues);
        if (isBlank(vsModel.getName())) {
            vsModel.setName(getValidName(variableNode.getName().getValue()) + ISTIO_VIRTUAL_SERVICE_POSTFIX);
        }
    
        setDefaultValues(vsModel);
        KubernetesContext.getInstance().getDataHolder().addIstioVirtualServiceModel(variableNode.getName().getValue(),
                vsModel);
    }
    
    /**
     * Sets default values for istio virtual service model.
     *
     * @param vsModel The virtual service model.
     */
    private void setDefaultValues(IstioVirtualServiceModel vsModel) {
        if (null == vsModel.getHosts() || vsModel.getHosts().size() == 0) {
            List<String> hosts = new LinkedList<>();
            hosts.add("*");
            vsModel.setHosts(hosts);
        }
    }
    
    /**
     * Process @kubernetes:IstioVirtualService annotation.
     *
     * @param vsFields Fields of the virtual service annotation.
     * @throws KubernetesPluginException Unable to process annotations.
     */
    private IstioVirtualServiceModel processIstioVSAnnotation(List<BLangRecordLiteral.BLangRecordKeyValue> vsFields)
            throws KubernetesPluginException {
        IstioVirtualServiceModel vsModel = new IstioVirtualServiceModel();
        for (BLangRecordLiteral.BLangRecordKeyValue vsField : vsFields) {
            switch (IstioVSConfig.valueOf(vsField.getKey().toString())) {
                case name:
                    vsModel.setName(resolveValue(vsField.getValue().toString()));
                    break;
                case labels:
                    BLangRecordLiteral labelsField = (BLangRecordLiteral) vsField.getValue();
                    vsModel.setLabels(getMap(labelsField.getKeyValuePairs()));
                    break;
                case annotations:
                    BLangRecordLiteral annotationsField = (BLangRecordLiteral) vsField.getValue();
                    vsModel.setAnnotations(getMap(annotationsField.getKeyValuePairs()));
                    break;
                case hosts:
                    BLangArrayLiteral hostsField = (BLangArrayLiteral) vsField.getValue();
                    List<String> hostsList = new ArrayList<>(getArray(hostsField));
                    vsModel.setHosts(hostsList);
                    break;
                case gateways:
                    BLangArrayLiteral gatewaysField = (BLangArrayLiteral)  vsField.getValue();
                    List<String> gatewayList = new ArrayList<>(getArray(gatewaysField));
                    vsModel.setGateways(gatewayList);
                    break;
                case http:
                    BLangArrayLiteral httpFields = (BLangArrayLiteral) vsField.getValue();
                    List<IstioHttpRoute> httpModels = processHttpAnnotation(httpFields);
                    vsModel.setHttp(httpModels);
                    break;
                default:
                    throw new KubernetesPluginException("unknown field found for istio virtual service: " +
                                                        vsField.getKey().toString());
            }
        }
        return vsModel;
    }
    
    /**
     * Process http annotation array of the virtual service annotation to a model.
     *
     * @param httpArray The list of http fields.
     * @return Converted list of Istio http routes.
     * @throws KubernetesPluginException When an unknown field is found.
     */
    private List<IstioHttpRoute> processHttpAnnotation(BLangArrayLiteral httpArray) throws KubernetesPluginException {
        List<IstioHttpRoute> httpRoutes = new LinkedList<>();
        for (ExpressionNode expression : httpArray.getExpressions()) {
            BLangRecordLiteral httpFields = (BLangRecordLiteral) expression;
            IstioHttpRoute httpRoute = new IstioHttpRoute();
            for (BLangRecordLiteral.BLangRecordKeyValue httpField : httpFields.getKeyValuePairs()) {
                switch (IstioHttpRouteConfig.valueOf(httpField.getKey().toString())) {
                    case route:
                        BLangArrayLiteral routeFields = (BLangArrayLiteral)  httpField.getValue();
                        httpRoute.setRoute(processRoutesAnnotation(routeFields));
                        break;
                    case timeout:
                        httpRoute.setTimeout(getLongValue(httpField.getValue()));
                        break;
                    case appendHeaders:
                        httpRoute.setAppendHeaders(getMap(((BLangRecordLiteral) httpField.valueExpr).keyValuePairs));
                        break;
                    default:
                        throw new KubernetesPluginException("unknown field found for istio virtual service: " +
                                                            httpField.getKey().toString());
                }
            }
            httpRoutes.add(httpRoute);
        }
        return httpRoutes;
    }
    
    /**
     * Process routes of http annotation to a model.
     *
     * @param routeArray The list of routes.
     * @return A list of istio destination weight models.
     * @throws KubernetesPluginException When an unknown field is found.
     */
    private List<IstioDestinationWeight> processRoutesAnnotation(BLangArrayLiteral routeArray)
            throws KubernetesPluginException {
        List<IstioDestinationWeight> destinationWeights = new LinkedList<>();
        for (ExpressionNode expression : routeArray.getExpressions()) {
            BLangRecordLiteral routeFields = (BLangRecordLiteral) expression;
            IstioDestinationWeight destinationWeight = new IstioDestinationWeight();
            for (BLangRecordLiteral.BLangRecordKeyValue routeField : routeFields.getKeyValuePairs()) {
                switch (IstioDestinationWeightConfig.valueOf(routeField.getKey().toString())) {
                    case destination:
                        BLangRecordLiteral destinationFields = (BLangRecordLiteral) routeField.getValue();
                        IstioDestination destination = processDestinationAnnotation(destinationFields);
                        destinationWeight.setDestination(destination);
                        break;
                    case weight:
                        destinationWeight.setWeight(Integer.parseInt((routeField).getValue().toString()));
                        break;
                    default:
                        throw new KubernetesPluginException("unknown field found for istio virtual service: " +
                                                            routeField.getKey().toString());
                }
            }
            destinationWeights.add(destinationWeight);
        }
        
        return destinationWeights;
    }
    
    /**
     * Process destination of the destination weight annotation to a model.
     *
     * @param destinationFields The destination field.
     * @return A istio destination model.
     * @throws KubernetesPluginException When an unknown field is found.
     */
    private IstioDestination processDestinationAnnotation(BLangRecordLiteral destinationFields)
            throws KubernetesPluginException {
        IstioDestination destination = new IstioDestination();
        for (BLangRecordLiteral.BLangRecordKeyValue destinationField : destinationFields.getKeyValuePairs()) {
            switch (IstioDestinationConfig.valueOf(destinationField.getKey().toString())) {
                case host:
                    destination.setHost(resolveValue((destinationField).getValue().toString()));
                    break;
                case subset:
                    destination.setSubset(resolveValue((destinationField).getValue().toString()));
                    break;
                case port:
                    destination.setPort(getIntValue(destinationField.getValue()));
                    break;
                default:
                    throw new KubernetesPluginException("unknown field found for istio virtual service.");
            }
        }
        
        return destination;
    }
    
    private enum IstioDestinationConfig {
        host,
        subset,
        port
    }
    
    private enum IstioDestinationWeightConfig {
        destination,
        weight
    }
    
    private enum IstioHttpRouteConfig {
        route,
        timeout,
        appendHeaders
    }
    
    private enum IstioVSConfig {
        name,
        labels,
        annotations,
        hosts,
        gateways,
        http,
    }
}
