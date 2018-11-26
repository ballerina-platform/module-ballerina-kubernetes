import ballerina/config;
import ballerina/http;
import ballerinax/kubernetes;
import ballerina/io;

@kubernetes:Service {}
@kubernetes:Ingress {
    hostname: "abc.com"
}
listener http:Server helloWorldEP = new http:Server(9090, config = {
    secureSocket: {
        keyStore: {
            path: "${ballerina.home}/bre/security/ballerinaKeystore.p12",
            password: "ballerina"
        },
        trustStore: {
            path: "${ballerina.home}/bre/security/ballerinaTruststore.p12",
            password: "ballerina"
        }
    }
});

@kubernetes:Deployment {
    copyFiles: [
        {
            target: "/home/ballerina/data/data.txt",
            source: "./data/data.txt"
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
        string payload = readFile("./data/data.txt");
        response.setTextPayload("Data: " + untaint payload + "\n");
        _ = outboundEP->respond(response);
    }
}


function readFile(string filePath) returns (string) {
    io:ReadableByteChannel bchannel = io:openReadableFile(filePath);
    io:ReadableCharacterChannel cChannel = new io:ReadableCharacterChannel(bchannel, "UTF-8");

    var readOutput = cChannel.read(50);
    match readOutput {
        string text => {
            return text;
        }
        error ioError => return "Error: Unable to read file";
    }
}

