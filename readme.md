BlockChain simulation in Java
===========================================
<h6>
Created by Karolis Medekša for BlockChain fundamentals course<br/>
Vilnius University, Faculty of Mathematics and Informatics, 2020 Fall
</h6>

About
-----

This project contains a simplified blockchain implementation in Java, 
Gradle is used as a build and dependency management tool.

You can run the application from a preferred IDE (Intellij IDEA, Eclipse, etc.) 
or straight from command line using gradle build tool.

**JDK version 11 or higher is required to build and run the application**

#### Gradle tasks
Only requirement to build the application is a sufficient Java Development Kit version.

Application can be built and run from the command line using Gradle tasks:
- `./gradlew build` compiles main and test files, runs unit tests, creates an executable .jar file
- `./gradlew jar` build an executable .jar file in `/build/libs` directory
- `./gradlew createExe` build an executable .exe file in `/build/launch4j` directory 
- `./gradlew test` run unit test suites
- `./gradlew clean` clean build data

#### Running the application
1. Build with Gradle: `./gradlew build`
2. Navigate to build folder: `cd build/libs`
3. Launch `java -jar BlockChain-[version].jar`

OR

1. Build with Gradle launch4j plugin: `./gradlew createExe`
2. Navigate to build folder: `cd build/launch4j`
3. Launch `./BlockChain.exe`

#### Summary
When the application is launched `1000` users are auto-generated. Then a total of `10000` transactions
are generated while simultaneously adding them to a block chain data structure. Each block holds up to `100`
transactions. Each block is being mined in parallel by 11 miners.

Difficulty of `5` is used for block mining `POW`, a custom hashing algorithm is used for hashing
 (more info [here](https://github.com/MKarolis/hash-function))
 
 The blockchain uses `UTXO` transaction model, similar model is used by `BitCoin`.
