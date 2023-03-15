package squareoneinsights.impl.employee

import akka.Done
import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import com.typesafe.config.ConfigFactory
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.{ByteArraySerializer, StringSerializer}
import play.api.libs.json.Json
import squareoneinsights.api.Send

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}


/*object KafkaProducerActor {
  def props = Props(new KafkaProducerActor)

  case class Send(topic: String, message: String)
}

class KafkaProducerActor extends Actor with ActorLogging {
  import KafkaProducerActor._

  implicit val system: ActorSystem = ActorSystem("KafkaToAcquirerRule")
  implicit val ec: ExecutionContextExecutor = system.dispatcher
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  private val config = ConfigFactory.load()
  //implicit val ec: ExecutionContextExecutor    = context.system.dispatcher


  val producerSettings = ProducerSettings(context.system, new StringSerializer, new StringSerializer)
    .withBootstrapServers(config.getString("bootstrap.servers"))

  val kafkaSink: Sink[ProducerRecord[String, String], Future[Done]] = Producer.plainSink(producerSettings)

  override def receive: Receive = {
    case Send(topic, message) =>
      val producerRecord = new ProducerRecord[String, String](topic, message)
      val result = Source.single(producerRecord).runWith(kafkaSink)
      result.onComplete {
        case Success(Done) =>
          log.info(s"Successfully sent message to Kafka topic: $topic")
        case Failure(ex) =>
          log.error(s"Failed to send message to Kafka topic: $topic", ex)
      }
  }
}*/
class KafkaProducerActor extends Actor with ActorLogging {

  implicit val system: ActorSystem = ActorSystem("employee-api")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val ec: ExecutionContextExecutor    = system.dispatcher

  //case class Send(topic: String, message: String)



  val producerSettings = ProducerSettings(system, new StringSerializer, new StringSerializer)
    .withBootstrapServers("localhost:9092")

  val kafkaSink: Sink[ProducerRecord[String, String], Future[Done]] = Producer.plainSink(producerSettings)

  val topic = "my-kafka-topic"
  val kafkaConsumerPipeline = new kafkaConsumerPipeline()

  override def receive: Receive = {
    case Send(topic, message) =>
      val producerRecord = new ProducerRecord[String, String](topic, message)

      val result = Source.single(producerRecord).runWith(kafkaSink)
      result.onComplete {
        case Success(Done) =>
          println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~")
          log.info(s"Successfully sent message to Kafka topic: $topic")
          kafkaConsumerPipeline.employeeConsumerPipeline.run()

        case Failure(ex) =>
          log.error(s"Failed to send message to Kafka topic: $topic", ex)
      }
    case _ => log.error(s"Invalid message Request !!!!!!!!!!!!!!!!!!!!: $topic")
  }

  override def postStop(): Unit = {
    super.postStop()
    log.info("Kafka producer actor stopped")
  }
}