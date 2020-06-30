import ballerina/http;
//Adding the import as `ballerina/kubernetes as _` will generate the Docker image, Dockerfile and Kubernetes artifacts for the `helloWorld` service.
import ballerina/kubernetes as _;

listener http:Listener helloWorldEP = new(9090);

@http:ServiceConfig {
    basePath: "/helloWorld"
}
service helloWorld on helloWorldEP {
    resource function sayHello(http:Caller outboundEP, http:Request request) returns error? {
        check outboundEP->respond("Hello World from Docker! \n");
    }
}
