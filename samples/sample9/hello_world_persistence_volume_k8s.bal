import ballerina/http;
import ballerina/log;
import ballerina/kubernetes;

@kubernetes:Service {}
@kubernetes:Ingress {
    hostname: "abc.com"
}
listener http:Listener helloWorldEP = new(9090, {
    secureSocket: {
        keyStore: {
            path: "./security/ballerinaKeystore.p12",
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
        var responseResult = outboundEP->respond(response);
        if (responseResult is error) {
            log:printError("error responding back to client.", responseResult);
        }
    }
}
