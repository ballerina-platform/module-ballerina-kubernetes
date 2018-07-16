import ballerina/http;
import ballerina/io;
import ballerina/log;
import ballerinax/kubernetes;

@kubernetes:Service {
    name: "buger-backend"
}
endpoint http:Listener burgerEP {
    port: 9090
};


@kubernetes:ConfigMap {
    configMaps: [
        {
            mountPath: "/home/ballerina/data/burger",
            data: ["./burger/menu.json"]
        }
    ]
}
@kubernetes:Deployment {}
@http:ServiceConfig {
    basePath: "/burger"
}
service<http:Service> BurgerAPI bind burgerEP {
    @http:ResourceConfig {
        methods: ["GET"],
        path: "/menu"
    }
    getBurgerMenu(endpoint outboundEP, http:Request req) {
        http:Response response = new;
        string filePath = "./data/burger/menu.json";
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