import ballerina/test;
import ballerina/http;

// Client endpoint
http:Client clientEP = new("http://localhost:7070/hotel");

// Function to test Hotel reservation service
@test:Config{}
function testHotelReservationService() returns error? {
    // Initialize the empty http requests and responses
    http:Request req;

    // Test the 'reserveRoom' resource
    // Construct a request payload
    json payload = {
        Name: "Alice",
        ArrivalDate: "12-03-2018",
        DepartureDate: "13-04-2018",
        Preference: "Air Conditioned"
    };

    // Send a 'post' request and obtain the response
    http:Response response = <@untainted> check clientEP -> post("/reserve", payload);
    // Expected response code is 200
    test:assertEquals(response.statusCode, 200, "Hotel reservation service did not respond with 200 OK signal!");
    // Check whether the response is as expected
    json resPayload = check response.getJsonPayload();
    json expected = {
        Status: "Success"
    };
    test:assertEquals(resPayload, expected, "Response mismatch!");
    return ();
}
