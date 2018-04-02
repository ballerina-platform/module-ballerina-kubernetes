package food_api_pkg;
import ballerina/http;
import ballerinax/kubernetes;

@kubernetes:Service{}
endpoint http:ServiceEndpoint burgerEP {
    port:9096,
    secureSocket:{
        keyStore:{
            filePath:"${ballerina.home}/bre/security/ballerinaKeystore.p12",
            password:"ballerina"
        }
    }
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