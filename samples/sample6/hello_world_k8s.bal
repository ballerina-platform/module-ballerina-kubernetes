import ballerina.net.http;
import ballerinax.kubernetes;

@kubernetes:svc{serviceType:"NodePort"}
endpoint http:ServiceEndpoint helloWorldSecuredEP {
    port:9090,
    ssl:{
        keyStoreFile:"${ballerina.home}/bre/security/ballerinaKeystore.p12",
        keyStorePassword:"ballerina",
        certPassword:"ballerina"
    }
};

@http:serviceConfig {
    basePath:"/helloWorld"
}
@kubernetes:ingress{
    hostname:"abc.com"
}
service<http:Service> helloWorld bind helloWorldSecuredEP {
     sayHello (endpoint outboundEP, http:Request request) {
        http:Response response = {};
        response.setStringPayload("Hello, World from service helloWorld ! ");
        _ = outboundEP -> respond(response);
    }
}
