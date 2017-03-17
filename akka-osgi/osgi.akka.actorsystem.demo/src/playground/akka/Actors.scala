package playground.akka


import akka.actor.{Actor, ActorRef, ActorSystem, Props, Terminated}
import akka.osgi.OsgiActorSystemFactory
import com.typesafe.config.ConfigFactory
import org.osgi.framework.BundleContext
import org.osgi.service.component.annotations.{Activate, Component, Deactivate, Reference}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}


case class Continue()
case class Message(body: String)

@Component
class ActorSystemService () {

  private var system : ActorSystem = _

  @Activate
  def activate(bundleContext: BundleContext) {
    try {
      // load default-config "application.conf"
      val myConfig = ConfigFactory.load(getClass.getClassLoader)
      system = OsgiActorSystemFactory(bundleContext, myConfig).createActorSystem("Test")
      println("ActorSystem started")
      val actor : ActorRef = system.actorOf(Props[ConsumingActor])
      actor ! Continue
    } catch {
      case t:Throwable => t.printStackTrace()
    }
  }

  @Deactivate
  def deactivate () {
    if(system != null) {
      val x: Future[Terminated] = system.terminate()
      x.onComplete {
        case Success(_) => system = null; println("ActorSystem stopped")
        case Failure(e) => e.printStackTrace()
      }
    }
  }

//  @Activate
//  def activate(){
//    val actor : ActorRef = system.actorOf(Props[ConsumingActor])
//    actor ! Continue
//  }
//
//  @Deactivate
//  def deactivate () {
//
//  }
//
//  @Reference
//  def bindActorSystem(actorSystem: ActorSystem): Unit ={
//    system = actorSystem
//  }
}


class ConsumingActor extends Actor {

  val outgoingActor: ActorRef = context.actorOf(Props[OutgoingActor])
  var eventCount = 0

  override def receive: Receive = {
    case Continue =>
      for(_ <- 1 to 5) {
        eventCount += 1
        outgoingActor ! Message("Event " + eventCount)
      }
      Thread.sleep(500)
      self ! Continue
  }
}


class OutgoingActor extends Actor {

  override def receive: Receive = {
    case message: Message =>
      println("Sending message to external system: " + message.body)
  }
}
