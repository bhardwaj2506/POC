package squareoneinsights.impl

import com.lightbend.lagom.scaladsl.playjson.{JsonSerializer, JsonSerializerRegistry}
import squareoneinsights.api.models.AddEmployeeRequest

object EmployeeapiSerializerRegistry extends JsonSerializerRegistry {
  override def serializers: Seq[JsonSerializer[_]] = Seq(
    JsonSerializer[AddEmployeeRequest]
  )
}