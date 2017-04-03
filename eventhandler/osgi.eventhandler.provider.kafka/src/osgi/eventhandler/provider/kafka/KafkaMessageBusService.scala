package osgi.eventhandler.provider.kafka

import org.osgi.framework.BundleContext
import org.osgi.service.component.annotations.{Activate, Component}
import osgi.eventhandler.api.{Message, MessageBusService}

@Component
class KafkaMessageBusService extends MessageBusService {



  override def send(message: Message): Unit = {
    // TODO use Kafka
    println(message.body)
  }

  @Activate
  def activate(config: KafkaConfiguration, context: BundleContext) {

  }

}
