import ballerina/http;
import ballerinax/kubernetes;

@kubernetes:Ingress {
    hostname:"internal.pizzashack.com"
}
@kubernetes:Service {}
listener http:Server pizzaEP = new http:Server(9090);

@kubernetes:Service {}
@kubernetes:Ingress {
    hostname:"pizzashack.com"
}
listener http:Server pizzaEPSecured = new http:Server(9090, config = {
    secureSocket: {
        keyStore: {
            path: "${ballerina.home}/bre/security/ballerinaKeystore.p12",
            password: "ballerina"
        }
    }
});

@kubernetes:Deployment {
    image:"ballerina.com/pizzashack:2.1.0"
}
@kubernetes:HPA {}
@http:ServiceConfig {
    basePath:"/customer"
}
service Customer on pizzaEP, pizzaEPSecured {
    @http:ResourceConfig {
        methods:["GET"],
        path:"/"
    }
    resource function getCustomer(http:Caller outboundEP, http:Request request) {
        http:Response response = new;
        response.setTextPayload("Get Customer resource !!!!\n");
        _ = outboundEP->respond(response);
    }
}
