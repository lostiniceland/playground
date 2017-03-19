package playground.akka;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(pid = "osgi.akka.actorsystem.messagehandler", name = "AkkaMessageHandler",
        description = "Configures the message-handling capabilities using Actors")
@interface AkkaConfiguration {

    @AttributeDefinition(description = "Amount of parallel producer threads")
    int parallelThreads() default 2;

    @AttributeDefinition(description = "The name for this actor-system (used as OSGi-service property)")
    String actorSystemName() default "MessageHandler-ActorSystem";
}