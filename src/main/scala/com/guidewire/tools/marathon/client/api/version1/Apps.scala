package com.guidewire.tools.marathon.client.api.version1

import play.api.libs.json._

import com.guidewire.tools.marathon.client

case class Container(
    image:String
  , options: Seq[String]
) extends client.Container

object Container {
  implicit val fmt = Json.format[Container]
}

case class Constraint(
    field:String
  , operator:String
  , value:Option[String]
) extends client.Constraint

object Constraint {
  implicit val fmt = Json.format[Constraint]
}

case class App(
    id           : String
  , cmd          : String
  , env          : Map[String, String]
  , instances    : Int
  , cpus         : Double
  , mem          : Double
  , executor     : String
  , constraints  : Seq[Constraint]
  , uris         : Seq[String]
  , ports        : Seq[Int]
  , container    : Container
  //, taskRateLimit: Option[Double]
) extends client.App

object App {
  implicit val fmt = Json.format[App]
}

case class AppScale(
    id           : String
  , instances    : Int
) extends client.AppScale

object AppScale {
  implicit val fmt = Json.format[AppScale]
}

case class AppDestroy(
    id           : String
) extends client.AppDestroy

object AppDestroy {
  implicit val fmt = Json.format[AppDestroy]
}
