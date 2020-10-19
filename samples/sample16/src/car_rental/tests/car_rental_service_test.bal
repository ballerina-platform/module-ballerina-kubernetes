import ballerina/test;
import ballerina/http;

// Client endpoint
http:Client clientEP = new("http://localhost:6060/car");

// Function to test Car rental service
@test:Config{}
function testCarRentalService() {
    // Test the 'rentCar' resource
    // Construct a request payload
    json payload = {
        Name: "Alice",
        ArrivalDate: "12-03-2018",
        DepartureDate: "13-04-2018",
        Preference: "Air Conditioned"
    };

    // Send a 'post' request and obtain the response
    http:Response response = <http:Response> checkpanic clientEP -> post("/rent", payload);
    // Expected response code is 200
    test:assertEquals(response.statusCode, 200, "Car rental service did not respond with 200 OK signal!");
    // Check whether the response is as expected
    json resPayload = checkpanic response.getJsonPayload();
    json expected = {
        Status: "Success"
    };
    test:assertEquals(resPayload, expected, "Response mismatch!");
    return ();
}
