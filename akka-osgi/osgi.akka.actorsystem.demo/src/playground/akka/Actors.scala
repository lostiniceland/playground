package playground.akka


import java.util.{Dictionary, Properties}

import akka.actor.{Actor, ActorRef, ActorSystem, Props, Terminated}
import akka.osgi.OsgiActorSystemFactory
import com.typesafe.config.ConfigFactory
import org.osgi.framework.{BundleContext, ServiceRegistration}
import org.osgi.service.component.annotations._
import org.osgi.service.metatype.annotations.{AttributeDefinition, ObjectClassDefinition}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

//@ObjectClassDefinition(pid = Array("Test"), name = "AktorSystem",
//  description = "Configures the ActorSystem provided by Bundle 'osgi.akka.actorsystem.demo'")
//trait AkkaConfiguration {
//
//  @AttributeDefinition(description = "The name for this actor system")
//  def actorSystemName() : String
//
//  @AttributeDefinition(description = "Amount of parallel producer thread")
//  def parallelThreads() : Int
//}


case class Continue()
case class Message(body: String)


@Component(configurationPolicy = ConfigurationPolicy.REQUIRE, configurationPid = Array("osgi.akka.actorsystem.demo"))
class ActorSystemService () {

  private var system : ActorSystem = _
  private var serviceRegistration : ServiceRegistration[ActorSystem] = _

  @Activate
  def activate(config: AkkaConfiguration, bundleContext: BundleContext) {
    try {
      // load default-config "application.conf"
      val myConfig = ConfigFactory.load(getClass.getClassLoader)
      system = OsgiActorSystemFactory(bundleContext, myConfig).createActorSystem(config.actorSystemName())
      println("ActorSystem started")

      val serviceProps = new Properties()
      serviceProps.put("name", system.name)
      serviceRegistration = bundleContext.registerService(classOf[ActorSystem],system,serviceProps.asInstanceOf[Dictionary[String, Any]])

      val actor : ActorRef = system.actorOf(Props(new ConsumingActor(config.parallelThreads())))
      actor ! Continue
    } catch {
      case t:Throwable =>
        t.printStackTrace()
        if(serviceRegistration != null){
          serviceRegistration.unregister()
        }
    }
  }

  @Deactivate
  def deactivate () {
    if(system != null) {
      if(serviceRegistration != null){
        serviceRegistration.unregister()
      }
      val x: Future[Terminated] = system.terminate()
      x.onComplete {
        case Success(_) => system = null; println("ActorSystem stopped")
        case Failure(e) => e.printStackTrace()
      }
    }
  }

}


class ConsumingActor(threadCount: Int) extends Actor {

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
