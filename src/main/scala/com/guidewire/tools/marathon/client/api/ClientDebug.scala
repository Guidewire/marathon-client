package com.guidewire.tools.marathon.client.api

import scalaz._
import dispatch._
import play.api.libs.json._
import scala.concurrent.ExecutionContext

import com.guidewire.tools.marathon.client._

import scala.language.implicitConversions
import java.nio.ByteBuffer

trait ClientDebug { self: ClientVersion =>
  import Client._

  /** Composes the URI for querying debug information. This will end up being similar to `http://&lt;host&gt;:&lt;port&gt;/v1/debug/isLeader`. */
  def uriQueryIsLeader(configuration: Connection): String =
    s"http://${configuration.host}:${configuration.port}/$uriVersionPart/debug/isLeader"

  /** Composes the URI for querying debug information. This will end up being similar to `http://&lt;host&gt;:&lt;port&gt;/v1/debug/leaderUrl`. */
  def uriQueryLeaderUrl(configuration: Connection): String =
    s"http://${configuration.host}:${configuration.port}/$uriVersionPart/debug/leaderUrl"

  /**
   * Makes a call to (effectively) `GET /v1/debug/isLeader` and deserializes the payload.
   *
   * @param connection data for constructing the endpoint URI
   * @return a [[scalaz.Validation]] that can be composed using normal scalaz methods
   */
  def queryDebugIsLeader(implicit connection: Connection, executor: ExecutionContext): Future[Validation[Error, Boolean]] =
    httpGet[Boolean](connection)(uriQueryIsLeader)(parseQueryDebugIsLeaderResponse)

  /**
   * Performs the actual parsing and validation of a JSON payload representing the payload from a call
   * to `GET /v1/debug/isLeader`.
   *
   * @param response JSON contents to parse and map to a [[scala.Boolean]]
   * @return a [[scalaz.Validation]] that can be composed using normal scalaz methods
   */
  def parseQueryDebugIsLeaderResponse(statusCode: Int, response: Array[Byte]): Validation[Error, Boolean] =
    processSingleBooleanStringHttpGetResponse(statusCode, response)

  /**
   * Makes a call to (effectively) `GET /v1/debug/leaderUrl` and deserializes the payload.
   *
   * @param connection data for constructing the endpoint URI
   * @return a [[scalaz.Validation]] that can be composed using normal scalaz methods
   */
  def queryDebugLeaderUrl(implicit connection: Connection, executor: ExecutionContext): Future[Validation[Error, String]] =
    httpGet[String](connection)(uriQueryLeaderUrl)(parseQueryDebugLeaderUrlResponse)

  /**
   * Performs the actual parsing and validation of a JSON payload representing the payload from a call
   * to `GET /v1/debug/leaderUrl`.
   *
   * @param response JSON contents to parse and map to a [[scala.Boolean]]
   * @return a [[scalaz.Validation]] that can be composed using normal scalaz methods
   */
  def parseQueryDebugLeaderUrlResponse(statusCode: Int, response: Array[Byte]): Validation[Error, String] =
    processSingleStringHttpGetResponse(statusCode, response)
}