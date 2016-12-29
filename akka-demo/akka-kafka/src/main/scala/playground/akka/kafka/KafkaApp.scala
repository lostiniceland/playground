package playground.akka.kafka

import akka.actor.{ActorRef, ActorSystem, Props}
import playground.akka.Reaper.WatchMe
import playground.akka.{ProductionReaper, StartMessage, StopMessage}

import scala.io.StdIn

object KafkaApp extends App {
  val system = ActorSystem("KafkaSystem")
  // Build our reaper
  val reaper = system.actorOf(Props(new ProductionReaper()))
  val kafka: ActorRef = system.actorOf(Props[Kafka])
  // watch the reaper
  reaper ! WatchMe(kafka)
  // start them going
  kafka ! StartMessage
  println("Type message in console (terminates when empty)")
  kafka ! KafkaMessage("Hello World")
  Iterator.continually(StdIn.readLine).takeWhile(_.nonEmpty).foreach(line => kafka ! KafkaMessage(line))
  kafka ! StopMessage
}
