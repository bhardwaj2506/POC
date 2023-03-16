package squareoneinsights.api

import com.lightbend.lagom.scaladsl.api.{Descriptor, Service, ServiceCall}
import squareoneinsights.api.models.{AddEmployeeRequest, AddEmployeeResponse}


trait EmployeeapiService extends Service {

  def addEmployee: ServiceCall[AddEmployeeRequest, AddEmployeeResponse]

  override final def descriptor: Descriptor = {
    import Service._

    named("employee-api")
      .withCalls(
        pathCall("/employees/add", addEmployee _)
      )
      .withAutoAcl(true)
  }
}

