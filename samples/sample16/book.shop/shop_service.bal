import ballerina/http;
import ballerinax/kubernetes;

@kubernetes:Deployment {}
@kubernetes:Service {}
@kubernetes:IstioGateway {}
@kubernetes:IstioVirtualService {}
endpoint http:Listener bookShopEP {
    port: 9090
};

endpoint http:Client bookDetailsEP {
    url: "http://book-detail:8080"
};

endpoint http:Client bookReviewEP {
    url: "http://book-review:7070"
};

@http:ServiceConfig {
    basePath: "/book"
}
service<http:Service> shopService bind bookShopEP {
    @http:ResourceConfig {
        methods: ["GET"],
        path: "/{id}"
    }
    getBook (endpoint caller, http:Request request, string id) {
        var detailCall = bookDetailsEP->get(string `/detail/{{untaint id}}`);
        match detailCall {
            http:Response detailResponse => {
                if (detailResponse.statusCode != 404) {
                    json details = check detailResponse.getJsonPayload();
                    var reviewCall = bookReviewEP->get(string `/review/{{untaint id}}`);
                    match reviewCall {
                        http:Response reviewResponse => {
                            string reviews = check reviewResponse.getTextPayload();
                            json bookInfo = {
                                id: id,
                                details: details,
                                reviews: reviews
                            };
                            http:Response bookResponse = new;
                            bookResponse.setJsonPayload(untaint bookInfo, contentType = "application/json");
                            _ = caller -> respond(bookResponse);
                        }
                        error => {
                            http:Response errResponse = new;
                            json serverErr = { message: "book reviews service is not accessible" };
                            errResponse.setJsonPayload(serverErr, contentType = "application/json");
                            _ = caller -> respond(errResponse);
                        }
                    }
                } else {
                    http:Response errResponse = new;
                    json serverErr = { message: string `book not found: {{untaint id}}` };
                    errResponse.setJsonPayload(serverErr, contentType = "application/json");
                    errResponse.statusCode = 404;
                    _ = caller -> respond(errResponse);
                }
            }
            error => {
                http:Response errResponse = new;
                json serverErr = { message: "book details service is not accessible" };
                errResponse.setJsonPayload(serverErr, contentType = "application/json");
                _ = caller -> respond(errResponse);
            }
        }
    }
}