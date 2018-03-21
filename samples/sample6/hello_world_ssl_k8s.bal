import ballerina.net.http;
import ballerinax.kubernetes;

@kubernetes:SVC{}
endpoint http:ServiceEndpoint helloWorldSecuredEP {
    port:9090,
    ssl:{
        keyStoreFile:"${ballerina.home}/bre/security/ballerinaKeystore.p12",
        keyStorePassword:"ballerina",
        certPassword:"ballerina"
    }
};

@http:ServiceConfig {
    basePath:"/helloWorld"
}
@kubernetes:Ingress{
    hostname:"abc.com"
}
service<http:Service> helloWorld bind helloWorldSecuredEP {
     sayHello (endpoint outboundEP, http:Request request) {
        http:Response response = {};
        response.setStringPayload("Hello, World from service helloWorld ! ");
        _ = outboundEP -> respond(response);
    }
}
