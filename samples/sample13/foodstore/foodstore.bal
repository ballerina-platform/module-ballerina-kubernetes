import ballerina/http;
import ballerinax/kubernetes;
import ballerina/log;

@kubernetes:Service {
    serviceType: "NodePort"
}
@kubernetes:Ingress {
    hostname: "foodstore.com"
}
endpoint http:Listener foodStoreEP {
    port: 9090
};

endpoint http:Client burgerBackend {
    url: "http://burger-backend:9090"
};

endpoint http:Client pizzaBackend {
    url: "http://pizza-backend:9090"
};

@kubernetes:Deployment {
    labels: { "location": "SL", "city": "COLOMBO" },
    enableLiveness: true,
    dependsOn: ["burger:burgerEP", "pizza:pizzaEP"]
}
@http:ServiceConfig {
    basePath: "/store"
}
service<http:Service> FoodStoreAPI bind foodStoreEP {
    @http:ResourceConfig {
        methods: ["GET"],
        path: "/pizza"
    }
    getPizzaMenu(endpoint outboundEP, http:Request req) {
        var response = pizzaBackend->get("/pizza/menu");

        match response {
            http:Response resp => {
                log:printInfo("GET request:");
                _ = outboundEP->respond(resp);
            }
            error err => {
                log:printError(err.message, err = err);
            }
        }
    }

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/burger"
    }
    getBurgerMenu(endpoint outboundEP, http:Request req) {
        var response = pizzaBackend->get("/burger/menu");

        match response {
            http:Response resp => {
                log:printInfo("GET request:");
                _ = outboundEP->respond(resp);
            }
            error err => {
                log:printError(err.message, err = err);
            }
        }
    }
}
