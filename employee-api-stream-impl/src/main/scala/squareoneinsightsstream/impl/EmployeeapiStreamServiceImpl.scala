package squareoneinsightsstream.impl

import com.lightbend.lagom.scaladsl.api.ServiceCall
import squareoneinsightsstream.api.EmployeeapiStreamService
import squareoneinsights.api.EmployeeapiService

import scala.concurrent.Future

/**
  * Implementation of the EmployeeapiStreamService.
  */
class EmployeeapiStreamServiceImpl(employeeapiService: EmployeeapiService) extends EmployeeapiStreamService {
  def stream = ServiceCall { hellos =>
    Future.successful(hellos.mapAsync(8)(employeeapiService.hello(_).invoke()))
  }
}
