package food_api_pkg;
import ballerina/net.http;
import ballerinax/kubernetes;

@kubernetes:Service{}
endpoint http:ServiceEndpoint burgerEP {
    port:9096
};

@kubernetes:Ingress {
    hostname:"burger.com",
    path:"/",
    targetPath:"/burger"
}
@http:ServiceConfig {
    basePath:"/burger"
}
service<http:Service> BurgerAPI bind burgerEP {
    @http:ResourceConfig {
        methods:["GET"],
        path:"/menu"
    }
    getBurgerMenu (endpoint outboundEP, http:Request req) {
        http:Response response = {};
        response.setStringPayload("Burger menu \n");
        _ = outboundEP -> respond(response);
    }
}