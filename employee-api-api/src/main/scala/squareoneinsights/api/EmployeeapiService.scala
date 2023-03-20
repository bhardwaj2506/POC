package squareoneinsights.api

import akka.{Done, NotUsed}
import com.lightbend.lagom.scaladsl.api.transport.Method.{DELETE, GET, POST, PUT}
import com.lightbend.lagom.scaladsl.api.{Descriptor, Service, ServiceCall}
import squareoneinsights.api.models.{AddEmployeeRequest, AddEmployeeResponse, EmployeeResponse, UpdateEmployeeRequest}


trait EmployeeapiService extends Service {

  def addEmployee(): ServiceCall[AddEmployeeRequest, AddEmployeeResponse]

  def getEmployee(id: Long): ServiceCall[NotUsed, EmployeeResponse]

  def updateEmployee(id: Long): ServiceCall[UpdateEmployeeRequest, EmployeeResponse]

  def deleteEmployee(id: Long): ServiceCall[NotUsed, Done]

  override final def descriptor: Descriptor = {
    import Service._

    named("employee-api")
      .withCalls(
        restCall(POST,"/api/employee/add", addEmployee _),
        restCall(GET,"/api/employee/get/:id", getEmployee _),
        restCall(PUT,"/api/employee/update/:id", updateEmployee _),
        restCall(DELETE,"/api/employee/delete/:id", deleteEmployee _)
      )
      .withAutoAcl(true)
  }
}

