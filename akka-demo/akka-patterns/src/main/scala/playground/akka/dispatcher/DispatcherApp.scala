package playground.akka.dispatcher

import akka.actor.{ActorRef, ActorSystem, Props}

object DispatcherApp extends App {
  val system = ActorSystem("DispatcherSystem")

  val consumerProvider : List[ActorRef] =
    for(otherWorks <- (1 to 10).toList) yield {
      system.actorOf(Props[ConsumingActor]
        .withDispatcher("consumer-dispatcher"))}

  val startingConsumer = consumerProvider(0)

  for(itemCount <- 1 to 100) {
    startingConsumer ! EventMessage("Event " + itemCount)
  }

}
