package playground.akka

import akka.actor.ActorSystem
import akka.osgi.ActorSystemActivator
import org.osgi.framework.BundleContext

class Activator extends ActorSystemActivator {

  def configure(context: BundleContext, system: ActorSystem) {
    // optionally register the ActorSystem in the OSGi Service Registry
    registerService(context, system)
  }

  override def start(context: BundleContext) {
    println("Akka service starting")
    super.start(context)
  }

  override def stop(context: BundleContext) {
    println("Akka service unregistred")
    super.stop(context)
  }

}
