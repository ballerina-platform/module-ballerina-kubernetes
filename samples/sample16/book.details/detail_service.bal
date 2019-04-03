import ballerina/http;
import ballerina/log;
import ballerinax/kubernetes;

@kubernetes:Service {
    name: "book-detail"
}
listener http:Listener bookDetailEP = new(8080);

@http:ServiceConfig {
    basePath: "/detail"
}
service detailService on bookDetailEP {
    @http:ResourceConfig {
        methods: ["GET"],
        path: "/{id}"
    }
    resource function getDetail(http:Caller caller, http:Request request, string id) {
        table<Detail> tbDetails = table {
                { id, author, cost },
            [
                { "B1", "John Jonathan", 10.00 },
                { "B2", "Anne Anakin", 15.00 },
                { "B3", "Greg George", 20.00 }
            ]
        };

        Detail? bookDetail = ();
        foreach Detail detail in tbDetails {
            if (detail.id == id) {
                bookDetail = detail;
                break;
            }
        }

        http:Response detailResponse = new;
        if (bookDetail is Detail) {
            json responseJson = {
                author: bookDetail.author,
                cost: bookDetail.cost
            };
            detailResponse.setJsonPayload(responseJson, contentType = "application/json");
        } else {
            json notFoundJson = {
                message: "book not found: " + untaint id
            };
            detailResponse.setJsonPayload(notFoundJson, contentType = "application/json");
            detailResponse.statusCode = 404;
        }
        var responseResult = caller -> respond(detailResponse);
        if (responseResult is error) {
            log:printError("error responding back to client.", err = responseResult);
        }
    }
}
