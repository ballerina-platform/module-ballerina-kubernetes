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
import ballerinax/kubernetes;


@kubernetes:Ingress {
    hostname: "pizza.com",
    path: "/pizzastore",
    targetPath: "/"
}
@kubernetes:Service {
    sessionAffinity: "ClientIP"
}
endpoint http:Listener pizzaEP {
    port: 9099
};

@kubernetes:Deployment {
    name: "invalid",
    image: "pizza-shop:latest",
    env: {
        "MY_NODE_NAME": {
            fieldRef: {
                name: "env-config",
                key: "log_level"
            }
        }
    }
}

@http:ServiceConfig {
    basePath: "/pizza"
}
service<http:Service> PizzaAPI bind pizzaEP {
    @http:ResourceConfig {
        methods: ["GET"],
        path: "/menu"
    }
    getPizzaMenu(endpoint outboundEP, http:Request req) {
        http:Response response = new;
        response.setTextPayload("Pizza menu \n");
        _ = outboundEP->respond(response);
    }
}