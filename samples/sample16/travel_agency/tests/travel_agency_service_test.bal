import ballerina/test;
import ballerina/http;

// Mocking airline reservation service
listener http:Listener airlineEP = new(8080);
@http:ServiceConfig {
    basePath: "/airline"
}
service airlineReservationService on airlineEP {
    @http:ResourceConfig {
        methods: ["POST"],
        path: "/reserve",
        consumes: ["application/json"],
        produces: ["application/json"]
    }
    resource function reserveTicket(http:Caller caller, http:Request request) {
        http:Response response = new;
        response.setJsonPayload({
            Status: "Success"
        });
        checkpanic caller->respond(response);
    }
}

// Mocking hotel reservation service
listener http:Listener hotelEP = new(7070);
@http:ServiceConfig {
    basePath: "/hotel"
}
service hotelReservationService on hotelEP {
    @http:ResourceConfig {
        methods: ["POST"],
        path: "/reserve",
        consumes: ["application/json"],
        produces: ["application/json"]
    }
    resource function reserveRoom(http:Caller caller, http:Request request) {
        http:Response response = new;
        response.setJsonPayload({
            Status: "Success"
        });
        checkpanic caller->respond(response);
    }
}

// Mocking car rental service
listener http:Listener carEP = new(6060);
@http:ServiceConfig {
    basePath: "/car"
}
service carRentalService on carEP {
    @http:ResourceConfig {
        methods: ["POST"],
        path: "/rent",
        consumes: ["application/json"],
        produces: ["application/json"]
    }
    resource function rentCar(http:Caller caller, http:Request request) {
        http:Response response = new;
        response.setJsonPayload({
            Status: "Success"
        });
        checkpanic caller->respond(response);
    }
}

// Client endpoint
http:Client clientEP = new("http://localhost:9090/travel");

// Function to test Travel agency service
@test:Config
function testTravelAgencyService() returns error? {
    airlineReservationEP = new("http://localhost:8080/airline");
    hotelReservationEP = new("http://localhost:7070/hotel");
    carRentalEP = new("http://localhost:6060/car");

    // Initialize the empty http requests and responses
    http:Request req;

    // Test the 'arrangeTour' resource
    // Construct a request payload
    json payload = {
        Name: "Alice",
        ArrivalDate: "12-03-2018",
        DepartureDate: "13-04-2018",
        Preference:{
            Airline: "Business",
            Accommodation: "Air Conditioned",
            Car: "Air Conditioned"
        }
    };

    // Send a 'post' request and obtain the response
    http:Response response = check clientEP -> post("/arrange", payload);
    // Expected response code is 200
    test:assertEquals(response.statusCode, 200, msg = "Travel agency service did not respond with 200 OK signal!");
    // Check whether the response is as expected
    json resPayload = check response.getJsonPayload();
    json expected = {
        Message: "Congratulations! Your journey is ready!!"
    };
    test:assertEquals(resPayload, expected, msg = "Response mismatch!");
    return ();
}