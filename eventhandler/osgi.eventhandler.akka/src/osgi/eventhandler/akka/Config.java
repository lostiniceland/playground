package osgi.eventhandler.akka;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(name = "Eventhandler ActorSystem",
        description = "Configures the message-handling capabilities using Actors")
@interface AkkaConfiguration {

    @AttributeDefinition(description = "The name for this actor-system (used as OSGi-service property)")
    String actorSystemName();

    @AttributeDefinition(description = "Amount of parallel producer threads")
    int parallelThreads();
}