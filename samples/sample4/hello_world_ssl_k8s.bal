import ballerina/http;
import ballerina/log;
import ballerina/kubernetes;

@kubernetes:Service {}
@kubernetes:Ingress {
    hostname: "abc.com"
}
listener http:Listener helloWorldSecuredEP = new(9090, {
    secureSocket: {
        keyStore: {
            path: "./security/ballerinaKeystore.p12",
            password: "ballerina"
        }
    }
});

@kubernetes:Deployment {}
@http:ServiceConfig {
    basePath: "/helloWorld"
}
service helloWorld on helloWorldSecuredEP {
    resource function sayHello(http:Caller outboundEP, http:Request request) {
        http:Response response = new;
        response.setTextPayload("Hello, World from secured service ! \n");
        var responseResult = outboundEP->respond(response);
        if (responseResult is error) {
            log:printError("error responding back to client.", err = responseResult);
        }
    }
}
