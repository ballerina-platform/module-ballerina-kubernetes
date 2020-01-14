import ballerina/http;
import ballerina/log;
import ballerina/config;
import ballerinax/java.jdbc;
import ballerina/kubernetes;

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
    conf: "src/cool_drink/ballerina.conf"
}
@kubernetes:Deployment {
    replicas: 2,
    livenessProbe: true,
    copyFiles: [{
        target: "/ballerina/runtime/bre/lib",
        sourceFile: "./libs/mysql-connector-java-8.0.11.jar"
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
            var jsonConversionRet = json.constructFrom(selectRet);
            if (jsonConversionRet is json) {
                response.setJsonPayload(<@untainted> jsonConversionRet);
            } else {
                log:printError("Error in table to json conversion", jsonConversionRet);
                response.setTextPayload("Error in table to json conversion");
            }
        } else {
            log:printError("Retrieving data from coolDrink table failed", selectRet);
            response.setTextPayload("Error in reading results");
        }

        var responseResult = outboundEP->respond(response);
        if (responseResult is error) {
            log:printError("error responding back to client.", responseResult);
        }
    }
}

type CoolDrink record {|
    int id;
    string name;
    string description;
    float price;
|};
