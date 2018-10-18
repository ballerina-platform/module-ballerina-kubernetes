import ballerina/io;
import ballerinax/kubernetes;

@kubernetes:Job {
    env: {
        "location": "SL"
    },
    image: "my-ballerina-job:1.0",
    labels: { "lang": "ballerina" },
    copyFiles: [
        {
            target: "/home/ballerina/data/data.txt",
            source: "./data/data.txt"
        }
    ]
}
public function main(string... args) {
    io:println("hello world");
}
