import ballerina/http;
import ballerinax/kubernetes;

@kubernetes:Service {
    name: "book-review"
}
listener http:Listener bookReviewEP = new(7070);

@http:ServiceConfig {
    basePath: "/review"
}
service reviewService on bookReviewEP {
    @http:ResourceConfig {
        methods: ["GET"],
        path: "/{id}"
    }
    resource function getReview (http:Caller caller, http:Request request, string id) {
        table<Review> tbReviews = table {
            { id, content },
            [
                { "B1", "Review of book1" },
                { "B2", "Review of book2" }
            ]
        };

        string reviewContent = "(no reviews found)";
        while (tbReviews.hasNext()) {
            Review review = <Review>tbReviews.getNext();
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
