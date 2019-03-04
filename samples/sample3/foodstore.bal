import ballerina/http;
import ballerinax/kubernetes;


@kubernetes:Ingress {
    hostname: "pizza.com",
    path: "/pizzastore",
    targetPath: "/"
}
@kubernetes:Service {
    sessionAffinity: "ClientIP"
}
listener http:Listener pizzaEP = new(9099);

@kubernetes:Deployment {
    name: "foodstore",
    replicas: 3,
    labels: { "location": "SL", "city": "COLOMBO" },
    livenessProbe: {
        port: 9099
    },
    singleYAML: false
}

@http:ServiceConfig {
    basePath: "/pizza"
}
service PizzaAPI on pizzaEP {
    @http:ResourceConfig {
        methods: ["GET"],
        path: "/menu"
    }
    resource function getPizzaMenu(http:Caller outboundEP, http:Request req) {
        http:Response response = new;
        response.setTextPayload("Pizza menu \n");
        _ = outboundEP->respond(response);
    }
}


@kubernetes:Ingress {
    hostname: "burger.com",
    path: "/",
    targetPath: "/burger"
}
@kubernetes:Service {}
listener http:Listener burgerEP = new(9096);

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
