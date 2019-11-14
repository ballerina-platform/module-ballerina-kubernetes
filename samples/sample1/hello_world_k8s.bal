import ballerina/http;
import ballerina/log;
import ballerinax/Knative;




@Knative:Service{
    namespace: "default"
}

@Knative:ConfigMap {
    conf: "./conf/micro-gw.conf"
}


service helloWorld on new http:Listener(8080) {
    resource function sayHello(http:Caller outboundEP, http:Request request) {
        http:Response response = new;
        response.setTextPayload("Hello, World from service helloWorld ! \n");
        var responseResult = outboundEP->respond(response);
        if (responseResult is error) {
            log:printError("error responding back to client.", responseResult);
        }
    }
}
