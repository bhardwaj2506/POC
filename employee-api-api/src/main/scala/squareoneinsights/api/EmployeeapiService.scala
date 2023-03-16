package squareoneinsights.api

import akka.Done
import com.lightbend.lagom.scaladsl.api.{Descriptor, Service, ServiceCall}
import squareoneinsights.api.models.AddEmployeeRequest

trait EmployeeapiService extends Service {

  def addEmployee: ServiceCall[AddEmployeeRequest, Done]

  override final def descriptor: Descriptor = {
    import Service._

    named("employee-api")
      .withCalls(
        pathCall("/employees", addEmployee _)
      )
      .withAutoAcl(true)
  }
}

