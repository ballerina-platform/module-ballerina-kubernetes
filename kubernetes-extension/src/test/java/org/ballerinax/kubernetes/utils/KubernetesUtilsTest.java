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

package org.ballerinax.kubernetes.utils;

import org.apache.commons.io.FileUtils;
import org.ballerinax.docker.generator.utils.DockerGenUtils;
import org.ballerinax.kubernetes.exceptions.KubernetesPluginException;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Kubernetes Utils Test Class.
 */
public class KubernetesUtilsTest {
    
    private Path tempDirectory;
    
    @BeforeClass
    public void setUp() throws IOException {
        tempDirectory = Files.createTempDirectory("ballerinax-docker-plugin-");
    }

    @Test
    public void extractBalxNameTest() {
        Path balxFilePath =
                Paths.get("/Users/anuruddha/workspace/ballerinax/docker/samples/sample5/hello_config_file.balx");
        String baxlFileName = "hello_config_file";
        Assert.assertEquals(DockerGenUtils.extractJarName(balxFilePath), baxlFileName);
        balxFilePath = Paths.get("/Users/anuruddha/workspace/ballerinax/docker/samples/sample5/");
        Assert.assertNull(DockerGenUtils.extractJarName((balxFilePath)));
    }

    @Test
    public void isBlankTest() {
        Assert.assertTrue(KubernetesUtils.isBlank(""));
        Assert.assertTrue(KubernetesUtils.isBlank(" "));
        Assert.assertTrue(KubernetesUtils.isBlank(null));
        Assert.assertFalse(KubernetesUtils.isBlank("value"));
    }

    @Test
    public void resolveValueTest() throws Exception {
        Map<String, String> env = new HashMap<>();
        env.put("DOCKER_USERNAME", "anuruddhal");
        env.put("DOCKER_PASSWORD", "");
        setEnv(env);
        try {
            Assert.assertEquals(KubernetesUtils.resolveValue("$env{DOCKER_USERNAME}"), "anuruddhal");
        } catch (KubernetesPluginException e) {
            Assert.fail("Unable to resolve environment variable");
        }
        try {
            Assert.assertEquals(KubernetesUtils.resolveValue("$env{DOCKER_PASSWORD}"), "");
            Assert.assertEquals(KubernetesUtils.resolveValue("$env{DOCKER_UNDEFINED}"), "");
        } catch (KubernetesPluginException e) {
            Assert.fail("Unable to resolve null/blank environment variable");
        }
        Assert.assertEquals(KubernetesUtils.resolveValue("demo"), "demo");
    }

    @Test
    public void deleteDirectoryTest() throws IOException, KubernetesPluginException {
        File file = tempDirectory.resolve("myfile.txt").toFile();
        Assert.assertTrue(file.createNewFile());
        File directory = tempDirectory.resolve("subFolder").toFile();
        Assert.assertTrue(directory.mkdirs());
        KubernetesUtils.deleteDirectory(file.toPath());
        KubernetesUtils.deleteDirectory(directory.toPath());
    }

    @Test
    public void getValidNameTest() {
        String testString = "HELLO_WORLD.DEMO";
        Assert.assertEquals("hello-world-demo", KubernetesUtils.getValidName(testString));
    }
    
    @Test
    public void copyFileTest() throws IOException, KubernetesPluginException {
        File testFile = tempDirectory.resolve("copy.txt").toFile();
        Assert.assertTrue(testFile.createNewFile());
        Path tempDirectory = Files.createTempDirectory("copy-test-");
        Path destinationFile = tempDirectory.resolve("copy.txt");
        KubernetesUtils.copyFileOrDirectory(testFile.getAbsolutePath(), destinationFile.toString());
        
        // assert
        Assert.assertTrue(Files.exists(destinationFile));
        
        // clean up
        FileUtils.deleteQuietly(testFile);
        FileUtils.deleteQuietly(tempDirectory.toFile());
    }
    
    @Test
    public void copyDirectoryTest() throws IOException, KubernetesPluginException {
        File testFolder = tempDirectory.resolve("copyDir").toFile();
        Assert.assertTrue(testFolder.mkdirs());
        Path copy1File = testFolder.toPath().resolve("copy1.txt");
        Path copy2File = testFolder.toPath().resolve("copy2.txt");
        Files.createFile(copy1File);
        Files.createFile(copy2File);
        
        Path destinationDir = Files.createTempDirectory("copy-test-");
        KubernetesUtils.copyFileOrDirectory(testFolder.getAbsolutePath(), destinationDir.toString());
        
        // assert
        Assert.assertTrue(Files.exists(destinationDir));
        Assert.assertTrue(Files.exists(destinationDir.resolve("copy1.txt")));
        Assert.assertTrue(Files.exists(destinationDir.resolve("copy2.txt")));
        
        // clean up
        FileUtils.deleteQuietly(testFolder);
        FileUtils.deleteQuietly(destinationDir.toFile());
    }
    
    @Test
    public void copyFileToDirectoryTest() throws IOException, KubernetesPluginException {
        File testFile = tempDirectory.resolve("copy.txt").toFile();
        Assert.assertTrue(testFile.createNewFile());
        Path destinationDir = Files.createTempDirectory("copy-test-");
        KubernetesUtils.copyFileOrDirectory(testFile.getAbsolutePath(), destinationDir.toString());
    
        // assert
        Assert.assertTrue(Files.exists(destinationDir.resolve("copy.txt")));
    
        // clean up
        FileUtils.deleteQuietly(testFile);
        FileUtils.deleteQuietly(destinationDir.toFile());
    }
    
    private void setEnv(Map<String, String> newenv) throws Exception {
        try {
            Class<?> processEnvironmentClass = Class.forName("java.lang.ProcessEnvironment");
            Field theEnvironmentField = processEnvironmentClass.getDeclaredField("theEnvironment");
            theEnvironmentField.setAccessible(true);
            Map<String, String> env = (Map<String, String>) theEnvironmentField.get(null);
            env.putAll(newenv);
            Field theCaseInsensitiveEnvironmentField = processEnvironmentClass.getDeclaredField
                    ("theCaseInsensitiveEnvironment");
            theCaseInsensitiveEnvironmentField.setAccessible(true);
            Map<String, String> cienv = (Map<String, String>) theCaseInsensitiveEnvironmentField.get(null);
            cienv.putAll(newenv);
        } catch (NoSuchFieldException e) {
            Class[] classes = Collections.class.getDeclaredClasses();
            Map<String, String> env = System.getenv();
            for (Class cl : classes) {
                if ("java.util.Collections$UnmodifiableMap".equals(cl.getName())) {
                    Field field = cl.getDeclaredField("m");
                    field.setAccessible(true);
                    Object obj = field.get(env);
                    Map<String, String> map = (Map<String, String>) obj;
                    map.clear();
                    map.putAll(newenv);
                }
            }
        }
    }
    
    @AfterClass
    public void cleanUp() {
        FileUtils.deleteQuietly(tempDirectory.toFile());
    }
}
