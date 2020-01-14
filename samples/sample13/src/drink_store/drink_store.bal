import ballerina/http;
import ballerina/log;
import ballerina/io;
import ballerina/math;
import ballerina/kubernetes;

@kubernetes:Service {
    serviceType: "NodePort"
}
@kubernetes:Ingress {
    hostname: "drinkstore.com"
}
listener http:Listener drinkStoreEP = new(9091, config = {
    secureSocket: {
        keyStore: {
            path: "src/drink_store/resources/ballerinaKeystore.p12",
            password: "ballerina"
        }
    }
});

http:Client hotDrinkBackend = new("http://hotdrink-backend:9090");

http:Client coolDrinkBackend = new("http://cooldrink-backend:9090");

http:Client weatherEP = new("http://api.openweathermap.org");

@kubernetes:Deployment {
    livenessProbe: true,
    dependsOn: ["cool_drink:coolDrinkEP", "hot_drink:hotDrinkEP"]
}
@http:ServiceConfig {
    basePath: "/store"
}
@kubernetes:HPA {}
service DrinkStoreAPI on drinkStoreEP {
    @http:ResourceConfig {
        methods: ["GET"],
        path: "/hotDrink"
    }
    resource function getHotDrinkMenu(http:Caller outboundEP, http:Request req) {
        var response = hotDrinkBackend->get("/hotDrink/menu");
        if (response is http:Response) {
            var msg = response.getJsonPayload();
            if (msg is json) {
                log:printInfo(msg.toString());
                response.setJsonPayload(<@untainted> getHotDrinkPrice(msg), contentType = "application/json");
            } else {
                log:printError("Invalid response received from hot drink server", msg);
            }
            log:printInfo("GET request:");
            var responseResult = outboundEP->respond(response);
            if (responseResult is error) {
                log:printError("error responding back to client.", responseResult);
            }
        } else {
            log:printError("Error when getting hot drink menu", response);
        }
    }

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/coolDrink"
    }
    resource function getCoolDrinkMenu(http:Caller outboundEP, http:Request req) {
        var response = coolDrinkBackend->get("/coolDrink/menu");
        if (response is http:Response) {
            var msg = response.getJsonPayload();
            if (msg is json) {
                log:printInfo(msg.toString());
                response.setJsonPayload(<@untainted> getCoolDrinkPrice(msg), contentType = "application/json");
            } else {
                log:printError("Invalid response received from cool drink server", msg);
            }
            log:printInfo("GET request: ");
            var responseResult = outboundEP->respond(response);
            if (responseResult is error) {
                log:printError("error responding back to client.", responseResult);
            }
        } else {
            log:printError("Error getting cool drink menu", response);
        }
    }

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/getTempreature"
    }
    resource function getTempreature(http:Caller outboundEP, http:Request req) {
        http:Response response = new;
        float celciusValue = roundFloat(getTempreatureInCelcius(),2);
        if (celciusValue > 15) {
            //response.setTextPayload("Tempreture in San Francisco: " + celciusValue + " clecius. Sunny."+"\n");
            response.setTextPayload(string`Tempreture in San Francisco: ${celciusValue} clecius. Sunny.`);
         } else {
             response.setTextPayload(string `Tempreture in San Francisco: ${celciusValue} clecius. Cold.`);
         }
        var responseResult = outboundEP->respond(response);
        if (responseResult is error) {
            log:printError("error responding back to client.", responseResult);
        }
    }
}

function getCoolDrinkPrice(json payload) returns json {
    json[] items = <json[]>payload;
    float celciusValue = getTempreatureInCelcius();
    foreach json item in items {
        float|error result = <float>item.price;
        if (result is float) {
            float priceVariation = roundFloat(result * (celciusValue / 100), 2);
            if (item is map<json>) {
                if (celciusValue > 15) {
                    // Increase Cooldrink price on hot days.
                    item["price"] = roundFloat((result + priceVariation), 2);
                    item ["diff"] = string`+ ${priceVariation}`;
                } else {
                    item["price"] = roundFloat((result + priceVariation), 2);
                    item ["diff"] = string`- ${priceVariation}`;
                    // Decrese Cooldrink price on cold days.
                }
            }
        }else {
            log:printError("Error while reading values.", result);
        }
    }
    io:println(items);
    return items;
}

function getHotDrinkPrice(json payload) returns json {
    json[] items = <json[]>payload;
    float celciusValue = getTempreatureInCelcius();
    foreach json item in items {
        float|error result = <float>item.price;
        if (result is float) {
            float priceVariation = roundFloat(result * (celciusValue / 100), 2);
            if (item is map<json>) {
                if (celciusValue < 15) {
                    // Increase Hot drink price on cold days.
                    item["price"] = roundFloat((result + priceVariation), 2);
                    item ["diff"] = string`+ ${priceVariation}`;
                } else {
                    // Decrese Hot drink price on hot days.
                    item["price"] = roundFloat((result + priceVariation), 2);
                    item ["diff"] = string`- ${priceVariation}`;
                    //item = {
                    //    price : roundFloat((result - priceVariation), 2),
                    //    diff : "-" + priceVariation
                    //}
                }
            }
        } else {
            log:printError("Error while reading values.", result);
        }
    }
    io:println(items);
    return items;
}

function roundFloat(float value, float decimalPlaces) returns float {
    float factor = math:pow(10, decimalPlaces);
    return  <float> math:round(value * factor) / factor;
}

function getTempreatureInCelcius() returns (float) {
    float celciusValue = 0.0;
    var response = weatherEP->get("/data/2.5/weather?q=San+Francisco,US&appid=133ae5bf5b9dffc18f68b93f9f7e5935");
    if (response is http:Response) {
        var msg = response.getJsonPayload();
        if (msg is json) {
            io:println(msg.main.temp);
            float|error result = <float>msg.main.temp;
            if (result is float) {
                celciusValue = result - 273.15;
            } else {
                log:printError("Error while reading values.", result);
            }
        }
    } else {
        log:printError("Error occurred when getting weather data.", response);
    }
    return celciusValue;
}
