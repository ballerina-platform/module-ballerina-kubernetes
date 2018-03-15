import ballerina.net.http;
import ballerinax.kubernetes;


@kubernetes:svc{}
endpoint<http:Service> pizzaEP {
    port:9099
}
@kubernetes:svc{}
endpoint<http:Service> burgerEP {
    port:9096
}
@kubernetes:deployment {
    name:"foodstore",
    replicas:3,
    labels:"location:SL,city:COLOMBO",
    enableLiveness:"enable",
    livenessPort:9099
}
@kubernetes:ingress {
    hostname:"pizza.com",
    path:"/pizzastore",
    targetPath:"/"
}
@http:serviceConfig {
    basePath:"/pizza",
    endpoints:[pizzaEP]
}
service<http:Service> PizzaAPI {
    @http:resourceConfig {
        methods:["GET"],
        path:"/menu"
    }
    resource getPizzaMenu (http:ServerConnector conn, http:Request req) {
        http:Response response = {};
        response.setStringPayload("Pizza menu ");
        _ = conn -> respond(response);
    }
}

@kubernetes:ingress {
    hostname:"burger.com",
    path:"/",
    targetPath:"/burger"
}
@http:serviceConfig {
    basePath:"/burger",
    endpoints:[burgerEP]
}
service<http:Service> BurgerAPI {
    @http:resourceConfig {
        methods:["GET"],
        path:"/menu"
    }
    resource getBurgerMenu (http:ServerConnector conn, http:Request req) {
        http:Response response = {};
        response.setStringPayload("Burger menu ");
        _ = conn -> respond(response);
    }
}
