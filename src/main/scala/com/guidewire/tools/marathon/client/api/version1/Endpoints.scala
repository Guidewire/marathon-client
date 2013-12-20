package com.guidewire.tools.marathon.client.api.version1

import play.api.libs.json._

import com.guidewire.tools.marathon.client

case class Endpoint(
    id       : String
  , ports    : Seq[Int]
  , instances: Seq[Task]
) extends client.Endpoint

object Endpoint {
  implicit val fmt = Json.format[Endpoint]
}
