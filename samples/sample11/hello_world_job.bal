import ballerina/io;
import ballerinax/kubernetes;

@kubernetes:Job {}
function main(string... args) {
    io:println("hello world");
}
