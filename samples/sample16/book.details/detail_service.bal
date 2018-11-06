import ballerina/http;
import ballerinax/kubernetes;

@kubernetes:Deployment {
    singleYAML: true
}
@kubernetes:Service {
    name: "book-detail"
}
endpoint http:Listener bookDetailEP {
    port: 8080
};

@http:ServiceConfig {
    basePath: "/detail"
}
service<http:Service> detailService bind bookDetailEP {
    @http:ResourceConfig {
        methods: ["GET"],
        path: "/{id}"
    }
    getDetail (endpoint caller, http:Request request, string id) {
        table<Detail> tbDetails = table {
            { id, author, cost },
            [ { "B1", "John Jonathan", 10.00 },
                { "B2", "Anne Anakin", 15.00 },
                { "B3", "Greg George", 20.00 }
            ]
        };

        Detail? bookDetail;
        while (tbDetails.hasNext()) {
            Detail detail = check <Detail>tbDetails.getNext();
            if (detail.id == id) {
                bookDetail = detail;
                break;
            }
        }

        http:Response detailResponse = new;
        match bookDetail {
            Detail detail => {
                json responseJson = {
                    author: bookDetail.author,
                    cost: bookDetail.cost
                };
                detailResponse.setJsonPayload(responseJson, contentType = "application/json");
            }
            () => {
                json notFoundJson = {
                    message: "book not found: " + untaint id
                };
                detailResponse.setJsonPayload(notFoundJson, contentType = "application/json");
                detailResponse.statusCode = 404;
            }
        }
        _ = caller -> respond(detailResponse);
    }
}