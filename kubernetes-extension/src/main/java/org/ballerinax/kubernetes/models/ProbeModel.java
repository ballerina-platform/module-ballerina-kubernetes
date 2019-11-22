/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinax.kubernetes.models;

/**
 * Probe configuration for kubernetes.
 */
public class ProbeModel {
    private int port;
    private int initialDelaySeconds = -1;
    private int periodSeconds = -1;
    
    public int getPort() {
        return port;
    }
    
    public void setPort(int port) {
        this.port = port;
    }
    
    public int getInitialDelaySeconds() {
        return initialDelaySeconds;
    }
    
    public void setInitialDelaySeconds(int initialDelaySeconds) {
        this.initialDelaySeconds = initialDelaySeconds;
    }
    
    public int getPeriodSeconds() {
        return periodSeconds;
    }
    
    public void setPeriodSeconds(int periodSeconds) {
        this.periodSeconds = periodSeconds;
    }
}
