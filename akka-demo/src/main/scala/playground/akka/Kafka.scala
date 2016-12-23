package playground.akka

import java.util
import java.util.Properties

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import org.apache.kafka.clients.consumer.{ConsumerRecords, KafkaConsumer}
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import org.apache.kafka.common.serialization.{StringDeserializer, StringSerializer}
import playground.akka.Reaper.WatchMe
import scala.io.StdIn

case class SendMessage (body: String)

class Kafka extends Actor {

  val producer: KafkaProducer[String, String] = {
    val props = new Properties()
    props.put("bootstrap.servers", "localhost:9092")
    props.put("key.serializer", classOf[StringSerializer].getName)
    props.put("value.serializer", classOf[StringSerializer].getName)
    props.put("batch.size", "10")
    new KafkaProducer(props)
  }

  val consumer: ActorRef =  context.actorOf(Props[Consumer])

  override def receive: Receive = {

    case StartMessage =>
      consumer.forward(StartMessage)

    case message: SendMessage =>
      producer.send(new ProducerRecord[String, String]("test", message.body))

    case StopMessage =>
      println("Stopping Kafka-Consumer")
      context.stop(consumer)
      context.stop(self)
  }
}

private class Consumer extends Actor {

  val consumer: KafkaConsumer [String, String] = {
      val props = new Properties()
      props.put("bootstrap.servers", "localhost:9092")
      props.put("group.id", "test")
      props.put("key.deserializer", classOf[StringDeserializer].getName)
      props.put("value.deserializer", classOf[StringDeserializer].getName)
      new KafkaConsumer(props)
    }

  val topics: util.List[String] = util.Arrays.asList("test")

  override def receive: Receive = {
    case StartMessage =>
      try {
        consumer.subscribe(topics)
        while (!Thread.currentThread().isInterrupted) {
          val records: ConsumerRecords[String, String] = consumer.poll(500)
          records.forEach((rec) => println(s"Message '${rec.value()}' reived"))
        }
      } catch {
        case e: Exception => e.printStackTrace()
      } finally {
        consumer.close()
      }
  }
}



object KafkaApp extends App {
  val system = ActorSystem("KafkaSystem")
  // Build our reaper
  val reaper = system.actorOf(Props(new ProductionReaper()))
  val kafka: ActorRef = system.actorOf(Props[Kafka])
  // watch the reaper
  reaper ! WatchMe(kafka)
  // start them going
  kafka ! StartMessage
  kafka ! SendMessage("Hello")
  kafka ! SendMessage("World")
  Iterator.continually(StdIn.readLine).takeWhile(_.nonEmpty).foreach(line => kafka ! SendMessage(line))
  kafka ! StopMessage
}

