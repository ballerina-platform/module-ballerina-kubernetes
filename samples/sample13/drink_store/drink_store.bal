import ballerina/http;
import ballerinax/kubernetes;
import ballerina/log;
import ballerina/io;
import ballerina/math;

@kubernetes:Service {
    serviceType: "NodePort"
}
@kubernetes:Ingress {
    hostname: "drinkstore.com"
}
endpoint http:Listener drinkStoreEP {
    port: 9091,
    secureSocket: {
        keyStore: {
            path: "${ballerina.home}/bre/security/ballerinaKeystore.p12",
            password: "ballerina"
        }
    }
};

endpoint http:Client hotDrinkBackend {
    url: "http://hotdrink-backend:9090"
};

endpoint http:Client coolDrinkBackend {
    url: "http://cooldrink-backend:9090"
};

endpoint http:Client weatherEP {
    url: "http://api.openweathermap.org"
};

@kubernetes:Deployment {
    enableLiveness: true,
    dependsOn: ["cool_drink:coolDrinkEP", "hot_drink:hotDrinkEP"],
    singleYAML: true
}
@http:ServiceConfig {
    basePath: "/store"
}
@kubernetes:HPA {}
service<http:Service> DrinkStoreAPI bind drinkStoreEP {
    @http:ResourceConfig {
        methods: ["GET"],
        path: "/hotDrink"
    }
    getHotDrinkMenu(endpoint outboundEP, http:Request req) {
        var response = hotDrinkBackend->get("/hotDrink/menu");
        match response {
            http:Response resp => {
                var msg = resp.getJsonPayload();
                match msg {
                    json jsonPayload => {
                        log:printInfo(jsonPayload.toString());
                        resp.setJsonPayload(untaint getHotDrinkPrice(jsonPayload), contentType = "application/json");
                    }
                    error err => {
                        log:printError(err.message, err = err);
                    }
                }
                log:printInfo("GET request:");
                _ = outboundEP->respond(resp);
            }
            error err => {
                log:printError(err.message, err = err);
            }
        }
    }

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/coolDrink"
    }
    getCoolDrinkMenu(endpoint outboundEP, http:Request req) {
        var response = coolDrinkBackend->get("/coolDrink/menu");
        match response {
            http:Response resp => {
                var msg = resp.getJsonPayload();
                match msg {
                    json jsonPayload => {
                        log:printInfo(jsonPayload.toString());
                        resp.setJsonPayload(untaint getCoolDrinkPrice(jsonPayload), contentType = "application/json");
                    }
                    error err => {
                        log:printError(err.message, err = err);
                    }
                }
                log:printInfo("GET request: ");
                _ = outboundEP->respond(resp);
            }
            error err => {
                log:printError(err.message, err = err);
            }
        }
    }

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/getTempreature"
    }
    getTempreature(endpoint outboundEP, http:Request req) {
        http:Response response = new;
        float celciusValue = roundFloat(getTempreatureInCelcius(),2);
        if(celciusValue>15){
            response.setTextPayload("Tempreture in San Francisco: " +celciusValue+" clecius. Sunny."+"\n");
         }else{
             response.setTextPayload("Tempreture in San Francisco: " +celciusValue+" clecius. Cold."+"\n");
         }
        _ = outboundEP->respond(response);
    }
}

function getCoolDrinkPrice(json payload) returns (json){
    json[] items = check <json[]>payload;
    float celciusValue = getTempreatureInCelcius();
    foreach item in items {
        float|error result = <float>
        item.price;
        match result {
            float value => {
                float priceVariation = roundFloat(value*(celciusValue/100),2);
                if(celciusValue > 15){
                    // Increase Cooldrink price on hot days.
                    item.price = roundFloat((value + priceVariation), 2);
                    item.diff = "+" + priceVariation;
                }else {
                    // Decrese Cooldrink price on cold days.
                    item.price = roundFloat((value - priceVariation), 2);
                    item.diff = "-" + priceVariation;
                }
            }
            error e => {
                io:println("Error while reading value: " + e.message);
            }
        }

    }
    io:println(items);
    return items;
}

function getHotDrinkPrice(json payload) returns (json){
    json[] items = check <json[]>payload;
    float celciusValue = getTempreatureInCelcius();
    foreach item in items {
        float|error result = <float>
        item.price;
        match result {
            float value => {
                float priceVariation = roundFloat(value*(celciusValue/100), 2);
                if(celciusValue < 15){
                    // Increase Hot drink price on cold days.
                    item.price = roundFloat((value + priceVariation), 2);
                    item.diff = "+" + priceVariation;
                }else {
                    // Decrese Hot drink price on hot days.
                    item.price = roundFloat((value - priceVariation), 2);
                    item.diff = "-" + priceVariation;
                }
            }
            error e => {
                io:println("Error while reading value: " + e.message);
            }
        }

    }
    io:println(items);
    return items;
}

function roundFloat(float value, float decimalPlaces) returns float {
    float factor = math:pow(10, decimalPlaces);
    return  <float> math:round(value * factor)/factor;
}

function getTempreatureInCelcius() returns (float) {
    float celciusValue = 0.0;
    var
    response = weatherEP->get("/data/2.5/weather?q=San+Francisco,US&appid=133ae5bf5b9dffc18f68b93f9f7e5935");
    match response {
        http:Response resp => {
            var
            msg = resp.getJsonPayload();
            match msg {
                json jsonPayload => {
                    io:println(
                        jsonPayload.main.temp);
                    float|error result = <float>
                    jsonPayload.main.temp;
                    match result {
                        float value => {
                            celciusValue = value-273.15;
                        }

                        error e => {
                            io:println("Error while reading value: " +
                                    e.message);
                        }
                    }
                }
                error err => {
                    log:printError(
                        err.message, err = err);
                }
            }
        }
        error err => {
            log:printError(
                err.message, err = err);
        }
    }
    return celciusValue;
}
