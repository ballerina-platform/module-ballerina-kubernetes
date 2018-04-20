import ballerina/http;
import ballerinax/kubernetes;

@kubernetes:Service {}
@kubernetes:Ingress {
    hostname:"abc.com"
}
endpoint http:Listener helloWorldEP {
    port:9090,
    secureSocket:{
        keyStore:{
            path:"${ballerina.home}/bre/security/ballerinaKeystore.p12",
            password:"ballerina"
        }
    }
};

@kubernetes:PersistentVolumeClaim {
    volumeClaims:[
        {name:"local-pv-2", mountPath:"/home/ballerina/tmp", readOnly:false, accessMode:"ReadWriteOnce", volumeClaimSize
        :"1Gi"}
    ]
}
@http:ServiceConfig {
    basePath:"/helloWorld"
}
service<http:Service> helloWorld bind helloWorldEP {
    @http:ResourceConfig {
        methods:["GET"],
        path:"/sayHello"
    }
    sayHello(endpoint outboundEP, http:Request request) {
        http:Response response = new;
        response.setStringPayload("Hello World\n");
        _ = outboundEP->respond(response);
    }
}
