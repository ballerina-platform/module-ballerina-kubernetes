import ballerina/http;
import ballerina/kubernetes;
import ballerina/log;

@kubernetes:Ingress {
    hostname: "pizza.com",
    path: "/pizzastore(/|$)(.*)",
    targetPath: "/$2"
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
    }
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
        var responseResult = outboundEP->respond(response);
        if (responseResult is error) {
            log:printError("error responding back to client.", responseResult);
        }
    }
}


@kubernetes:Ingress {
    hostname: "burger.com",
    path: "/(.*)",
    targetPath: "/burger/$1"
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
        var responseResult = outboundEP->respond(response);
        if (responseResult is error) {
            log:printError("error responding back to client.", responseResult);
        }
    }
}
