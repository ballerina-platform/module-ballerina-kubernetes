import ballerina.net.http;
import ballerinax.kubernetes;

@kubernetes:svc{}
endpoint<http:Service> backendEP {
    port:9090
}

@kubernetes:deployment {
    image:"ballerina.com/pizzashack:2.1.0"
}

@kubernetes :ingress{
    hostname:"pizzashack.com",
    path:"/"
}
@kubernetes:hpa{}
@http:serviceConfig {
    basePath:"/customer",
    endpoints:[backendEP]
}
service<http:Service> Customer {
    @http:resourceConfig {
        methods:["GET"],
        path:"/"
    }
    resource getCustomer (http:ServerConnector conn, http:Request request) {
        http:Response response = {};
        response.setStringPayload("Get Customer resource !!!!");
        _ = conn -> respond(response);
    }
}

@kubernetes:ingress{}
@http:serviceConfig {
    basePath:"/orders",
    endpoints:[backendEP]
}
service<http:Service> Order {
    @http:resourceConfig {
        methods:["GET"],
        path:"/"
    }
    resource getOrder (http:ServerConnector conn, http:Request request) {
        http:Response response = {};
        response.setStringPayload("Get order resource !!!!");
        _ = conn-> respond(response);
    }
}