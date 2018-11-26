import ballerina/http;
import ballerina/io;
import ballerina/log;
import ballerina/config;
import ballerinax/jdbc;
import ballerinax/kubernetes;

@kubernetes:Service {
    name: "cooldrink-backend"
}
listener http:Listener coolDrinkEP = new(9090);

endpoint jdbc:Client coolDrinkDB {
    url: "jdbc:mysql://cooldrink-mysql.mysql:3306/cooldrinkdb",
    username: config:getAsString("db.username"),
    password: config:getAsString("db.password"),
    poolOptions: { maximumPoolSize: 5 },
    dbOptions: { useSSL: false }
};

@kubernetes:ConfigMap {
    ballerinaConf: "./cool_drink/ballerina.conf"
}
@kubernetes:Deployment {
    replicas: 2,
    enableLiveness: true,
    singleYAML: true,
    copyFiles: [{ target: "/ballerina/runtime/bre/lib",
        source: "./resource/lib/mysql-connector-java-8.0.11.jar" }]
}
@http:ServiceConfig {
    basePath: "/coolDrink"
}
service cooldrinkAPI on coolDrinkEP {
    @http:ResourceConfig {
        methods: ["GET"],
        path: "/menu"
    }
    resource function getcooldrinkMenu(http:Caller outboundEP, http:Request req) {
        http:Response response = new;

        var selectRet = coolDrinkDB->select("SELECT * FROM cooldrink", ());
        table dt;
        match selectRet {
            table tableReturned => dt = tableReturned;
            error e => io:println("Retrieving data from coolDrink table failed: "
                    + e.message);
        }

        var jsonConversionRet = <json>dt;
        match jsonConversionRet {
            json jsonRes => {
                response.setJsonPayload(untaint jsonRes);
            }
            error e => {
                io:println("Error in table to json conversion");
                response.setTextPayload("Error in table to json conversion");
            }
        }
        _ = outboundEP->respond(response);
    }
}
