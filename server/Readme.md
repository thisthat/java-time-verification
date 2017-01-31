# REST URLs
Common port: **9000**

Common address: **127.0.0.1**

| URL        | METHOD | Output        | Description  |
|------------|:------:|:-------------:| :------------|
| / | GET/POST | txt | Check the connection with the server. |
| /getAllFiles | POST | json | It expects two parameters: <ul><li>projectPath `String`: Path from where search java files</li><li>skipTest `int`: skip test files</li></ul> `projectPath` supports only *file://* as URI protocol atm. <br/> `skipTest` is considered true if equal to **1**, any other value are considered as false. |
| /getFile     | POST | json | It expects one parameter: <ul><li>filePath `String`: Path from of the java file</li></ul>  It returns the intermediate model in json format of the given file. <br /> `filePath` supports only *file://* as URI protocol atm.|









