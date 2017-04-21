#!/bin/bash

# Start mongo
mongod &
# Start server
/usr/lib/jvm/java-8-openjdk-amd64/bin/java -jar server/target/server-0.2.jar -debug