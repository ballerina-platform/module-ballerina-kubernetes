## Module Overview

This module offers an annotation based Knative extension implementation for Ballerina.

- For information on the operations, which you can perform with this module, see [Objects](/learn/api-docs/ballerina/knative/index.html#objects). 
- For examples on the usage of the operations, see the [Knative Deployment Example](/learn/by-example/knative-deployment.html).

### Annotation Usage Sample:

```ballerina
import ballerina/http;
import ballerina/log;
import ballerina/knative;

@knative:Service {
    name:"hello"
}
listener http:Listener helloEP = new(9090);

@http:ServiceConfig {
    basePath: "/helloWorld"
}
service helloWorld on helloEP {
    resource function sayHello(http:Caller caller, http:Request request) {
        var responseResult = caller->respond("Hello, World from service helloWorld ! ");
        if (responseResult is error) {
            log:printError("error responding", responseResult);
        }
    }
}
```
