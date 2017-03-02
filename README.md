# JAVA-XAL 
![Build](https://rtse-isys.aau.at/giovanni.liva/java-xal/badges/master/build.svg)

# How to install & Run
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
java -jar intermediate-model-0.1-SNAPSHOT.jar {FILE}
```

This project uses the JDT parser to convert Java source code to XAL files annotated with constraint.
In the std output it reports all the time constraint that it founded in the code.


# Direction of the software

The master branch will be keep updated with the latest perfectly functional version.
All the development will be performed in a separate branch that will be merge to the main only when it is perfectly stable.

The next features to implement are:
* Indexing before parse [DONE]
* Store information in a database [DONE]
* Create a PCFG w/ Time Constraints from the source code [DONE]
* User defined heuristics
    * Create a grammar to specify them
    * Parse and create the heuristic automatically
* Introduce the annotations
* Rewrite the source code with the annotations

# Known Issues


# Current Branch

The following branch will use to test new features.
When they are ready and without knowing issue, a merge request can be created.

#Extract all Classes and methods form Jar
```bash
find . | grep ".class" | xargs javap -p > _classes.txt
```




