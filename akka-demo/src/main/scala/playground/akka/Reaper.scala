package playground.akka

import akka.actor._

import scala.collection.mutable.ArrayBuffer

object Reaper {
  // Used by others to register an Actor for watching
  case class WatchMe(ref: ActorRef)
}

abstract class Reaper extends Actor {
  import Reaper._

  // Keep track of what we're watching
  val watched: ArrayBuffer[ActorRef] = ArrayBuffer.empty[ActorRef]

  // Derivations need to implement this method.  It's the
  // hook that's called when everything's dead
  def allSoulsReaped(): Unit

  // Watch and check for termination
  final def receive: PartialFunction[Any, Unit] = {
    case WatchMe(ref) =>
      context.watch(ref)
      watched += ref
    case Terminated(ref) =>
      watched -= ref
      if (watched.isEmpty) allSoulsReaped()
  }
}

class ProductionReaper extends Reaper {
  // Shutdown
  def allSoulsReaped(): Unit = {
    println("All actors reaped...shutting down")
    context.system.terminate()
  }
}