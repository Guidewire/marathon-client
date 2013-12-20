package com.guidewire.tools.marathon.client

import scalaz._
import scala.concurrent._

object Marathon {
  import api._
  import Utils._
  import ExecutionContext.Implicits._
  import scala.collection.JavaConverters._

  val apps = Apps
  val debug = Debug
  val tasks = Tasks
  val endpoints = Endpoints

  object Apps {
    def query(host: String, port: Int): java.util.concurrent.Future[java.util.List[App]] =
      query(host, port, implicitly[api.Client])

    def query(host: String, port: Int, version: Client): java.util.concurrent.Future[java.util.List[App]] =
      toJavaFutureWithValidation(query(Connection(host, port), version))(_.toSeq.asJava)

    def start(host: String, port: Int, app: App): java.util.concurrent.Future[AppResponse] =
      start(host, port, app, implicitly[api.Client])

    def start(host: String, port: Int, app: App, version: Client): java.util.concurrent.Future[AppResponse] =
      toJavaFutureWithValidation(start(app)(Connection(host, port), version))(x => x)

    def scale(host: String, port: Int, appID: String, instances: Int): java.util.concurrent.Future[AppResponse] =
      scale(host, port, appID, instances, implicitly[api.Client])

    def scale(host: String, port: Int, appID: String, instances: Int, version: Client): java.util.concurrent.Future[AppResponse] =
      scale(host, port, AppScale(appID, instances), version)

    def scale(host: String, port: Int, appToScale: AppScale): java.util.concurrent.Future[AppResponse] =
      scale(host, port, appToScale, implicitly[api.Client])

    def scale(host: String, port: Int, appToScale: AppScale, version: Client): java.util.concurrent.Future[AppResponse] =
      toJavaFutureWithValidation(scale(appToScale)(Connection(host, port), version))(x => x)

    def scale(appID: String, instances: Int)(implicit connection: Connection, version: Client, executor: ExecutionContext): Future[Validation[Error, AppResponse]] =
      version.scaleApp(AppScale(appID, instances))(connection, executor)

    def suspend(host: String, port: Int, appID: String): java.util.concurrent.Future[AppResponse] =
      suspend(host, port, appID, implicitly[api.Client])

    def suspend(host: String, port: Int, appID: String, version: Client): java.util.concurrent.Future[AppResponse] =
      toJavaFutureWithValidation(suspend(appID)(Connection(host, port), version, implicitly[ExecutionContext]))(x => x)

    def suspend(appID: String)(implicit connection: Connection, version: Client, executor: ExecutionContext): Future[Validation[Error, AppResponse]] =
      version.scaleApp(AppScale(appID, 0))(connection, executor)

    def destroy(host: String, port: Int, appID: String): java.util.concurrent.Future[AppResponse] =
      destroy(host, port, appID, implicitly[api.Client])

    def destroy(host: String, port: Int, appID: String, version: Client): java.util.concurrent.Future[AppResponse] =
      toJavaFutureWithValidation(destroy(appID)(Connection(host, port), version, implicitly[ExecutionContext]))(x => x)

    def destroy(appID: String)(implicit connection: Connection, version: Client, executor: ExecutionContext): Future[Validation[Error, AppResponse]] =
      destroy(AppDestroy(appID))(connection, version, executor)

    /**
     * Queries marathon for a list of
     * @param connection
     * @param version
     * @param executor
     * @return
     */
    def query(implicit connection: Connection, version: Client = DEFAULT_MARATHON_VERSION, executor: ExecutionContext = implicitly[ExecutionContext]): Future[Validation[Error, Traversable[App]]] =
      version.queryApps(connection, executor)

    def start(app: App)(implicit connection: Connection, version: Client = DEFAULT_MARATHON_VERSION, executor: ExecutionContext = implicitly[ExecutionContext]): Future[Validation[Error, AppResponse]] =
      version.startApp(app)(connection, executor)

    def scale(appToScale: AppScale)(implicit connection: Connection, version: Client = DEFAULT_MARATHON_VERSION, executor: ExecutionContext = implicitly[ExecutionContext]): Future[Validation[Error, AppResponse]] =
      version.scaleApp(appToScale)(connection, executor)

    def suspend(appToSuspend: App)(implicit connection: Connection, version: Client = DEFAULT_MARATHON_VERSION, executor: ExecutionContext = implicitly[ExecutionContext]): Future[Validation[Error, AppResponse]] =
      version.scaleApp(AppScale(appToSuspend.id, 0))(connection, executor)

    def destroy(destroy: AppDestroy)(implicit connection: Connection, version: Client = DEFAULT_MARATHON_VERSION, executor: ExecutionContext = implicitly[ExecutionContext]): Future[Validation[Error, AppResponse]] =
      version.destroyApp(destroy)(connection, executor)
  }

  object Debug {
    def isLeader(host: String, port: Int): java.util.concurrent.Future[java.lang.Boolean] =
      isLeader(host, port, implicitly[api.Client])

    def isLeader(host: String, port: Int, version: Client): java.util.concurrent.Future[java.lang.Boolean] =
      toJavaFutureWithValidation(isLeader(Connection(host, port), version, implicitly[ExecutionContext]))(x => x)

