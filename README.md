# ExCELL Anonymization Service

The Anonymization API takes individual GPS tracks and removes properties which are critical in terms of user privacy. It is also used internally by the ExCELL Tracking Service. Start and end points as well as intermediate stops are edited to avoid tracing back personal information such as addresses.

## Setup

This web service comes as a [SpringBoot](https://projects.spring.io/spring-boot/) application so it's very easy to test it on your local machine. If you run the service from inside a Java IDE a Tomcat server will be launched and you can access the service through a browser via localhost:45555.

### Build it

The project is using [Maven](https://maven.apache.org/) as a build tool and for managing the software dependencies. So in order to build the software you should install Maven on your machine. To create an executable JAR file for your local machine open you favourite shell environment and run:

<pre>mvn clean package</pre>

This creates a JAR file called `anonymization-0.0.1-SNAPSHOT.jar`. You can change the name in the pom.xml file.

### Run it

On your local machine run the JAR with:

<pre>java -jar anonymization-0.0.1-SNAPSHOT.jar</pre>

You might also want to change the server port

<pre>java -jar anonymization-0.0.1-SNAPSHOT.jar --server.port=45555</pre>

## API Doc

This projects provides a [Swagger](https://swagger.io/) interface to support the Open API initiative. The Java library [Springfox](http://springfox.github.io/springfox/) is used to automatically create the swagger UI configuration from annotations in the Java Spring code.

An online version of the scheduling API is available on the ExCELL Developer Portal: [Try it out!](https://www.excell-mobility.de/developer/docs.php?service=anonymization_service). You need to sign up first in order to access the services from the portal. Every user receives a token that he/she has to use for authorization for each service.


## Developers

* Stephan Pieper (BHS)


## Acknowledgement
The Anonymization Service has been realized within the ExCELL project funded by the Federal Ministry for Economic Affairs and Energy (BMWi) and German Aerospace Center (DLR) - agreement 01MD15001B.


## Contact

* spieper [at] beuth-hochschule.de

## Disclaimer

THIS SOFTWARE IS PROVIDED "AS IS" AND "WITH ALL FAULTS." 
BHS MAKES NO REPRESENTATIONS OR WARRANTIES OF ANY KIND CONCERNING THE 
QUALITY, SAFETY OR SUITABILITY OF THE SKRIPTS, EITHER EXPRESSED OR 
IMPLIED, INCLUDING WITHOUT LIMITATION ANY IMPLIED WARRANTIES OF 
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, OR NON-INFRINGEMENT.

IN NO EVENT WILL BHS BE LIABLE FOR ANY INDIRECT, PUNITIVE, SPECIAL, 
INCIDENTAL OR CONSEQUENTIAL DAMAGES HOWEVER THEY MAY ARISE AND EVEN IF 
BHS HAS BEEN PREVIOUSLY ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
