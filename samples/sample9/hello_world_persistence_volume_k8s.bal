import ballerina/http;
import ballerinax/kubernetes;

@kubernetes:Service {}
@kubernetes:Ingress {
    hostname: "abc.com"
}
listener http:Listener helloWorldEP = new(9090, config = {
    secureSocket:{
        keyStore:{
            path: "${ballerina.home}/bre/security/ballerinaKeystore.p12",
            password: "ballerina"
        }
    }
});

@kubernetes:PersistentVolumeClaim {
    volumeClaims: [
        {
            name: "local-pv-2",
            mountPath: "/home/ballerina/tmp",
            readOnly: false,
            accessMode: "ReadWriteOnce",
            volumeClaimSize: "1Gi"
        }
    ]
}

@kubernetes:Deployment {}
@http:ServiceConfig {
    basePath: "/helloWorld"
}
service helloWorld on helloWorldEP {
    @http:ResourceConfig {
        methods: ["GET"],
        path: "/sayHello"
    }
    resource function sayHello(http:Caller outboundEP, http:Request request) {
        http:Response response = new;
        response.setTextPayload("Hello World\n");
        _ = outboundEP->respond(response);
    }
}
