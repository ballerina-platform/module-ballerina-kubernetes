import ballerina.net.http;
import ballerina.kubernetes;


@kubernetes:deployment{
    liveness:"enable",
    push:true,
    image:"index.docker.io/<user_name>/gce-sample:1.0",
    username:"<user_name>",
    password:"<password>"
}
@kubernetes:svc{}
@kubernetes:hpa{}
@kubernetes:ingress{
    hostname:"abc.com"
}
@http:configuration {
    basePath:"/helloWorld"
}
service<http> helloWorld {
    resource sayHello (http:Connection conn, http:InRequest req) {
        http:OutResponse res = {};
        res.setStringPayload("Hello, World from service helloWorld! ");
        _ = conn.respond(res);
    }
}
