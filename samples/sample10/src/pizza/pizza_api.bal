import ballerina/http;
import ballerina/kubernetes;
import ballerina/log;

@kubernetes:Ingress {
    hostname: "pizza.com",
    path: "/pizzastore",
    targetPath: "/"
}
@kubernetes:Service {}
listener http:Listener pizzaEP = new(9099);

@kubernetes:Deployment {
    name: "foodstore",
    replicas: 3,
    env: { "location": "SL", "city": "COLOMBO" },
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
