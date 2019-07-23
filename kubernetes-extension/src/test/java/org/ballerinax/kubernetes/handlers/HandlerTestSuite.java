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
import org.ballerinax.kubernetes.models.DeploymentModel;
import org.ballerinax.kubernetes.models.KubernetesContext;
import org.ballerinax.kubernetes.models.KubernetesDataHolder;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.ballerinax.kubernetes.KubernetesConstants.DOCKER;
import static org.ballerinax.kubernetes.KubernetesConstants.KUBERNETES;

public class HandlerTestSuite {
    static PackageID module = new PackageID(Names.ANON_ORG, new Name("my_pkg"), Names.DEFAULT_VERSION);

    @BeforeSuite
    public static void setUp() {
        KubernetesContext context = KubernetesContext.getInstance();
        context.addDataHolder(module, Paths.get("target"));
        KubernetesDataHolder dataHolder = context.getDataHolder();
        dataHolder.setK8sArtifactOutputPath(Paths.get("target").resolve(KUBERNETES).resolve(module.name.toString()));
        dataHolder.setDockerArtifactOutputPath(Paths.get("target").resolve(DOCKER).resolve(module.name.toString()));
        Path resourcesDirectory = Paths.get("src").resolve("test").resolve("resources");
        DeploymentModel deploymentModel = new DeploymentModel();
        deploymentModel.setSingleYAML(false);
        dataHolder.setDeploymentModel(deploymentModel);
        dataHolder.setUberJarPath(resourcesDirectory.toAbsolutePath().resolve("hello-executable.jar"));
    }

    @AfterSuite
    public static void tearDown() {
    }

}
