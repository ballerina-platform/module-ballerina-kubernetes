import ballerina/http;
import ballerinax/kubernetes;

@kubernetes:Service {}
@kubernetes:Ingress {
    hostname:"burger.com",
    path:"/",
    targetPath:"/burger"
}
endpoint http:Listener burgerEP {
    port:9096,
    secureSocket:{
        keyStore:{
            path:"${ballerina.home}/bre/security/ballerinaKeystore.p12",
            password:"ballerina"
        }
    }
};


@kubernetes:Deployment {}
@http:ServiceConfig {
    basePath:"/burger"
}
service<http:Service> BurgerAPI bind burgerEP {
    @http:ResourceConfig {
        methods:["GET"],
        path:"/menu"
    }
    getBurgerMenu(endpoint outboundEP, http:Request req) {
        http:Response response = new;
        response.setTextPayload("Burger menu \n");
        _ = outboundEP->respond(response);
    }
}