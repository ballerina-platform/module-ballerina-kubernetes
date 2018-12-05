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
    private List<Object> match;
    private List<IstioDestinationWeight> route;
    private IstioHttpRedirect redirect;
    private Object rewrite;
    private String timeout;
    private Object retries;
    private Object fault;
    private Object mirror;
    private Object corsPolicy;
    private Map<String, String> appendHeaders;
    
    public List<Object> getMatch() {
        return match;
    }
    
    public void setMatch(List<Object> match) {
        this.match = match;
    }
    
    public List<IstioDestinationWeight> getRoute() {
        return route;
    }
    
    public void setRoute(List<IstioDestinationWeight> route) {
        this.route = route;
    }
    
    public IstioHttpRedirect getRedirect() {
        return redirect;
    }
    
    public void setRedirect(IstioHttpRedirect redirect) {
        this.redirect = redirect;
    }
    
    public Object getRewrite() {
        return rewrite;
    }
    
    public void setRewrite(Object rewrite) {
        this.rewrite = rewrite;
    }
    
    public String getTimeout() {
        return timeout;
    }
    
    public void setTimeout(String timeout) {
        this.timeout = timeout;
    }
    
    public Object getRetries() {
        return retries;
    }
    
    public void setRetries(Object retries) {
        this.retries = retries;
    }
    
    public Object getFault() {
        return fault;
    }
    
    public void setFault(Object fault) {
        this.fault = fault;
    }
    
    public Object getMirror() {
        return mirror;
    }
    
    public void setMirror(Object mirror) {
        this.mirror = mirror;
    }
    
    public Object getCorsPolicy() {
        return corsPolicy;
    }
    
    public void setCorsPolicy(Object corsPolicy) {
        this.corsPolicy = corsPolicy;
    }
    
    public Map<String, String> getAppendHeaders() {
        return appendHeaders;
    }
    
    public void setAppendHeaders(Map<String, String> appendHeaders) {
        this.appendHeaders = appendHeaders;
    }
}
