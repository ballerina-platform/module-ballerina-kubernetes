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

import ballerina/io;
import ballerinax/kubernetes;

@kubernetes:Deployment {
    name: "pizzashack",
    labels: {
        "task_type": "printer"
    },
    singleYAML: false
}
@kubernetes:ConfigMap {
    conf: "./artifacts/conf/ballerina.conf"
}
@kubernetes:HPA {}
@kubernetes:Secret {
    secrets: [
        {
            name: "private",
            mountPath: "/home/ballerina/private",
            data: ["./artifacts/secrets/MySecret1.txt"]
        }
    ]
}
@kubernetes:PersistentVolumeClaim {
    volumeClaims: [
        {
            name: "local-pv-2",
            mountPath: "/home/ballerina/tmp",
            readOnly: false,
            accessMode: "ReadWriteOnce",
            volumeClaimSize: "1Gi"
        }
    ]
}
public function main(string... args) {
    io:println("hello world");
}
