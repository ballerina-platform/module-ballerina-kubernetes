import ballerina.net.http;
import ballerinax.kubernetes;

@kubernetes:deployment {
    image:"ballerina.com/pizzashack:2.1.0"
}
@kubernetes:svc{}
@kubernetes :ingress{
    hostname:"pizzashack.com",
    path:"/"
}
@http:configuration {
    basePath:"/customer"
}
service<http> Customer {
    @http:resourceConfig {
        methods:["GET"],
        path:"/"
    }
    resource getCustomer (http:Connection conn, http:InRequest req) {
        http:OutResponse res = {};
        res.setStringPayload("Get Customer resource ");
        _ = conn.respond(res);
    }
}

@kubernetes:svc{}
@kubernetes:ingress{}
@http:configuration {
    basePath:"/orders"
}
service<http> Order {
    @http:resourceConfig {
        methods:["GET"],
        path:"/"
    }
    resource getOrder (http:Connection conn, http:InRequest req) {
        http:OutResponse res = {};
        res.setStringPayload("Get order resource ");
        _ = conn.respond(res);
    }
}

