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

package artifactgen.org.ballerinax.kubernetes.handlers;

import org.ballerinax.kubernetes.exceptions.KubernetesPluginException;
import org.ballerinax.kubernetes.handlers.SecretHandler;
import org.ballerinax.kubernetes.models.SecretModel;
import org.ballerinax.kubernetes.utils.KubernetesUtils;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * Generates kubernetes Secret from annotations.
 */
public class KubernetesSecretGeneratorTests {

    private final Logger log = LoggerFactory.getLogger(KubernetesSecretGeneratorTests.class);

    @Test
    public void testSecretGenerate() {
        SecretModel secretModel = new SecretModel();
        secretModel.setName("MySecret");
        secretModel.setReadOnly(true);
        secretModel.setMountPath("/user/dir");
        SecretHandler secretHandler = new SecretHandler(secretModel);
        try {
            String secretYaml = secretHandler.generate();
            Assert.assertNotNull(secretYaml);
            File artifactLocation = new File("target/kubernetes");
            artifactLocation.mkdir();
            File tempFile = File.createTempFile("temp", secretModel.getName() + ".yaml", artifactLocation);
            KubernetesUtils.writeToFile(secretYaml, tempFile.getPath());
            log.info("Generated YAML: \n" + secretYaml);
            Assert.assertTrue(tempFile.exists());
            //tempFile.deleteOnExit();
        } catch (IOException e) {
            Assert.fail("Unable to write to file");
        } catch (KubernetesPluginException e) {
            Assert.fail("Unable to generate yaml from service");
        }
    }
}
