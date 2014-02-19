package com.guidewire.tools.marathon.client.api

import scalaz._
import dispatch._
import play.api.libs.json._
import scala.concurrent.ExecutionContext

import com.guidewire.tools.marathon.client._

import scala.language.implicitConversions

trait ClientApps { self: ClientVersion =>
  import Client._

  type VersionSpecificApp <: App
  type VersionSpecificAppScale <: AppScale
  type VersionSpecificContainer <: Container
  type VersionSpecificAppDestroy <: AppDestroy
  type VersionSpecificConstraint <: Constraint

  val VersionSpecificAppFormat: Format[VersionSpecificApp]
  val VersionSpecificAppApply : App.Apply[VersionSpecificContainer, VersionSpecificConstraint, VersionSpecificApp]

  val VersionSpecificAppScaleFormat: Format[VersionSpecificAppScale]
  val VersionSpecificAppScaleApply : AppScale.Apply[VersionSpecificAppScale]

  val VersionSpecificAppDestroyFormat: Format[VersionSpecificAppDestroy]
  val VersionSpecificAppDestroyApply : AppDestroy.Apply[VersionSpecificAppDestroy]

  val VersionSpecificContainerFormat: Format[VersionSpecificContainer]
  val VersionSpecificContainerApply : Container.Apply[VersionSpecificContainer]

  val VersionSpecificConstraintFormat: Format[VersionSpecificConstraint]
  val VersionSpecificConstraintApply : Constraint.Apply[VersionSpecificConstraint]

  /** Provides access to a [[play.api.libs.json.Format]] for serializing [[com.guidewire.tools.marathon.client.App]] instances. */
  lazy implicit val AppFormat       : Format[App]        = mapJsonFormat(VersionSpecificAppFormat)

  /** Provides access to a [[play.api.libs.json.Format]] for serializing [[com.guidewire.tools.marathon.client.AppScale]] instances. */
  lazy implicit val AppScaleFormat  : Format[AppScale]   = mapJsonFormat(VersionSpecificAppScaleFormat)

  /** Provides access to a [[play.api.libs.json.Format]] for serializing [[com.guidewire.tools.marathon.client.AppDestroy]] instances. */
  lazy implicit val AppDestroyFormat: Format[AppDestroy] = mapJsonFormat(VersionSpecificAppDestroyFormat)

  /** Provides access to a [[play.api.libs.json.Format]] for serializing [[com.guidewire.tools.marathon.client.Constraint]] instances. */
  lazy implicit val ConstraintFormat: Format[Constraint] = mapJsonFormat(VersionSpecificConstraintFormat)

  /** Composes the URI for querying apps. This will end up being similar to `http://&lt;host&gt;:&lt;port&gt;/v1/apps/`. */
  def uriQueryApps(configuration: Connection): String =
    s"http://${configuration.host}:${configuration.port}/$uriVersionPart/apps"

  /** Composes the URI for posting an app. This will end up being similar to `http://&lt;host&gt;:&lt;port&gt;/v1/apps/start`. */
  def uriStartApp(configuration: Connection): String =
    s"http://${configuration.host}:${configuration.port}/$uriVersionPart/apps/start"

  /** Composes the URI for scaling apps. This will end up being similar to `http://&lt;host&gt;:&lt;port&gt;/v1/apps/scale`. */
  def uriScaleApp(configuration: Connection): String =
    s"http://${configuration.host}:${configuration.port}/$uriVersionPart/apps/scale"

  /** Composes the URI for destroying apps. This will end up being similar to `http://&lt;host&gt;:&lt;port&gt;/v1/apps/stop`. */
  def uriDestroyApp(configuration: Connection): String =
    s"http://${configuration.host}:${configuration.port}/$uriVersionPart/apps/stop"

  /**
   * Makes a call to (effectively) `GET /v1/apps/` and deserializes the payload.
   *
   * @param connection data for constructing the endpoint URI
   * @return a [[scalaz.Validation]] that can be composed using normal scalaz methods
   */
  def queryApps(implicit connection: Connection, executor: ExecutionContext): Future[Validation[Error, Traversable[App]]] =
    httpGet[Traversable[App]](connection)(uriQueryApps)(parseQueryAppsResponse)

