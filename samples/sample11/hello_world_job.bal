import ballerina/io;
import ballerina/kubernetes;

@kubernetes:Job {}
public function main(string... args) {
    io:println("hello world");
}
