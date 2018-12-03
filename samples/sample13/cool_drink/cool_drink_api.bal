import ballerina/http;
import ballerina/log;
import ballerina/config;
import ballerinax/jdbc;
import ballerinax/kubernetes;

@kubernetes:Service {
    name: "cooldrink-backend"
}
listener http:Listener coolDrinkEP = new(9090);

jdbc:Client coolDrinkDB = new({
    url: "jdbc:mysql://cooldrink-mysql.mysql:3306/cooldrinkdb",
    username: config:getAsString("db.username"),
    password: config:getAsString("db.password"),
    poolOptions: { maximumPoolSize: 5 },
    dbOptions: { useSSL: false }
});

@kubernetes:ConfigMap {
    ballerinaConf: "./cool_drink/ballerina.conf"
}
@kubernetes:Deployment {
    replicas: 2,
    enableLiveness: true,
    singleYAML: true,
    copyFiles: [{
        target: "/ballerina/runtime/bre/lib",
        source: "./resource/lib/mysql-connector-java-8.0.11.jar"
    }]
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

        var selectRet = coolDrinkDB->select("SELECT * FROM cooldrink", CoolDrink);
        if (selectRet is table<CoolDrink>) {
            var jsonConversionRet = json.create(selectRet);
            if (jsonConversionRet is json) {
                response.setJsonPayload(untaint jsonConversionRet);
            } else if (jsonConversionRet is error) {
                log:printError("Error in table to json conversion", err = jsonConversionRet);
                response.setTextPayload("Error in table to json conversion");
            }
        } else if (selectRet is error) {
            log:printError("Retrieving data from coolDrink table failed", err = selectRet);
            response.setTextPayload("Error in reading results");
        }

        _ = outboundEP->respond(response);
    }
}

type CoolDrink record {
    int id;
    string name;
    string description;
    float price;
    !...
};