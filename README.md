# JAVA-XAL 
![Build](https://rtse-isys.aau.at/giovanni.liva/java-xal/badges/master/build.svg)

In order to package the project call 
```bash
mvn clean package
```

If you'd like to skip the test packege it with:
```bash
mvn clean package -DskipTests
```

To run it, go into the intermediate-model/target subfolder and run the java -jar command passing the file on which operate.
```bash
java -jar intermediate-model-0.1-SNAPSHOT.jar ../src/test/resources/vuze/com/aelitis/net/udp/mc/impl/MCGroupImpl.java
```

This project uses the ANTLRv4/JDT parser to convert Java source code to XAL files.


