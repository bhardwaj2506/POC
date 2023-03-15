package squareoneinsights.api

import akka.Done
import com.lightbend.lagom.scaladsl.api.{Descriptor, Service, ServiceCall}


trait EmployeeService extends Service {
  def addEmployee: ServiceCall[AddEmployeeRequest, Done]

  override def descriptor: Descriptor = {
    import Service._
    named("employee-service")
      .withCalls(
        pathCall("/employees", addEmployee _)
      )
      .withAutoAcl(true)
  }
}
