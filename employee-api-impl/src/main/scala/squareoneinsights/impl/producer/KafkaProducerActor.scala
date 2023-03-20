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
import squareoneinsights.impl.utils.Constants

import scala.concurrent.{ExecutionContextExecutor, Future}


class KafkaProducerActor(implicit system: ActorSystem,
                         executionContext: ExecutionContextExecutor,
                         materializer: Materializer) extends Actor with Logging {

  val log: Logger = LoggerFactory.getLogger(classOf[KafkaProducerActor])
  private val config = ConfigFactory.load()
  val topic: String = config.getString(Constants.TOPIC)
  private val producerSettings = ProducerSettings(system, new StringSerializer, new StringSerializer)
    .withBootstrapServers(config.getString(Constants.KAFKA_BOOTSTRAP_SERVERS))

  private val kafkaSink: Sink[ProducerRecord[String, String], Future[Done]] = Producer.plainSink(producerSettings)
  val kafkaConsumerPipeline = new kafkaConsumerPipeline()

  override def receive: Receive = {
    case Send(topic, message) => sendMessageToKafkaConsumer(topic, message)
    case _ => throw new IllegalStateException(Constants.SEND_MESSAGE_FAILURE)
  }

  private def sendMessageToKafkaConsumer(topic: String, message: String): Future[Done] = {
    val producerRecord = new ProducerRecord[String, String](topic, message)
    Source.single(producerRecord).runWith(kafkaSink).recover {
      case ex: Exception =>
        log.error(s"Error while producing employee records : ${ex.getMessage}")
        throw new RuntimeException(Constants.PRODUCING_FAILURE)
    }
    kafkaConsumerPipeline.employeeConsumerPipeline.run()
      .recover {
        case ex: Exception =>
          log.error(s"Error while consuming employees records : ${ex.getMessage}")
          throw new RuntimeException(Constants.CONSUMING_FAILURE)
      }
  }
}
