import ballerina/test;
import ballerina/http;

// Client endpoint
http:Client clientEP = new("http://localhost:9090/travel");

// Function to test Travel agency service
@test:Config
function testTravelAgencyService() returns error? {
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
    http:Response response = check clientEP -> post("/arrangeTour", payload);
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