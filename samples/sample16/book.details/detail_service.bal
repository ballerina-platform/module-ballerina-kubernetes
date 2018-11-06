import ballerina/http;

table<Detail> tbDetails = table {
        { id, author, cost },
        [ { "B1", "John Jonathan", 10.00 },
          { "B2", "Anne Anakin", 15.00 },
          { "B3", "Greg George", 20.00 }
        ]
    };

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
        table<Detail> bookDetail = from tbDetails where id == id select *;
        http:Response detailResponse = new;
        if (bookDetail.count() == 1) {
            json detailJson = check <json>bookDetail;
            json responseJson = {
                author: detailJson.author,
                cost: detailJson.cost
            };
            detailResponse.setJsonPayload(responseJson, contentType = "application/json");
        } else {
            json notFoundJson = {
                message: "book not found: " + untaint id
            };
            detailResponse.setJsonPayload(notFoundJson, contentType = "application/json");
            detailResponse.statusCode = 404;
        }
        _ = caller -> respond(detailResponse);
    }
}