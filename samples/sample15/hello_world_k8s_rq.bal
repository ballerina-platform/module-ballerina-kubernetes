import ballerina/http;
import ballerina/log;
import ballerina/kubernetes;

@kubernetes:Deployment {
    livenessProbe: true,
    namespace: "ballerina",
    replicas: 2
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
        var responseResult = outboundEP->respond(response);
        if (responseResult is error) {
            log:printError("error responding back to client.", responseResult);
        }
    }
}
