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

package org.ballerinax.kubernetes.models.istio;

import java.util.List;
import java.util.Map;

/**
 * Istio virtual service annotation's http route model class.
 *
 * @since 0.985.0
 */
public class IstioHttpRoute {
    private List<IstioDestinationWeight> route;
    private String timeout;
    private Map<String, String> appendHeaders;
    
    public List<IstioDestinationWeight> getRoute() {
        return route;
    }
    
    public void setRoute(List<IstioDestinationWeight> route) {
        this.route = route;
    }

    public String getTimeout() {
        return timeout;
    }
    
    public void setTimeout(String timeout) {
        this.timeout = timeout;
    }
    
    public Map<String, String> getAppendHeaders() {
        return appendHeaders;
    }
    
    public void setAppendHeaders(Map<String, String> appendHeaders) {
        this.appendHeaders = appendHeaders;
    }
}
