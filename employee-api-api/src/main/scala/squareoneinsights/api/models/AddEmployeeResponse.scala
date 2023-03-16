package squareoneinsights.api.models

import play.api.libs.json.{Format, Json}


case class AddEmployeeResponse(message: String)

object AddEmployeeResponse {

  implicit val format: Format[AddEmployeeResponse] = Json.format[AddEmployeeResponse]
}