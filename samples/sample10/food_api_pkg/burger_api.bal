package food_api_pkg;
import ballerina/http;
import ballerinax/kubernetes;

@kubernetes:Service{}
@kubernetes:Ingress {
    hostname:"burger.com",
    path:"/",
    targetPath:"/burger"
}
endpoint http:ServiceEndpoint burgerEP {
    port:9096,
    secureSocket:{
        keyStore:{
            filePath:"${ballerina.home}/bre/security/ballerinaKeystore.p12",
            password:"ballerina"
        }
    }
};


@http:ServiceConfig {
    basePath:"/burger"
}
service<http:Service> BurgerAPI bind burgerEP {
    @http:ResourceConfig {
        methods:["GET"],
        path:"/menu"
    }
    getBurgerMenu (endpoint outboundEP, http:Request req) {
        http:Response response = new;
        response.setStringPayload("Burger menu \n");
        _ = outboundEP -> respond(response);
    }
}