import ballerina/http;
import ballerinax/kubernetes;
import ballerina/io;

@kubernetes:Service {}
@kubernetes:Ingress {
    hostname: "abc.com"
}
listener http:Listener helloWorldEP = new(9090, config = {
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

@kubernetes:Secret {
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

@kubernetes:Deployment {
    singleYAML: false
}
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
        string payload = readFile("./private/MySecret1.txt");
        response.setTextPayload("Secret1 resource: " + untaint payload + "\n");
        _ = outboundEP->respond(response);
    }

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/secret2"
    }
    resource function getSecret2(http:Caller outboundEP, http:Request request) {
        http:Response response = new;
        string payload = readFile("./public/MySecret2.txt");
        response.setTextPayload("Secret2 resource: " + untaint payload + "\n");
        _ = outboundEP->respond(response);
    }

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/secret3"
    }
    resource function getSecret3(http:Caller outboundEP, http:Request request) {
        http:Response response = new;
        string payload = readFile("./public/MySecret3.txt");
        response.setTextPayload("Secret3 resource: " + untaint payload + "\n");
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

