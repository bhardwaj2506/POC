package squareoneinsights.impl

import squareoneinsights.api
import squareoneinsights.api.{AddEmployeeRequest, EmployeeapiService, Send}
import akka.Done
import akka.NotUsed
import akka.actor.{ActorSystem, Props}
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.cluster.sharding.typed.scaladsl.EntityRef
import akka.stream.ActorMaterializer
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.broker.TopicProducer
import com.lightbend.lagom.scaladsl.persistence.EventStreamElement
import com.lightbend.lagom.scaladsl.persistence.PersistentEntityRegistry

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future}
import scala.concurrent.duration._
import akka.util.Timeout
import com.lightbend.lagom.scaladsl.api.transport.BadRequest
import play.api.libs.json.Json
import squareoneinsights.impl.employee.KafkaProducerActor

/**
  * Implementation of the EmployeeapiService.
  */
class EmployeeapiServiceImpl(
  clusterSharding: ClusterSharding,
  persistentEntityRegistry: PersistentEntityRegistry
)(implicit ec: ExecutionContext)
  extends EmployeeapiService {

  /**
    * Looks up the entity for the given ID.
    */
  private def entityRef(id: String): EntityRef[EmployeeapiCommand] =
    clusterSharding.entityRefFor(EmployeeapiState.typeKey, id)

  implicit val timeout = Timeout(5.seconds)

  override def hello(id: String): ServiceCall[NotUsed, String] = ServiceCall {
    _ =>
      // Look up the sharded entity (aka the aggregate instance) for the given ID.
      val ref = entityRef(id)

      // Ask the aggregate instance the Hello command.
      ref
        .ask[Greeting](replyTo => Hello(id, replyTo))
        .map(greeting => greeting.message)
  }

  override def useGreeting(id: String) = ServiceCall { request =>
    // Look up the sharded entity (aka the aggregate instance) for the given ID.
    val ref = entityRef(id)

    // Tell the aggregate to use the greeting message specified.
    ref
      .ask[Confirmation](
        replyTo => UseGreetingMessage(request.message, replyTo)
      )
      .map {
        case Accepted => Done
        case _        => throw BadRequest("Can't upgrade the greeting message.")
      }
  }

  override def greetingsTopic(): Topic[api.GreetingMessageChanged] =
    TopicProducer.singleStreamWithOffset { fromOffset =>
      persistentEntityRegistry
        .eventStream(EmployeeapiEvent.Tag, fromOffset)
        .map(ev => (convertEvent(ev), ev.offset))
    }

  private def convertEvent(
    helloEvent: EventStreamElement[EmployeeapiEvent]
  ): api.GreetingMessageChanged = {
    helloEvent.event match {
      case GreetingMessageChanged(msg) =>
        api.GreetingMessageChanged(helloEvent.entityId, msg)
    }
  }

  //override def addEmployee: ServiceCall[AddEmployeeRequest, Done] = ???

  implicit val system: ActorSystem = ActorSystem("employee-api")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  val topic = "my-kafka-topic"


  val kafkaProducerActor = system.actorOf(Props(new KafkaProducerActor))
  kafkaProducerActor ! Send("request.dbType", "request.content")

 // case class Send(topic: String, message: String)


  override def addEmployee(): ServiceCall[AddEmployeeRequest, Done] = { request =>
    // kafkaProducer ! Send(request.dbType, request.content)
    val message  = request
    println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@")
   // kafkaProducerActor ! "invalid Request"
   // kafkaProducerActor ! Send("request.dbType", "request.content")
    println("##################################"+request.dbType + request.content)
    kafkaProducerActor ! Send(topic,Json.toJson(message).toString())
    Future.successful(Done)
  }
}
