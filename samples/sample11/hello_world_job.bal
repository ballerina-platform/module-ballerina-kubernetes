import ballerina/io;
import ballerinax/kubernetes;

@kubernetes:Job {}
public function main(string... args) {
    io:println("hello world");
}
