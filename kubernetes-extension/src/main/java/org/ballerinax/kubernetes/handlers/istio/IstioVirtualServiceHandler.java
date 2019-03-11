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

package org.ballerinax.kubernetes.handlers.istio;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import me.snowdrop.istio.api.networking.v1alpha3.DestinationBuilder;
import me.snowdrop.istio.api.networking.v1alpha3.DestinationWeight;
import me.snowdrop.istio.api.networking.v1alpha3.DestinationWeightBuilder;
import me.snowdrop.istio.api.networking.v1alpha3.PortSelector;
import me.snowdrop.istio.api.networking.v1alpha3.PortSelectorBuilder;
import me.snowdrop.istio.api.networking.v1alpha3.TLSMatchAttributes;
import me.snowdrop.istio.api.networking.v1alpha3.TLSMatchAttributesBuilder;
import me.snowdrop.istio.api.networking.v1alpha3.TLSRoute;
import me.snowdrop.istio.api.networking.v1alpha3.TLSRouteBuilder;
import me.snowdrop.istio.api.networking.v1alpha3.VirtualService;
import me.snowdrop.istio.api.networking.v1alpha3.VirtualServiceBuilder;
import org.ballerinax.kubernetes.exceptions.KubernetesPluginException;
import org.ballerinax.kubernetes.handlers.AbstractArtifactHandler;
import org.ballerinax.kubernetes.models.KubernetesContext;
import org.ballerinax.kubernetes.models.ServiceModel;
import org.ballerinax.kubernetes.models.istio.IstioDestination;
import org.ballerinax.kubernetes.models.istio.IstioDestinationWeight;
import org.ballerinax.kubernetes.models.istio.IstioGatewayModel;
import org.ballerinax.kubernetes.models.istio.IstioHttpRedirect;
import org.ballerinax.kubernetes.models.istio.IstioHttpRoute;
import org.ballerinax.kubernetes.models.istio.IstioTLSMatchAttributes;
import org.ballerinax.kubernetes.models.istio.IstioTLSRoute;
import org.ballerinax.kubernetes.models.istio.IstioVirtualServiceModel;
import org.ballerinax.kubernetes.utils.KubernetesUtils;
import org.yaml.snakeyaml.DumperOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.ballerinax.kubernetes.KubernetesConstants.ISTIO_VIRTUAL_SERVICE_FILE_POSTFIX;
import static org.ballerinax.kubernetes.KubernetesConstants.YAML;

/**
 * Generates istio virtual service artifacts.
 *
 * @since 0.985.0
 */
public class IstioVirtualServiceHandler extends AbstractArtifactHandler {
    
    @Override
    public void createArtifacts() throws KubernetesPluginException {
        Map<String, IstioVirtualServiceModel> istioVSModels = dataHolder.getIstioVirtualServiceModels();
        int size = istioVSModels.size();
        if (size > 0) {
            OUT.println();
        }
    
        int count = 0;
        for (Map.Entry<String, IstioVirtualServiceModel> vsModel : istioVSModels.entrySet()) {
            count++;
    
            IstioGatewayModel gwModel =
                    KubernetesContext.getInstance().getDataHolder().getIstioGatewayModel(vsModel.getKey());
            if ((null == vsModel.getValue().getGateways() || vsModel.getValue().getGateways().size() == 0) &&
                                                                                                    null != gwModel) {
                if (null == vsModel.getValue().getGateways()) {
                    vsModel.getValue().setGateways(new LinkedList<>());
                }
        
                if (vsModel.getValue().getGateways().size() == 0) {
                    vsModel.getValue().getGateways().add(gwModel.getName());
                } else if (vsModel.getValue().getHosts().size() == 1 && vsModel.getValue().getHosts().contains("*")) {
                    throw new KubernetesPluginException("unable to resolve a gateway for '" + vsModel + "' " +
                                                        "virtual service. Add @kubernetes:IstioGateway{} annotation" +
                                                        " to your listener or service, else explicitly state to " +
                                                        "use the 'mesh' gateway.");
                }
            }
            
            generate(vsModel.getKey(), vsModel.getValue());
            OUT.print("\t@kubernetes:IstioVirtualService \t - complete " + count + "/" + size + "\r");
        }
    }
    
