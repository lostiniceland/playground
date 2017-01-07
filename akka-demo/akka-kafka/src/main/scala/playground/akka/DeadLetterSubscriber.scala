package playground.akka

import akka.actor.{Actor, ActorLogging, DeadLetter}

class DeadLetterSubscriber extends Actor with ActorLogging {
  override def receive: Receive = {
    case deadLetter: DeadLetter =>
      log.error(s"DeadLetter message '${deadLetter.message.getClass}' from '${deadLetter.sender}' to '${deadLetter.recipient}'!")
  }
}