    def leaderUrl(host: String, port: Int): java.util.concurrent.Future[java.lang.String] =
      leaderUrl(host, port, implicitly[api.Client])

    def leaderUrl(host: String, port: Int, version: Client): java.util.concurrent.Future[java.lang.String] =
      toJavaFutureWithValidation(leaderUrl(Connection(host, port), version, implicitly[ExecutionContext]))(x => x)


    def isLeader(implicit connection: Connection, version: Client = DEFAULT_MARATHON_VERSION, executor: ExecutionContext = implicitly[ExecutionContext]): Future[Validation[Error, Boolean]] =
      version.queryDebugIsLeader(connection, executor)

    def leaderUrl(implicit connection: Connection, version: Client = DEFAULT_MARATHON_VERSION, executor: ExecutionContext = implicitly[ExecutionContext]): Future[Validation[Error, String]] =
      version.queryDebugLeaderUrl(connection, executor)
  }

  object Endpoints {
    def query(host: String, port: Int): java.util.concurrent.Future[java.util.List[Endpoint]] =
      query(host, port, implicitly[api.Client])

    def query(host: String, port: Int, version: Client): java.util.concurrent.Future[java.util.List[Endpoint]] =
      toJavaFutureWithValidation(query(Connection(host, port), version))(_.toSeq.asJava)

    def queryForApp(host: String, port: Int, appID: String): java.util.concurrent.Future[Endpoint] =
      queryForApp(host, port, appID, implicitly[api.Client])

    def queryForApp(host: String, port: Int, appID: String, version: Client): java.util.concurrent.Future[Endpoint] =
      toJavaFutureWithValidation(queryForApp(appID)(Connection(host, port), version))(x => x)


    def query(implicit connection: Connection, version: Client = DEFAULT_MARATHON_VERSION, executor: ExecutionContext = implicitly[ExecutionContext]): Future[Validation[Error, Traversable[Endpoint]]] =
      version.queryEndpoints(connection, executor)

    def queryForApp(appID: String)(implicit connection: Connection, version: Client = DEFAULT_MARATHON_VERSION, executor: ExecutionContext = implicitly[ExecutionContext]): Future[Validation[Error, Endpoint]] =
      version.queryEndpointsForApp(appID)(connection, executor)
  }

  object Tasks {
    def query(host: String, port: Int): java.util.concurrent.Future[java.util.Map[String, java.util.List[Task]]] =
      query(host, port, implicitly[api.Client])

    def query(host: String, port: Int, version: Client): java.util.concurrent.Future[java.util.Map[String, java.util.List[Task]]] =
      toJavaFutureWithValidation(query(Connection(host, port), version))(_.toMap.mapValues(_.toSeq.asJava).asJava)

    def kill(host: String, port: Int, appID: String, taskHost: String): java.util.concurrent.Future[java.util.List[Task]] =
      kill(host, port, TaskKill(appID, taskHost), implicitly[api.Client])

    def kill(host: String, port: Int, appID: String, taskHost: String, id: String): java.util.concurrent.Future[java.util.List[Task]] =
      kill(host, port, TaskKill(appID, taskHost, id), implicitly[api.Client])

    def kill(host: String, port: Int, appID: String, taskHost: String, id: String, scale: Boolean): java.util.concurrent.Future[java.util.List[Task]] =
      kill(host, port, TaskKill(appID, taskHost, id, scale), implicitly[api.Client])

    def kill(host: String, port: Int, appID: String, taskHost: String, id: String, scale: Boolean, version: Client): java.util.concurrent.Future[java.util.List[Task]] =
      kill(host, port, TaskKill(appID, taskHost, id, scale), version)

    def kill(host: String, port: Int, taskKill: TaskKill, version: Client): java.util.concurrent.Future[java.util.List[Task]] =
      toJavaFutureWithValidation(kill(taskKill)(Connection(host, port), version))(_.toSeq.asJava)

    def kill(appID: String, taskHost: String)(implicit connection: Connection, version: Client, executor: ExecutionContext): Future[Validation[Error, Traversable[Task]]] =
      version.killTasks(TaskKill(appID, taskHost))(connection, executor)

    def kill(appID: String, taskHost: String, id: String)(implicit connection: Connection, version: Client, executor: ExecutionContext): Future[Validation[Error, Traversable[Task]]] =
      version.killTasks(TaskKill(appID, taskHost, id))(connection, executor)

    def kill(appID: String, taskHost: String, id: String, scale: Boolean)(implicit connection: Connection, version: Client, executor: ExecutionContext): Future[Validation[Error, Traversable[Task]]] =
      version.killTasks(TaskKill(appID, taskHost, id, scale))(connection, executor)


    def query(implicit connection: Connection, version: Client = DEFAULT_MARATHON_VERSION, executor: ExecutionContext = implicitly[ExecutionContext]): Future[Validation[Error, Map[String, Traversable[Task]]]] =
      version.queryTasks(connection, executor)

    def kill(taskKill: TaskKill)(implicit connection: Connection, version: Client = DEFAULT_MARATHON_VERSION, executor: ExecutionContext = implicitly[ExecutionContext]): Future[Validation[Error, Traversable[Task]]] =
      version.killTasks(taskKill)(connection, executor)
  }
}
