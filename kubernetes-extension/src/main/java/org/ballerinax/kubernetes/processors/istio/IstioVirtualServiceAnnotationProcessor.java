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
import org.ballerinalang.model.tree.EndpointNode;
import org.ballerinalang.model.tree.ServiceNode;
import org.ballerinalang.model.tree.expressions.ExpressionNode;
import org.ballerinalang.model.tree.expressions.RecordLiteralNode;
import org.ballerinalang.model.types.TypeTags;
import org.ballerinax.kubernetes.exceptions.KubernetesPluginException;
import org.ballerinax.kubernetes.models.KubernetesContext;
import org.ballerinax.kubernetes.models.istio.IstioDestination;
import org.ballerinax.kubernetes.models.istio.IstioDestinationWeight;
import org.ballerinax.kubernetes.models.istio.IstioHttpRedirect;
import org.ballerinax.kubernetes.models.istio.IstioHttpRoute;
import org.ballerinax.kubernetes.models.istio.IstioVirtualService;
import org.ballerinax.kubernetes.processors.AbstractAnnotationProcessor;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.BLangEndpoint;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangArrayLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.ballerinax.kubernetes.KubernetesConstants.ISTIO_GATEWAY_POSTFIX;
import static org.ballerinax.kubernetes.KubernetesConstants.ISTIO_VIRTUAL_SERVICE_POSTFIX;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.getArray;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.getMap;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.getValidName;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.isBlank;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.resolveValue;

/**
 * Istio virtual service annotation processor.
 */
public class IstioVirtualServiceAnnotationProcessor extends AbstractAnnotationProcessor {
    
    @Override
    public void processAnnotation(ServiceNode serviceNode, AnnotationAttachmentNode attachmentNode)
            throws KubernetesPluginException {
        List<BLangRecordLiteral.BLangRecordKeyValue> keyValues =
                ((BLangRecordLiteral) ((BLangAnnotationAttachment) attachmentNode).expr).getKeyValuePairs();
    
        IstioVirtualService vsModel = this.processIstioVSAnnotation(keyValues);
    
        if (isBlank(vsModel.getName())) {
            vsModel.setName(getValidName(serviceNode.getName().getValue()) + ISTIO_VIRTUAL_SERVICE_POSTFIX);
        }
        RecordLiteralNode anonymousEndpoint = serviceNode.getAnonymousEndpointBind();
        List<BLangRecordLiteral.BLangRecordKeyValue> endpointConfig =
                ((BLangRecordLiteral) anonymousEndpoint).getKeyValuePairs();
    
        setDefaultValues(serviceNode.getName().getValue(), vsModel, endpointConfig);
    
        KubernetesContext.getInstance().getDataHolder().addIstioVirtualServiceModels(vsModel);
    }
    
    @Override
    public void processAnnotation(EndpointNode endpointNode, AnnotationAttachmentNode attachmentNode)
            throws KubernetesPluginException {
        List<BLangRecordLiteral.BLangRecordKeyValue> keyValues =
                ((BLangRecordLiteral) ((BLangAnnotationAttachment) attachmentNode).expr).getKeyValuePairs();
    
        IstioVirtualService vsModel = this.processIstioVSAnnotation(keyValues);
        if (isBlank(vsModel.getName())) {
            vsModel.setName(getValidName(endpointNode.getName().getValue()) + ISTIO_VIRTUAL_SERVICE_POSTFIX);
        }
    
        List<BLangRecordLiteral.BLangRecordKeyValue> endpointConfig =
                ((BLangRecordLiteral) ((BLangEndpoint) endpointNode).configurationExpr).getKeyValuePairs();
    
        setDefaultValues(endpointNode.getName().getValue(), vsModel, endpointConfig);
    
        KubernetesContext.getInstance().getDataHolder().addIstioVirtualServiceModels(vsModel);
    }
    
    private void setDefaultValues(String vsName, IstioVirtualService vsModel,
                                  List<BLangRecordLiteral.BLangRecordKeyValue> endpointConfig)
            throws KubernetesPluginException {
        if (null == vsModel.getHosts() || vsModel.getHosts().size() == 0) {
            List<String> hosts = new LinkedList<>();
            hosts.add("*");
            vsModel.setHosts(hosts);
        }
        
        if (null == vsModel.getGateways() || vsModel.getGateways().size() == 0) {
            List<String> gateways = new LinkedList<>();
            gateways.add(getValidName(vsName) + ISTIO_GATEWAY_POSTFIX);
            vsModel.setGateways(gateways);
        }
    
        if ((null == vsModel.getHttp() || vsModel.getHttp().size() == 0) &&
            (null == vsModel.getTls() || vsModel.getTls().size() == 0) &&
            (null == vsModel.getTcp() || vsModel.getTcp().size() == 0)) {
            vsModel.setHttp(new LinkedList<>());
        }
        
        if ((null == vsModel.getTls() && null == vsModel.getTcp()) && vsModel.getHttp().size() == 0) {
            List<IstioHttpRoute> httpRoutes = new LinkedList<>();
            IstioHttpRoute httpRoute = new IstioHttpRoute();
            List<IstioDestinationWeight> destinationWeights = new LinkedList<>();
            IstioDestinationWeight destinationWeight = new IstioDestinationWeight();
            
            IstioDestination destination = new IstioDestination();
            destination.setServiceName(vsName);
            destination.setPort(extractPort(endpointConfig));
            destinationWeight.setDestination(destination);
            
            destinationWeights.add(destinationWeight);
            httpRoute.setRoute(destinationWeights);
            httpRoutes.add(httpRoute);
            vsModel.setHttp(httpRoutes);
        }
    }
    
