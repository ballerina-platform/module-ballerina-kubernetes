import ballerina/http;
import ballerina/kubernetes;
import ballerina/log;
import ballerina/stringutils;

@kubernetes:Service {
    name: "hotel-reservation"
}
listener http:Listener hotelEP = new(7070);

// Available room types
const string AC = "Air Conditioned";
const string NORMAL = "Normal";

// Hotel reservation service to reserve hotel rooms
@http:ServiceConfig {
    basePath: "/hotel"
}
service hotelReservationService on hotelEP {

    // Resource to reserve a room
    @http:ResourceConfig {
        methods: ["POST"],
        path: "/reserve",
        consumes: ["application/json"],
        produces: ["application/json"]
    }
    resource function reserveRoom(http:Caller caller, http:Request request) {
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
                Message:"Invalid payload - Not a valid JSON payload"
            });
            var result = caller->respond(response);
            handleError(result);
            return;
        }

        json|error name = reqPayload.Name;
        json|error arrivalDate = reqPayload.ArrivalDate;
        json|error departDate = reqPayload.DepartureDate;
        json|error preferredRoomType = reqPayload.Preference;

        // If payload parsing fails, send a "Bad Request" message as the response
        if (name is error || arrivalDate is error || departDate is error || preferredRoomType is error) {
            response.statusCode = 400;
            response.setJsonPayload({
                Message:"Bad Request - Invalid Payload"
            });
            var result = caller->respond(response);
            handleError(result);
            return;
        }

        // Mock logic
        // If request is for an available room type, send a reservation successful status
        string preferredTypeStr = preferredRoomType.toString();
        if (stringutils:equalsIgnoreCase(preferredTypeStr, AC) || stringutils:equalsIgnoreCase(preferredTypeStr, NORMAL)) {
            response.setJsonPayload({
                Status: "Success"
            });
        }
        else {
            // If request is not for an available room type, send a reservation failure status
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
