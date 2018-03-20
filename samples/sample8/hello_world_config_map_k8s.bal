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

@kubernetes:configMap{
    configMaps:[
		{name:"ballerina-config",mountPath:"/home/ballerina/conf",
			data:["./conf/ballerina.conf"]
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
        path:"/conf"
    }
    getSecret1 (endpoint outboundEP, http:Request request) {
        http:Response response = {};
		io:CharacterChannel sourceChannel = getFileCharacterChannel("./conf/ballerina.conf", "r", "UTF-8");
		string payload = process(sourceChannel);
        response.setStringPayload("conf resource: "+ payload +"\n");
        _ = outboundEP -> respond(response);
    }
}

function getFileCharacterChannel (string filePath, string permission, string encoding)(io:CharacterChannel) {
	io:ByteChannel channel = io:openFile(filePath, permission);
	var characterChannel,err = io:createCharacterChannel(channel, encoding);
	return characterChannel;
}

function process (io:CharacterChannel sourceChannel)(string ) {
	var content,err = sourceChannel.readCharacters(25);
    return content;
}