    /**
     * Process @Kubernetes:IstioGateway annotation.
     * @param gatewayFields Fields of the gateway annotation.
     * @throws KubernetesPluginException Unable to process annotations.
     */
    private IstioVirtualService processIstioVSAnnotation(List<BLangRecordLiteral.BLangRecordKeyValue> gatewayFields)
            throws KubernetesPluginException {
        IstioVirtualService vsModel = new IstioVirtualService();
        for (BLangRecordLiteral.BLangRecordKeyValue gatewayField : gatewayFields) {
            switch (IstioVSConfig.valueOf(gatewayField.getKey().toString())) {
                case name:
                    vsModel.setName(resolveValue(gatewayField.getValue().toString()));
                    break;
                case namespace:
                    vsModel.setNamespace(resolveValue(gatewayField.getValue().toString()));
                    break;
                case labels:
                    BLangRecordLiteral labelsField = (BLangRecordLiteral) gatewayField.getValue();
                    vsModel.setLabels(getMap(labelsField.getKeyValuePairs()));
                    break;
                case annotations:
                    BLangRecordLiteral annotationsField = (BLangRecordLiteral) gatewayField.getValue();
                    vsModel.setAnnotations(getMap(annotationsField.getKeyValuePairs()));
                    break;
                case hosts:
                    BLangArrayLiteral hostsField = (BLangArrayLiteral) gatewayField.getValue();
                    List<String> hostsList = new ArrayList<>(getArray(hostsField));
                    vsModel.setHosts(hostsList);
                    break;
                case gateways:
                    BLangArrayLiteral gatewaysField = (BLangArrayLiteral)  gatewayField.getValue();
                    List<String> gatewayList = new ArrayList<>(getArray(gatewaysField));
                    vsModel.setGateways(gatewayList);
                    break;
                case http:
                    BLangArrayLiteral httpFields = (BLangArrayLiteral) gatewayField.getValue();
                    List<IstioHttpRoute> httpModels = processHttpAnnotation(httpFields);
                    vsModel.setHttp(httpModels);
                    break;
                case tls:
                    BLangArrayLiteral tlsFields = (BLangArrayLiteral) gatewayField.getValue();
                    List<Object> tlsModels = (List<Object>) processAnnotation(tlsFields);
                    vsModel.setTls(tlsModels);
                    break;
                case tcp:
                    BLangArrayLiteral tcpFields = (BLangArrayLiteral) gatewayField.getValue();
                    List<Object> tcpModels = (List<Object>) processAnnotation(tcpFields);
                    vsModel.setTcp(tcpModels);
                    break;
                default:
                    throw new KubernetesPluginException("Unknown field found for istio virtual service.");
            }
        }
        return vsModel;
    }
    
