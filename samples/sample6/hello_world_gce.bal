import ballerina/http;
import ballerinax/kubernetes;

@kubernetes:Ingress {
    hostname: "abc.com"
}
@kubernetes:Service { serviceType: "NodePort" }
listener http:Server gceHelloWorldDEP = new http:Server(9090);

@kubernetes:Deployment {
    enableLiveness: true,
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
        _ = outboundEP->respond(response);
    }
}
