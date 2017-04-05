# Eventhadler

This project is a simple demo to test several reactive Java frameworks. The usecase is a eventhandler 
which has to distribute internal events to an external system (like Kafka).

Another goal is to test how well those frameworks can be integrated in an OSGi environment and how
to make use of OSGi services and configuration capabilities.

Last but not least, the project uses Scala. This is just as a reference to see how Scala can be integrated
in Bndtools (Bnd Workspace).

## Build an Run

The project comes with a Gradle wrapper and can be build  easily. Currently only the Akka sample is doing anything.

    // Windows
    gradlew.bat jar export.launch-akka
    // Linux
    ./gradlew jar export.launch-akka
    
    // Running the generated executable
    java -Dfelix.fileinstall.dir=configuration\config-admin -jar osgi.eventhandler.akka\generated\distributions\executable\launch-akka.jar

## Akka

The module `osgi.eventhandler.akka` starts a single ActorSystem once the necessary configuration is available.
The `OutgoingActor` has a dependency on a `MessageBusService` and there are currently two implementations available

1. `ConsoleMessageBusService` in `osgi.eventhandler.provider.inmemory` which simple prints each event to the console.
This is the default configuration in `launch-akka.bndrun`
1. `KafkaMessageBusService` in `osgi.eventhandler.provider.kafka` though currently the kafka-clients lib doesn't 
provide OSGi metadata (see [Pull-Request](https://github.com/apache/kafka/pull/2807)).

If the dependency to the MessageBusService isn't satisfied, or is removed during runtime, the ActorSystem wont start 
(stopped in case of runtime-removal).

### Configuration

The ActorSystem is only started if a valid configuration is supplied.

#### File based using Felix Fileinstall

The application comes with Felix Fileinstall, a frontend for the OSGi ConfigurationAdmin. The configuration is provided
by simple property files.

The [default configuration](configuration/config-admin/osgi.eventhandler.akka.ActorSystemService.cfg) is already 
provided and can be changed during runtime (the folder will be polled every 2 seconds)

#### UI with Felix Webconsole

Once the application runs, a http server is available with the Felix Webconsole. 

1. Open [localhost:8080/system/console/configMgr](localhost:8080/system/console/configMgr) with `admin` `admin` credentials.
1. The configuration is under `Eventhandler ActorSystem`
 
Changing the values in the Webconsole will reflect in the Fileinstall config-files.

## Vertx

Nothing done yet

## RxJava

Nothing done yet...does it make sense at all?