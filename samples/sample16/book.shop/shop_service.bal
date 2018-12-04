import ballerina/http;
import ballerinax/kubernetes;

@kubernetes:Service {}
@kubernetes:IstioGateway {}
@kubernetes:IstioVirtualService {}
listener http:Listener bookShopEP = new(9080);

http:Client bookDetailsEP = new("http://book-detail:8080");

http:Client bookReviewEP = new("http://book-review:7070");

@kubernetes:Deployment {
    singleYAML: false
}
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
                            _ = caller -> respond(bookResponse);
                        } else {
                            http:Response errResponse = new;
                            json serverErr = { message: "unexpected response from review service" };
                            errResponse.setJsonPayload(serverErr, contentType = "application/json");
                            errResponse.statusCode = 404;
                            _ = caller -> respond(errResponse);
                        }
                    } else {
                        http:Response errResponse = new;
                        json serverErr = { message: "book reviews service is not accessible" };
                        errResponse.setJsonPayload(serverErr, contentType = "application/json");
                        _ = caller -> respond(errResponse);
                    }
                } else {
                    http:Response errResponse = new;
                    json serverErr = { message: "unexpected response from detail service" };
                    errResponse.setJsonPayload(serverErr, contentType = "application/json");
                    errResponse.statusCode = 404;
                    _ = caller -> respond(errResponse);
                }
            } else {
                http:Response errResponse = new;
                json serverErr = { message: string `book not found: {{untaint id}}` };
                errResponse.setJsonPayload(serverErr, contentType = "application/json");
                errResponse.statusCode = 404;
                _ = caller -> respond(errResponse);
            }
        } else {
            http:Response errResponse = new;
            json serverErr = { message: "book details service is not accessible" };
            errResponse.setJsonPayload(serverErr, contentType = "application/json");
            _ = caller -> respond(errResponse);
        }
    }
}
