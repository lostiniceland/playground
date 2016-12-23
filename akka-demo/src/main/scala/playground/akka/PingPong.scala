package playground.akka

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import playground.akka.Reaper.WatchMe


case object PingMessage
case object PongMessage


class Ping(pong: ActorRef) extends Actor {
  var count = 0
  def incrementAndPrint() { count += 1; println("ping") }
  def receive: PartialFunction[Any, Unit] = {
    case StartMessage =>
      incrementAndPrint()
      pong ! PingMessage
    case PongMessage =>
      incrementAndPrint()
      if (count > 99) {
        sender ! StopMessage
        println("ping stopped")
        context.stop(self)
      } else {
        sender ! PingMessage
      }
  }
}

class Pong extends Actor {
  def receive: PartialFunction[Any, Unit] = {
    case PingMessage =>
      println("  pong")
      sender ! PongMessage
    case StopMessage =>
      println("pong stopped")
      context.stop(self)
  }
}


object PingPong extends App {
  val system = ActorSystem("PingPongSystem")
  // Build our reaper
  val reaper = system.actorOf(Props(new ProductionReaper()))
  val pong: ActorRef = system.actorOf(Props[Pong], name = "pong")
  val ping: ActorRef = system.actorOf(Props(new Ping(pong)), name = "ping")
  // watch the reaper
  reaper ! WatchMe(pong)
  reaper ! WatchMe(ping)
  // start them going
  ping ! StartMessage
}
