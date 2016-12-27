package playground.akka.kafka

import java.util
import java.util.Properties

import akka.actor.{Actor, ActorRef, Props}
import com.typesafe.scalalogging.Logger
import org.apache.kafka.clients.consumer.{ConsumerRecords, KafkaConsumer}
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import org.apache.kafka.common.serialization.{StringDeserializer, StringSerializer}
import playground.akka.{StartMessage, StopMessage}


case class KafkaMessage(body: String)
private case object ConsumerStopped

class Kafka extends Actor {

  val logger = Logger(classOf[Kafka])

  val producer: KafkaProducer[String, String] = {
    val props = new Properties()
    props.put("bootstrap.servers", "localhost:9092")
    props.put("key.serializer", classOf[StringSerializer].getName)
    props.put("value.serializer", classOf[StringSerializer].getName)
    props.put("batch.size", "10")
    new KafkaProducer(props)
  }

  val consumerActor: ActorRef =  context.actorOf(Props[Consumer])

  override def receive: Receive = {

    case StartMessage =>
      logger.info("Activating Kafka-Producer")
      consumerActor.forward(StartMessage)

    case message: KafkaMessage =>
      producer.send(new ProducerRecord[String, String]("test", message.body))

    case StopMessage =>
      consumerActor ! StopMessage

    case ConsumerStopped =>
      context.stop(self)
      logger.info("Kafka-Producer stopped")
  }
}

private class Consumer extends Actor {

  object ConsumeNext

  val logger = Logger(classOf[Consumer])

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
      logger.info("Activating Kafka-Consumer")
      consumer.subscribe(topics)
      context.become(active)
      self ! ConsumeNext
  }

  def active: Receive = {
    case ConsumeNext =>
      val records: ConsumerRecords[String, String] = consumer.poll(500)
      records.forEach((rec) => println(s"Message '${rec.value()}' received"))
      self ! ConsumeNext
    case StopMessage =>
      context.become(inactive)
      consumer.close()
      logger.info("Kafka-Consumer stopped")
      sender() ! ConsumerStopped
  }
}





