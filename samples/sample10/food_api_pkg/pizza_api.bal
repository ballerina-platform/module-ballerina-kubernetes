package food_api_pkg;
import ballerina/http;
import ballerinax/kubernetes;

@kubernetes:Ingress {
    hostname:"pizza.com",
    path:"/pizzastore",
    targetPath:"/"
}
@kubernetes:Service{}
endpoint http:Listener pizzaEP {
    port:9099
};

@kubernetes:Deployment {
    name:"foodstore",
    replicas:3,
    labels:{"location":"SL","city":"COLOMBO"},
    enableLiveness:"enable",
    livenessPort:9099
}
@http:ServiceConfig {
    basePath:"/pizza"
}
service<http:Service> PizzaAPI bind pizzaEP{
    @http:ResourceConfig {
        methods:["GET"],
        path:"/menu"
    }
    getPizzaMenu (endpoint outboundEP, http:Request req) {
        http:Response response = new;
        response.setStringPayload("Pizza menu \n");
        _ = outboundEP -> respond(response);
    }
}