import ballerina/http;
import ballerina/kubernetes;
import ballerina/log;

@kubernetes:Ingress {
    hostname: "abc.com"
}
@kubernetes:Service { serviceType: "NodePort" }
listener http:Listener gceHelloWorldDEP = new(9090);

@kubernetes:Deployment {
    livenessProbe: true,
    push: true,
    image: "index.docker.io/$env{DOCKER_USERNAME}/gce-sample:1.0",
    username: "$env{DOCKER_USERNAME}",
    password: "$env{DOCKER_PASSWORD}"
}
@kubernetes:HPA {}
@http:ServiceConfig {
    basePath:"/helloWorld"
}
service helloWorld on gceHelloWorldDEP {
    resource function sayHello(http:Caller outboundEP, http:Request request) {
        http:Response response = new;
        response.setTextPayload("Hello, World from service helloWorld! \n");
        var responseResult = outboundEP->respond(response);
        if (responseResult is error) {
            log:printError("error responding back to client.", err = responseResult);
        }
    }
}
