import ballerina/config;
import ballerina/http;
import ballerinax/kubernetes;
import ballerina/io;

@kubernetes:Service{}
@kubernetes:Ingress{
    hostname:"abc.com"
}
endpoint http:Listener helloWorldEP {
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
		{name:"ballerina-config", mountPath:"/home/ballerina/conf", isBallerinaConf:true,
			data:["./conf/ballerina.conf"]
		}
	]
}
@http:ServiceConfig {
    basePath:"/helloWorld"
}
service<http:Service> helloWorld bind helloWorldEP {
    @http:ResourceConfig {
        methods:["GET"],
        path:"/config/{user}"
    }
    getConfig (endpoint outboundEP, http:Request request,string user) {
        http:Response response = new;
        string userId = getConfigValue(user, "userid");
        string groups = getConfigValue(user, "groups");
        string payload = "{userId: "+userId+", groups: "+groups+"}";
        response.setStringPayload(payload +"\n");
        _ = outboundEP -> respond(response);
    }
}

function getConfigValue (string instanceId, string property) returns (string) {
    match config:getAsString(instanceId + "." + property) {
        string value => {
            return value == null ? "Invalid user" : value;
        }
        () => return "Invalid user";
    }
}

