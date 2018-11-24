import ballerina/http;
import ballerinax/kubernetes;

@kubernetes:Service {}
@kubernetes:Ingress {
    hostname: "burger.com",
    path: "/",
    targetPath: "/burger"
}
listener http:Server burgerEP = new http:Server(9096, config = {
    secureSocket: {
        keyStore: {
            path: "${ballerina.home}/bre/security/ballerinaKeystore.p12",
            password: "ballerina"
        }
    }
});


@kubernetes:Deployment {
    singleYAML: false
}
@http:ServiceConfig {
    basePath: "/burger"
}
service BurgerAPI on burgerEP {
    @http:ResourceConfig {
        methods: ["GET"],
        path: "/menu"
    }
    resource function getBurgerMenu(http:Caller outboundEP, http:Request req) {
        http:Response response = new;
        response.setTextPayload("Burger menu \n");
        _ = outboundEP->respond(response);
    }
}