import ballerina.net.http;
import ballerinax.kubernetes;
import ballerina.io;

@kubernetes:SVC{}
endpoint http:ServiceEndpoint helloWorldEP {
    port:9090,
	ssl:{
		keyStoreFile:"${ballerina.home}/bre/security/ballerinaKeystore.p12",
		keyStorePassword:"ballerina",
		certPassword:"ballerina"
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
@kubernetes:Ingress{
	hostname:"abc.com"
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
        http:Response response = {};
		io:CharacterChannel sourceChannel = getFileCharacterChannel("./private/MySecret1.txt", "r", "UTF-8");
		string payload = process(sourceChannel);
        response.setStringPayload("Secret1 resource: "+ payload +"\n");
        _ = outboundEP -> respond(response);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/secret2"
    }
    getSecret2 (endpoint outboundEP, http:Request request) {
        http:Response response = {};
		io:CharacterChannel sourceChannel = getFileCharacterChannel("./public/MySecret2.txt", "r", "UTF-8");
		string payload = process(sourceChannel);
        response.setStringPayload("Secret2 resource: "+ payload +"\n");
        _ = outboundEP -> respond(response);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/secret3"
    }
    getSecret3 (endpoint outboundEP, http:Request request) {
        http:Response response = {};
		io:CharacterChannel sourceChannel = getFileCharacterChannel("./public/MySecret3.txt", "r", "UTF-8");
		string payload = process(sourceChannel);
        response.setStringPayload("Secret3 resource: "+ payload+ "\n");
        _ = outboundEP -> respond(response);
    }
}

function getFileCharacterChannel (string filePath, string permission, string encoding)(io:CharacterChannel) {
	io:ByteChannel channel = io:openFile(filePath, permission);
	var characterChannel,err = io:createCharacterChannel(channel, encoding);
	return characterChannel;
}

function process (io:CharacterChannel sourceChannel)(string ) {
	var content,err = sourceChannel.readCharacters(12);
    return content;
}