import ballerina/http;
import ballerinax/kubernetes;

@kubernetes:Deployment {
    enableLiveness: true,
    namespace: "ballerina",
    replicas: 2,
    singleYAML: false
}
@kubernetes:Ingress {
    hostname: "abc.com"
}
@kubernetes:Service { name: "hello" }
listener http:Listener helloEP = new(9090);

@kubernetes:ResourceQuota {
    resourceQuotas: [
        {
            name: "pod-limit",
            hard: {
                "pods": "2",
                "requests.cpu": "1",
                "requests.memory": "1Gi",
                "limits.cpu": "2",
                "limits.memory": "2Gi"

            }
        }
    ]
}
@http:ServiceConfig {
    basePath: "/helloWorld"
}
service helloWorld on helloEP {
    resource function sayHello(http:Caller outboundEP, http:Request request) {
        http:Response response = new;
        response.setTextPayload("Hello, World from service helloWorld ! \n");
        _ = outboundEP->respond(response);
    }
}
