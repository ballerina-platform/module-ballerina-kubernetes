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

package org.ballerinax.kubernetes.handlers;

import io.fabric8.kubernetes.api.model.extensions.HTTPIngressPath;
import io.fabric8.kubernetes.api.model.extensions.HTTPIngressPathBuilder;
import io.fabric8.kubernetes.api.model.extensions.Ingress;
import io.fabric8.kubernetes.api.model.extensions.IngressBackend;
import io.fabric8.kubernetes.api.model.extensions.IngressBackendBuilder;
import io.fabric8.kubernetes.api.model.extensions.IngressBuilder;
import io.fabric8.kubernetes.api.model.extensions.IngressTLS;
import io.fabric8.kubernetes.api.model.extensions.IngressTLSBuilder;
import io.fabric8.kubernetes.client.internal.SerializationUtils;
import org.ballerinax.kubernetes.KubernetesConstants;
import org.ballerinax.kubernetes.exceptions.KubernetesPluginException;
import org.ballerinax.kubernetes.models.IngressModel;
import org.ballerinax.kubernetes.models.SecretModel;
import org.ballerinax.kubernetes.models.ServiceModel;
import org.ballerinax.kubernetes.utils.KubernetesUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.ballerinax.kubernetes.KubernetesConstants.INGRESS_FILE_POSTFIX;
import static org.ballerinax.kubernetes.KubernetesConstants.NGINX;
import static org.ballerinax.kubernetes.KubernetesConstants.YAML;


/**
 * Generates kubernetes ingress from annotations.
 */
public class IngressHandler extends AbstractArtifactHandler {

    /**
     * Generate kubernetes ingress definition from annotation.
     *
     * @param ingressModel IngressModel object
     * @throws KubernetesPluginException If an error occurs while generating artifact.
     */
    private void generate(IngressModel ingressModel) throws KubernetesPluginException {
        //generate ingress backend
        IngressBackend ingressBackend = new IngressBackendBuilder()
                .withServiceName(ingressModel.getServiceName())
                .withNewServicePort(ingressModel.getServicePort())
                .build();

        //generate ingress path
        HTTPIngressPath ingressPath = new HTTPIngressPathBuilder()
                .withBackend(ingressBackend)
                .withPath(ingressModel
                        .getPath()).build();

        //generate TLS
        List<IngressTLS> ingressTLS = new ArrayList<>();
        if (ingressModel.isEnableTLS()) {
            ingressTLS.add(new IngressTLSBuilder()
                    .withHosts(ingressModel.getHostname())
                    .build());
        }

        //generate annotationMap
        Map<String, String> annotationMap = new HashMap<>();
        annotationMap.put("kubernetes.io/ingress.class", ingressModel.getIngressClass());
        if (NGINX.equals(ingressModel.getIngressClass())) {
            annotationMap.put("nginx.ingress.kubernetes.io/ssl-passthrough", String.valueOf(ingressModel.isEnableTLS
                    ()));
            if (ingressModel.getTargetPath() != null) {
                annotationMap.put("nginx.ingress.kubernetes.io/rewrite-target", ingressModel.getTargetPath());
            }
        }
        //Add user defined ingress annotations to yaml.
        Map<String, String> userDefinedAnnotationMap = ingressModel.getAnnotations();
        if (userDefinedAnnotationMap != null) {
            userDefinedAnnotationMap.forEach(annotationMap::putIfAbsent);
        }

        //generate ingress
        Ingress ingress = new IngressBuilder()
                .withNewMetadata()
                .withName(ingressModel.getName())
                .withNamespace(dataHolder.getNamespace())
                .addToLabels(ingressModel.getLabels())
                .addToAnnotations(annotationMap)
                .endMetadata()
                .withNewSpec()
                .withTls(ingressTLS)
                .addNewRule()
                .withHost(ingressModel.getHostname())
                .withNewHttp()
                .withPaths(ingressPath)
                .endHttp()
                .endRule()
                .endSpec()
                .build();
        String ingressYAML;
        try {
            ingressYAML = SerializationUtils.dumpWithoutRuntimeStateAsYaml(ingress);
            KubernetesUtils.writeToFile(ingressYAML, INGRESS_FILE_POSTFIX + YAML);
        } catch (IOException e) {
            String errorMessage = "error while generating yaml file for ingress: " + ingressModel.getName();
            throw new KubernetesPluginException(errorMessage, e);
        }
    }

    @Override
    public void createArtifacts() throws KubernetesPluginException {
        Set<IngressModel> ingressModels = dataHolder.getIngressModelSet();
        int size = ingressModels.size();
        if (size > 0) {
            OUT.println();
        }
        int count = 0;
        Map<String, Set<SecretModel>> secretModelsMap = dataHolder.getSecretModels();
        for (IngressModel ingressModel : ingressModels) {
            ServiceModel serviceModel = dataHolder.getServiceModel(ingressModel.getListenerName());
            if (serviceModel == null) {
                throw new KubernetesPluginException("@kubernetes:Ingress annotation should be followed by " +
                        "@kubernetes:Service annotation.");
            }
            ingressModel.setServiceName(serviceModel.getName());
            ingressModel.setServicePort(serviceModel.getPort());
            String balxFileName = KubernetesUtils.extractBalxName(dataHolder.getBalxFilePath());
            ingressModel.getLabels().put(KubernetesConstants.KUBERNETES_SELECTOR_KEY, balxFileName);
            if (secretModelsMap.get(ingressModel.getListenerName()) != null && secretModelsMap.get(ingressModel
                    .getListenerName()).size() != 0) {
                ingressModel.setEnableTLS(true);
            }
            generate(ingressModel);
            count++;
            OUT.print("\t@kubernetes:Ingress \t\t\t - complete " + count + "/" + size + "\r");
        }
    }
}
