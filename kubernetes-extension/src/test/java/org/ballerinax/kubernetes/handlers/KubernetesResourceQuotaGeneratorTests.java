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

import io.fabric8.kubernetes.api.KubernetesHelper;
import io.fabric8.kubernetes.api.model.ResourceQuota;
import org.ballerinax.kubernetes.exceptions.KubernetesPluginException;
import org.ballerinax.kubernetes.models.KubernetesContext;
import org.ballerinax.kubernetes.models.ResourceQuotaModel;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static org.ballerinax.kubernetes.KubernetesConstants.RESOURCE_QUOTA_FILE_POSTFIX;
import static org.ballerinax.kubernetes.KubernetesConstants.YAML;

/**
 * Test resource quotas
 */
public class KubernetesResourceQuotaGeneratorTests {
    
    @Test
    public void testResourceQuota() {
        ResourceQuotaModel resourceQuotaModel = new ResourceQuotaModel();
        resourceQuotaModel.setName("MyResourceQuota");
    
        Map<String, String> quotas = new LinkedHashMap<>();
        quotas.put("cpu", "1000");
        quotas.put("memory", "200Gi");
        quotas.put("pods", "10");
        resourceQuotaModel.setHard(quotas);
        
        Set<String> scopes = new LinkedHashSet<>();
        scopes.add("high");
        resourceQuotaModel.setScopes(scopes);
    
        Set<ResourceQuotaModel> resourceQuotaModels = new LinkedHashSet<>();
        resourceQuotaModels.add(resourceQuotaModel);
        KubernetesContext.getInstance().getDataHolder().setResourceQuotaModels(resourceQuotaModels);
        try {
            new ResourceQuotaHandler().createArtifacts();
            File yamlFile = new File("target" + File.separator + "kubernetes" + File.separator +
                                     "hello" + RESOURCE_QUOTA_FILE_POSTFIX + YAML);
            Assert.assertTrue(yamlFile.exists(), "Generated file not found.");
            ResourceQuota resourceQuota = KubernetesHelper.loadYaml(yamlFile);
            
            // metadata
            Assert.assertEquals("MyResourceQuota", resourceQuota.getMetadata().getName());
            
            // quotas
            Assert.assertEquals(3, resourceQuota.getSpec().getHard().size());
            Assert.assertEquals("1000", resourceQuota.getSpec().getHard().get("cpu").getAmount());
            Assert.assertEquals("200Gi", resourceQuota.getSpec().getHard().get("memory").getAmount());
            Assert.assertEquals("10", resourceQuota.getSpec().getHard().get("pods").getAmount());
            
            // scopes
            Assert.assertEquals(1, resourceQuota.getSpec().getScopes().size());
            Assert.assertEquals("high", resourceQuota.getSpec().getScopes().get(0));
            
            yamlFile.deleteOnExit();
        } catch (IOException e) {
            Assert.fail("Unable to write to file: " + e.getMessage());
        } catch (KubernetesPluginException e) {
            Assert.fail("Unable to generate yaml from service: " + e.getMessage());
        }
    }
}
