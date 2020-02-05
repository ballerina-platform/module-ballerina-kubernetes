import ballerina/http;
import ballerina/kubernetes;
import ballerina/log;

@kubernetes:Service {}
@kubernetes:Ingress {
    hostname: "burger.com",
    path: "/(.*)",
    targetPath: "/burger/$1"
}
listener http:Listener burgerEP = new(9096, {
    secureSocket: {
        keyStore: {
            path: "src/burger/resources/ballerinaKeystore.p12",
            password: "ballerina"
        }
    }
});


@kubernetes:Deployment {}
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
