import ballerina/net.http;
import ballerinax/kubernetes;

@kubernetes:Service{}
endpoint http:ServiceEndpoint helloWorldSecuredEP {
    port:9090,
    secureSocket: {
        keyStore: {
            filePath: "${ballerina.home}/bre/security/ballerinaKeystore.p12",
            password: "ballerina"
        }
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
