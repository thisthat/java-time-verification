#!/bin/sh

MVN=`which mvn`
SERVER_JAR=`ls target/server-*.jar | sort -r | head -1`
SERVER_FLAGS=-debug
MVN_FLAGS=-DskipTests=True

if [ ! -e ${SERVER_JAR} ]; then
    # the file does not exist: compile
    $MVN package $MVN_FLAGS
fi

java -jar $SERVER_JAR $SERVER_FLAGS
