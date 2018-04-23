import ballerina/http;
import ballerinax/kubernetes;

@kubernetes:Service {serviceType:"NodePort"}
@http:ServiceConfig {
    basePath:"/helloWorld"
}
service<http:Service> helloWorld bind {port:9090} {
    sayHello(endpoint outboundEP, http:Request request) {
        http:Response response = new;
        response.setStringPayload("Hello, World from service helloWorld ! \n");
        _ = outboundEP->respond(response);
    }
}
