import ballerina.net.http;
import ballerinax.kubernetes;


@kubernetes:svc{}
endpoint http:ServiceEndpoint pizzaEP {
    port:9099
};
@kubernetes:svc{}
endpoint http:ServiceEndpoint burgerEP {
    port:9096
};
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
    basePath:"/pizza"
}
service<http:Service> PizzaAPI bind pizzaEP{
    @http:resourceConfig {
        methods:["GET"],
        path:"/menu"
    }
    getPizzaMenu (endpoint outboundEP, http:Request req) {
        http:Response response = {};
        response.setStringPayload("Pizza menu ");
        _ = outboundEP -> respond(response);
    }
}

@kubernetes:ingress {
    hostname:"burger.com",
    path:"/",
    targetPath:"/burger"
}
@http:serviceConfig {
    basePath:"/burger"
}
service<http:Service> BurgerAPI bind burgerEP {
    @http:resourceConfig {
        methods:["GET"],
        path:"/menu"
    }
    getBurgerMenu (endpoint outboundEP, http:Request req) {
        http:Response response = {};
        response.setStringPayload("Burger menu ");
        _ = outboundEP -> respond(response);
    }
}
