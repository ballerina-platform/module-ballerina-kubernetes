import ballerina/http;
import ballerina/io;

table<Review> tbReviews = table {
        { id, content },
        [ { "B1", "Review of book1" },
          { "B2", "Review of book2" }
        ]
    };

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
        http:Response reviewResponse = new;
        string reviewContent = "(no reviews found)";
        while (tbReviews.hasNext()) {
            Review review = check <Review>tbReviews.getNext();
            io:println(review);
            io:println(id);
            if (review.id == id) {
                reviewContent = review.content;
                break;
            }
        }

        reviewResponse.setTextPayload(reviewContent);
        _ = caller -> respond(reviewResponse);
    }
}