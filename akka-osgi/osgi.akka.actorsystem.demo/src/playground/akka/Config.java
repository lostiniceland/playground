package playground.akka;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(pid = "osgi.akka.actorsystem.demo", name = "AktorSystem",
        description = "Configures the ActorSystem provided by Bundle 'osgi.akka.actorsystem.demo'")
@interface AkkaConfiguration {

    @AttributeDefinition(description = "The name for this actor system")
    int parallelThreads() default 1;

    @AttributeDefinition(description = "Amount of parallel producer thread")
    String actorSystemName() default "ActorSystem";
}