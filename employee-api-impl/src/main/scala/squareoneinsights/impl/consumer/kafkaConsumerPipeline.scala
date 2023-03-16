package squareoneinsights.impl.consumer

import akka.Done
import akka.actor.ActorSystem
import akka.kafka.scaladsl.Consumer
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.stream.scaladsl.{Keep, RunnableGraph, Sink}
import com.datastax.oss.driver.api.core.CqlSession
import com.lightbend.lagom.internal.broker.kafka.ConsumerConfig
import com.typesafe.config.ConfigFactory
import org.apache.kafka.common.serialization.StringDeserializer
import org.slf4j.{Logger, LoggerFactory}
import play.api.libs.json.Json
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile
import squareoneinsights.api.models.AddEmployeeRequest
import squareoneinsights.impl.db.{CassandraEmployeeRepository, EmployeeRepository, PostgresEmployeeRepository}

import java.net.InetSocketAddress
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt


class kafkaConsumerPipeline(implicit system: ActorSystem) {
  val log: Logger = LoggerFactory.getLogger("kafkaConsumerPipeline")

  private val config = ConfigFactory.load()
  val dbProfile = config.getString("ifrm.db.profile")
  val hostName = config.getString("hostname")
  val portName = config.getInt("portname")
  val dataCenter = config.getString("datacenter")
  val groupId = config.getString("groupId")
  val topic = config.getString("topic")

  val databaseConfig = DatabaseConfig.forConfig[JdbcProfile](dbProfile)

  val sessionCql = CqlSession.builder()
    .addContactPoint(new InetSocketAddress(hostName, portName))
    .withLocalDatacenter(dataCenter)
    .build()

  implicit val postgresRepository: EmployeeRepository = new PostgresEmployeeRepository(databaseConfig)
  implicit val cassandraRepository: EmployeeRepository = new CassandraEmployeeRepository(sessionCql)

  val p: Option[StringDeserializer] = Some(new StringDeserializer)
  val q: Option[StringDeserializer] = Some(new StringDeserializer)
  private val employeeConsumerSettings = ConsumerSettings(system, p, q)
    .withBootstrapServers(config.getString("kafkaBootstrapServer"))
    .withGroupId(groupId)
    .withProperty(ConsumerConfig.configPath, "earliest")
    .withStopTimeout(0.seconds)


  def employeeConsumerPipeline: RunnableGraph[Future[Done]] = Consumer
    .atMostOnceSource(employeeConsumerSettings, Subscriptions.topics(topic))
    .mapAsync(1) { record =>
      val request = Json.parse(record.value()).as[AddEmployeeRequest]
      val repository = request.dbType match {
        case "C" => cassandraRepository
        case "P" => postgresRepository
        case _ => throw new Exception("Invalid Database Type Request")
      }
      log.info(s"Adding employee data ${request.content} to ${repository.getClass.getName} database")
      repository.addEmployee(request.content)
    }
    .toMat(Sink.ignore)(Keep.right)
}