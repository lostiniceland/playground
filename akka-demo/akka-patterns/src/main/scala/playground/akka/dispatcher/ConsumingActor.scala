package playground.akka.dispatcher

import akka.actor.{Actor, ActorLogging}


case class EventMessage(body: String)

class ConsumingActor extends Actor with ActorLogging{
  override def receive: Receive = {
    case event : EventMessage => log.info(event.body)
  }
}
