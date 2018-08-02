import ballerina/http;
import ballerinax/kubernetes;

@kubernetes:Deployment {
    enableLiveness:true
}
@kubernetes:Ingress {
    hostname:"abc.com"
}
@kubernetes:Service {
    name:"hello",
    serviceType:"NodePort"
}
endpoint http:Listener helloEP {
    port:9090,
    secureSocket:{
        keyStore:{
            path:"${ballerina.home}/bre/security/ballerinaKeystore.p12",
            password:"ballerina"
        }
    }};


@http:ServiceConfig {
    basePath:"/helloWorld"
}
service<http:Service> helloWorld bind helloEP {
    sayHello(endpoint outboundEP, http:Request request) {
        http:Response response = new;
        response.setTextPayload("Hello, World from service helloWorld ! \n");
        _ = outboundEP->respond(response);
    }
}
