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
            filePath:"${ballerina.home}/bre/security/ballerinaKeystore.p12",
            password:"ballerina"
        },
        trustStore:{
            filePath:"${ballerina.home}/bre/security/ballerinaTruststore.p12",
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
        string payload = readFile("./data/data.txt", "r", "UTF-8");
        response.setStringPayload("Data: " + payload + "\n");
        _ = outboundEP -> respond(response);
    }
}


function readFile(string filePath, string permission, string encoding) returns (string) {
    io:ByteChannel channel = io:openFile(filePath, permission);
    var characterChannelResult = io:createCharacterChannel(channel, encoding);
    io:CharacterChannel sourceChannel = new;
    match characterChannelResult {
        (io:CharacterChannel) res => {
            sourceChannel = res;
        }
        error err => {
            io:println(err);
        }
    }
    var contentResult = sourceChannel.readCharacters(50);
    match contentResult {
        (string) res => {
            return res;
        }
        error err => {
            io:println(err);
            return err.message;
        }
    }
}

