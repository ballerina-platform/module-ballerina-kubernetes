import ballerina/config;
import ballerina/http;
import ballerina/io;
import ballerina/knative;
import ballerina/log;

@knative:Service {}
listener http:Listener helloWorldEP = new(8080, {
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

@knative:ConfigMap {
    conf: "./conf/ballerina.conf",
    configMaps:[
        {
            mountPath: "/home/ballerina/data",
            data: ["./conf/data.txt"]
        }
    ]
}
@http:ServiceConfig {
    basePath: "/helloWorld"
}
service helloWorld on helloWorldEP {
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
    @http:ResourceConfig {
        methods: ["GET"],
        path: "/data"
    }
    resource function getData(http:Caller outboundEP, http:Request request) {
        http:Response response = new;
        string payload = <@untainted> readFile("./data/data.txt");
        response.setTextPayload("Data: " + <@untainted> payload + "\n");
        var responseResult = outboundEP->respond(response);
        if (responseResult is error) {
            log:printError("error responding back to client.", responseResult);
        }
    }
}

function getConfigValue(string instanceId, string property) returns string {
    string key = <@untainted> instanceId + "." + <@untainted> property;
    return config:getAsString(key, "Invalid User");
}

function readFile(string filePath) returns @tainted string {
    io:ReadableByteChannel bChannel = checkpanic io:openReadableFile(filePath);
    io:ReadableCharacterChannel cChannel = new io:ReadableCharacterChannel(bChannel, "UTF-8");

    var readOutput = cChannel.read(50);
    if (readOutput is string) {
        return readOutput;
    } else {
        return "Error: Unable to read file";
    }
}
