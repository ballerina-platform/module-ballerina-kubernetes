## Module Overview

This module offers an annotation based Kubernetes extension implementation for Ballerina. 

- For information on the operations, which you can perform with this module, see [Objects](https://ballerina.io/learn/api-docs/ballerina/kubernetes/index.html#objects). 
- For more information on the deployment, see [Kubernetes Deployment Guide](https://ballerina.io/learn/deployment/kubernetes/).
- For examples on the usage of the operations, see the [Kubernetes Deployment Example](https://ballerina.io/learn/by-example/kubernetes-deployment.html).

### Annotation Usage Sample:

```ballerina
import ballerina/http;
import ballerina/log;
import ballerinax/kubernetes;

@kubernetes:Ingress{
    hostname: "abc.com"
}
@kubernetes:Service {
    name:"hello"
}
listener http:Listener helloEP = new(9090);

@kubernetes:Deployment {
    livenessProbe: true
}
@http:ServiceConfig {
    basePath: "/helloWorld"
}
service helloWorld on helloEP {
    resource function sayHello(http:Caller caller, http:Request request) {
        http:Response response = new;
        response.setTextPayload("Hello, World from service helloWorld ! ");
        var responseResult = caller->respond(response);
        if (responseResult is error) {
            log:printError("error responding back to client.", err = responseResult);
        }
    }
}
```
