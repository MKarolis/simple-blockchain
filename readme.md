Custom Hash function implementation in Java
===========================================
<h6>
Created by Karolis Medekša for BlockChain fundamentals course<br/>
Vilnius University, Faculty of Mathematics and Informatics, 2020 Fall
</h6>

About
-----

This application providesa  custom hash function implementation in Java, 
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
3. Launch `./java -jar HashFunction-[version].jar [Options] [Value]`

OR

1. Build with Gradle launch4j plugin: `./gradlew createExe`
2. Navigate to build folder: `cd build/launch4j`
3. Launch `./HashFunction.exe [Options] [Value]`

#### Command line arguments
App usage from .exe file (same applies to .jar): `./HashFunction.exe [Options] [Value]`
```
Options:
    --help 
        Shows app usage instructions
    -m, -mode
        Sets app operation mode
        Default: ARG
        Possible values:
            ARG - Hash String passed as [Value]
            I - Interactive mode
            BENCH - Benchmarking mode
            VS - Benchmarking against a specified algorithm mode. 
                 Name of the algorithm is accepted as [Value]
            FILE - Hash contents of a file, filename is accepted as [Value]

Value - Value that is used as an input by supported operation modes
```
#### Operation modes

- Argument mode - hash argument passed to the function
```
$ ./HashFunction.exe -m ARG "ValueToHash"
28A5FE015D9993FE92F861CBC39D5B8838EEDFB2754323F14BCABE3551FFD4C7
```
- Interactive mode - enter values to hash line by line
```
$ ./HashFunction.exe -m i "ValueToHash"
Enter text and press [Enter] to hash it. Press [Ctr + C] to exit
A
43AE66A7DF429D367E89ED504922929740BE5A83872B2AE6D184416EB9F88C28
B
6EA760C41E005B415A95557FD4B5EBD48FE34FEDF31B975F616AB5CE7F1ABFA4
...
```
- Versus mode - compare custom hash to an existing realisation. Pass the name of the other hash
as value:
```
$ ./HashFunction.exe -m vs md5
Benchmarking md5 against custom hash
Benchmarking hash performance for 1 000 000 digests...
...
```
Supported hashes: MD2, MD5, SHA-1, SHA-256, SHA3-224, SHA3-156 and others...
- Benchmark mode - Benches hash performance
```
$ ./HashFunction.exe -m bench
Preparing benchmarking input data...
Starting benchmarking of file konstitucija.txt...
...
```
- File mode - hashes contents of the given file. File must be in the same directory as the executable
```
$ ./HashFunction.exe -m file file.txt
28A5FE015D9993FE92F861CBC39D5B8838EEDFB2754323F14BCABE3551FFD4C7
```

Hashing Performance
-------------------
Hashing quality and performance analysis (in benchmark mode):
- Time to hash constitution of Republic of Lithuania - **48ms**
- Number of collisions among 100 000 pairs of words - **0**
- Diff results among 100 000 pairs of similar words:

|               | Min diff (%) | Max diff (%) | Average diff (%) |
|---------------|:------------:|:------------:|:----------------:|
| Per Character | 75.56        | 100.00       | 93.76            |
| Per bit       | 26.76        | 51.95        | 38.87            |

- Time to hash 1.5GB size file ~**35s**

Performance against other realisations (in versus mode):

|      Hash name       | Time to hash 1 000 000 digests (s) | Average diff between similar word hashes (%) |
|-------------|:----------------------------------:|:--------------------------------------------:|
| Custom Hash | 7.58                               | 93.74                                        |
| MD2         | 8.93                               | 93.75                                        |
| MD5         | 0.55                               | 93.74                                        |
| SHA-256     | 0.68                               | 93.75                                        |
| SHA-512     | 0.59                               | 93.75                                        |
| SHA3-256    | 1.05                               | 93.76                                        |

##### Conclusion
Quality of the hash algorithm is reasonably good in the sense that no collisions are produced while benchmarking
and that diff score is on par with other hashing implementations. On the other hand, the algorithm is more
than 10 times slower than industry standard realisations.

Algorithm
---------

`HashingService` is responsible for instantiating `HashGenerator` with a predefined hash seed,
processing input in portions of 60 bytes and outputting formatted hash.

Seed used by `HashingService` was generated randomly:  
```
int HASH_SEED = {
    11000011011111010011011000110000
    10011010101100100100110010010010
    01001001101010101101100100101100
    10100011000100010011011011010101
    10001111011001101101001001001011
    10111011000100110010010010100010
    01010001010001110010000101010110
    11101111001011011010110110011011
};
```

