/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 *
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

import org.ballerinalang.model.elements.PackageID;
import org.ballerinax.kubernetes.exceptions.KubernetesPluginException;
import org.ballerinax.kubernetes.models.DeploymentModel;
import org.ballerinax.kubernetes.models.KubernetesContext;
import org.ballerinax.kubernetes.models.KubernetesDataHolder;
import org.ballerinax.kubernetes.utils.KubernetesUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeSuite;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.ballerinax.kubernetes.KubernetesConstants.DOCKER;
import static org.ballerinax.kubernetes.KubernetesConstants.KUBERNETES;

/**
 * Test suite handler for all unit tests.
 */
public class HandlerTestSuite {
    static PackageID module = new PackageID(Names.ANON_ORG, new Name("my_pkg"), Names.DEFAULT_VERSION);
    protected static KubernetesDataHolder dataHolder;
    
    @BeforeSuite
    public void setUp() {
        KubernetesContext context = KubernetesContext.getInstance();
        context.addDataHolder(module, Paths.get("build"));
        dataHolder = context.getDataHolder();
        Path buildDir = Paths.get(System.getProperty("buildDir"));
        dataHolder.setK8sArtifactOutputPath(buildDir.resolve(KUBERNETES).resolve(module.name.toString()));
        dataHolder.setDockerArtifactOutputPath(buildDir.resolve(DOCKER).resolve(module.name.toString()));
        Path resourcesDirectory = Paths.get("src").resolve("test").resolve("resources");
        DeploymentModel deploymentModel = new DeploymentModel();
        deploymentModel.setSingleYAML(false);
        dataHolder.setDeploymentModel(deploymentModel);
        dataHolder.setUberJarPath(resourcesDirectory.toAbsolutePath().resolve("hello.jar"));
    }
    
    @AfterClass
    public void clearArtifacts() throws KubernetesPluginException {
        KubernetesUtils.deleteDirectory(dataHolder.getK8sArtifactOutputPath());
        KubernetesUtils.deleteDirectory(dataHolder.getDockerArtifactOutputPath());
    }
}
