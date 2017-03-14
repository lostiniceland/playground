package playground.akka

import akka.actor.{Actor, ActorRef, ActorSystem, DeadLetter, Props}
import playground.akka.Reaper.WatchMe

import scala.io.StdIn



case class Message(body: String)


object App extends App {
  val system = ActorSystem("KafkaSystem")
  // Register  DeadLetter-Listener via EventStream
  val sysListener = system.actorOf(Props[DeadLetterSubscriber], "sysListener")
  system.eventStream.subscribe(sysListener, classOf[DeadLetter])
  // Build our reaper
  val reaper = system.actorOf(Props(new ProductionReaper()))
  val test: ActorRef = system.actorOf(Props[TestActor])
  // watch the reaper
  reaper ! WatchMe(test)
  // start them going
  println("Type message in console (terminates when empty)")
  test ! Message("Hello World")
  Iterator.continually(StdIn.readLine).takeWhile(_.nonEmpty).foreach(line => test ! Message(line))
  test ! StopMessage
}


class TestActor extends Actor {

  override def receive: Receive = {

    case message: Message =>
      println(message.body)

    case StopMessage =>
      context.stop(self)
  }
}
