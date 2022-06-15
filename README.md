# Compilers Project

## Group 20220-9a

Name | Number | Grade | Contribution
-----|--------|-------|-------------
Bruno Gomes | 201906401 | 18.2 | 25%
Marcelo Couto | 201906086 | 18.2 | 25%
Francisco Oliveira | 201907361 | 18.2 | 25%
Sara Marinha | 201906805 | 18.2 | 25%

**Global Grade of the Project:** 18.2

## Summary

Our project is able to compile a file written in Java-- language (.jmm file extension)
defined in the project's handout, to a Jasmin file (.j extension). The resulting file can be then transformed into java bytecode (.class file) through the use of jasmin.jar. 
Finally, the .class file originated can be run with java. 

The compiler we designed and constructed is divided into 4 steps:
- Syntactic Analysis
- Semantic Analysis
- Ollir Code Generation
- Jasmin Code Generation

The compilation process takes into account the structure and semantic validity of the file given, 
raising errors or warning when guidelines for the language are not met. Our project also
implements optimizations, both in Ollir Code Generation and Jasmin Code Generation stages.

## Semantic-Analysis

For the Semantic Analysis part, our project resorts to the use of a Symbol Table, which stores 
import paths, methods of the class, 

## Code Generation

## Pros
- Compiles any Java-- file to Jasmin 
- Supports negative numbers
- Optimizes the code to a certain extent

## Cons
- Does not support overloading
- Does not use Register Allocation

[//]: # ()
[//]: # (# Compilers Project)

[//]: # ()
[//]: # (For this project, you need to install [Java]&#40;https://jdk.java.net/&#41;, [Gradle]&#40;https://gradle.org/install/&#41;, and [Git]&#40;https://git-scm.com/downloads/&#41; &#40;and optionally, a [Git GUI client]&#40;https://git-scm.com/downloads/guis&#41;, such as TortoiseGit or GitHub Desktop&#41;. Please check the [compatibility matrix]&#40;https://docs.gradle.org/current/userguide/compatibility.html&#41; for Java and Gradle versions.)

[//]: # ()
[//]: # (## Project setup)

[//]: # ()
[//]: # (There are three important subfolders inside the main folder. First, inside the subfolder named ``javacc`` you will find the initial grammar definition. Then, inside the subfolder named ``src`` you will find the entry point of the application. Finally, the subfolder named ``tutorial`` contains code solutions for each step of the tutorial. JavaCC21 will generate code inside the subfolder ``generated``.)

[//]: # ()
[//]: # (## Compile and Running)

[//]: # ()
[//]: # (To compile and install the program, run ``gradle installDist``. This will compile your classes and create a launcher script in the folder ``./build/install/comp2022-00/bin``. For convenience, there are two script files, one for Windows &#40;``comp2022-00.bat``&#41; and another for Linux &#40;``comp2022-00``&#41;, in the root folder, that call tihs launcher script.)

[//]: # ()
[//]: # (After compilation, a series of tests will be automatically executed. The build will stop if any test fails. Whenever you want to ignore the tests and build the program anyway, you can call Gradle with the flag ``-x test``.)

[//]: # ()
[//]: # (## Test)

[//]: # ()
[//]: # (To test the program, run ``gradle test``. This will execute the build, and run the JUnit tests in the ``test`` folder. If you want to see output printed during the tests, use the flag ``-i`` &#40;i.e., ``gradle test -i``&#41;.)

[//]: # (You can also see a test report by opening ``./build/reports/tests/test/index.html``.)

[//]: # ()
[//]: # (## Checkpoint 1)

[//]: # (For the first checkpoint the following is required:)

[//]: # ()
[//]: # (1. Convert the provided e-BNF grammar into JavaCC grammar format in a .jj file)

[//]: # (2. Resolve grammar conflicts, preferably with lookaheads no greater than 2)

[//]: # (3. Include missing information in nodes &#40;i.e. tree annotation&#41;. E.g. include the operation type in the operation node.)

[//]: # (4. Generate a JSON from the AST)

[//]: # ()
[//]: # (### JavaCC to JSON)

[//]: # (To help converting the JavaCC nodes into a JSON format, we included in this project the JmmNode interface, which can be seen in ``src-lib/pt/up/fe/comp/jmm/ast/JmmNode.java``. The idea is for you to use this interface along with the Node class that is automatically generated by JavaCC &#40;which can be seen in ``generated``&#41;. Then, one can easily convert the JmmNode into a JSON string by invoking the method JmmNode.toJson&#40;&#41;.)

[//]: # ()
[//]: # (Please check the JavaCC tutorial to see an example of how the interface can be implemented.)

[//]: # ()
[//]: # (### Reports)

[//]: # (We also included in this project the class ``src-lib/pt/up/fe/comp/jmm/report/Report.java``. This class is used to generate important reports, including error and warning messages, but also can be used to include debugging and logging information. E.g. When you want to generate an error, create a new Report with the ``Error`` type and provide the stage in which the error occurred.)

[//]: # ()
[//]: # ()
[//]: # (### Parser Interface)

[//]: # ()
[//]: # (We have included the interface ``src-lib/pt/up/fe/comp/jmm/parser/JmmParser.java``, which you should implement in a class that has a constructor with no parameters &#40;please check ``src/pt/up/fe/comp/CalculatorParser.java`` for an example&#41;. This class will be used to test your parser. The interface has a single method, ``parse``, which receives a String with the code to parse, and returns a JmmParserResult instance. This instance contains the root node of your AST, as well as a List of Report instances that you collected during parsing.)

[//]: # ()
[//]: # (To configure the name of the class that implements the JmmParser interface, use the file ``config.properties``.)

[//]: # ()
[//]: # (### Compilation Stages )

[//]: # ()
[//]: # (The project is divided in four compilation stages, that you will be developing during the semester. The stages are Parser, Analysis, Optimization and Backend, and for each of these stages there is a corresponding Java interface that you will have to implement &#40;e.g. for the Parser stage, you have to implement the interface JmmParser&#41;.)

[//]: # ()
[//]: # ()
[//]: # (### config.properties)

[//]: # ()
[//]: # (The testing framework, which uses the class TestUtils located in ``src-lib/pt/up/fe/comp``, has methods to test each of the four compilation stages &#40;e.g., ``TestUtils.parse&#40;&#41;`` for testing the Parser stage&#41;. )

[//]: # ()
[//]: # (In order for the test class to find your implementations for the stages, it uses the file ``config.properties`` that is in root of your repository. It has four fields, one for each stage &#40;i.e. ``ParserClass``, ``AnalysisClass``, ``OptimizationClass``, ``BackendClass``&#41;, and initially it only has one value, ``pt.up.fe.comp.SimpleParser``, associated with the first stage.)

[//]: # ()
[//]: # (During the development of your compiler you will update this file in order to setup the classes that implement each of the compilation stages.)
