import ballerina.net.http;
import ballerinax.kubernetes;

@kubernetes:SVC{serviceType:"NodePort"}
endpoint http:ServiceEndpoint gceHelloWorldDEP {
    port:9090
};

@kubernetes:Deployment{
    enableLiveness:"enable",
    push:true,
    image:"index.docker.io/$env{DOCKER_USERNAME}/gce-sample:1.0",
    username:"$env{DOCKER_USERNAME}",
    password:"$env{DOCKER_PASSWORD}"
}
@kubernetes:HPA{}
@kubernetes:Ingress{
    hostname:"abc.com"
}
@http:ServiceConfig {
    basePath:"/helloWorld"
}
service<http:Service>  helloWorld bind gceHelloWorldDEP {
    sayHello (endpoint outboundEP, http:Request request) {
        http:Response response = {};
        response.setStringPayload("Hello, World from service helloWorld! ");
        _ = outboundEP -> respond(response);
    }
}
