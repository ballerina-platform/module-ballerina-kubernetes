import ballerina/http;
import ballerinax/kubernetes;

@kubernetes:Ingress {
    hostname:"internal.pizzashack.com"
}
@kubernetes:Service {}
endpoint http:Listener pizzaEP {
    port:9090
};


@kubernetes:Service {}
@kubernetes:Ingress {
    hostname:"pizzashack.com"
}
endpoint http:Listener pizzaEPSecured {
    port:9095,
    secureSocket:{
        keyStore:{
            path:"${ballerina.home}/bre/security/ballerinaKeystore.p12",
            password:"ballerina"
        }
    }
};


@kubernetes:Deployment {
    image:"ballerina.com/pizzashack:2.1.0"
}
@kubernetes:HPA {}
@http:ServiceConfig {
    basePath:"/customer"
}
service<http:Service> Customer bind pizzaEP, pizzaEPSecured {
    @http:ResourceConfig {
        methods:["GET"],
        path:"/"
    }
    getCustomer(endpoint outboundEP, http:Request request) {
        http:Response response = new;
        response.setStringPayload("Get Customer resource !!!!\n");
        _ = outboundEP->respond(response);
    }
}
