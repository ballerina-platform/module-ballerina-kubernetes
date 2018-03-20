import ballerina.net.http;
import ballerinax.kubernetes;

@kubernetes:svc{}
endpoint http:ServiceEndpoint helloWorldEP {
    port:9090,
	ssl:{
		keyStoreFile:"${ballerina.home}/bre/security/ballerinaKeystore.p12",
		keyStorePassword:"ballerina",
		certPassword:"ballerina"
	}
};

@kubernetes:persistentVolumeClaim{
	volumeClaims:[
		{name:"local-pv-2",mountPath:"/home/ballerina/tmp",readOnly:false,accessMode:"ReadWriteOnce",volumeClaimSize:"1Gi"}
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
        path:"/sayHello"
    }
    sayHello (endpoint outboundEP, http:Request request) {
        http:Response response = {};
        response.setStringPayload("Hello World\n");
        _ = outboundEP -> respond(response);
    }
}
