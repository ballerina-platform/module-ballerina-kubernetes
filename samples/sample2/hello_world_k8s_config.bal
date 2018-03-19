import ballerina.net.http;
import ballerinax.kubernetes;

@kubernetes:svc{name:"hello"}
endpoint http:ServiceEndpoint helloEP {
    port:9090
};

@kubernetes:deployment{
    enableLiveness:"enable"
}
@kubernetes:ingress{
    hostname:"abc.com"
}
@http:serviceConfig {
    basePath:"/helloWorld"
}
service<http:Service> helloWorld bind helloEP {
    sayHello (endpoint outboundEP, http:Request request) {
        http:Response response = {};
        response.setStringPayload("Hello, World from service helloWorld ! ");
        _ = outboundEP -> respond(response);
    }
}
