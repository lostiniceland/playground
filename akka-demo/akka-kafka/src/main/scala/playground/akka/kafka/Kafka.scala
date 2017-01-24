package playground.akka.kafka

import java.util
import java.util.Properties

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import org.apache.kafka.clients.consumer.{ConsumerRecords, KafkaConsumer}
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import org.apache.kafka.common.serialization.{StringDeserializer, StringSerializer}
import playground.akka.{StartMessage, StopMessage}


case class KafkaMessage(body: String)
private case object ConsumerStopped

class Kafka extends Actor with ActorLogging {


  val producer: KafkaProducer[String, String] = {
    val props = new Properties()
    props.put("bootstrap.servers", "localhost:9092")
    props.put("key.serializer", classOf[StringSerializer].getName)
    props.put("value.serializer", classOf[StringSerializer].getName)
    new KafkaProducer(props)
  }

  val consumerActor: ActorRef =  context.actorOf(Props[Consumer])

  override def receive: Receive = {

    case StartMessage =>
      log.info("Activating Kafka-Producer")
      consumerActor.forward(StartMessage)

    case message: KafkaMessage =>
      producer.send(new ProducerRecord[String, String]("test", message.body))

    case StopMessage =>
      consumerActor ! StopMessage

    case ConsumerStopped =>
      context.stop(self)
      log.info("Kafka-Producer stopped")
  }
}

private class Consumer extends Actor with ActorLogging {

  object ConsumeNext

  val consumer: KafkaConsumer [String, String] = {
      val props = new Properties()
      props.put("bootstrap.servers", "localhost:9092")
      props.put("group.id", "test")
      props.put("key.deserializer", classOf[StringDeserializer].getName)
      props.put("value.deserializer", classOf[StringDeserializer].getName)
      new KafkaConsumer(props)
    }
  val topics: util.List[String] = util.Arrays.asList("test")

  override def receive: Receive = inactive

  def inactive: Receive = {
    case StartMessage =>
      log.info("Activating Kafka-Consumer")
      consumer.subscribe(topics)
      context.become(active)
      self ! ConsumeNext
  }

  def active: Receive = {
    case ConsumeNext =>
      val records: ConsumerRecords[String, String] = consumer.poll(500)
      log.info(s"Consumed '${records.count()}' messages from Kafka.")
      records.forEach((rec) => println(s"Message '${rec.value()}' received"))
      self ! ConsumeNext
    case StopMessage =>
      context.become(inactive)
      consumer.close()
      log.info("Kafka-Consumer stopped")
      sender() ! ConsumerStopped
  }
}





