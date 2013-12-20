package com.guidewire.tools.marathon.client.api

import scalaz._
import dispatch._
import play.api.libs.json._
import scala.concurrent.ExecutionContext

import com.guidewire.tools.marathon.client._

import scala.language.implicitConversions

trait ClientEndpoints { self: ClientVersion with ClientTasks =>
  import Client._

  type VersionSpecificEndpoint <: Endpoint

  val VersionSpecificEndpointFormat: Format[VersionSpecificEndpoint]
  val VersionSpecificEndpointApply : Endpoint.Apply[VersionSpecificTask, VersionSpecificEndpoint]

  /** Provides access to a [[play.api.libs.json.Format]] for serializing [[com.guidewire.tools.marathon.client.Endpoint]] instances. */
  lazy implicit val EndpointFormat: Format[Endpoint]   = mapJsonFormat(VersionSpecificEndpointFormat)

  /** Composes the URI for querying all endpoints. This will end up being similar to `http://&lt;host&gt;:&lt;port&gt;/v1/endpoints`. */
  def uriQueryEndpoints(configuration: Connection): String =
    s"http://${configuration.host}:${configuration.port}/$uriVersionPart/endpoints"

  /** Composes the URI for querying all endpoints for a specific app. This will end up being similar to `http://&lt;host&gt;:&lt;port&gt;/v1/endpoints/&lt;app id&gt;`. */
  def uriQueryEndpointsForApp(appID: String)(configuration: Connection): String =
    s"http://${configuration.host}:${configuration.port}/$uriVersionPart/endpoints/$appID"

  /**
   * Makes a call to (effectively) `GET /v1/endpoints/` and deserializes the payload.
   *
   * @param connection data for constructing the endpoint URI
   * @return a [[scalaz.Validation]] that can be composed using normal scalaz methods
   */
  def queryEndpoints(implicit connection: Connection, executor: ExecutionContext): Future[Validation[Error, Traversable[Endpoint]]] =
    httpGet[Traversable[Endpoint]](connection)(uriQueryEndpoints)(parseQueryEndpointsResponse)

  /**
   * Performs the actual parsing and validation of a JSON payload representing the payload from a call
   * to `GET /v1/endpoints/`.
   *
   * @param response JSON contents to parse and map to a [[scala.collection.Traversable]]
   *                 of [[com.guidewire.tools.marathon.client.Endpoint]]
   * @return a [[scalaz.Validation]] that can be composed using normal scalaz methods
   */
  def parseQueryEndpointsResponse(statusCode: Int, response: Array[Byte]): Validation[Error, Traversable[Endpoint]] =
    validateify(statusCode, Json.parse(response).validate[Seq[Endpoint]])

  /**
   * Makes a call to (effectively) `GET /v1/endpoints/{id}` and deserializes the payload.
   *
   * @param connection data for constructing the endpoint URI
   * @return a [[scalaz.Validation]] that can be composed using normal scalaz methods
   */
  def queryEndpointsForApp(appID: String)(implicit connection: Connection, executor: ExecutionContext): Future[Validation[Error, Endpoint]] =
    httpGet[Endpoint](connection)(uriQueryEndpointsForApp(appID))(parseQueryEndpointsForAppResponse)

  /**
   * Performs the actual parsing and validation of a JSON payload representing the payload from a call
   * to `GET /v1/endpoints/{id}`.
   *
   * @param response JSON contents to parse and map to a [[scala.collection.Traversable]]
   *                 of [[com.guidewire.tools.marathon.client.Endpoint]]
   * @return a [[scalaz.Validation]] that can be composed using normal scalaz methods
   */
  def parseQueryEndpointsForAppResponse(statusCode: Int, response: Array[Byte]): Validation[Error, Endpoint] =
    validateify(statusCode, Json.parse(response).validate[Endpoint])
}