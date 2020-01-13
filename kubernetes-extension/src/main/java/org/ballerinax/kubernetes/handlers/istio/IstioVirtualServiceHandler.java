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

import io.fabric8.kubernetes.client.internal.SerializationUtils;
import me.snowdrop.istio.api.Duration;
import me.snowdrop.istio.api.DurationBuilder;
import me.snowdrop.istio.api.networking.v1alpha3.Destination;
import me.snowdrop.istio.api.networking.v1alpha3.DestinationBuilder;
import me.snowdrop.istio.api.networking.v1alpha3.HTTPRoute;
import me.snowdrop.istio.api.networking.v1alpha3.HTTPRouteBuilder;
import me.snowdrop.istio.api.networking.v1alpha3.HTTPRouteDestination;
import me.snowdrop.istio.api.networking.v1alpha3.HTTPRouteDestinationBuilder;
import me.snowdrop.istio.api.networking.v1alpha3.PortSelectorBuilder;
import me.snowdrop.istio.api.networking.v1alpha3.VirtualService;
import me.snowdrop.istio.api.networking.v1alpha3.VirtualServiceBuilder;
import org.ballerinax.kubernetes.exceptions.KubernetesPluginException;
import org.ballerinax.kubernetes.handlers.AbstractArtifactHandler;
import org.ballerinax.kubernetes.models.KubernetesContext;
import org.ballerinax.kubernetes.models.ServiceModel;
import org.ballerinax.kubernetes.models.istio.IstioDestination;
import org.ballerinax.kubernetes.models.istio.IstioDestinationWeight;
import org.ballerinax.kubernetes.models.istio.IstioGatewayModel;
import org.ballerinax.kubernetes.models.istio.IstioHttpRoute;
import org.ballerinax.kubernetes.models.istio.IstioVirtualServiceModel;
import org.ballerinax.kubernetes.utils.KubernetesUtils;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
                                                        "virtual service. Add @istio:Gateway{} annotation" +
                                                        " to your listener or service, else explicitly state to " +
                                                        "use the 'mesh' gateway.");
                }
            }
            
            generate(vsModel.getKey(), vsModel.getValue());
            OUT.print("\t@istio:VirtualService \t\t\t - complete " + count + "/" + size + "\r");
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
                    .withHttp(populateHttp(serviceName, vsModel.getHttp()))
                    .endSpec()
                    .build();
    
            String gatewayContent = SerializationUtils.dumpWithoutRuntimeStateAsYaml(virtualService);
            KubernetesUtils.writeToFile(gatewayContent, ISTIO_VIRTUAL_SERVICE_FILE_POSTFIX + YAML);
        } catch (IOException e) {
            String errorMessage = "error while generating yaml file for istio virtual service: " + vsModel.getName();
            throw new KubernetesPluginException(errorMessage, e);
        }
    }
    
    /**
     * Parsing a list of http routes to yaml maps.
     *
     * @param serviceName The name of the service where to route to.
     * @param httpRouteModels        The list of http routes.
     * @return A list of yaml maps.
     */
    private List<HTTPRoute> populateHttp(String serviceName, List<IstioHttpRoute> httpRouteModels) {
        if (null == httpRouteModels) {
            httpRouteModels = new LinkedList<>();
        }
        
        if (httpRouteModels.size() == 0) {
            httpRouteModels.add(new IstioHttpRoute());
        }
        
        List<HTTPRoute> httpRoutes = new LinkedList<>();
        for (IstioHttpRoute httpRouteModel : httpRouteModels) {
            Duration timoutDuration = null;
    
            if (-1 != httpRouteModel.getTimeout()) {
                timoutDuration = new DurationBuilder()
                        .withSeconds(httpRouteModel.getTimeout())
                        .build();
            }
            
            HTTPRoute httpRoute = new HTTPRouteBuilder()
                    .withRoute(populateRouteList(serviceName, httpRouteModel.getRoute()))
                    .withTimeout(timoutDuration)
                    .withAppendHeaders(httpRouteModel.getAppendHeaders())
                    .build();
    
            httpRoutes.add(httpRoute);
        }
        
        return httpRoutes;
    }
    
    /**
     * Parse an route list to a yaml map.
     *
     * @param serviceName The name of the service.
     * @param routeModels       The list of destination weights
     * @return A list of yaml maps.
     */
    private List<HTTPRouteDestination> populateRouteList(String serviceName, List<IstioDestinationWeight> routeModels) {
        if (routeModels == null) {
            routeModels = new LinkedList<>();
        }
        
        if (routeModels.size() == 0) {
            routeModels.add(new IstioDestinationWeight());
        }
    

        List<HTTPRouteDestination> destinationWeightList = new LinkedList<>();
        for (IstioDestinationWeight destinationWeightModel : routeModels) {
            HTTPRouteDestination routeDestination = new HTTPRouteDestinationBuilder()
                    .withWeight(destinationWeightModel.getWeight())
                    .withDestination(populateDestination(serviceName, destinationWeightModel.getDestination()))
                    .build();
    
            destinationWeightList.add(routeDestination);
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
    private Destination populateDestination(String serviceName, IstioDestination destination) {
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
        
        return new DestinationBuilder()
                .withHost(destination.getHost())
                .withSubset(destination.getSubset())
                .withPort(new PortSelectorBuilder()
                    .withNewNumberPort(serviceModel.getPort())
                    .build())
                .build();
    }
}
