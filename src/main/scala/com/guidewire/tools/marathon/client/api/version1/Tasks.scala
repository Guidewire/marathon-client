package com.guidewire.tools.marathon.client.api.version1

import play.api.libs.json._

import com.guidewire.tools.marathon.client

case class Task(
    id  : String
  , host: String
  , ports: Seq[Int]
) extends client.Task

object Task {
  implicit val fmt = Json.format[Task]
}

case class TaskKill(
    appId: String
  , host : String
  , id   : String
  , scale: Boolean
) extends client.TaskKill

object TaskKill {
  implicit val fmt = Json.format[TaskKill]
}