    /**
     * Generate artifact for istio virtual service model.
     *
     * @param serviceName The name of the service in which the virtual service routes to.
     * @param vsModel     The virtual service model.
     * @throws KubernetesPluginException Error when writing artifact files.
     */
    private void generate(String serviceName, IstioVirtualServiceModel vsModel) throws KubernetesPluginException {
        try {
            VirtualService virtualService = new VirtualServiceBuilder()
                    .withNewMetadata()
                    .withName(vsModel.getName())
                    .withNamespace(dataHolder.getNamespace())
                    .withLabels(vsModel.getLabels())
                    .withAnnotations(vsModel.getAnnotations())
                    .endMetadata()
                    .withNewSpec()
                    .withHosts(vsModel.getHosts())
                    .withGateways(vsModel.getGateways())
                    .withTls(populateTLS(vsModel.getTls()))
                    .withHttp(populateRouteList())
                    
                    
                    
                    
                    
                    
            Map<String, Object> vsYamlModel = new LinkedHashMap<>();
            vsYamlModel.put("apiVersion", "networking.istio.io/v1alpha3");
            vsYamlModel.put("kind", "VirtualService");
        
            // metadata
            Map<String, Object> metadata = new LinkedHashMap<>();
            metadata.put("name", vsModel.getName());
            if (null != dataHolder.getNamespace()) {
                metadata.put("namespace", dataHolder.getNamespace());
            }
            if (null != vsModel.getLabels() && vsModel.getLabels().size() > 0) {
                metadata.put("labels", vsModel.getLabels());
            }
            if (null != vsModel.getAnnotations() && vsModel.getAnnotations().size() > 0) {
                metadata.put("annotations", vsModel.getAnnotations());
            }
            vsYamlModel.put("metadata", metadata);
        
            // spec
            Map<String, Object> spec = new LinkedHashMap<>();
            if (null != vsModel.getHosts() && vsModel.getHosts().size() > 0) {
                spec.put("hosts", vsModel.getHosts());
            }
    
    
            spec.put("gateways", vsModel.getGateways());
    
            if (null != vsModel.getTls() && vsModel.getTls().size() > 0) {
                spec.put("tls", vsModel.getTls());
            }
    
            if (null != vsModel.getTcp() && vsModel.getTcp().size() > 0) {
                spec.put("tcp", vsModel.getTcp());
            }
    
            // parse and add default values for http list if tls and tcp are not set
            if (null == vsModel.getTls() && null == vsModel.getTcp()) {
                spec.put("http", populateHttpRouteList(serviceName, vsModel.getHttp()));
            }
    
            vsYamlModel.put("spec", spec);
        
            DumperOptions options = new DumperOptions();
            options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
    
            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            String vsYamlString = mapper.writeValueAsString(vsYamlModel);
            
            KubernetesUtils.writeToFile(vsYamlString, ISTIO_VIRTUAL_SERVICE_FILE_POSTFIX + YAML);
        } catch (IOException e) {
            String errorMessage = "Error while generating yaml file for istio virtual service: " + vsModel.getName();
            throw new KubernetesPluginException(errorMessage, e);
        }
    }
    
    private List<TLSRoute> populateTLS(List<IstioTLSRoute> tlsRouteModels) {
        if (null == tlsRouteModels) {
            return null;
        }
        
        List<TLSRoute> tlsRoutes = new LinkedList<>();
        for (IstioTLSRoute tlsRouteModel : tlsRouteModels) {
            List<TLSMatchAttributes> matches = new LinkedList<>();
            for (IstioTLSMatchAttributes istioTLSMatchAttribute : tlsRouteModel.getIstioTLSMatchAttributes()) {
                TLSMatchAttributes tlsMatchAttributes = new TLSMatchAttributesBuilder()
                        .withSniHosts(new ArrayList<>(istioTLSMatchAttribute.getSniHosts()))
                        .withDestinationSubnets(new ArrayList<>(istioTLSMatchAttribute.getDestinationSubnets()))
                        .withPort(istioTLSMatchAttribute.getPort())
                        .withSourceLabels(istioTLSMatchAttribute.getSourceLabels())
                        .withGateways(new ArrayList<>(istioTLSMatchAttribute.getGateways()))
                        .build();
                
                matches.add(tlsMatchAttributes);
            }
    
            List<DestinationWeight> destinationWeights = new LinkedList<>();
            for (IstioDestinationWeight istioDestinationWeight : tlsRouteModel.getRoute()) {
                DestinationWeight destinationWeight = new DestinationWeightBuilder()
                    .withDestination(new DestinationBuilder()
                        .withHost(istioDestinationWeight.getDestination().getHost())
                        .withSubset(istioDestinationWeight.getDestination().getSubset())
                        .withPort(new PortSelectorBuilder()
                            .withNewNumberPort(istioDestinationWeight.getDestination().getPort()
                            ).build()
                        ).build()
                    ).withWeight(istioDestinationWeight.getWeight())
                    .build();
    
                destinationWeights.add(destinationWeight);
            }

            TLSRoute tlsRoute = new TLSRouteBuilder()
                    .withMatch(matches)
                    .withRoute(destinationWeights)
                    .build();
    
            tlsRoutes.add(tlsRoute);
        }
        
        return tlsRoutes;
    }
    
