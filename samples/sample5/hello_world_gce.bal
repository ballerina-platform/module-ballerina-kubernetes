import ballerina.net.http;
import ballerinax.kubernetes;

@kubernetes:svc{serviceType:"NodePort"}
endpoint http:ServiceEndpoint gceHelloWorldDEP {
    port:9090
};

@kubernetes:deployment{
    enableLiveness:"enable",
    push:true,
    image:"index.docker.io/<username>/gce-sample:1.0",
    username:"<username>",
    password:"<password>"
}
@kubernetes:hpa{}
@kubernetes:ingress{
    hostname:"abc.com"
}
@http:serviceConfig {
    basePath:"/helloWorld"
}
service<http:Service>  helloWorld bind gceHelloWorldDEP {
    sayHello (endpoint outboundEP, http:Request request) {
        http:Response response = {};
        response.setStringPayload("Hello, World from service helloWorld! ");
        _ = outboundEP -> respond(response);
    }
}
