package com.guidewire.tools.marathon.client

trait Endpoint {
  def id   : String
  def ports: Seq[Int]
  def instances: Seq[Task]
}


object Endpoint {
  type Apply[TTask <: Task, TEndpoint <: Endpoint] = (
      String
    , Seq[Int]
    , Seq[TTask]
  ) => TEndpoint

  def apply(
      host     : String
    , ports    : Seq[Int]
    , instances: Seq[Task]
  )
  (implicit version: api.Client)
  = validate(version.VersionSpecificEndpointApply(host, ports, for(i <- instances) yield version.VersionSpecificTaskApply(i.id, i.host, i.ports)))

  def validate(endpoint: Endpoint): Endpoint =
    endpoint
}

import scala.collection.JavaConverters._

/**
 * WARNING: This should never be a companion object or it will not be exposed to the Java API
 *          correctly.
 */
object Endpoints {
  def create(
      id       : String
    , ports    : java.util.List[java.lang.Integer]
    , instances: java.util.List[Task]
  )
  = Endpoint(id, ports.asScala.map(_.toInt).toSeq, instances.asScala.toSeq)(implicitly[api.Client])

  def create(
      id       : String
    , ports    : java.util.List[java.lang.Integer]
    , instances: java.util.List[Task]
    , version  : api.Client
  )
  = Endpoint(id, ports.asScala.map(_.toInt), instances.asScala.toSeq)(version)
}
