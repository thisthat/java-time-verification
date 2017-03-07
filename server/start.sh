#!/bin/sh

MVN=`which mvn`
SERVER_JAR=`ls target/server-*.jar 2> /dev/null | sort -r | head -1`
SERVER_FLAGS=-debug
MVN_FLAGS=-DskipTests=True

echo "server jar $SERVER_JAR ..."

if [ -z "${SERVER_JAR}" ]; then
    # the file does not exist: compile
    CURR_PWD=`pwd`
    cd ..
    $MVN package $MVN_FLAGS
    cd $CURR_PWD
    SERVER_JAR=`ls target/server-*.jar 2> /dev/null | sort -r | head -1`
fi

java -jar $SERVER_JAR $SERVER_FLAGS
