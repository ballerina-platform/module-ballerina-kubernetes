import ballerina/http;
import ballerina/log;
import ballerinax/kubernetes;

@kubernetes:Service {
    name: "car-rental"
}
listener http:Listener carEP = new(6060);

// Available car types
const string AC = "Air Conditioned";
const string NORMAL = "Normal";

// Car rental service to rent cars
@http:ServiceConfig {
    basePath: "/car"
}
service carRentalService on carEP {

    // Resource to rent a car
    @http:ResourceConfig {
        methods: ["POST"],
        path: "/rent",
        consumes: ["application/json"],
        produces: ["application/json"]
    }
    resource function rentCar(http:Caller caller, http:Request request) {
        http:Response response = new;
        json reqPayload = {};

        var payload = request.getJsonPayload();
        // Try parsing the JSON payload from the request
        if (payload is json) {
            // Valid JSON payload
            reqPayload = payload;
        } else {
            // NOT a valid JSON payload
            response.statusCode = 400;
            response.setJsonPayload({
                Message: "Invalid payload - Not a valid JSON payload"
            });
            var result = caller->respond(response);
            handleError(result);
            return;
        }

        json name = reqPayload.Name;
        json arrivalDate = reqPayload.ArrivalDate;
        json departDate = reqPayload.DepartureDate;
        json preferredType = reqPayload.Preference;

        // If payload parsing fails, send a "Bad Request" message as the response
        if (name == () || arrivalDate == () || departDate == () || preferredType == ()) {
            response.statusCode = 400;
            response.setJsonPayload({
                Message:"Bad Request - Invalid Payload"
            });
            var result = caller->respond(response);
            handleError(result);
            return;
        }

        // Mock logic
        // If request is for an available car type, send a rental successful status
        string preferredTypeStr = preferredType.toString();
        if (preferredTypeStr.equalsIgnoreCase(AC) || preferredTypeStr.equalsIgnoreCase(NORMAL)) {
            response.setJsonPayload({
                Status: "Success"
            });
        }
        else {
            // If request is not for an available car type, send a rental failure status
            response.setJsonPayload({
                Status: "Failed"
            });
        }
        // Send the response
        var result = caller->respond(response);
        handleError(result);
    }
}

function handleError(error? result) {
    if (result is error) {
        log:printError(result.reason(), result);
    }
}
