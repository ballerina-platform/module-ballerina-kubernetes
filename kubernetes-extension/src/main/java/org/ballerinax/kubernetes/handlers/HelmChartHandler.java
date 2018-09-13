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

import org.ballerinax.kubernetes.KubernetesConstants;
import org.ballerinax.kubernetes.exceptions.KubernetesPluginException;
import org.ballerinax.kubernetes.utils.KubernetesUtils;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Locale;

import static org.ballerinax.kubernetes.KubernetesConstants.HELM_CHART;
import static org.ballerinax.kubernetes.KubernetesConstants.HELM_CHART_TEMPLATES;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.extractBalxName;

/**
 * Generates the Helm chart from annotations.
 */
public class HelmChartHandler extends AbstractArtifactHandler {

    @Override
    public void createArtifacts() throws KubernetesPluginException {
        try {
            OUT.println();
            String helmBaseOutputDir = this.dataHolder.getOutputDir();
            if (helmBaseOutputDir.endsWith("target" + File.separator + "kubernetes" + File.separator)) {
                helmBaseOutputDir = helmBaseOutputDir + File.separator + 
                        extractBalxName(this.dataHolder.getBalxFilePath());
            }
            helmBaseOutputDir = helmBaseOutputDir + File.separator + HELM_CHART;
            String helmTemplatesOutputDir = helmBaseOutputDir + File.separator + HELM_CHART_TEMPLATES;
            // Create the Helm templates directory
            new File(helmTemplatesOutputDir).mkdirs();
            // Create the helm template files using the generated Kubernetes artifacts
            this.copyKubernetesArtifactsToHelmTemplates(helmTemplatesOutputDir);
            // Create the Chart.yaml
            this.generateChartYAML();
            OUT.print("\t@kubernetes:Helm \t\t\t - complete 1/1");
        } catch (Exception e) {
            throw new KubernetesPluginException("Error in generating the Helm chart: " + e.getMessage(), e);
        }
    }
    
    private void copyKubernetesArtifactsToHelmTemplates(String helmTemplatesOutputDir) 
            throws KubernetesPluginException {
        File dir = new File(this.dataHolder.getOutputDir());
        File[] yamlFiles = dir.listFiles(new KuberenetesArtifactsFileFilter());
        if (yamlFiles == null) {
            throw new KubernetesPluginException("Kuberenetes artifacts not available to generate Helm templates");
        }
        for (File yamlFile : yamlFiles) {
            KubernetesUtils.copyFile(yamlFile.getAbsolutePath(), 
                    helmTemplatesOutputDir + File.separator + yamlFile.getName());
        }
    }

    private void generateChartYAML() {
        //TODO
    }
    
    /**
     * This class represents a {@link FilenameFilter} implementation to filter out Kubernetes artifacts.
     */
    private static class KuberenetesArtifactsFileFilter implements FilenameFilter {
        
        @Override
        public boolean accept(File dir, String name) {
            return name.toLowerCase(Locale.getDefault()).endsWith(KubernetesConstants.YAML);
        }
        
    }
    
}
