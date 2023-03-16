package squareoneinsights.impl

import akka.Done
import akka.actor.{ActorSystem, Props}
import akka.stream.ActorMaterializer
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.typesafe.config.ConfigFactory
import play.api.libs.json.Json
import squareoneinsights.api.models.{AddEmployeeRequest, Send}
import squareoneinsights.api.EmployeeapiService
import squareoneinsights.impl.producer.KafkaProducerActor

import scala.concurrent.{ExecutionContext, Future}


class EmployeeapiServiceImpl(implicit ec: ExecutionContext) extends EmployeeapiService {

  implicit val system: ActorSystem = ActorSystem("employee-api")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  private val config = ConfigFactory.load()
  val topic = config.getString("topic")

  val kafkaProducerActor = system.actorOf(Props(new KafkaProducerActor))

  override def addEmployee(): ServiceCall[AddEmployeeRequest, Done] = { request =>
    val message = request
    kafkaProducerActor ! Send(topic, Json.toJson(message).toString())
    Future.successful(Done)
  }
}
