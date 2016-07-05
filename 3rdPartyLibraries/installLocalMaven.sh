#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
mvn install:install-file -Dfile=${DIR}/org.eclipse.jdt.core-3.12.0.v20160516-2131.jar -DpomFile=${DIR}/org.eclipse.jdt.core-3.12.0.v20160516-2131.pom
