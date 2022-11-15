# DesigniteJava
It also computes many commonly used object-oriented metrics.

## Object-oriented metrics
Software development expanded quickly in the 1970s. The scale of the projects under development was also expanding. A software measure that represents particular features of the object-oriented design is called an object-oriented software metric. These are employed to assess a person's success or failure as well as to put a number on how much the programme has improved throughout the course of development. Good object-oriented programming practices can be strengthened with the help of these metrics, which results in more dependable code.

Object-oriented software engineering metrics are units of measurement that are used to characterize:
* object-oriented software engineering products like designing source code, and test cases.
* Object-oriented software engineering processes, like designing and coding. 
* Object-oriented software engineering people, like the productivity of an individual designer.

Object-oriented metrics are different due to following reasons:
* Localization
* Encapsulation
* Information hiding
* Inheritance
* Object abstraction techniques

Calculations of class-level and method level code metrics in java projects by means of static analysis
* FAN-IN: Counts the number of classes that reference a specific class, or the amount of input dependencies a class has. Consider a class X as an example. The fan-in of X would be the number of classes that reference X as an attribute, access X's attributes, use X's methods, etc.


* FAN-OUT: Calculates the number of other classes that a given class references, or the amount of output dependencies a class has. To put it another way, the number of classes called by a certain class, X, via attribute references, method invocations, object instances, etc.


* DIT (Depth Inheritance Tree): It keeps track of how many "fathers" each class has. Every class has at least one DIT (everyone inherits java.lang.Object). Classes must be present in the project for it to work (for example, if a class depends on X, which is dependent on a jar/dependency file, and X depends on other classes, DIT is counted as 2).


* NC (Number of Children): It keeps track of how many immediate subclasses a given class has.


* Number of fields: Determines the quantity of fields. The precise amount of static, public, private, protected, default, final, and synchronised fields, as well as the total number of fields.


* Number of methods: Counts the variety of techniques. Total method count, static, public, abstract, private, protected, default, final, and synchronised method counts are all specified. Constructor methods are included in this.


* Number of public fields: Counts total number of public fields


* Number of public method: Counts total number of public methods


* LOC (Lines of code): It is Source Lines of Code, or SLOC, and it counts the lines of code while disregarding blank lines and comments.


* Lack of cohesion in methods (LCOM): It measures the number of “connected components” in a class. LCOM for a class will range between 0 and 1, with 0 being totally cohesive and 1 being totally non-cohesive.


* Cyclomatic complexity(CC): It is a software metric used to indicate the complexity of a program. It is a quantitative measure of the number of linearly independent paths through a program's source code.


* Weighted method per class(WMC): It measure is an aggregate count of the number of methods in each class. This count includes constructors and destructors of the class.

## Tools
* Java8: It includes a huge upgrade to the Java programming model and a coordinated evolution of the JVM, Java language, and libraries.


* IntelliJ: It is an integrated development environment written in Java for developing computer software written in Java, Kotlin, Groovy, and other JVM-based languages.


* Eclipse's JDT Core library: It is Java infrastructure of the Java IDE. It has no built-in JDK version dependencies, it also does not depend on any particular Java UI and can be run headless.
Maven: It is a popular open-source build tool developed by the Apache Group to build, publish, and deploy several projects at once for better project management.

## Compilation
We use maven to develop and build this application with the help of Eclipse IDE and libraries.
To create a runnable jar, run the following command in the directory where the repository is cloned:
```text
mvn clean install
```



## Credits

- Neha Kumari


