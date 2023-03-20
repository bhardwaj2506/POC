package squareoneinsights.impl

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.stream.ActorMaterializer
import akka.{Done, NotUsed}
import cats.data.Validated.{Invalid, Valid}
import cats.data.ValidatedNel
import cats.implicits._
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.api.transport.BadRequest
import com.typesafe.config.ConfigFactory
import org.slf4j.{Logger, LoggerFactory}
import play.api.libs.json.Json
import squareoneinsights.api.EmployeeapiService
import squareoneinsights.api.models._
import squareoneinsights.impl.db.PostgresEmployeeRepository
import squareoneinsights.impl.producer.KafkaProducerActor
import squareoneinsights.impl.utils.Constants

import scala.concurrent.{ExecutionContextExecutor, Future}


class EmployeeapiServiceImpl(postgresEmployeeRepository: PostgresEmployeeRepository) extends EmployeeapiService {
  private final val log: Logger =
    LoggerFactory.getLogger(classOf[EmployeeapiServiceImpl])

  implicit val system: ActorSystem = ActorSystem("employee-api")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContextExecutor: ExecutionContextExecutor = system.dispatcher

  private val config = ConfigFactory.load()
  val topic: String = config.getString(Constants.TOPIC)

  private val kafkaProducerActor: ActorRef = system.actorOf(Props(new KafkaProducerActor))

  override def addEmployee: ServiceCall[AddEmployeeRequest, AddEmployeeResponse] = { request =>
    validRequest(request) match {
      case Valid(_) =>
        log.info(s"Making request for Adding employee name ${request.employeeName} into ${if (request.dbType.equals("P")) "Postgres" else "Cassandra "} database")
        kafkaProducerActor ! Send(topic, Json.toJson(request).toString())
        Future(AddEmployeeResponse(s"${request.employeeName} ${Constants.MESSAGE_SENT_SUCCESSFULLY} to ${if (request.dbType.equals("P")) "Postgres" else "Cassandra"} database"))

      case Invalid(e) =>
        log.info(s"Error while sending employee records to producer: ${e.head}")
        throw BadRequest(e.head)
    }
  }

  override def getEmployee(id: Long): ServiceCall[NotUsed, EmployeeResponse] = ServiceCall { _ =>
    postgresEmployeeRepository.get(id).flatMap {
      case Some(value) => Future(EmployeeResponse(Constants.EMPLOYEE_FOUND, value.name, value.id))
      case None => Future.failed(new Exception(Constants.EMPLOYEE_NOT_FOUND))
    }
  }

  override def updateEmployee(id: Long): ServiceCall[UpdateEmployeeRequest, EmployeeResponse] = ServiceCall { request =>
    val updateEmployee = Employee(request.name, request.id)
    postgresEmployeeRepository.update(updateEmployee)
    Future(EmployeeResponse(Constants.EMPLOYEE_UPDATED_SUCCESS, updateEmployee.name, updateEmployee.id))
  }

  override def deleteEmployee(id: Long): ServiceCall[NotUsed, Done] = ServiceCall { _ =>
    postgresEmployeeRepository.delete(id)
    Future.successful(Done)
  }

  def validRequest(request: AddEmployeeRequest): ValidatedNel[String, AddEmployeeRequest] = {
    val validName = validateName(request.employeeName)
    val validDbType = validateDbType(request.dbType)
    (validName, validDbType).mapN((_, _) => request)
  }

  private def validateName(name: String): ValidatedNel[String, String] =
    if (name.nonEmpty) name.validNel else Constants.INVALID_EMPLOYEE.invalidNel

  private def validateDbType(dbType: String): ValidatedNel[String, String] =
    if (dbType.contains("P") || dbType.contains("C")) dbType.validNel else Constants.INVALID_DBTYPE.invalidNel
}
