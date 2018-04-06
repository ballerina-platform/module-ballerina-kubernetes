import ballerina/http;
import ballerinax/kubernetes;

@kubernetes:Deployment{
    enableLiveness:"enable"
}
@kubernetes:Ingress{
    hostname:"abc.com"
}
@kubernetes:Service{name:"hello"}
endpoint http:ServiceEndpoint helloEP {
    port:9090
};


@http:ServiceConfig {
    basePath:"/helloWorld"
}
service<http:Service> helloWorld bind helloEP {
    sayHello (endpoint outboundEP, http:Request request) {
        http:Response response = {};
        response.setStringPayload("Hello, World from service helloWorld ! \n");
        _ = outboundEP -> respond(response);
    }
}
