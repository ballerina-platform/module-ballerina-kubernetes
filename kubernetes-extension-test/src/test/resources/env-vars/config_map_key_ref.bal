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
listener http:Listener pizzaEP = new(9099);

@kubernetes:Deployment {
    name: "config-map-key-ref",
    image: "pizza-shop:latest",
    env: {
        "SPECIAL_LEVEL_KEY": {
            configMapKeyRef: {
                key: "special.how",
                name: "special-config"
            }
        },
        "LOG_LEVEL": {
            configMapKeyRef: {
                name: "env-config",
                key: "log_level"
            }
        }
    },
    singleYAML: false
}

@http:ServiceConfig {
    basePath: "/pizza"
}
service PizzaAPI on pizzaEP {
    @http:ResourceConfig {
        methods: ["GET"],
        path: "/menu"
    }
    resource function getPizzaMenu(http:Caller outboundEP, http:Request req) {
        http:Response response = new;
        response.setTextPayload("Pizza menu \n");
        checkpanic outboundEP->respond(response);
    }
}
