package com.guidewire.tools.marathon.client.api

import scalaz._
import dispatch._
import play.api.libs.json._
import scala.concurrent.ExecutionContext

import com.guidewire.tools.marathon.client._

import scala.language.implicitConversions

trait ClientTasks { self: ClientVersion =>
  import Client._

  type VersionSpecificTask <: Task
  type VersionSpecificTaskKill <: TaskKill

  val VersionSpecificTaskFormat: Format[VersionSpecificTask]
  val VersionSpecificTaskApply : Task.Apply[VersionSpecificTask]

  val VersionSpecificTaskKillFormat: Format[VersionSpecificTaskKill]
  val VersionSpecificTaskKillApply : TaskKill.Apply[VersionSpecificTaskKill]

  /** Provides access to a [[play.api.libs.json.Format]] for serializing [[com.guidewire.tools.marathon.client.Task]] instances. */
  lazy implicit val TaskFormat    : Format[Task]       = mapJsonFormat(VersionSpecificTaskFormat)

  /** Composes the URI for querying all tasks. This will end up being similar to `http://&lt;host&gt;:&lt;port&gt;/v1/tasks`. */
  def uriQueryTasks(configuration: Connection): String =
    s"http://${configuration.host}:${configuration.port}/$uriVersionPart/tasks"

  /** Composes the URI for killing tasks. This will end up being similar to `http://&lt;host&gt;:&lt;port&gt;/v1/tasks/kill`. */
  def uriKillTasks(taskKill: TaskKill)(configuration: Connection): String =
    (url(s"http://${configuration.host}:${configuration.port}/$uriVersionPart/tasks/kill") <<? Seq(
        ("appId", taskKill.appId)
      , ("host", taskKill.host)
      , ("id", taskKill.id)
      , ("scale", taskKill.scale.toString.toLowerCase)
    )).url

  /**
   * Makes a call to (effectively) `GET /v1/tasks` and deserializes the payload.
   *
   * @param connection data for constructing the tasks URI
   * @return a [[scalaz.Validation]] that can be composed using normal scalaz methods
   */
  def queryTasks(implicit connection: Connection, executor: ExecutionContext): Future[Validation[Error, Map[String, Traversable[Task]]]] =
    httpGet[Map[String, Traversable[Task]]](connection)(uriQueryTasks)(parseQueryTasksResponse)

  /**
   * Performs the actual parsing and validation of a JSON payload representing the payload from a call
   * to `GET /v1/tasks`.
   *
   * @param response JSON contents to parse and map to a [[scala.collection.Traversable]]
   *                 of [[com.guidewire.tools.marathon.client.Task]]
   * @return a [[scalaz.Validation]] that can be composed using normal scalaz methods
   */
  def parseQueryTasksResponse(statusCode: Int, response: Array[Byte]): Validation[Error, Map[String, Traversable[Task]]] =
    validateify(statusCode, Json.parse(response).validate[Map[String, Seq[Task]]])

  /**
   * Makes a call to (effectively) `POST /v1/tasks/kill` to kill existing tasks and returns the result.
   *
   * @param connection data for constructing the task kill URI
   * @param taskKill app ID, host, id, scale et. al. needed for killing tasks
   * @return a [[scalaz.Validation]] that can be composed using normal scalaz methods
   */
  def killTasks(taskKill: TaskKill)(implicit connection: Connection, executor: ExecutionContext): Future[Validation[Error, Traversable[Task]]] =
    httpPostEmpty[TaskKill, Traversable[Task]](taskKill, connection)(uriKillTasks(taskKill))(parsePostKillTasksResponse)

  /**
   * Performs the actual parsing and validation of a JSON payload representing the payload from a call
   * to `POST /v1/apps/stop`.
   *
   * @param response JSON contents to parse and map to a [[scala.collection.Traversable]] of
   *                 [[com.guidewire.tools.marathon.client.Task]]
   * @return a [[scalaz.Validation]] that can be composed using normal scalaz methods
   */
  def parsePostKillTasksResponse(statusCode: Int, response: Array[Byte]): Validation[Error, Traversable[Task]] =
    validateify(statusCode, Json.parse(response).validate[Seq[Task]])
}