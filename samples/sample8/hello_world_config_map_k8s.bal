import ballerina/net.http;
import ballerinax/kubernetes;
import ballerina/io;

@kubernetes:Service{}
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

@kubernetes:ConfigMap{
    configMaps:[
		{name:"ballerina-config", mountPath:"/home/ballerina/conf",
			data:["./conf/ballerina.conf"]
		}
	]
}
@kubernetes:Ingress{
	hostname:"abc.com"
}
@http:ServiceConfig {
    basePath:"/helloWorld"
}
service<http:Service> helloWorld bind helloWorldEP {
	@http:ResourceConfig {
        methods:["GET"],
        path:"/conf"
    }
    getConf (endpoint outboundEP, http:Request request) {
        http:Response response = {};
		string payload = readFile("./conf/ballerina.conf", "r", "UTF-8");
        response.setStringPayload("conf resource: "+ payload +"\n");
        _ = outboundEP -> respond(response);
    }
}


function readFile (string filePath, string permission, string encoding) returns (string) {
    io:ByteChannel channel = io:openFile(filePath, permission);
    var characterChannelResult = io:createCharacterChannel(channel, encoding);
    io:CharacterChannel sourceChannel={};
    match characterChannelResult {  
        (io:CharacterChannel) res => {
            sourceChannel = res;
        }
        error err => {
            io:println(err);
        }
    }
    var contentResult = sourceChannel.readCharacters(20);
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