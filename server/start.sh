#!/bin/sh


TOKEN=7YZxXyYBKvz55x1yreQ2

MVN=`which mvn`

MVN_FLAGS=-DskipTests=True
ARTIFACT_URL=https://rtse-isys.aau.at/giovanni.liva/java-xal/builds/artifacts/development/download?job=release
ARTIFACT_NAME=server.zip

# check for new version
touch .head_development
rm -fR server
LOCAL_HEAD_DEV=`cat .head_development`
REMOTE_HEAD_DEV=`git fetch; git log development | head -1 | cut -d ' ' -f 2`
if [ "${LOCAL_HEAD_DEV}" != "${REMOTE_HEAD_DEV}" ]; then
    printf "Server is not update. "
    printf "Updating to "
    echo ${REMOTE_HEAD_DEV}
    # Update reference in file
    echo ${REMOTE_HEAD_DEV} > .head_development
    wget --header="PRIVATE-TOKEN: ${TOKEN}" ${ARTIFACT_URL} --output-document=${ARTIFACT_NAME}
    unzip -o ${ARTIFACT_NAME}
    rm -fR target
    mkdir target
    cp server/target/server-*.jar target/
fi

SERVER_JAR=`ls target/server-*.jar 2> /dev/null | sort -r | head -1`
SERVER_FLAGS=-debug

echo "server jar $SERVER_JAR ..."

if [ -z "${SERVER_JAR}" ]; then
    # the file does not exist: compile
    CURR_PWD=`pwd`
    cd ..
    echo "Compile current version with maven"
    $MVN package $MVN_FLAGS > /dev/null
    cd $CURR_PWD
    SERVER_JAR=`ls target/server-*.jar 2> /dev/null | sort -r | head -1`
fi

java -jar $SERVER_JAR $SERVER_FLAGS
