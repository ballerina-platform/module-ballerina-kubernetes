import ballerina/http;
import ballerinax/kubernetes;

@kubernetes:Ingress {
    hostname: "pizza.com",
    path: "/pizzastore",
    targetPath: "/"
}
@kubernetes:Service {}
listener http:Server pizzaEP = new http:Server(9099);

@kubernetes:Deployment {
    name: "foodstore",
    replicas: 3,
    env: { "location": "SL", "city": "COLOMBO" },
    enableLiveness: true,
    livenessPort: 9099,
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