import ballerina.net.http;
import ballerinax.kubernetes;

@kubernetes:SVC{name:"hello"}
endpoint http:ServiceEndpoint helloEP {
    port:9090
};

@kubernetes:Deployment{
    enableLiveness:"enable"
}
@kubernetes:Ingress{
    hostname:"abc.com"
}
@http:ServiceConfig {
    basePath:"/helloWorld"
}
service<http:Service> helloWorld bind helloEP {
    sayHello (endpoint outboundEP, http:Request request) {
        http:Response response = {};
        response.setStringPayload("Hello, World from service helloWorld ! ");
        _ = outboundEP -> respond(response);
    }
}
