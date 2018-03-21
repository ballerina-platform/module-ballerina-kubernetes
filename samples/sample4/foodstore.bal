import ballerina.net.http;
import ballerinax.kubernetes;


@kubernetes:SVC{}
endpoint http:ServiceEndpoint pizzaEP {
    port:9099
};
@kubernetes:SVC{}
endpoint http:ServiceEndpoint burgerEP {
    port:9096
};
@kubernetes:Deployment {
    name:"foodstore",
    replicas:3,
    labels:"location:SL,city:COLOMBO",
    enableLiveness:"enable",
    livenessPort:9099
}
@kubernetes:Ingress {
    hostname:"pizza.com",
    path:"/pizzastore",
    targetPath:"/"
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
        http:Response response = {};
        response.setStringPayload("Pizza menu ");
        _ = outboundEP -> respond(response);
    }
}

@kubernetes:Ingress {
    hostname:"burger.com",
    path:"/",
    targetPath:"/burger"
}
@http:ServiceConfig {
    basePath:"/burger"
}
service<http:Service> BurgerAPI bind burgerEP {
    @http:ResourceConfig {
        methods:["GET"],
        path:"/menu"
    }
    getBurgerMenu (endpoint outboundEP, http:Request req) {
        http:Response response = {};
        response.setStringPayload("Burger menu ");
        _ = outboundEP -> respond(response);
    }
}
