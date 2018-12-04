import ballerina/http;
import ballerina/log;
import ballerina/io;
import ballerina/math;
import ballerinax/kubernetes;

@kubernetes:Service {
    serviceType: "NodePort"
}
@kubernetes:Ingress {
    hostname: "drinkstore.com"
}
listener http:Listener drinkStoreEP = new(9091, config = {
    secureSocket: {
        keyStore: {
            path: "${ballerina.home}/bre/security/ballerinaKeystore.p12",
            password: "ballerina"
        }
    }
});

http:Client hotDrinkBackend = new("http://hotdrink-backend:9090");

http:Client coolDrinkBackend = new("http://cooldrink-backend:9090");

http:Client weatherEP = new("http://api.openweathermap.org");

@kubernetes:Deployment {
    enableLiveness: true,
    dependsOn: ["cool_drink:coolDrinkEP", "hot_drink:hotDrinkEP"],
    singleYAML: true
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
                response.setJsonPayload(untaint getHotDrinkPrice(msg), contentType = "application/json");
            } else if (msg is error) {
                log:printError("Invalid response received from hot drink server", err = msg);
            }
            log:printInfo("GET request:");
            _ = outboundEP->respond(response);
        } else if (response is error) {
            log:printError("Error when getting hot drink menu", err = response);
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
                response.setJsonPayload(untaint getCoolDrinkPrice(msg), contentType = "application/json");
            } else if (msg is error) {
                log:printError("Invalid response received from cool drink server", err = msg);
            }
            log:printInfo("GET request: ");
            _ = outboundEP->respond(response);
        } else if (response is error) {
            log:printError("Error getting cool drink menu", err = response);
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
            response.setTextPayload("Tempreture in San Francisco: " + celciusValue + " clecius. Sunny."+"\n");
         } else {
             response.setTextPayload("Tempreture in San Francisco: " + celciusValue + " clecius. Cold."+"\n");
         }
        _ = outboundEP->respond(response);
    }
}

function getCoolDrinkPrice(json payload) returns (json){
    json[] items = <json[]>payload;
    float celciusValue = getTempreatureInCelcius();
    foreach item in items {
        float|error result = <float>item.price;
        if (result is float) {
            float priceVariation = roundFloat(result * (celciusValue / 100), 2);
            if (celciusValue > 15) {
                // Increase Cooldrink price on hot days.
                item.price = roundFloat((result + priceVariation), 2);
                item.diff = "+" + priceVariation;
            } else {
                // Decrese Cooldrink price on cold days.
                item.price = roundFloat((result - priceVariation), 2);
                item.diff = "-" + priceVariation;
            }
        } else if (result is error) {
            log:printError("Error while reading values.", err = result);
        }
    }
    io:println(items);
    return items;
}

function getHotDrinkPrice(json payload) returns (json){
    json[] items = <json[]>payload;
    float celciusValue = getTempreatureInCelcius();
    foreach item in items {
        float|error result = <float>item.price;
        if (result is float) {
            float priceVariation = roundFloat(result * (celciusValue / 100), 2);
            if (celciusValue < 15) {
                // Increase Hot drink price on cold days.
                item.price = roundFloat((result + priceVariation), 2);
                item.diff = "+" + priceVariation;
            } else {
                // Decrese Hot drink price on hot days.
                item.price = roundFloat((result - priceVariation), 2);
                item.diff = "-" + priceVariation;
            }
        } else if (result is error) {
            log:printError("Error while reading values.", err = result);
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
            } else if (result is error) {
                log:printError("Error while reading values.", err = result);
            }
        }
    } else if (response is error) {
        log:printError("Error occurred when getting weather data.", err = response);
    }
    return celciusValue;
}
