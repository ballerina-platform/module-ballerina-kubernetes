import ballerina/http;
import ballerinax/kubernetes;

@kubernetes:Deployment {
    enableLiveness: true
}
@kubernetes:Ingress {
    hostname: "abc.com"
}
@kubernetes:Service {
    name: "hello",
    serviceType: "NodePort"
}
listener http:Server helloEP = new http:Server(9090, config = {
    secureSocket: {
        keyStore: {
            path: "${ballerina.home}/bre/security/ballerinaKeystore.p12",
            password: "ballerina"
        }
    }
});

@http:ServiceConfig {
    basePath: "/helloWorld"
}
service helloWorld on helloEP {
    resource function sayHello(http:Caller outboundEP, http:Request request) {
        http:Response response = new;
        response.setTextPayload("Hello, World from service helloWorld ! \n");
        _ = outboundEP->respond(response);
    }
}
