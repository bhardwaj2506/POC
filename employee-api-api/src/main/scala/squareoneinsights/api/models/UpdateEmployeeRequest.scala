package squareoneinsights.api.models

import play.api.libs.json.{Format, Json}

case class UpdateEmployeeRequest(id: Option[Long], name: String)

object UpdateEmployeeRequest {

  implicit val format: Format[UpdateEmployeeRequest] = Json.format[UpdateEmployeeRequest]
}