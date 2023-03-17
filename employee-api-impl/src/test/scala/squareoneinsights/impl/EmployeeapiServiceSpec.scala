package squareoneinsights.impl

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKitBase}
import com.lightbend.lagom.scaladsl.server.LocalServiceLocator
import com.lightbend.lagom.scaladsl.testkit.ServiceTest
import org.mockito.IdiomaticMockito
import org.scalatest.BeforeAndAfterAll
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.scalatest.wordspec.AnyWordSpecLike
import squareoneinsights.api._
import squareoneinsights.api.models.{AddEmployeeRequest, AddEmployeeResponse, EmployeeResponse, UpdateEmployeeRequest}
import squareoneinsights.impl.db.PostgresEmployeeRepository

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global

class EmployeeapiServiceSpec extends AnyWordSpecLike
  with TestKitBase
  with ImplicitSender
  with ScalaFutures
  with IdiomaticMockito
  with BeforeAndAfterAll {

  private val server = ServiceTest.startServer(
    ServiceTest.defaultSetup
      .withCassandra()
  ) { ctx =>
    new EmployeeapiApplication(ctx) with LocalServiceLocator
  }

  val executionContext = ExecutionContext

  val client: EmployeeapiService = server.serviceClient.implement[EmployeeapiService]
  val mockpostgresEmployeeRepository: PostgresEmployeeRepository = mock[PostgresEmployeeRepository]

  override implicit def system: ActorSystem = ActorSystem("testActorSystem")

  override protected def afterAll(): Unit = server.stop()

  "employee-api service" should {

    "add an employee" in {
      val addEmployeeRequest = AddEmployeeRequest("P", "Siddharth Bhardwaj")
      val responseMessage = AddEmployeeResponse("Message send successfully")
      client.addEmployee.invoke(addEmployeeRequest).map { response =>
        response shouldBe responseMessage
      }
    }
    "get an existing employee" in {
      val addResponse = client.getEmployee(1).invoke()
      whenReady(addResponse) { _ =>
        client.getEmployee(1).invoke().map { response =>
          response == EmployeeResponse("Pappu", "Bhardwaj")
        }
      }
    }
    "update an existing employee" in {
      val updateEmployeeRequest = UpdateEmployeeRequest(Some(101), "Pappu Bhardwaj")
      val addResponse = client.updateEmployee(1).invoke(updateEmployeeRequest)
      whenReady(addResponse) { _ =>
        client.getEmployee(1).invoke().map { response =>
          response == EmployeeResponse("Pappu", "Bhardwaj")
        }
      }
    }
    "delete an existing employee" in {
      val addResponse = client.deleteEmployee(1).invoke()
      whenReady(addResponse) { _ =>
        client.getEmployee(1).invoke().map { response =>
          response == EmployeeResponse("Pappu", "Bhardwaj")
        }
      }
    }
  }
}
