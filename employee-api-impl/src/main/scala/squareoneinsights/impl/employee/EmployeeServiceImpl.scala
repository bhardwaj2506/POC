package squareoneinsights.impl.employee

import akka.{Done, NotUsed}
import akka.actor.TypedActor.context
import akka.actor.{ActorRef, ActorSystem, Props}
import akka.stream.ActorMaterializer
import com.lightbend.lagom.scaladsl.api.ServiceCall
import squareoneinsights.api.{AddEmployeeRequest, EmployeeService}

import scala.concurrent.Future


class EmployeeServiceImpl extends EmployeeService {
  implicit val system = context.system
  implicit val materializer = ActorMaterializer()
  val kafkaProducerActor = system.actorOf(Props(new KafkaProducerActor))
  override def addEmployee(): ServiceCall[AddEmployeeRequest, Done] = { request =>
    //val message = request.toString
    // kafkaProducer ! Send(request.dbType, request.content)
    kafkaProducerActor ! request
    Future.successful(Done)
  }

/*  override def hello(id: String): ServiceCall[NotUsed, String] = ServiceCall {
    _ =>
      "Hello World!"
      Future.successful("Hello World!")
  }*/
}



