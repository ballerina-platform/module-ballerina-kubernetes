import ballerina/http;
import ballerinax/kubernetes;

@kubernetes:Ingress {
    hostname: "pizza.com",
    path: "/pizzastore",
    targetPath: "/"
}
@kubernetes:Service {}
endpoint http:Listener pizzaEP {
    port: 9099
};

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
service<http:Service> PizzaAPI bind pizzaEP {
    @http:ResourceConfig {
        methods: ["GET"],
        path: "/menu"
    }
    getPizzaMenu(endpoint outboundEP, http:Request req) {
        http:Response response = new;
        response.setTextPayload("Pizza menu \n");
        _ = outboundEP->respond(response);
    }
}