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
* Indexing before parse
* Store information in a database
* Create a PCFG from the source code
* User defined heuristics
    * Create a grammar to specify them
    * Parse and create the heuristic automatically
* Introduce the annotations
* Rewrite the source code with the annotations

# Known Issues
new Object does not take in account that an hidden class can be created in the initialization.
At the moment, the method of the hidden class are put as method of the last visited class. *(SOLVED)*

# Current Branch
In the current branch we try to create the index structure of the project in order to create the PCFG.
In particular we'd like to index:
* single file in order to have what are the global vars that are timed related -> avoid order problem
* single file to have the list of what methods returns time related variables
* the strcuture of the class for generating the Fully Qualified Names

