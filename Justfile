default:
    just --list

# Compiles the code
compile:
    ./mvnw clean compile

# Builds the final jlinked image
jlink:
    ./mvnw clean compile jlink:jlink

# Tests the code
test:
    ./mvnw clean test

run: jlink
    ./target/maven-jlink/default/bin/java -m dev.mccue.microhttp.realworld/dev.mccue.microhttp.realworld.Main