Hashing service converts given String into an array of bytes.
Then bytes are processed by `HashGenerator` in form of buckets 
64 bytes. Last 4 bytes in each bucket are reserved for input size.
In code `FREE_BUCKET_SIZE` is used to indicate processable 60 bytes. 

Finally, code is formatted by `HashGenerator` and returned.

Pseudo-code schema:
```
function hash(input) {
    Extract bytes from input String
    SET bucketCount TO CEIL(bytes size / FREE_BUCKET_SIZE)
    FOR i = 0 TO bucketCount
        CALL generator.processBucket(bytes, i times FREE_BUCKET_SIZE)
    ENDFOR

    RETURN generator.formatHash();
}
```

`HashGenerator` keeps an internal array of 32 bit values called `hashWords`.
When initialised it is set to passed SEED array. 
Actual hashing is performed by mutating `hashWords` values.

Bucket processing algorithm:
```
function processBucket(bytes, startIndex) {
    SET words to NEW INT[64]
    CALL addWordsFromExistingBytes(bytes, startIndex)
    CALL generateAdditionalWords(words)
    CALL mutateHashWords(mutateHashWords)
}
```

While hashing, several bitwise operations are used:

- `SHIFTL(a, n)`, shift bits of a n bytes left
- `SHIFTR(a, n)`, shift bits of a n bytes right
- `ROTR(a, n)`, rotate bits of a n bytes right
- `ROTL(a, n)`, rotate bits of a n bytes left

Helper operations used in hashing:

- `OP1(a)` = `ROTL(a, 8) XOR ROTR(a, 16) XOR SHIFTR(a, 12)`
- `OP2(a)` = `a XOR ROTL(a, 6) XOR ROTR(a, 12) XOR ROTL(a, 15)`
- `OP3(a)` = `a XOR ROTR(a, 21) XOR SHIFTR(a, 7)`
- `OP4(a)` = `a XOR (NOT SHIFTL(a, 12)) XOR (NOT SHIFTR(a, 12))`
- `OP5(a)` = `ROTR(a, 8) XOR SHIFTL(a, 12) XOR SHIFTR(a, 16)`
- `OP6(a)` = `SHIFTL(a, 27) OR SHIFTR(a, 27) XOR SHIFTR(a, 18) XOR SHIFTL(a, 18)`

16 words (32 bit length) are added from input. Other 48 words are 
generated by using previous ones

Adding words from existing bytes:
```
function addWordsFromExistingBytes(bytes, words, startIndex) {
    DO
        SET word TO next 4 bytes
        SET words AT starIndex TO word
        INCREMENT startIndex
    WHILE startIndex WITHIN bucketBounds

    SET words AT bucketSize to SIZE OF bytes
}
```

Generating existing words:
```
function generateAdditionalWords(words) {
    FOR i = 16 TO 64
        SET words AT i TO
            OP1(words AT i - 1) XOR
            OP6(words AT i - 1) XOR
            OP2(words AT i - 2) XOR
            OP4(words AT i - 1) XOR
            OP3(words AT i - 16) XOR (
                OP4(words AT i - 14) AND
                OP5(words AT i - 14) OR
                OP2(words AT i - 2)
            )
    ENDFOR
}
```

Mutating hashWords for each bucket 
**(index is initialized to 0 on HashGenerator creation)**:
```
function mutateHashWords(words) {
    FOR word OF words DO
        SET w1 TO hashWords AT index + 1 MOD 8
        SET w2 TO hashWords AT index + 3 MOD 8
        SET w3 TO hashWords AT index + 6 MOD 8
        
        SET temp1 TO OP1(w1) XOR
            (NOT OP5(w2) AND OP2(word)) XOR
               OP3(word)
        SET temp2 TO OP5(w2) XOR OP2(
                OP4(w3) AND NOT OP3(word)
            ) XOR OP6(word)
        SET temp3 TO (OP4(w1) AND OP2(word)) XOR
            OP5(word) XOR
            OP1(w3)

        SET hashWords AT index TO OP1(temp1 XOR temp2) XOR
            OP5(temp3 XOR OP1(temp2))
    
        INCREMENT index
    ENDFOR   
}
```

When hash is output each word of `hashWords` is simply converted to hex:
```
function formatHash() {
    SET output TO empty_string
    FOR word OF hashWords DO
        ADD toHex(word) to output
    ENDFOR
}
```
