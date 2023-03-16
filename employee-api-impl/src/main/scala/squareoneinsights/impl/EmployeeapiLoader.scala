package squareoneinsights.impl

import com.lightbend.lagom.scaladsl.api.ServiceLocator
import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
import com.lightbend.lagom.scaladsl.broker.kafka.LagomKafkaComponents
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraPersistenceComponents
import com.lightbend.lagom.scaladsl.playjson.JsonSerializerRegistry
import com.lightbend.lagom.scaladsl.server._
import com.softwaremill.macwire._
import play.api.libs.ws.ahc.AhcWSComponents
import squareoneinsights.api.EmployeeapiService

class EmployeeapiLoader extends LagomApplicationLoader {

  override def load(context: LagomApplicationContext): LagomApplication =
    new EmployeeapiApplication(context) {
      override def serviceLocator: ServiceLocator = NoServiceLocator
    }

  override def loadDevMode(context: LagomApplicationContext): LagomApplication =
    new EmployeeapiApplication(context) with LagomDevModeComponents

  override def describeService = Some(readDescriptor[EmployeeapiService])
}

abstract class EmployeeapiApplication(context: LagomApplicationContext)
  extends LagomApplication(context)
    with CassandraPersistenceComponents
    with LagomKafkaComponents
    with AhcWSComponents {


  override lazy val lagomServer: LagomServer = serverFor[EmployeeapiService](wire[EmployeeapiServiceImpl])

  override lazy val jsonSerializerRegistry: JsonSerializerRegistry = EmployeeapiSerializerRegistry

}
