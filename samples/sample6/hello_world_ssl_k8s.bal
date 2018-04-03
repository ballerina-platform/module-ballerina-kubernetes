import ballerina/http;
import ballerinax/kubernetes;

@kubernetes:Service{}
@kubernetes:Ingress{
    hostname:"abc.com"
}
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
service<http:Service> helloWorld bind helloWorldSecuredEP {
     sayHello (endpoint outboundEP, http:Request request) {
        http:Response response = {};
        response.setStringPayload("Hello, World from secured service ! \n");
        _ = outboundEP -> respond(response);
    }
}
