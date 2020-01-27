import ballerina/http;
import ballerina/kubernetes;
import ballerina/log;

@kubernetes:Deployment {
    livenessProbe: true
}
@kubernetes:Ingress {
    hostname: "abc.com"
}
@kubernetes:Service {name: "hello"}
listener http:Listener helloEP = new(9090);

@http:ServiceConfig {
    basePath: "/helloWorld"
}
service helloWorld on helloEP {
    resource function sayHello(http:Caller outboundEP, http:Request request) {
        http:Response response = new;
        response.setTextPayload("Hello, World from service helloWorld ! \n");
        var responseResult = outboundEP->respond(response);
        if (responseResult is error) {
            log:printError("error responding back to client.", err = responseResult);
        }
    }
}
