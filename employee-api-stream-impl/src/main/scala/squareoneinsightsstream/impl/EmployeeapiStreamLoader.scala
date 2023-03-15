package squareoneinsightsstream.impl

import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
import com.lightbend.lagom.scaladsl.server._
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import play.api.libs.ws.ahc.AhcWSComponents
import squareoneinsightsstream.api.EmployeeapiStreamService
import squareoneinsights.api.EmployeeapiService
import com.softwaremill.macwire._

class EmployeeapiStreamLoader extends LagomApplicationLoader {

  override def load(context: LagomApplicationContext): LagomApplication =
    new EmployeeapiStreamApplication(context) {
      override def serviceLocator: NoServiceLocator.type = NoServiceLocator
    }

  override def loadDevMode(context: LagomApplicationContext): LagomApplication =
    new EmployeeapiStreamApplication(context) with LagomDevModeComponents

  override def describeService = Some(readDescriptor[EmployeeapiStreamService])
}

abstract class EmployeeapiStreamApplication(context: LagomApplicationContext)
  extends LagomApplication(context)
    with AhcWSComponents {

  // Bind the service that this server provides
  override lazy val lagomServer: LagomServer = serverFor[EmployeeapiStreamService](wire[EmployeeapiStreamServiceImpl])

  // Bind the EmployeeapiService client
  lazy val employeeapiService: EmployeeapiService = serviceClient.implement[EmployeeapiService]
}
