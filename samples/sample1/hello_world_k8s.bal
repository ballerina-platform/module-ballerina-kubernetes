import ballerina.net.http;
import ballerinax.kubernetes;

@kubernetes:svc{serviceType:"NodePort"}
endpoint<http:Service> helloWorldEP {
    port:9090
}

@http:serviceConfig {
    basePath:"/helloWorld",
    endpoints:[helloWorldEP]
}
service<http:Service> helloWorld {
    resource sayHello (http:ServerConnector conn, http:Request request) {
        http:Response response = {};
        response.setStringPayload("Hello, World from service helloWorld ! ");
        _ = conn -> respond(response);
    }
}
