package squareoneinsights.impl.producer

import akka.Done
import akka.actor.{Actor, ActorLogging, ActorSystem}
import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import akka.stream.Materializer
import akka.stream.scaladsl.{Sink, Source}
import com.typesafe.config.ConfigFactory
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import squareoneinsights.api.models.Send
import squareoneinsights.impl.consumer.kafkaConsumerPipeline

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}


class KafkaProducerActor(implicit system: ActorSystem,
                         executionContext: ExecutionContext,
                         materializer: Materializer) extends Actor with ActorLogging {


  private val config = ConfigFactory.load()
  val topic = config.getString("topic")
  val producerSettings = ProducerSettings(system, new StringSerializer, new StringSerializer)
    .withBootstrapServers(config.getString("kafkaBootstrapServer"))

  val kafkaSink: Sink[ProducerRecord[String, String], Future[Done]] = Producer.plainSink(producerSettings)
  val kafkaConsumerPipeline = new kafkaConsumerPipeline()

  override def receive: Receive = {
    case Send(topic, message) =>
      val producerRecord = new ProducerRecord[String, String](topic, message)

      val result = Source.single(producerRecord).runWith(kafkaSink)
      result.onComplete {
        case Success(Done) =>
          log.info(s"Successfully sent message to Kafka topic: $topic")
          kafkaConsumerPipeline.employeeConsumerPipeline.run()

        case Failure(ex) =>
          log.error(s"Failed to send message to Kafka topic: $topic", ex.getMessage)
      }
    case _ => log.error(s"Invalid message Request for the topic: $topic")
  }

  override def postStop(): Unit = {
    super.postStop()
    log.info("Kafka producer actor stopped")
  }
}
