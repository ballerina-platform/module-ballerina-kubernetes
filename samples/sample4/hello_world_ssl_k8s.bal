import ballerina/http;
import ballerinax/kubernetes;

@kubernetes:Service {}
@kubernetes:Ingress {
    hostname:"abc.com"
}
listener http:Server helloWorldSecuredEP = new http:Server(9090, config = {
    secureSocket: {
        keyStore: {
            path: "${ballerina.home}/bre/security/ballerinaKeystore.p12",
            password: "ballerina"
        }
    }
});

@kubernetes:Deployment {
    singleYAML: false
}
@http:ServiceConfig {
    basePath:"/helloWorld"
}
service helloWorld on helloWorldSecuredEP {
    resource function sayHello(http:Caller outboundEP, http:Request request) {
        http:Response response = new;
        response.setTextPayload("Hello, World from secured service ! \n");
        _ = outboundEP->respond(response);
    }
}
