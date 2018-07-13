import ballerina/http;
import ballerinax/kubernetes;

@kubernetes:Service {
    name: "buger-backend"
}
endpoint http:Listener burgerEP {
    port: 9090,
    secureSocket: {
        keyStore: {
            path: "${ballerina.home}/bre/security/ballerinaKeystore.p12",
            password: "ballerina"
        }
    }
};


@kubernetes:Deployment {
    dependsOn: ["pizza:pizzaEP"]
}
@http:ServiceConfig {
    basePath: "/burger"
}
service<http:Service> BurgerAPI bind burgerEP {
    @http:ResourceConfig {
        methods: ["GET"],
        path: "/menu"
    }
    getBurgerMenu(endpoint outboundEP, http:Request req) {
        http:Response response = new;
        response.setTextPayload("Burger menu \n");
        _ = outboundEP->respond(response);
    }
}