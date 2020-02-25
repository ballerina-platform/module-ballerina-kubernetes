import ballerina/config;
import ballerina/http;
import ballerina/io;
import ballerina/kubernetes;
import ballerina/log;

@kubernetes:Service {}
@kubernetes:Ingress {
    hostname: "abc.com"
}
listener http:Listener helloWorldEP = new (9090, {
    secureSocket: {
        keyStore: {
            path: "./security/ballerinaKeystore.p12",
            password: "ballerina"
        },
        trustStore: {
            path: "./security/ballerinaTruststore.p12",
            password: "ballerina"
        }
    }
});

@kubernetes:Secret {
    conf: "./conf/ballerina.conf",
    secrets: [
        {
            name: "private",
            mountPath: "/home/ballerina/private",
            data: ["./secrets/MySecret1.txt"]
        },
        {
            name: "public",
            mountPath: "/home/ballerina/public",
            data: ["./secrets/MySecret2.txt", "./secrets/MySecret3.txt"]
        }
    ]
}

@kubernetes:Deployment {}
@http:ServiceConfig {
    basePath: "/helloWorld"
}
service helloWorld on helloWorldEP {
    @http:ResourceConfig {
        methods: ["GET"],
        path: "/secret1"
    }
    resource function getSecret1(http:Caller outboundEP, http:Request request) {
        http:Response response = new;
        string payload = <@untainted>readFile("./private/MySecret1.txt");
        response.setTextPayload("Secret1 resource: " + <@untainted>payload + "\n");
        var responseResult = outboundEP->respond(response);
        if (responseResult is error) {
            log:printError("error responding back to client.", err = responseResult);
        }
    }

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/secret2"
    }
    resource function getSecret2(http:Caller outboundEP, http:Request request) {
        http:Response response = new;
        string payload = <@untainted>readFile("./public/MySecret2.txt");
        response.setTextPayload("Secret2 resource: " + <@untainted>payload + "\n");
        var responseResult = outboundEP->respond(response);
        if (responseResult is error) {
            log:printError("error responding back to client.", err = responseResult);
        }
    }

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/secret3"
    }
    resource function getSecret3(http:Caller outboundEP, http:Request request) {
        http:Response response = new;
        string payload = <@untainted>readFile("./public/MySecret3.txt");
        response.setTextPayload("Secret3 resource: " + <@untainted>payload + "\n");
        var responseResult = outboundEP->respond(response);
        if (responseResult is error) {
            log:printError("error responding back to client.", err = responseResult);
        }
    }

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/config/{user}"
    }
    resource function getConfig(http:Caller outboundEP, http:Request request, string user) {
        http:Response response = new;
        string userId = getConfigValue(user, "userid");
        string groups = getConfigValue(user, "groups");
        string payload = "{userId: " + userId + ", groups: " + groups + "}";
        response.setTextPayload(payload + "\n");
        var responseResult = outboundEP->respond(response);
        if (responseResult is error) {
            log:printError("error responding back to client.", responseResult);
        }
    }
}

function readFile(string filePath) returns @tainted string {
    io:ReadableByteChannel bchannel = checkpanic io:openReadableFile(filePath);
    io:ReadableCharacterChannel cChannel = new io:ReadableCharacterChannel(bchannel, "UTF-8");

    var readOutput = cChannel.read(50);
    if (readOutput is string) {
        return readOutput;
    } else {
        return "Error: Unable to read file";
    }
}

function getConfigValue(string instanceId, string property) returns (string) {
    string key = <@untainted>instanceId + "." + <@untainted>property;
    return config:getAsString(key, "Invalid User");
}
