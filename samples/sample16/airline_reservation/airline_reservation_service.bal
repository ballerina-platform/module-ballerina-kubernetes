import ballerina/http;
import ballerina/log;
import ballerinax/kubernetes;

// Service endpoint
@kubernetes:Service {
    name: "airline-reservation"
}
listener http:Listener airlineEP = new(8080);

// Available flight classes
const string ECONOMY = "Economy";
const string BUSINESS = "Business";
const string FIRST = "First";

// Airline reservation service to reserve airline tickets
@http:ServiceConfig {
    basePath: "/airline"
}
service airlineReservationService on airlineEP {

    // Resource to reserve a ticket
    @http:ResourceConfig {
        methods: ["POST"],
        path: "/reserve",
        consumes: ["application/json"],
        produces: ["application/json"]
    }
    resource function reserveTicket(http:Caller caller, http:Request request) {
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
        json preferredClass = reqPayload.Preference;

        // If payload parsing fails, send a "Bad Request" message as the response
        if (name == () || arrivalDate == () || departDate == () || preferredClass == ()) {
            response.statusCode = 400;
            response.setJsonPayload({
                Message: "Bad Request - Invalid Payload"
            });
            var result = caller->respond(response);
            handleError(result);
            return;
        }

        // Mock logic
        // If request is for an available flight class, send a reservation successful status
        string preferredClassStr = preferredClass.toString();
        if (preferredClassStr.equalsIgnoreCase(ECONOMY) || preferredClassStr.equalsIgnoreCase(BUSINESS) ||
            preferredClassStr.equalsIgnoreCase(FIRST)) {
            response.setJsonPayload({
                Status: "Success"
            });
        }
        else {
            // If request is not for an available flight class, send a reservation failure status
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