    /**
     * Parsing a list of http routes to yaml maps.
     *
     * @param serviceName The name of the service where to route to.
     * @param http        The list of http routes.
     * @return A list of yaml maps.
     */
    private Object populateHttpRouteList(String serviceName, List<IstioHttpRoute> http) {
        if (null == http) {
            http = new LinkedList<>();
        }
        
        if (http.size() == 0) {
            http.add(new IstioHttpRoute());
        }
        
        List<Map<String, Object>> httpList = new LinkedList<>();
        for (IstioHttpRoute httpRoute : http) {
            Map<String, Object> httpMap = new LinkedHashMap<>();
            if (null != httpRoute.getMatch()) {
                httpMap.put("match", httpRoute.getMatch());
            }
            if (null != httpRoute.getRewrite()) {
                httpMap.put("rewrite", httpRoute.getRewrite());
            }
            if (null != httpRoute.getRedirect()) {
                httpMap.put("redirect", populateRedirect(httpRoute.getRedirect()));
            }
            if (null != httpRoute.getTimeout()) {
                httpMap.put("timeout", httpRoute.getTimeout());
            }
            if (null != httpRoute.getRetries()) {
                httpMap.put("retries", httpRoute.getRetries());
            }
            if (null != httpRoute.getFault()) {
                httpMap.put("fault", httpRoute.getFault());
            }
            if (null != httpRoute.getMirror()) {
                httpMap.put("mirror", httpRoute.getMirror());
            }
            if (null != httpRoute.getCorsPolicy()) {
                httpMap.put("corsPolicy", httpRoute.getCorsPolicy());
            }
            if (null != httpRoute.getAppendHeaders()) {
                httpMap.put("appendHeaders", httpRoute.getAppendHeaders());
            }
    
            // route and redirect cannot exists together.
            if (null == httpRoute.getRedirect()) {
                // route is mandatory, no need to null check
                httpMap.put("route", populateRouteList(serviceName, httpRoute.getRoute()));
            }
    
            httpList.add(httpMap);
        }
        
        return httpList;
    }
    
    /**
     * Parse an http redirect object to yaml maps.
     *
     * @param redirect The redirect object.
     * @return A yaml map.
     */
    private Map<String, String> populateRedirect(IstioHttpRedirect redirect) {
        Map<String, String> redirectMap = new LinkedHashMap<>();
        if (null != redirect.getUri()) {
            redirectMap.put("uri", redirect.getUri());
        }
        if (null != redirect.getAuthority()) {
            redirectMap.put("authority", redirect.getAuthority());
        }
        return redirectMap;
    }
    
    /**
     * Parse an route list to a yaml map.
     *
     * @param serviceName The name of the service.
     * @param route       The list of destination weights
     * @return A list of yaml maps.
     */
    private Object populateRouteList(String serviceName, List<IstioDestinationWeight> route) {
        if (route == null) {
            route = new LinkedList<>();
        }
        
        if (route.size() == 0) {
            route.add(new IstioDestinationWeight());
        }
        
        List<Map<String, Object>> destinationWeightList = new LinkedList<>();
        for (IstioDestinationWeight destinationWeight : route) {
            Map<String, Object> destinationWeightMap = new LinkedHashMap<>();
            if (destinationWeight.getWeight() != -1) {
                destinationWeightMap.put("weight", destinationWeight.getWeight());
            }
    
            destinationWeightMap.put("destination", populateDestination(serviceName, destinationWeight.getDestination()));
            destinationWeightList.add(destinationWeightMap);
        }
        return destinationWeightList;
    }
    
    /**
     * Parse destination object to yaml map.
     *
     * @param serviceName The name of the service which is routed to.
     * @param destination The destination object.
     * @return A yaml map.
     */
    private Map<String, Object> populateDestination(String serviceName, IstioDestination destination) {
        if (null == destination) {
            destination = new IstioDestination();
        }
    
        ServiceModel serviceModel = KubernetesContext.getInstance().getDataHolder().getServiceModel(serviceName);
        if (null == destination.getHost()) {
            destination.setHost(serviceModel.getName());
        }
        
        if (-1 == destination.getPort()) {
            destination.setPort(serviceModel.getPort());
        }
        
        Map<String, Object> destinationMap = new LinkedHashMap<>();
    
        // host is mandatory, no need to check null as defaults are set.
        destinationMap.put("host", destination.getHost());
        
        if (null != destination.getSubset()) {
            destinationMap.put("subset", destination.getSubset());
        }
        
        // port and it's number is mandatory, no need to null check as defaults are set.
        Map<String, Integer> port = new LinkedHashMap<>();
        port.put("number", serviceModel.getPort());
        destinationMap.put("port", port);
        return destinationMap;
    }
}
