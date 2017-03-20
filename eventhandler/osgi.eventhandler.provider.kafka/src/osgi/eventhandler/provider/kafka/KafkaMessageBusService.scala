package osgi.eventhandler.provider.kafka

import org.osgi.service.component.annotations.Component
import osgi.eventhandler.api.{Message, MessageBusService}

@Component
class KafkaMessageBusService extends MessageBusService {
  override def send(message: Message): Unit = {
    // TODO use Kafka
    println(message.body)
  }
}
