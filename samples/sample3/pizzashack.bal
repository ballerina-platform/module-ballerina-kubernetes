import ballerina/net.http;
import ballerinax/kubernetes;

@kubernetes:SVC{}
endpoint http:ServiceEndpoint pizzaEP {
    port:9090
};

@kubernetes:Deployment {
    image:"ballerina.com/pizzashack:2.1.0"
}

@kubernetes :Ingress{
    hostname:"pizzashack.com",
    path:"/"
}
@kubernetes:HPA{}
@http:ServiceConfig {
    basePath:"/customer"
}
service<http:Service> Customer bind pizzaEP{
    @http:ResourceConfig {
        methods:["GET"],
        path:"/"
    }
    getCustomer (endpoint outboundEP, http:Request request) {
        http:Response response = {};
        response.setStringPayload("Get Customer resource !!!!");
        _ = outboundEP -> respond(response);
    }
}

@kubernetes:Ingress{}
@http:ServiceConfig {
    basePath:"/orders",
    endpoints:[pizzaEP]
}
service<http:Service> Order bind pizzaEP{
    @http:ResourceConfig {
        methods:["GET"],
        path:"/"
    }
    getOrder (endpoint outboundEP, http:Request request) {
        http:Response response = {};
        response.setStringPayload("Get order resource !!!!");
        _ = outboundEP -> respond(response);
    }
}