package com.guidewire.tools.marathon.client

import play.api.libs.json._

case class ResponseError(attribute: String, error: String)
object ResponseError {
  implicit val fmt = Json.format[ResponseError]
}

case class MultipleResponseErrors(errors: Seq[ResponseError])
object MultipleResponseErrors {
  implicit val fmt = Json.format[MultipleResponseErrors]
}

case class SingleResponseError(message: String)
object SingleResponseError {
  implicit val fmt = Json.format[SingleResponseError]
}

case class Error(statusCode: Int, message: String)
