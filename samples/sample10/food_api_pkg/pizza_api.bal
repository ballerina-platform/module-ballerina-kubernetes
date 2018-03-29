package food_api_pkg;
import ballerina/net.http;
import ballerinax/kubernetes;


@kubernetes:Service{}
endpoint http:ServiceEndpoint pizzaEP {
    port:9099,
    secureSocket: {
        keyStore: {
          filePath: "${ballerina.home}/bre/security/ballerinaKeystore.p12",
          password: "ballerina"
        }
    }
};

@kubernetes:Deployment {
    name:"foodstore",
    replicas:3,
    labels:{"location":"SL","city":"COLOMBO"}
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
        response.setStringPayload("Pizza menu \n");
        _ = outboundEP -> respond(response);
    }
}