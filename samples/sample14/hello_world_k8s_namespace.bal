import ballerina/http;
import ballerinax/kubernetes;

@kubernetes:Deployment {
    enableLiveness: true,
    singleYAML: true,
    namespace: "ballerina"
}
@kubernetes:Ingress {
    hostname: "abc.com"
}
@kubernetes:Service { name: "hello" }
endpoint http:Listener helloEP {
    port: 9090
};

@http:ServiceConfig {
    basePath: "/helloWorld"
}
service<http:Service> helloWorld bind helloEP {
    sayHello(endpoint outboundEP, http:Request request) {
        http:Response response = new;
        response.setTextPayload("Hello, World from service helloWorld ! \n");
        _ = outboundEP->respond(response);
    }
}