  /**
   * Performs the actual parsing and validation of a JSON payload representing the payload from a call
   * to `GET /v1/apps/`.
   *
   * @param response JSON contents to parse and map to a [[scala.collection.Traversable]]
   *                 of [[com.guidewire.tools.marathon.client.App]]
   * @return a [[scalaz.Validation]] that can be composed using normal scalaz methods
   */
  def parseQueryAppsResponse(statusCode: Int, response: Array[Byte]): Validation[Error, Traversable[App]] =
    //validateify(statusCode, Json.parse(response).transform((__ \ 'container).json.update(__.read[JsObject].map { o => if (o == JsNull) implicitly[Writes[Container]].writes(VersionSpecificContainerApply("", Seq())) else o })).validate[Seq[App]])
    validateify(statusCode, Json.parse(response).validate[Seq[App]])

  /**
   * Makes a call to (effectively) `POST /v1/apps/start` to start the app and returns the result.
   *
   * @param connection data for constructing the endpoint URI
   * @param app data
   * @return a [[scalaz.Validation]] that can be composed using normal scalaz methods
   */
  def startApp(app: App)(implicit connection: Connection, executor: ExecutionContext): Future[Validation[Error, AppResponse]] =
    httpPostAsJson[App, AppResponse](app, connection)(uriStartApp)(parsePostAppStartResponse)

  /**
   * Performs the actual parsing and validation of a JSON payload representing the payload from a call
   * to `POST /v1/apps/start`.
   *
   * @param response JSON contents to parse and map to a [[scala.Boolean]]
   * @return a [[scalaz.Validation]] that can be composed using normal scalaz methods
   */
  def parsePostAppStartResponse(statusCode: Int, response: Array[Byte]): Validation[Error, AppResponse] =
    processStandardHttpPostResponse(statusCode, response)(AppResponse.fromStatusCode(statusCode))

  /**
   * Makes a call to (effectively) `POST /v1/apps/scale` to scale the app and returns the result.
   *
   * @param connection data for constructing the endpoint URI
   * @param scale ID and new count for the number of instances to run for an app that will be scaled
   * @return a [[scalaz.Validation]] that can be composed using normal scalaz methods
   */
  def scaleApp(scale: AppScale)(implicit connection: Connection, executor: ExecutionContext): Future[Validation[Error, AppResponse]] =
    httpPostAsJson[AppScale, AppResponse](scale, connection)(uriScaleApp)(parsePostAppScaleResponse)

  /**
   * Performs the actual parsing and validation of a JSON payload representing the payload from a call
   * to `POST /v1/apps/scale`.
   *
   * @param response JSON contents to parse and map to a [[scala.Boolean]]
   * @return a [[scalaz.Validation]] that can be composed using normal scalaz methods
   */
  def parsePostAppScaleResponse(statusCode: Int, response: Array[Byte]): Validation[Error, AppResponse] =
    processStandardHttpPostResponse(statusCode, response)(AppResponse.fromStatusCode(statusCode))

  /**
   * Makes a call to (effectively) `POST /v1/apps/stop` to destroy (delete) the app and returns the result.
   *
   * @param connection data for constructing the endpoint URI
   * @param destroy ID and new count for the number of instances to run for an app that will be scaled
   * @return a [[scalaz.Validation]] that can be composed using normal scalaz methods
   */
  def destroyApp(destroy: AppDestroy)(implicit connection: Connection, executor: ExecutionContext): Future[Validation[Error, AppResponse]] =
    httpPostAsJson[AppDestroy, AppResponse](destroy, connection)(uriDestroyApp)(parsePostAppDestroyResponse)

  /**
   * Performs the actual parsing and validation of a JSON payload representing the payload from a call
   * to `POST /v1/apps/stop`.
   *
   * @param response JSON contents to parse and map to a [[scala.Boolean]]
   * @return a [[scalaz.Validation]] that can be composed using normal scalaz methods
   */
  def parsePostAppDestroyResponse(statusCode: Int, response: Array[Byte]): Validation[Error, AppResponse] =
    processStandardHttpPostResponse(statusCode, response)(AppResponse.fromStatusCode(statusCode))
}