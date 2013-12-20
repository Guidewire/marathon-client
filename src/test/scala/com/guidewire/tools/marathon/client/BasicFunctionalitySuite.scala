package com.guidewire.tools.marathon.client

import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfterAll, SeveredStackTraces, FunSuite}
import org.junit.runner.RunWith
import org.scalatest.matchers.ShouldMatchers

import dispatch._, Defaults._

import play.api.libs.json._
import play.api.libs.functional._

import scalaz._

@RunWith(classOf[JUnitRunner])
class BasicFunctionalitySuite extends FunSuite
                     with ShouldMatchers
                     with SeveredStackTraces {
  import api._
  import ClientScalaTest._

  test("Can connect to running Marathon") (ignoreIfHostNotUp { (host, port) =>
    val apps =
      blockAndValidateSuccess {
        Marathon.Apps.query(Connection(host, port))
      }
    for(app <- apps) {
      app should not be null
      app.id should not be ""
      //println(s"$app")
    }
  })

  test("Can call start on running Marathon") (ignoreIfHostNotUp { (host, port) =>
    val List(start, cleanup) =
      blockAndValidateSuccess {
        implicit val cn = Connection(host, port)
        for {
                _ <- Marathon.Apps.destroy("scalatest-app-start")
            start <- Marathon.Apps.start(App("scalatest-app-start", "echo scalatest-app-start"))
          cleanup <- Marathon.Apps.destroy("scalatest-app-start")
        } yield List(start, cleanup)
      }

    withClue(s"[CLEANUP REQUIRED] Unable to fully process test on <$host:$port>: ") {
      start.isSuccess should be (true)
      cleanup.isSuccess should be (true)
    }
  })

  test("Can call scale on running Marathon") (ignoreIfHostNotUp { (host, port) =>
    val result =
      blockAndValidateSuccess {
        implicit val cn = Connection(host, port)
        for {
                _ <- Marathon.Apps.destroy("scalatest-app-scale")
          created <- Marathon.Apps.start(App("scalatest-app-scale", "echo scalatest-app-scale"))
            scale <- Marathon.Apps.scale("scalatest-app-scale", 3)
          cleanup <- Marathon.Apps.destroy("scalatest-app-scale")
        } yield List(created, scale, cleanup)
      }

    withClue(s"[CLEANUP REQUIRED] Unable to fully process test on <$host:$port>: ") {
      result.forall(_.isSuccess) should be (true)
    }
  })

  test("Can call suspend on running Marathon") (ignoreIfHostNotUp { (host, port) =>
   val result =
      blockAndValidateSuccess {
        implicit val cn = Connection(host, port)
        for {
                _ <- Marathon.Apps.destroy("scalatest-app-suspend")
          created <- Marathon.Apps.start(App("scalatest-app-suspend", "echo scalatest-app-suspend"))
          suspend <- Marathon.Apps.suspend("scalatest-app-suspend")
          cleanup <- Marathon.Apps.destroy("scalatest-app-suspend")
        } yield List(created, suspend, cleanup)
      }

    withClue(s"[CLEANUP REQUIRED] Unable to fully process test on <$host:$port>: ") {
      result.forall(_.isSuccess) should be (true)
    }
  })

  test("Can call destroy (stop) on running Marathon") (ignoreIfHostNotUp { (host, port) =>
    val result =
      blockAndValidateSuccess {
        implicit val cn = Connection(host, port)
        for {
                _ <- Marathon.Apps.destroy("scalatest-app-destroy")
          created <- Marathon.Apps.start(App("scalatest-app-destroy", "echo scalatest-app-destroy"))
          cleanup <- Marathon.Apps.destroy("scalatest-app-destroy")
        } yield List(created, cleanup)
      }

    withClue(s"[CLEANUP REQUIRED] Unable to fully process test on <$host:$port>: ") {
      result.forall(_.isSuccess) should be (true)
    }
  })

  test("Can call debug isLeader on running Marathon") (ignoreIfHostNotUp { (host, port) =>
    blockAndValidateSuccess {
      implicit val cn = Connection(host, port)
      for {
        done <- Marathon.Debug.isLeader
      } yield done
    }
  })

  test("Can call debug leaderUrl on running Marathon") (ignoreIfHostNotUp { (host, port) =>
    val result =
      blockAndValidateSuccess {
        implicit val cn = Connection(host, port)
        for {
          done <- Marathon.Debug.leaderUrl
        } yield done
      }
    result should not be ("")
  })

  test("Can call endpoints on running Marathon") (ignoreIfHostNotUp { (host, port) =>
    val result =
      blockAndValidateSuccess {
        implicit val cn = Connection(host, port)
        for {
          done <- Marathon.Endpoints.query
        } yield {
          done.isSuccess should be (true)
          done
        }
      }
    for(endpoint <- result) {
      endpoint should not be (null)
      endpoint.id should not be ("")
      //println(endpoint)
    }
  })

  test("Can call endpoints for app on running Marathon") (ignoreIfHostNotUp { (host, port) =>
    val result =
      blockAndValidateSuccess {
        implicit val cn = Connection(host, port)
        for {
                _ <- Marathon.Apps.start(App("scalatest-endpoints-queryForApp", "echo scalatest endpoints queryForApp"))
            query <- Marathon.Endpoints.queryForApp("scalatest-endpoints-queryForApp")
          cleanup <- Marathon.Apps.destroy("scalatest-endpoints-queryForApp")
        } yield List(query, cleanup)
      }
    result.forall(_ ne null) should be (true)
  })

  test("Can call tasks on running Marathon") (ignoreIfHostNotUp { (host, port) =>
    val result =
      blockAndValidateSuccess {
        implicit val cn = Connection(host, port)
        for {
          done <- Marathon.Tasks.query
        } yield {
          done.isSuccess should be (true)
          done
        }
      }
    for((id, tasks) <- result) {
      id should not be (null)
      id should not be ("")

      tasks should not be (null)
      tasks.foreach { task =>
        task.host should not be ("")
      }

      //println(s"$id: $tasks")
    }
  })

  test("Can kill tasks on running Marathon") (ignoreIfHostNotUp { (host, port) =>
    val result =
      blockAndValidateSuccess {
        implicit val cn = Connection(host, port)
        for {
          done <- Marathon.Tasks.kill("scalatest-tasks-kill", host)
        } yield {
          done.isSuccess should be (true)
          done
        }
      }
    for(task <- result) {
      task.id should not be (null)
      task.id should not be ("")

      //println(s"$task")
    }
  })

  test("Testing Json serialization idempotency") {
    import api.v1._

    val ORIGINAL = Seq(App("100", "run", Map(), 1, 4, 200, "", List(), List()))
    var appsPayload = ORIGINAL

    for(_ <- 0 until 20) {
      val json = Json.toJson(appsPayload)
      val pp: String = Json.prettyPrint(json)
      appsPayload = Json.parse(pp).as[Seq[App]]
      appsPayload should equal (ORIGINAL)
    }
  }

  test("Ports") (ignoreIfHostNotUp { (host, port) =>
    val result =
      blockAndValidateSuccess {
        implicit val cn = Connection(host, port)
        for {
             _ <- Marathon.Apps.destroy("ports-test-1")
          done <- Marathon.Apps.start(App(
                      id        = "ports-test-1"
                    , cmd       = "while sleep 1; do echo \"MY PORTS: $PORTS\" >> /tmp/ports.log; done"
                    , instances = 1
                    , cpus      = 4.0
                    , mem       = 4096
                    , ports     = Seq.fill(10)(0) //Asks for 10 ports to use
                  ))
        } yield done
      }
    result.success should be (true)
  })

  test("Docker") (ignoreIfHostNotUp { (host, port) =>
    val result =
      blockAndValidateSuccess {
        implicit val cn = Connection(host, port)
        for {
             _ <- Marathon.Apps.destroy("docker-gitmo-2")
          done <- Marathon.Apps.start(App(
                      id        = "docker-gitmo-2"
                    , cmd       = "-v /var/log/gitmo:/var/log/gitmo -p 12345:12345 docker.sandbox-master.guidewire.com/ubuntu:sandbox-raring deploy -app bc -version 8.0.0 -appserver tomcat7 -port 12345 -overwrite"
                    , instances = 1
                    , cpus      = 4.0
                    , mem       = 4096
                    , executor  = "/opt/mesos/default/var/lib/mesos/executors/guidewire-docker-executor"
                  ))
        } yield done
      }
    result.success should be (true)
  })

  for(i <- 1 to 3)
    test(f"Can deserialize simple app lists (/marathon-apps-query-apps-${i}%03d.json)")(validateResourceParse(f"/marathon-apps-query-apps-${i}%03d.json")(api.v1.parseQueryAppsResponse))

  for(i <- 1 to 1)
    test(f"Can properly handle errors when POSTing to /app/start (/marathon-apps-multiple-error-response-post-start-${i}%03d.json)")(validateErrorResponseResourceParse(f"/marathon-apps-multiple-error-response-post-start-${i}%03d.json")(api.v1.parsePostAppStartResponse))
}
