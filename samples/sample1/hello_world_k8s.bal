import ballerina/http;
import ballerinax/kubernetes;

@kubernetes:Service{serviceType:"NodePort"}
endpoint http:ServiceEndpoint helloWorldEP {
    port:9090
};

@http:ServiceConfig {
    basePath:"/helloWorld"
}
service<http:Service> helloWorld bind helloWorldEP {
     sayHello (endpoint outboundEP, http:Request request) {
        http:Response response = new;
        response.setStringPayload("Hello, World from service helloWorld ! \n");
        _ = outboundEP -> respond(response);
    }
}
