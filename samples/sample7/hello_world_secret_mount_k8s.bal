import ballerina/http;
import ballerinax/kubernetes;
import ballerina/io;

@kubernetes:Service{}
@kubernetes:Ingress{
    hostname:"abc.com"
}
endpoint http:ServiceEndpoint helloWorldEP {
    port:9090,
    secureSocket: {
        keyStore: {
            filePath: "${ballerina.home}/bre/security/ballerinaKeystore.p12",
            password: "ballerina"
        },
        trustStore: {
            filePath: "${ballerina.home}/bre/security/ballerinaTruststore.p12",
            password: "ballerina"
        }
    }
};

@kubernetes:Secret{
    secrets:[
        {name:"private",mountPath:"/home/ballerina/private",
            data:["./secrets/MySecret1.txt"]
        },
        {name:"public",mountPath:"/home/ballerina/public",
            data:["./secrets/MySecret2.txt", "./secrets/MySecret3.txt"]
        }
    ]
}

@http:ServiceConfig {
    basePath:"/helloWorld"
}
service<http:Service> helloWorld bind helloWorldEP {
    @http:ResourceConfig {
        methods:["GET"],
        path:"/secret1"
    }
    getSecret1 (endpoint outboundEP, http:Request request) {
        http:Response response = new;
        string payload = readFile("./private/MySecret1.txt", "r", "UTF-8");
        response.setStringPayload("Secret1 resource: "+ payload +"\n");
        _ = outboundEP -> respond(response);
    }
     
    @http:ResourceConfig {
        methods:["GET"],
        path:"/secret2"
    }
    getSecret2 (endpoint outboundEP, http:Request request) {
        http:Response response = new;
        string payload = readFile("./public/MySecret2.txt", "r", "UTF-8");
        response.setStringPayload("Secret2 resource: "+ payload +"\n");
        _ = outboundEP -> respond(response);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/secret3"
    }
    getSecret3 (endpoint outboundEP, http:Request request) {
        http:Response response = new;
        string payload = readFile("./public/MySecret3.txt", "r", "UTF-8");
        response.setStringPayload("Secret3 resource: "+ payload +"\n");
        _ = outboundEP -> respond(response);
    }
}

function readFile (string filePath, string permission, string encoding) returns (string) {
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
    var contentResult = sourceChannel.readCharacters(12);
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
