import ballerina/http;
import ballerinax/kubernetes;

@kubernetes:Service {
    name: "pizza-backend"
}
endpoint http:Listener pizzaEP {
    port: 9090
};

@kubernetes:Deployment {
    replicas: 3,
    enableLiveness: true
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