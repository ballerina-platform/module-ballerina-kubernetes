import ballerina/http;
import ballerina/io;
import ballerina/kubernetes;
import ballerina/log;

@kubernetes:Service {}
@kubernetes:Ingress {
    hostname: "abc.com"
}

listener http:Listener helloWorldEP = new (9090);

@kubernetes:Deployment {
    singleYAML: false
}
@kubernetes:Secret {
    secrets: [
            {
                mountPath: "/home/ballerina/data",
                data: ["./conf/data.txt"],
                defaultMode: 755
            }
        ]
}
@http:ServiceConfig {
    basePath: "/helloWorld"
}
service helloWorld on helloWorldEP {
    @http:ResourceConfig {
        methods: ["GET"],
        path: "/data"
    }
    resource function getData(http:Caller outboundEP, http:Request request) {
        http:Response response = new;
        string payload = <@untainted>readFile("./data/data.txt");
        response.setTextPayload("Data: " + <@untainted>payload + "\n");
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
