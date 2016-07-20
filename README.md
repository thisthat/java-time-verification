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
At the moment, the method of the hidden class are put as method of the last visited class.

# Current Branch
The current branch aim to introduce the rewriting logic to apply heuristics.
In order to do that, we have to:
* introduce the possibilities to add annotation to the elements of the IM
* create an interface for the heuristics that has the following elements
    * **let** it creates a set of annotations
    * **where** it restricts the space of search, when it matches we can apply the heuristic
    * **rewrite** it express how to rewrite the node of the IM

Questions:
* the IM has to keep with it for each node the Env that it can see?