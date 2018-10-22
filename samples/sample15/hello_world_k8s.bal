import ballerina/http;
import ballerinax/kubernetes;

@kubernetes:Deployment {
    enableLiveness: true,
    namespace: "ballerina",
    replicas: 2 // Needs to pods minimum
}
@kubernetes:Ingress {
    hostname: "abc.com"
}
@kubernetes:Service {name: "hello"}
endpoint http:Listener helloEP {
    port: 9090
};

@kubernetes:ResourceQuota {
    resourceQuotas: [
        {
            name: "pod-limit",
            hard: {
                "pods": "2" // Maximum of 2 pods
            }
        }
    ]
}
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
