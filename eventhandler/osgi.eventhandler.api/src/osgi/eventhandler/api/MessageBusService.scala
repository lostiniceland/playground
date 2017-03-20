package osgi.eventhandler.api

import org.osgi.annotation.versioning.ProviderType

case class Message(body: String)

@ProviderType
trait MessageBusService {

  def send(message: Message)

}
