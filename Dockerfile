FROM java:8

RUN apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv 0C49F3730359A14518585931BC711F9BA15703C6
RUN echo "deb http://repo.mongodb.org/apt/debian jessie/mongodb-org/3.4 main" | tee /etc/apt/sources.list.d/mongodb-org-3.4.list
RUN apt-get update
RUN apt-get install -y maven
RUN apt-get install -y mongodb-org
WORKDIR /code

RUN mkdir -p /data/db

# Prepare by downloading dependencies
RUN mkdir intermediate-model
RUN mkdir java-parser
RUN mkdir PCFG
RUN mkdir server
RUN mkdir testJUppaal
RUN mkdir time-annotation
RUN mkdir uppaal-library
RUN mkdir xal

ADD pom.xml /code/pom.xml
ADD ./intermediate-model/pom.xml /code/intermediate-model/pom.xml
ADD ./java-parser/pom.xml /code/java-parser/pom.xml
ADD ./PCFG/pom.xml /code/PCFG/pom.xml
ADD ./server/pom.xml /code/server/pom.xml
ADD ./testJUppaal/pom.xml /code/testJUppaal/pom.xml
ADD ./time-annotation/pom.xml /code/time-annotation/pom.xml
ADD ./uppaal-library/pom.xml /code/uppaal-library/pom.xml
ADD ./xal/pom.xml /code/xal/pom.xml
ADD ./pom.xml /code/pom.xml
ADD ./server/copyDoc.sh /code/server/copyDoc.sh
RUN ["mvn", "verify"]

ADD . /code
# RUN ["mvn", "compile"]

# Adding source, compile and package into a fat jar

RUN ["mvn", "package", "-DskipTests=true"]

EXPOSE 9000
CMD ["./cmd.sh"]