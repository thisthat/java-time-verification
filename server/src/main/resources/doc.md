# REST URLs
Default PORT: **9000**

Default IP: **127.0.0.1**

| URL        | METHOD | Output        | Description  |
|------------|:------:|:-------------:| :------------|
| /                 | GET  | txt  | Check the connection with the server. |
| /getAllFiles      | POST | json | Return all the java file |
| /getFile          | POST | json | Return the IM of a file |
| /openProject      | POST | json | Calculate indexes of the project |
| /isProjectOpen    | POST | json | Return if a project has the indexes computed |
| /getFilesByType   | POST | json | Return the list of files that extends/implements the given type |
| /getThreads       | POST | json | Return the list of files that implements threads |
| /getMains         | POST | json | Return the list of files which contains a public void main |



# /getAllFiles

The route returns the list of all *java* files in the given directory.

It expects two parameters: 
* `projectPath` : path from where search java files
* `skipTest` : if equal to **1** it skips test files
 
`projectPath` supports only *file://* as URI protocol atm. 

# /getFile 
It returns the intermediate model in json format of the given file.

It expects one parameter: 
* `filePath`: Path from of the java file
 
`filePath` supports only *file://* as URI protocol atm.

# /openProject

The following REST API creates a db with the types of the project. 
If we do not use this call we cannot create a proper **getThreads()** and **..()** functions. 
Functions that requires infos about types will return an error 406 instead of 400 if the project is not currently in the db. 
Process the project could take times, therefore the function only offer an eventually consistency. 
Users can check the ending of the indexing phases with the **isProjectOpen()** function.

It expects two parameters: 
* `name` : Name of the project
* `path` : Path of the project in URI format (only the *file://* protocol is supported atm.)

Plus one optional parameter:
* `invalidCache` : if equal to **1** it invalidates the cache and compute again the indexes. Default value is `0`.

The function returns a value with a status code:
* `0` : There is already an indexing on that project ongoing
* `1` : The indexing process started correctly

# /isProjectOpen

The route returns the status of the indexes for the given project. 

It expects one parameter: 
* `name` : Name of the project

The return value is a status code:
* `0` : The project has not yet the indexes in the database
* `1` : The indexes are available

# /getFilesByType

Return the list of files in which each contains a class that extends or implements the given searched type.

It expects two parameters:
* `name` : Name of the project
* `type` : Type to use as filter

The output is a list of a structured data with the following format:
* `path` : Path of the file which contains the type
* `className` : name of the class that extends/implements the given type
* `packageName` : name of the package of the class that extends/implements the given type

A file can contains multiple classes. Therefore, to find the correct class users should use `packageName` and `className`.

# /getThreads

Return the list of files in which each contains a class that defines a Thread.

It expects one parameter:
* `name` : Name of the project

The output is a list of a structured data with the following format:
* `path` : Path of the file which contains the type
* `className` : name of the class that extends/implements the Thread Java API
* `packageName` : name of the package of the class that extends/implements the Thread Java API

A file can contains multiple classes. Therefore, to find the correct class users should use `packageName` and `className`.

# /getMains

Return the list of files in which each contains a class that defines a public static void main method.

It expects one parameter:
* `name` : Name of the project

The output is a list of a structured data with the following format:
* `path` : Path of the file which contains the type
* `className` : name of the class that extends/implements the Thread Java API
* `packageName` : name of the package of the class that extends/implements the Thread Java API

A file can contains multiple classes. Therefore, to find the correct class users should use `packageName` and `className`.



