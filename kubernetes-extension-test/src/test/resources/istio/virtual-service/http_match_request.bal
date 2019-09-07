// Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import ballerina/kubernetes;
import ballerina/istio;

@istio:VirtualService {
    name: "ratings-route",
    hosts: [
        "ratings.prod.svc.cluster.local"
    ],
    http: [
        {
            'match: [
                {
                    headers: {
                        'end-user: {
                            exact: "jason"
                        }
                    },
                    uri: {
                        prefix: "/ratings/v2/"
                    }
                }
            ],
            route: [
                {
                    destination: {
                        host: "ratings.prod.svc.cluster.local"
                    }
                }
            ]
        }
    ]
}
@kubernetes:Deployment {
    image: "pizza-shop:latest",
    singleYAML: false
}
@kubernetes:Service {name: "hello"}
listener http:Listener helloEP = new(9090);

@http:ServiceConfig {
    basePath: "/helloWorld"
}
service helloWorld on helloEP {
    resource function sayHello(http:Caller outboundEP, http:Request request) {
        http:Response response = new;
        response.setTextPayload("Hello, World from service helloWorld ! \n");
        checkpanic outboundEP->respond(response);
    }
}
