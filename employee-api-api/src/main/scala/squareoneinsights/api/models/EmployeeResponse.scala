package squareoneinsights.api.models

import play.api.libs.json.{Format, Json}


case class EmployeeResponse(message: String, name: String, id: Option[Long] = None)

object EmployeeResponse {

  implicit val format: Format[EmployeeResponse] = Json.format[EmployeeResponse]
}