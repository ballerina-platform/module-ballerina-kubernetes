import ballerina/http;
import ballerina/log;
import ballerinax/kubernetes;
import ballerinax/istio;

@kubernetes:Service {}
@istio:Gateway {}
@istio:VirtualService {}
listener http:Listener bookShopEP = new(9080);

http:Client bookDetailsEP = new("http://book-detail:8080");

http:Client bookReviewEP = new("http://book-review:7070");

@kubernetes:Deployment {}
@http:ServiceConfig {
    basePath: "/book"
}
service shopService on bookShopEP {
    @http:ResourceConfig {
        methods: ["GET"],
        path: "/{id}"
    }
    resource function getBook (http:Caller caller, http:Request request, string id) {
        var detailCall = bookDetailsEP->get(string `/detail/{{untaint id}}`);
        if (detailCall is http:Response) {
            if (detailCall.statusCode != 404) {
                var details = detailCall.getJsonPayload();
                if (details is json) {
                    var reviewCall = bookReviewEP->get(string `/review/{{untaint id}}`);
                    if (reviewCall is http:Response) {
                        var reviews = reviewCall.getTextPayload();
                        if (reviews is string) {
                            json bookInfo = {
                            id: id,
                                details: details,
                                reviews: reviews
                            };
                            http:Response bookResponse = new;
                            bookResponse.setJsonPayload(untaint bookInfo, contentType = "application/json");
                            var responseResult = caller->respond(bookResponse);
                            if (responseResult is error) {
                                log:printError("error responding back to client.", err = responseResult);
                            }
                        } else {
                            http:Response errResponse = new;
                            json serverErr = { message: "unexpected response from review service" };
                            errResponse.setJsonPayload(serverErr, contentType = "application/json");
                            errResponse.statusCode = 404;
                            var responseResult = caller->respond(errResponse);
                            if (responseResult is error) {
                                log:printError("error responding back to client.", err = responseResult);
                            }
                        }
                    } else {
                        http:Response errResponse = new;
                        json serverErr = { message: "book reviews service is not accessible" };
                        errResponse.setJsonPayload(serverErr, contentType = "application/json");
                        var responseResult = caller->respond(errResponse);
                        if (responseResult is error) {
                            log:printError("error responding back to client.", err = responseResult);
                        }
                    }
                } else {
                    http:Response errResponse = new;
                    json serverErr = { message: "unexpected response from detail service" };
                    errResponse.setJsonPayload(serverErr, contentType = "application/json");
                    errResponse.statusCode = 404;
                    var responseResult = caller->respond(errResponse);
                    if (responseResult is error) {
                        log:printError("error responding back to client.", err = responseResult);
                    }
                }
            } else {
                http:Response errResponse = new;
                json serverErr = { message: string `book not found: {{untaint id}}` };
                errResponse.setJsonPayload(serverErr, contentType = "application/json");
                errResponse.statusCode = 404;
                var responseResult = caller->respond(errResponse);
                if (responseResult is error) {
                    log:printError("error responding back to client.", err = responseResult);
                }
            }
        } else {
            http:Response errResponse = new;
            json serverErr = { message: "book details service is not accessible" };
            errResponse.setJsonPayload(serverErr, contentType = "application/json");
            var responseResult = caller->respond(errResponse);
            if (responseResult is error) {
                log:printError("error responding back to client.", err = responseResult);
            }
        }
    }
}
