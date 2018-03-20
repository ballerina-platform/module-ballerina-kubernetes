import ballerina.net.http;
import ballerinax.kubernetes;
import ballerina.io;

@kubernetes:svc{}
endpoint http:ServiceEndpoint helloWorldEP {
    port:9090,
	ssl:{
		keyStoreFile:"${ballerina.home}/bre/security/ballerinaKeystore.p12",
		keyStorePassword:"ballerina",
		certPassword:"ballerina"
	}
};

@kubernetes:secret{
	secrets:[
		{name:"private",mountPath:"/home/ballerina/private",readOnly:false,
			data:[
				{key:"private.txt",filePath:"./secrets/MySecret1.txt"}
			]
		},
		{name:"public",mountPath:"/home/ballerina/public",readOnly:false,
			data:[
				{key:"public1.txt",filePath:"./secrets/MySecret2.txt"},
				{key:"public2.txt",filePath:"./secrets/MySecret3.txt"}
			]
		}
	]
}
@kubernetes:ingress{
	hostname:"abc.com"
}
@http:serviceConfig {
    basePath:"/helloWorld"
}
service<http:Service> helloWorld bind helloWorldEP {
	@http:resourceConfig {
        methods:["GET"],
        path:"/secret1"
    }
    getSecret1 (endpoint outboundEP, http:Request request) {
        http:Response response = {};
		io:CharacterChannel sourceChannel = getFileCharacterChannel("./private/private.txt", "r", "UTF-8");
		string payload = process(sourceChannel);
        response.setStringPayload("Secret1 resource: "+ payload +"\n");
        _ = outboundEP -> respond(response);
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/secret2"
    }
    getSecret2 (endpoint outboundEP, http:Request request) {
        http:Response response = {};
		io:CharacterChannel sourceChannel = getFileCharacterChannel("./public/public1.txt", "r", "UTF-8");
		string payload = process(sourceChannel);
        response.setStringPayload("Secret2 resource: "+ payload +"\n");
        _ = outboundEP -> respond(response);
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/secret3"
    }
    getSecret3 (endpoint outboundEP, http:Request request) {
        http:Response response = {};
		io:CharacterChannel sourceChannel = getFileCharacterChannel("./public/public2.txt", "r", "UTF-8");
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