    private List<IstioHttpRoute> processHttpAnnotation(BLangArrayLiteral httpArray) throws KubernetesPluginException {
        List<IstioHttpRoute> httpRoutes = new LinkedList<>();
        for (ExpressionNode expression : httpArray.getExpressions()) {
            BLangRecordLiteral httpFields = (BLangRecordLiteral) expression;
            IstioHttpRoute httpRoute = new IstioHttpRoute();
            for (BLangRecordLiteral.BLangRecordKeyValue httpField : httpFields.getKeyValuePairs()) {
                switch (IstioHttpRouteConfig.valueOf(httpField.getKey().toString())) {
                    case match:
                        List<Object> matches = (List<Object>) processAnnotation(httpField.getValue());
                        httpRoute.setMatch(matches);
                        break;
                    case route:
                        BLangArrayLiteral routeFields = (BLangArrayLiteral)  httpField.getValue();
                        httpRoute.setRoute(processRoutesAnnotation(routeFields));
                        break;
                    case redirect:
                        BLangRecordLiteral redirectFields = (BLangRecordLiteral) httpField.getValue();
                        IstioHttpRedirect httpRedirect = new IstioHttpRedirect();
                        for (BLangRecordLiteral.BLangRecordKeyValue redirectField : redirectFields.getKeyValuePairs()) {
                            switch (redirectField.getKey().toString()) {
                                case "uri":
                                    httpRedirect.setUri(resolveValue((redirectField).getValue().toString()));
                                    break;
                                case "authority":
                                    httpRedirect.setAuthority(resolveValue((redirectField).getValue().toString()));
                                    break;
                                default:
                                    throw new KubernetesPluginException(
                                            "Unknown field found for istio virtual service.");
                            }
                        }
                        httpRoute.setRedirect(httpRedirect);
                        break;
                    case rewrite:
                        httpRoute.setRewrite(processAnnotation(httpField.getValue()));
                        break;
                    case timeout:
                        httpRoute.setTimeout(resolveValue((httpField).getValue().toString()));
                        break;
                    case retries:
                        httpRoute.setRetries(processAnnotation(httpField.getValue()));
                        break;
                    case fault:
                        httpRoute.setFault(processAnnotation(httpField.getValue()));
                        break;
                    case mirror:
                        httpRoute.setMirror(processAnnotation(httpField.getValue()));
                        break;
                    case corsPolicy:
                        httpRoute.setCorsPolicy(processAnnotation(httpField.getValue()));
                        break;
                    case appendHeaders:
                        httpRoute.setAppendHeaders(getMap(((BLangRecordLiteral) httpField.valueExpr).keyValuePairs));
                        break;
                    default:
                        throw new KubernetesPluginException("Unknown field found for istio virtual service.");
                }
            }
            httpRoutes.add(httpRoute);
        }
        return httpRoutes;
    }
    
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
                        throw new KubernetesPluginException("Unknown field found for istio virtual service.");
                }
            }
            destinationWeights.add(destinationWeight);
        }
        
        return destinationWeights;
    }
    
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
                    BLangRecordLiteral portFields = (BLangRecordLiteral) destinationField.getValue();
                    BLangRecordLiteral.BLangRecordKeyValue portField = portFields.getKeyValuePairs().get(0);
                    destination.setPort(Integer.parseInt(portField.getValue().toString()));
                    break;
                default:
                    throw new KubernetesPluginException("Unknown field found for istio virtual service.");
            }
        }
        
        return destination;
    }
    
    private Object processAnnotation(ExpressionNode annotationValue) throws KubernetesPluginException {
        if (annotationValue instanceof BLangArrayLiteral) {
            BLangArrayLiteral arrayValue = (BLangArrayLiteral) annotationValue;
            List<Object> arrayModels = new LinkedList<>();
            for (ExpressionNode expression : arrayValue.getExpressions()) {
                arrayModels.add(processAnnotation(expression));
            }
            return arrayModels;
        } else if (annotationValue instanceof BLangRecordLiteral) {
            BLangRecordLiteral serverFieldRecord = (BLangRecordLiteral) annotationValue;
            Map<String, Object> mapModels = new LinkedHashMap<>();
            for (BLangRecordLiteral.BLangRecordKeyValue keyValuePair : serverFieldRecord.getKeyValuePairs()) {
                mapModels.put(keyValuePair.getKey().toString(), processAnnotation(keyValuePair.getValue()));
            }
            return mapModels;
        } else if (annotationValue instanceof BLangLiteral) {
            BLangLiteral literal = (BLangLiteral) annotationValue;
            if (literal.typeTag == TypeTags.INT_TAG) {
                return Integer.parseInt((literal).getValue().toString());
            } else if (literal.typeTag == TypeTags.BOOLEAN_TAG) {
                return Boolean.parseBoolean((literal).getValue().toString());
            } else if (literal.typeTag == TypeTags.FLOAT_TAG) {
                return Float.parseFloat((literal).getValue().toString());
            } else {
                return resolveValue((literal).getValue().toString());
            }
        } else {
            throw new KubernetesPluginException("Unable to resolve annotation values.");
        }
    }
    
    private int extractPort(List<BLangRecordLiteral.BLangRecordKeyValue> endpointConfig) throws
            KubernetesPluginException {
        for (BLangRecordLiteral.BLangRecordKeyValue keyValue : endpointConfig) {
            String key = keyValue.getKey().toString();
            if ("port".equals(key)) {
                try {
                    return Integer.parseInt(keyValue.getValue().toString());
                } catch (NumberFormatException e) {
                    throw new KubernetesPluginException("Listener endpoint port must be an integer to use " +
                                                        "@kubernetes annotations.");
                }
            }
        }
        throw new KubernetesPluginException("Unable to extract port from endpoint");
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
        match,
        route,
        redirect,
        rewrite,
        timeout,
        retries,
        fault,
        mirror,
        corsPolicy,
        appendHeaders
    }
    
    private enum IstioVSConfig {
        name,
        namespace,
        labels,
        annotations,
        hosts,
        gateways,
        http,
        tls,
        tcp
    }
}
