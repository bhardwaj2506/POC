package squareoneinsights.impl.producer

import akka.Done
import akka.actor.{Actor, ActorSystem}
import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import akka.stream.Materializer
import akka.stream.scaladsl.{Sink, Source}
import com.typesafe.config.ConfigFactory
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import org.slf4j.{Logger, LoggerFactory}
import play.api.Logging
import squareoneinsights.api.models.Send
import squareoneinsights.impl.consumer.kafkaConsumerPipeline

import scala.concurrent.{ExecutionContextExecutor, Future}


class KafkaProducerActor(implicit system: ActorSystem,
                         executionContext: ExecutionContextExecutor,
                         materializer: Materializer) extends Actor with Logging {

  val log: Logger = LoggerFactory.getLogger(classOf[KafkaProducerActor])
  private val config = ConfigFactory.load()
  val topic: String = config.getString("topic")
  private val producerSettings = ProducerSettings(system, new StringSerializer, new StringSerializer)
    .withBootstrapServers(config.getString("kafkaBootstrapServer"))

  private val kafkaSink: Sink[ProducerRecord[String, String], Future[Done]] = Producer.plainSink(producerSettings)
  val kafkaConsumerPipeline = new kafkaConsumerPipeline()

  override def receive: Receive = {
    case Send(topic, message) => sendMessageToKafkaConsumer(topic, message)
    case _ => throw new IllegalStateException(s"Error occurred while sending message to kafka consumer")
  }

  private def sendMessageToKafkaConsumer(topic: String, message: String): Future[Done] = {
    val producerRecord = new ProducerRecord[String, String](topic, message)
    Source.single(producerRecord).runWith(kafkaSink).recover {
      case ex: Exception =>
        log.error(s"Error while producing employee records : ${ex.getMessage}")
        throw new RuntimeException(s"Error while producing employee records : ${ex.getMessage}")
    }
    kafkaConsumerPipeline.employeeConsumerPipeline.run()
      .recover {
        case ex: Exception =>
          log.error(s"Error while consuming employees records from kafka consumer : ${ex.getMessage}")
          (throw new RuntimeException(s"Error while consuming employees records from kafka consumer : : ${ex.getMessage}"))
      }
  }
}
