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
import org.ballerinax.kubernetes.models.istio.IstioVirtualService;
import org.ballerinax.kubernetes.utils.KubernetesUtils;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import static org.ballerinax.kubernetes.KubernetesConstants.ISTIO_VIRTUAL_SERVICE_FILE_POSTFIX;
import static org.ballerinax.kubernetes.KubernetesConstants.YAML;

/**
 * Generates istio virtual service artifacts.
 */
public class IstioVirtualHandler extends AbstractArtifactHandler {
    @Override
    public void createArtifacts() throws KubernetesPluginException {
        Set<IstioVirtualService> istioVSModels = dataHolder.getIstioVirtualServiceModels();
        int size = istioVSModels.size();
        if (size > 0) {
            OUT.println();
        }
    
        int count = 0;
        for (IstioVirtualService vsModel : istioVSModels) {
            count++;
            generate(vsModel);
            OUT.print("\t@kubernetes:IstioVirtualService \t - complete " + count + "/" + istioVSModels.size() + "\r");
        }
    }
    
    private void generate(IstioVirtualService vsModel) throws KubernetesPluginException {
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
    
            if (null != vsModel.getGateways() && vsModel.getGateways().size() > 0) {
                spec.put("gateways", vsModel.getGateways());
            }
    
            if (null != vsModel.getHttp() && vsModel.getHttp().size() > 0) {
                spec.put("http", vsModel.getHttp());
            }
            
            if (null != vsModel.getTls() && vsModel.getTls().size() > 0) {
                spec.put("tls", vsModel.getTls());
            }
    
            if (null != vsModel.getTcp() && vsModel.getTcp().size() > 0) {
                spec.put("tcp", vsModel.getTcp());
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
}
