import ballerina/http;
import ballerina/log;
import ballerinax/kubernetes;
import ballerinax/openshift;

//Add `@kubernetes:Service` to a listener endpoint to expose the endpoint as Kubernetes Service.
@kubernetes:Service {}
//Add `@openshift:Route` to expose Kubernetes Service through an OpenShift Route.
@openshift:Route {
    host: "www.oc-example.com"
}
listener http:Listener helloEP = new(9090);

//Add `@kubernetes:Deployment` annotation to a Ballerina service to generate Kuberenetes Deployment for a Ballerina module.
@kubernetes:Deployment {
    //OpenShift project name.
    namespace: "hello-api",
    //IP and port of the OpenShift docker registry. Use `minishift openshift registry` to find the docker registry if you are using minishift.
    registry: "172.30.1.1:5000",
    //Generate Docker image with name `172.30.1.1:5000/hello-api/hello-service:v1.0`.
    image: "hello-service:v1.0",
    //Disable building image by default so that OpenShift BuildConfig can build it.
    buildImage: false,
    //Generate the OpenShift BuildConfig for building the docker image.
    buildExtension: openshift:BUILD_EXTENSION_OPENSHIFT
}
@http:ServiceConfig {
    basePath: "/hello"
}
service hello on helloEP {
    @http:ResourceConfig {
        methods: ["GET"],
        path: "/{user}"
    }
    resource function sayHello(http:Caller caller, http:Request request, string user) {
        string payload = string `Hello ${untaint user}!`;
        var responseResult = caller->respond(payload);
        if (responseResult is error) {
            error err = responseResult;
            log:printError("Error sending response", err = err);
        }
    }
}
