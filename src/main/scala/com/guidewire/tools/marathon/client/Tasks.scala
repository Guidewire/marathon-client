package com.guidewire.tools.marathon.client

trait Task {
  def id   : String
  def host : String
  def ports: Seq[Int]
}

trait TaskKill {
  def appId: String
  def host : String
  def id   : String
  def scale: Boolean
}


object Task {
  type Apply[TTask <: Task] = (
      String
    , String
    , Seq[Int]
  ) => TTask

  def apply(
      id   : String
    , host : String
    , ports: Seq[Int]
  )
  (implicit version: api.Client)
  = validate(version.VersionSpecificTaskApply(id, host, ports))

  def validate(task: Task): Task =
    task
}

/**
 * WARNING: This should never be a companion object or it will not be exposed to the Java API
 *          correctly.
 */
object Tasks {
  def create(
      id   : String
    , host : String
    , ports: Seq[Int]
  )
  = Task(id = id, host = host, ports = ports)(implicitly[api.Client])

  def create(
      id     : String
    , host   : String
    , ports  : Seq[Int]
    , version: api.Client
  )
  = Task(id = id, host = host, ports = ports)(version)
}

object TaskKill {
  type Apply[TTaskKill <: TaskKill] = (
      String
    , String
    , String
    , Boolean
  ) => TTaskKill

  def apply(
      appId: String
    , host : String
    , id   : String = "*"
    , scale: Boolean = false
  )
  (implicit version: api.Client)
  = validate(version.VersionSpecificTaskKillApply(appId, host, id, scale))

  def validate(taskKill: TaskKill): TaskKill =
    taskKill
}

/**
 * WARNING: This should never be a companion object or it will not be exposed to the Java API
 *          correctly.
 */
object TaskKills {
  def create(
      appId: String
    , host : String
    , id   : String
    , scale: Boolean
  )
  = TaskKill(appId = appId, host = host, id = id, scale = scale)(implicitly[api.Client])

  def create(
      appId  : String
    , host   : String
    , id     : String
    , scale  : Boolean
    , version: api.Client
  )
  = TaskKill(appId = appId, host = host, id = id, scale = scale)(version)

  def create(
      appId: String
    , host : String
  )
  = TaskKill(appId = appId, host = host)(implicitly[api.Client])

  def create(
      appId  : String
    , host   : String
    , version: api.Client
  )
  = TaskKill(appId = appId, host = host)(version)

  def create(
      appId: String
    , host : String
    , id   : String
  )
  = TaskKill(appId = appId, host = host, id = id)(implicitly[api.Client])

  def create(
      appId  : String
    , host   : String
    , id     : String
    , version: api.Client
  )
  = TaskKill(appId = appId, host = host, id = id)(version)
}
