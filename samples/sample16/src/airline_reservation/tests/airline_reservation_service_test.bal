import ballerina/test;
import ballerina/http;

// Client endpoint
http:Client clientEP = new("http://localhost:8080/airline");

// Function to test Airline reservation service
@test:Config{}
function testAirlineReservationService() returns error? {
    // Test the 'reserveTicket' resource
    // Construct a request payload
    json payload = {
        Name: "Alice",
        ArrivalDate: "12-03-2018",
        DepartureDate: "13-04-2018",
        Preference: "Business"
    };

    // Send a 'post' request and obtain the response
    http:Response response = check clientEP->post("/reserve", payload);
    // Expected response code is 200
    test:assertEquals(response.statusCode, 200, "Airline reservation service did not respond with 200 OK signal!");
    // Check whether the response is as expected
    json resPayload = check response.getJsonPayload();
    json expected = {
        Status: "Success"
    };
    test:assertEquals(resPayload, expected, "Response mismatch!");
    return ();
}
