#!/bin/sh

SERVER_JAR="target/server-0.1-SNAPSHOT.jar"
MVN=`which mvn`

if [ ! -e ${SERVER_JAR} ]; then
    # the file does not exist: compile
    $MVN package -DskipTests=true
fi
