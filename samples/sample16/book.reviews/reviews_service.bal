import ballerina/http;
import ballerinax/kubernetes;

@kubernetes:Deployment {
    singleYAML: true
}
@kubernetes:Service {
    name: "book-review"
}
endpoint http:Listener bookReviewEP {
    port: 7070
};

@http:ServiceConfig {
    basePath: "/review"
}
service<http:Service> reviewService bind bookReviewEP {
    @http:ResourceConfig {
        methods: ["GET"],
        path: "/{id}"
    }
    getReview (endpoint caller, http:Request request, string id) {
        table<Review> tbReviews = table {
            { id, content },
            [ { "B1", "Review of book1" },
                { "B2", "Review of book2" }
            ]
        };

        string reviewContent = "(no reviews found)";
        while (tbReviews.hasNext()) {
            Review review = check <Review>tbReviews.getNext();
            if (review.id == id) {
                reviewContent = review.content;
                break;
            }
        }

        http:Response reviewResponse = new;
        reviewResponse.setTextPayload(reviewContent);
        _ = caller -> respond(reviewResponse);
    }
}
