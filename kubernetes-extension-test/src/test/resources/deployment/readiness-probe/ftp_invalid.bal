// Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import ballerina/log;
import wso2/ftp;
import ballerina/kubernetes;

listener ftp:Listener remoteServer = new({
    protocol: ftp:FTP,
    host: "<The FTP host>",
    secureSocket: {
        basicAuth: {
            username: "<The FTP username>",
            password: "<The FTP passowrd>"
        }
    },
    port: 9090,
    path: "<The remote FTP direcotry location>",
    pollingInterval: 5,
    fileNamePattern: "<File type>"
});

@kubernetes:Deployment {
    image: "pizza-shop:latest",
    singleYAML: false,
    readinessProbe: true
}
service ftpServerConnector on remoteServer {
    resource function onFileChange(ftp:WatchEvent fileEvent) {

        foreach ftp:FileInfo addedFile in fileEvent.addedFiles {
            log:printInfo("Added file path: " + addedFile.path);
        }
        foreach string deletedFile in fileEvent.deletedFiles {
            log:printInfo("Deleted file path: " + deletedFile);
        }
    }
}
