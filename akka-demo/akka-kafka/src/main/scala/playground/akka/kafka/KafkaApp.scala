package playground.akka.kafka

import akka.actor.{ActorRef, ActorSystem, DeadLetter, Props}
import playground.akka.Reaper.WatchMe
import playground.akka.{DeadLetterSubscriber, ProductionReaper, StartMessage, StopMessage}

import scala.io.StdIn

object KafkaApp extends App {
  val system = ActorSystem("KafkaSystem")
  // Register  DeadLetter-Listener via EventStream
  val sysListener = system.actorOf(Props[DeadLetterSubscriber], "sysListener")
  system.eventStream.subscribe(sysListener, classOf[DeadLetter])
  // Build our reaper
  val reaper = system.actorOf(Props(new ProductionReaper()))
  val kafka: ActorRef = system.actorOf(Props[Kafka])
  // watch the reaper
  reaper ! WatchMe(kafka)
  // start them going
  kafka ! StartMessage
  println("Type message in console (terminates when empty)")
  Iterator.continually(StdIn.readLine).takeWhile(_.nonEmpty).foreach(line => kafka ! KafkaMessage(line))
  kafka ! StopMessage
}
