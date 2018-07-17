import ballerina/http;
import ballerinax/kubernetes;
import ballerina/log;

@kubernetes:Service {
    serviceType: "NodePort"
}
@kubernetes:Ingress {
    hostname: "drinkstore.com"
}
endpoint http:Listener drinkStoreEP {
    port: 9091,
    secureSocket: {
        keyStore: {
            path: "${ballerina.home}/bre/security/ballerinaKeystore.p12",
            password: "ballerina"
        }
    }
};

endpoint http:Client hotDrinkBackend {
    url: "http://hotdrink-backend:9090"
};

endpoint http:Client coolDrinkBackend {
    url: "http://cooldrink-backend:9090"
};

@kubernetes:Deployment {
    enableLiveness: true,
    dependsOn: ["cool_drink:coolDrinkEP", "hot_drink:hotDrinkEP"],
    singleYAML: true
}
@http:ServiceConfig {
    basePath: "/store"
}
@kubernetes:HPA {}
service<http:Service> DrinkStoreAPI bind drinkStoreEP {
    @http:ResourceConfig {
        methods: ["GET"],
        path: "/hotDrink"
    }
    getPizzaMenu(endpoint outboundEP, http:Request req) {
        var response = coolDrinkBackend->get("/coolDrink/menu");

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
        path: "/coolDrink"
    }
    getBurgerMenu(endpoint outboundEP, http:Request req) {
        var response = hotDrinkBackend->get("/hotDrink/menu");
        match response {
            http:Response resp => {
                log:printInfo("GET request: ");
                _ = outboundEP->respond(resp);
            }
            error err => {
                log:printError(err.message, err = err);
            }
        }
    }
}
