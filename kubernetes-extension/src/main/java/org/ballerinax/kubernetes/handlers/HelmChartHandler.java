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

import org.ballerinax.kubernetes.exceptions.KubernetesPluginException;
import org.ballerinax.kubernetes.models.DeploymentModel;
import org.ballerinax.kubernetes.utils.KubernetesUtils;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.DumperOptions.FlowStyle;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import static org.ballerinax.kubernetes.KubernetesConstants.HELM_API_VERSION;
import static org.ballerinax.kubernetes.KubernetesConstants.HELM_API_VERSION_DEFAULT;
import static org.ballerinax.kubernetes.KubernetesConstants.HELM_APP_VERSION;
import static org.ballerinax.kubernetes.KubernetesConstants.HELM_APP_VERSION_DEFAULT;
import static org.ballerinax.kubernetes.KubernetesConstants.HELM_CHART_TEMPLATES;
import static org.ballerinax.kubernetes.KubernetesConstants.HELM_CHART_YAML_FILE_NAME;
import static org.ballerinax.kubernetes.KubernetesConstants.HELM_DESCRIPTION;
import static org.ballerinax.kubernetes.KubernetesConstants.HELM_NAME;
import static org.ballerinax.kubernetes.KubernetesConstants.HELM_VERSION;
import static org.ballerinax.kubernetes.KubernetesConstants.HELM_VERSION_DEFAULT;
import static org.ballerinax.kubernetes.KubernetesConstants.YAML;
import static org.ballerinax.kubernetes.utils.KubernetesUtils.extractBalxName;

/**
 * Generates the Helm chart from annotations.
 */
public class HelmChartHandler extends AbstractArtifactHandler {

    @Override
    public void createArtifacts() throws KubernetesPluginException {
        DeploymentModel model = this.dataHolder.getDeploymentModel();
        OUT.println();
        String helmBaseOutputDir = this.dataHolder.getOutputDir();
        if (helmBaseOutputDir.endsWith("target" + File.separator + "kubernetes" + File.separator)) {
            helmBaseOutputDir = helmBaseOutputDir + File.separator + 
                    extractBalxName(this.dataHolder.getBalxFilePath());
        }
        helmBaseOutputDir = helmBaseOutputDir + File.separator + model.getName();
        String helmTemplatesOutputDir = helmBaseOutputDir + File.separator + HELM_CHART_TEMPLATES;
        // Create the Helm templates directory
        new File(helmTemplatesOutputDir).mkdirs();
        // Create the helm template files using the generated Kubernetes artifacts
        this.copyKubernetesArtifactsToHelmTemplates(helmTemplatesOutputDir);
        // Create the Chart.yaml
        this.generateChartYAML(helmBaseOutputDir);
        OUT.print("\t@kubernetes:Helm \t\t\t - complete 1/1");
    }
    
    private void copyKubernetesArtifactsToHelmTemplates(String helmTemplatesOutputDir) 
            throws KubernetesPluginException {
        File dir = new File(this.dataHolder.getOutputDir());
        File[] yamlFiles = dir.listFiles(new KubernetesArtifactsFileFilter());
        if (yamlFiles == null) {
            throw new KubernetesPluginException("Kuberenetes artifacts not available to generate Helm templates");
        }
        for (File yamlFile : yamlFiles) {
            KubernetesUtils.copyFile(yamlFile.getAbsolutePath(), 
                    helmTemplatesOutputDir + File.separator + yamlFile.getName());
        }
    }

    private void generateChartYAML(String helmBaseOutputDir) throws KubernetesPluginException {
        DeploymentModel model = this.dataHolder.getDeploymentModel();
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(FlowStyle.BLOCK);
        Yaml yaml = new Yaml(options);
        Map<String, String> values = new LinkedHashMap<>();
        values.put(HELM_API_VERSION, HELM_API_VERSION_DEFAULT);
        values.put(HELM_APP_VERSION, HELM_APP_VERSION_DEFAULT);
        values.put(HELM_DESCRIPTION, "Helm chart for " + model.getName());
        values.put(HELM_NAME, model.getName());
        values.put(HELM_VERSION, model.getVersion() == null ? HELM_VERSION_DEFAULT : model.getVersion());
        try (FileWriter writer = new FileWriter(helmBaseOutputDir + File.separator + HELM_CHART_YAML_FILE_NAME)) {
            yaml.dump(values, writer);
        } catch (IOException e) {
            throw new KubernetesPluginException("Error in generating the Helm chart: " + e.getMessage(), e);
        }
    }
    
    /**
     * This class represents a {@link FilenameFilter} implementation to filter out Kubernetes artifacts.
     */
    private static class KubernetesArtifactsFileFilter implements FilenameFilter {
        
        @Override
        public boolean accept(File dir, String name) {
            return name.toLowerCase(Locale.getDefault()).endsWith(YAML);
        }
        
    }
    
}
