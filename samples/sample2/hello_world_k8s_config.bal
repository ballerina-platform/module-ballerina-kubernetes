import ballerina.net.http;
import ballerinax.kubernetes;

@kubernetes:svc{}
endpoint<http:Service> backendEP {
    port:9090
}

@kubernetes:deployment{
    enableLiveness:"enable"
}
@kubernetes:hpa{}
@kubernetes:ingress{
    hostname:"abc.com"
}
@http:serviceConfig {
    basePath:"/helloWorld",
    endpoints:[backendEP]
}
service<http:Service> helloWorld {
    resource sayHello (http:ServerConnector conn, http:Request request) {
        http:Response response = {};
        response.setStringPayload("Hello, World from service helloWorld ! ");
        _ = conn -> respond(response);
    }
}
