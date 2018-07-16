import ballerina/http;
import ballerinax/kubernetes;
import ballerina/io;
import ballerina/log;

@kubernetes:Service {
    name: "pizza-backend"
}
endpoint http:Listener pizzaEP {
    port: 9090
};

@kubernetes:ConfigMap {
    configMaps: [
        {
            mountPath: "/home/ballerina/data/pizza",
            data: ["./pizza/menu.json"]
        }
    ]
}
@kubernetes:Deployment {
    replicas: 3,
    enableLiveness: true
}
@http:ServiceConfig {
    basePath: "/pizza"
}
service<http:Service> PizzaAPI bind pizzaEP {
    @http:ResourceConfig {
        methods: ["GET"],
        path: "/menu"
    }
    getPizzaMenu(endpoint outboundEP, http:Request req) {
        http:Response response = new;
        string filePath = "./data/pizza/menu.json";
        json content = read(filePath);
        response.setJsonPayload(untaint content);
        _ = outboundEP->respond(response);
    }
}

function close(io:CharacterChannel characterChannel) {
    characterChannel.close() but {
        error e =>
        log:printError("Error occurred while closing character stream",
            err = e)
    };
}

function read(string path) returns json {
    io:ByteChannel byteChannel = io:openFile(path, io:READ);
    io:CharacterChannel ch = new io:CharacterChannel(byteChannel, "UTF8");

    match ch.readJson() {
        json result => {
            close(ch);
            return result;
        }
        error err => {
            close(ch);
            throw err;
        }
    }
}