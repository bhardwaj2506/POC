package squareoneinsights.api

import play.api.libs.json.{Format, Json}

case class AddEmployeeRequest(dbType: String, content: String)

object AddEmployeeRequest {

  implicit val format: Format[AddEmployeeRequest] = Json.format[AddEmployeeRequest]
}