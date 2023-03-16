package squareoneinsights.api.models

import play.api.libs.json.{Format, Json}

case class AddEmployeeRequest(dbType: String, employeeName: String)

object AddEmployeeRequest {

  implicit val format: Format[AddEmployeeRequest] = Json.format[AddEmployeeRequest]
}