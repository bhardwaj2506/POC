package squareoneinsights.impl.employee

import akka.Done
import akka.actor.ActorSystem
import akka.actor.TypedActor.context
import akka.kafka.scaladsl.Consumer
import akka.kafka.{ConsumerMessage, ConsumerSettings, Subscriptions}
import akka.stream.{ActorMaterializer, Materializer}
import akka.stream.scaladsl.{Keep, RunnableGraph, Sink, Source}
import com.lightbend.lagom.internal.broker.kafka.ConsumerConfig
import com.typesafe.config.ConfigFactory
import org.apache.kafka.common.serialization.StringDeserializer
import play.api.libs.json.Json
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile
import squareoneinsights.api.{AddEmployeeRequest, Employee}

import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future}


trait EmployeeRepository {
  def addEmployee(name: String): Future[Employee]
}

class kafkaConsumerPipeline(implicit system: ActorSystem,
                            executionContext: ExecutionContext,
                            materializer: Materializer) {
  //val dbProfile = ConfigFactory.load().getString("postgre.profile")
  val dbProfile = ConfigFactory.load().getString("ifrm.db.profile")

  val databaseConfig = DatabaseConfig.forConfig[JdbcProfile](dbProfile)
  implicit val postgresRepository: EmployeeRepository = new PostgresEmployeeRepository(databaseConfig)
  // implicit val cassandraRepository: EmployeeRepository = ???

  //implicit val system: ActorSystem = ActorSystem("KafkaToAcquirerRule")
  //implicit val ec: ExecutionContextExecutor = system.dispatcher
  //implicit val materializer: ActorMaterializer = ActorMaterializer()
  // private val config = ConfigFactory.load()
 // implicit val ec: ExecutionContextExecutor = context.system.dispatcher

  //val employeeConsumerSettings: ConsumerSettings[String, String] = ???
  val kafkaBootstrapServers: String = "localhost:9092"
    //ConfigFactory.load().getString("localhost:9092")
    val groupId: String ="groupId"
  //    ConfigFactory.load().getString("kafkaToCassandraOracleGroupId")
  val p: Option[StringDeserializer] = Some(new StringDeserializer)
  val q: Option[StringDeserializer] = Some(new StringDeserializer)
  val employeeConsumerSettings = ConsumerSettings(system, p, q)
    .withBootstrapServers(kafkaBootstrapServers)
    .withGroupId(groupId)
    .withProperty(ConsumerConfig.configPath, "earliest")
    .withStopTimeout(0.seconds)

  val topic = "my-kafka-topic"
  val kafkaSource: Source[ConsumerMessage.CommittableMessage[String, String], Consumer.Control] =
    Consumer.committableSource(employeeConsumerSettings, Subscriptions.topics(topic)).log("KafkaSource")

  def employeeConsumerPipeline: RunnableGraph[Future[Done]] = Consumer
    .atMostOnceSource(employeeConsumerSettings, Subscriptions.topics("my-kafka-topic"))
    .mapAsync(1) { record =>
      println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^"+record)
      val request = Json.parse(record.value()).as[AddEmployeeRequest]
      println("************************************8"+ request)
      val repository = request.dbType match {
        //        case "C" =>
        //          cassandraRepository
        case "P" =>
          println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%")
          postgresRepository


        case _ =>
          println("Invalid Database Type")
          throw new Exception("Invalid Database Type")
      }
      println("#####     ###### " + repository)
      repository.addEmployee(request.content)
     // Future.successful(Done)
    }
    .toMat(Sink.ignore)(Keep.right)
}