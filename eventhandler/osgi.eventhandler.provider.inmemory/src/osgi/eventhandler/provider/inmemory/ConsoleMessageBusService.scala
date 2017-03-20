package osgi.eventhandler.provider.inmemory

import org.osgi.service.component.annotations.Component
import osgi.eventhandler.api.{Message, MessageBusService}

@Component
class ConsoleMessageBusService extends MessageBusService {
  override def send(message: Message): Unit = println(message.body)
}
