package osgi.eventhandler.akka

import java.util.{Dictionary, Properties}

import akka.actor.{Actor, ActorRef, ActorSystem, Props, Terminated}
import akka.osgi.OsgiActorSystemFactory
import com.typesafe.config.ConfigFactory
import org.osgi.framework.{BundleContext, ServiceRegistration}
import org.osgi.service.component.annotations._
import org.osgi.service.metatype.annotations.Designate
import osgi.eventhandler.api.{Message, MessageBusService}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

// FIXME Bnd annotations not working on Scala traits (workaround is using a plain Java Interface)
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

@Component(configurationPolicy=ConfigurationPolicy.REQUIRE)
@Designate(ocd=classOf[AkkaConfiguration])
class ActorSystemService () {

  private var system : ActorSystem = _
  private var serviceRegistration : ServiceRegistration[ActorSystem] = _
  private var messageBusService : MessageBusService = _

  @Activate
  def activate(config: AkkaConfiguration, bundleContext: BundleContext) {
    try {
      // load default-config "application.conf"
      val myConfig = ConfigFactory.load(getClass.getClassLoader)
      system = OsgiActorSystemFactory(bundleContext, myConfig).createActorSystem(config.actorSystemName())

      val serviceProps = new Properties()
      serviceProps.put("actorSystemName", system.name)
      serviceRegistration = bundleContext.registerService(classOf[ActorSystem],system,serviceProps.asInstanceOf[Dictionary[String, Any]])



      val actor : ActorRef = system.actorOf(Props(new ConsumingActor(createSendingActor(system, config.parallelThreads(), messageBusService))))
      actor ! Continue
      println(s"ActorSystem '${config.actorSystemName()}' started")
    } catch {
      case t:Throwable =>
        t.printStackTrace()
        if(serviceRegistration != null){
          serviceRegistration.unregister()
        }
    }
  }

  def createSendingActor(system: ActorSystem, threadCount: Int, messageBusService: MessageBusService) : ActorRef = {
      if(threadCount > 1) {
        println(s"Starting ${classOf[OutgoingActor].getSimpleName} with Balancing-Dispatcher in ${threadCount} Threads." )
        val consumerProvider : List[ActorRef] =
          for(_ <- 1.to(threadCount).toList) yield {
            system.actorOf(Props(new OutgoingActor(messageBusService))
              .withDispatcher("consumer-dispatcher"))}
        consumerProvider.head
      } else {
        println(s"Starting single-threaded ${classOf[OutgoingActor].getSimpleName}")
        system.actorOf(Props(new OutgoingActor(messageBusService)))
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
        case Success(_) => println(s"ActorSystem '${system.name}' stopped")
        case Failure(e) => e.printStackTrace()
      }
    }
  }


  @Reference
  def bindMessageBusService(service: MessageBusService): Unit ={
    messageBusService = service
  }

}


class ConsumingActor(outgoingActor: ActorRef) extends Actor {

  private var eventCount = 0

  override def receive: Receive = {
    case Continue =>
      for(_ <- 1 to 10) {
        eventCount += 1
        outgoingActor ! Message("Event " + eventCount)
      }
      Thread.sleep(2000)
      self ! Continue
  }
}


class OutgoingActor(messageBusService: MessageBusService) extends Actor {

  override def receive: Receive = {
    case message: Message =>
      messageBusService.send(message)
  }
}
