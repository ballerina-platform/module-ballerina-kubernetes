import ballerina/io;
import ballerinax/kubernetes;

@kubernetes:Job {
    singleYAML: false
}
public function main(string... args) {
    io:println("hello world");
}
