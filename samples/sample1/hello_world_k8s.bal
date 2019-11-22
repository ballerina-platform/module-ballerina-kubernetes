import ballerina/http;
import ballerina/log;
import ballerina/kubernetes;

@kubernetes:Service {
    serviceType: "NodePort"
}
@kubernetes:Deployment {}
@http:ServiceConfig {
    basePath: "/helloWorld"
}


service helloWorld on new http:Listener(8080) {
    resource function sayHello(http:Caller outboundEP, http:Request request) {
        http:Response response = new;
        response.setTextPayload("Hello, World from service helloWorld ! \n");
        var responseResult = outboundEP->respond(response);
        if (responseResult is error) {
            log:printError("error responding back to client.", responseResult);
        }
    }
}
