package squareoneinsights.impl

import akka.actor.{ActorSystem, Props}
import akka.stream.ActorMaterializer
import cats.data.Validated.{Invalid, Valid}
import cats.data.ValidatedNel
import cats.implicits._
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.api.transport.BadRequest
import com.typesafe.config.ConfigFactory
import org.slf4j.{Logger, LoggerFactory}
import play.api.libs.json.Json
import squareoneinsights.api.EmployeeapiService
import squareoneinsights.api.models.{AddEmployeeRequest, AddEmployeeResponse, Send}
import squareoneinsights.impl.producer.KafkaProducerActor

import scala.concurrent.{ExecutionContext, Future}


class EmployeeapiServiceImpl(implicit ec: ExecutionContext) extends EmployeeapiService {
  private final val log: Logger =
    LoggerFactory.getLogger(classOf[EmployeeapiServiceImpl])

  implicit val system: ActorSystem = ActorSystem("employee-api")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  private val config = ConfigFactory.load()
  val topic = config.getString("topic")

  val kafkaProducerActor = system.actorOf(Props(new KafkaProducerActor))
  override def addEmployee(): ServiceCall[AddEmployeeRequest, AddEmployeeResponse] = { request =>
    validaRequest(request) match {
      case Valid(_) =>
        log.info(s"Making request for Adding employee name ${request.employeeName} into ${if (request.dbType.equals("P")) "Postgres" else "Cassandra "} database")
        kafkaProducerActor ! Send(topic, Json.toJson(request).toString())
        Future(AddEmployeeResponse("Message send successfully"))

      case Invalid(e) =>
        log.info(s"Error while sending employee records to producer: ${e.head}")
        throw BadRequest(e.head)
    }
  }

  def validaRequest(request: AddEmployeeRequest): ValidatedNel[String, AddEmployeeRequest] = {
    val validName = validateName(request.employeeName)
    val validDbType = validateDbType(request.dbType)
    (validName, validDbType).mapN((_, _) => request)
  }

  private def validateName(name: String): ValidatedNel[String, String] =
    if (name.nonEmpty) name.validNel else "Employee Name cannot be empty".invalidNel

  private def validateDbType(dbType: String): ValidatedNel[String, String] =
    if (dbType.contains("P") || dbType.contains("C")) dbType.validNel else "DbType only have P for Postgres or C for Cassandra".invalidNel
}
