import ballerina/http;
import ballerinax/kubernetes;

@kubernetes:IstioGateway {
    name: "my-gateway",
    namespace: "ballerina",
    annotations: {
        anno1: "anno1Val",
        anno2: "anno2Val"
    },
    labels: {
        label1: "label1",
        label2: "label2"
    },
    selector: {
        app: "my-gateway-controller"
    },
    servers: [
        {
            port: {
                number: 80,
                name: "http",
                protocol: "HTTP"
            },
            hosts: [
                "uk.bookinfo.com",
                "eu.bookinfo.com"
            ],
            tls: {
                httpsRedirect: true
            }
        }
    ]
}
@kubernetes:Deployment {
    name: "all_fields",
    image: "pizza-shop:latest"
}
@kubernetes:Ingress {
    hostname: "abc.com"
}
@kubernetes:Service {name: "hello"}
endpoint http:Listener helloEP {
    port: 9090
};


@http:ServiceConfig {
    basePath: "/helloWorld"
}
service<http:Service> helloWorld bind helloEP {
    sayHello(endpoint outboundEP, http:Request request) {
        http:Response response = new;
        response.setTextPayload("Hello, World from service helloWorld ! \n");
        _ = outboundEP->respond(response);
    }
}