import ballerina/config;
import ballerina/http;
import ballerinax/kubernetes;
import ballerina/io;

@kubernetes:Service {}
@kubernetes:Ingress {
    hostname:"abc.com"
}
endpoint http:Listener helloWorldEP {
    port:9090,
    secureSocket:{
        keyStore:{
            path:"${ballerina.home}/bre/security/ballerinaKeystore.p12",
            password:"ballerina"
        },
        trustStore:{
            path:"${ballerina.home}/bre/security/ballerinaTruststore.p12",
            password:"ballerina"
        }
    }
};

@kubernetes:Deployment {
    copyFiles:[
        {
            target:"/home/ballerina/data/data.txt",
            source:"./data/data.txt"
        }
    ]
}
@http:ServiceConfig {
    basePath:"/helloWorld"
}
service<http:Service> helloWorld bind helloWorldEP {
    @http:ResourceConfig {
        methods:["GET"],
        path:"/data"
    }
    getData(endpoint outboundEP, http:Request request) {
        http:Response response = new;
        string payload = readFile("./data/data.txt");
        response.setStringPayload("Data: " + payload + "\n");
        _ = outboundEP->respond(response);
    }
}


function readFile(string filePath) returns (string) {
    io:Mode permission = "r";
    io:ByteChannel bchannel = io:openFile(filePath, permission);
    io:CharacterChannel channel = new io:CharacterChannel(bchannel, "UTF-8");

    var readOutput = channel.read(50);
    match readOutput {
        string text => {
            return text;
        }
        error ioError => return "Error: Unable to read file";
    }
}

