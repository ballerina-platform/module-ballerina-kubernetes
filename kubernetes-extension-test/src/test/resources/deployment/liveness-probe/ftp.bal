// Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

import ballerina/http;
import ballerina/'lang\.object as lang;
import ballerina/kubernetes;

@kubernetes:Service {
}
listener MockListener remoteServer = new({
    protocol: "FTP",
    host: "<The FTP host>",
    port: 9090
});

public class MockListener {
    *lang:Listener;
    private ListenerConfig config = {};

    public function init(ListenerConfig listenerConfig) {
        self.config = listenerConfig;
    }

    public function __attach(service s, string? name = ()) returns error? {
    }

    public function __detach(service s) returns error? {
    }

    public function __start() returns error? {
    }

    public function __gracefulStop() returns error? {
        return ();
    }

    public function __immediateStop() returns error? {
        return ();
    }
}

public type ListenerConfig record {|
    string protocol = "FTP";
    string host = "127.0.0.1";
    int port = 21;
|};

@kubernetes:Deployment {
    image: "pizza-shop:latest",
    singleYAML: false,
    livenessProbe: true
}
service helloWorld on remoteServer {
    resource function sayHello(http:Caller outboundEP, http:Request request) {
        http:Response response = new;
        response.setTextPayload("Hello, World from service helloWorld ! \n");
        checkpanic outboundEP->respond(response);
    }
}
