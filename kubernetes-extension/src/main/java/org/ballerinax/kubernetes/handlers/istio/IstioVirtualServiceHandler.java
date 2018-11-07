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

import org.ballerinax.kubernetes.exceptions.KubernetesPluginException;
import org.ballerinax.kubernetes.handlers.AbstractArtifactHandler;
import org.ballerinax.kubernetes.models.KubernetesContext;
import org.ballerinax.kubernetes.models.ServiceModel;
import org.ballerinax.kubernetes.models.istio.IstioDestination;
import org.ballerinax.kubernetes.models.istio.IstioDestinationWeight;
import org.ballerinax.kubernetes.models.istio.IstioGatewayModel;
import org.ballerinax.kubernetes.models.istio.IstioHttpRedirect;
import org.ballerinax.kubernetes.models.istio.IstioHttpRoute;
import org.ballerinax.kubernetes.models.istio.IstioVirtualService;
import org.ballerinax.kubernetes.utils.KubernetesUtils;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.ballerinax.kubernetes.KubernetesConstants.ISTIO_VIRTUAL_SERVICE_FILE_POSTFIX;
import static org.ballerinax.kubernetes.KubernetesConstants.YAML;

/**
 * Generates istio virtual service artifacts.
 */
public class IstioVirtualServiceHandler extends AbstractArtifactHandler {
    @Override
    public void createArtifacts() throws KubernetesPluginException {
        Map<String, IstioVirtualService> istioVSModels = dataHolder.getIstioVirtualServiceModels();
        int size = istioVSModels.size();
        if (size > 0) {
            OUT.println();
        }
    
        int count = 0;
        for (Map.Entry<String, IstioVirtualService> vsModel : istioVSModels.entrySet()) {
            count++;
            generate(vsModel);
            OUT.print("\t@kubernetes:IstioVirtualService \t - complete " + count + "/" + istioVSModels.size() + "\r");
        }
    }
    
    private void generate(Map.Entry<String, IstioVirtualService> vsModelEntry) throws KubernetesPluginException {
        IstioVirtualService vsModel = vsModelEntry.getValue();
        try {
            Map<String, Object> vsYamlModel = new LinkedHashMap<>();
            vsYamlModel.put("apiVersion", "networking.istio.io/v1alpha3");
            vsYamlModel.put("kind", "VirtualService");
        
            // metadata
            Map<String, Object> metadata = new LinkedHashMap<>();
            metadata.put("name", vsModel.getName());
            if (null != vsModel.getNamespace()) {
                metadata.put("namespace", vsModel.getNamespace());
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
    
            if (null != vsModel.getTls() && vsModel.getTls().size() > 0) {
                spec.put("tls", vsModel.getTls());
            }
    
            if (null != vsModel.getTcp() && vsModel.getTcp().size() > 0) {
                spec.put("tcp", vsModel.getTcp());
            }
    
            // parse and add default values for http list if tls and tcp are not set
            if (null == vsModel.getTls() && null == vsModel.getTcp()) {
                spec.put("http", parseHttpRouteList(vsModelEntry.getKey(), vsModel.getHttp()));
            }
    
            if (null == vsModel.getGateways()) {
                vsModel.setGateways(new LinkedList<>());
            }
            
            if (vsModel.getGateways().size() == 0) {
                IstioGatewayModel gwModel =
                        KubernetesContext.getInstance().getDataHolder().getIstioGatewayModel(vsModelEntry.getKey());
    
                if (null != gwModel) {
                    vsModel.getGateways().add(gwModel.getName());
                }
            }
            
            // when gateways are not set, it will apply to all side cars.
            if (vsModel.getGateways().size() != 0) {
                spec.put("gateways", vsModel.getGateways());
            }
    
            vsYamlModel.put("spec", spec);
        
            DumperOptions options = new DumperOptions();
            options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        
            Yaml yamlProcessor = new Yaml(options);
            String vsYamlString = yamlProcessor.dump(vsYamlModel);
        
            KubernetesUtils.writeToFile(vsYamlString, ISTIO_VIRTUAL_SERVICE_FILE_POSTFIX + YAML);
        } catch (IOException e) {
            String errorMessage = "Error while generating yaml file for istio virtual service: " + vsModel.getName();
            throw new KubernetesPluginException(errorMessage, e);
        }
    }
    
    private Object parseHttpRouteList(String serviceName, List<IstioHttpRoute> http) {
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
            if (null != httpRoute.getRedirect()) {
                httpMap.put("redirect", parseRedirect(httpRoute.getRedirect()));
            }
            if (null != httpRoute.getRewrite()) {
                httpMap.put("rewrite", httpRoute.getRewrite());
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
            
            // route is mandatory, no need to null check
            httpMap.put("route", parseRouteList(serviceName, httpRoute.getRoute()));
            
            httpList.add(httpMap);
        }
        
        return httpList;
    }
    
    private Map<String, String> parseRedirect(IstioHttpRedirect redirect) {
        Map<String, String> redirectMap = new LinkedHashMap<>();
        if (null != redirect.getUri()) {
            redirectMap.put("uri", redirect.getUri());
        }
        if (null != redirect.getAuthority()) {
            redirectMap.put("authority", redirect.getAuthority());
        }
        return redirectMap;
    }
    
    private Object parseRouteList(String serviceName, List<IstioDestinationWeight> route) {
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
    
            destinationWeightMap.put("destination", parseDestination(serviceName, destinationWeight.getDestination()));
            destinationWeightList.add(destinationWeightMap);
        }
        return destinationWeightList;
    }
    
    private Map<String, Object> parseDestination(String serviceName, IstioDestination destination) {
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
