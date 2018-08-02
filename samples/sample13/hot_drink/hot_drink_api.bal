import ballerina/http;
import ballerina/io;
import ballerina/log;
import ballerina/jdbc;
import ballerina/config;
import ballerinax/kubernetes;

@kubernetes:Service {
    name: "hotdrink-backend"
}
endpoint http:Listener hotDrinkEP {
    port: 9090
};


endpoint jdbc:Client hotdrinkDB {
    url: "jdbc:mysql://hotdrink-mysql.mysql:3306/hotdrinkdb",
    username: config:getAsString("db.username"),
    password: config:getAsString("db.password"),
    poolOptions: { maximumPoolSize: 5 },
    dbOptions: { useSSL: false }
};


@kubernetes:ConfigMap {
    ballerinaConf: "./hot_drink/ballerina.conf"
}
@kubernetes:Deployment {
    singleYAML: true,
    copyFiles: [{ target: "/ballerina/runtime/bre/lib",
        source: "./resource/lib/mysql-connector-java-8.0.11.jar" }]
}
@http:ServiceConfig {
    basePath: "/hotDrink"
}
service<http:Service> HotDrinksAPI bind hotDrinkEP {
    @http:ResourceConfig {
        methods: ["GET"],
        path: "/menu"
    }
    gethotdrinkMenu(endpoint outboundEP, http:Request req) {
        http:Response response = new;

        var selectRet = hotdrinkDB->select("SELECT * FROM hotdrink", ());
        table dt;
        match selectRet {
            table tableReturned => dt = tableReturned;
            error e => io:println("Retrieving data from hotdrink table failed: "
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
