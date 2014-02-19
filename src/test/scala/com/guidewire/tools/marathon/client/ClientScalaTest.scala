package com.guidewire.tools.marathon.client

import org.scalatest.Assertions._

import scalaz._
import scala.concurrent._
import scala.concurrent.duration._

import dispatch._, Defaults._

import scala.language.postfixOps
import java.nio.charset.Charset
import org.jboss.netty.handler.codec.http.HttpResponseStatus

object ClientScalaTest {
  import ClientDefaults._

  def blockAndValidateSuccess[A](f: => scala.concurrent.Future[List[Validation[Error, A]]]): List[A] = {
    val future = f
    future.onComplete {
      case scala.util.Failure(failure) => fail(failure)
      case scala.util.Success(result) => result foreach validateSuccess
    }
    val result = Await.result(future, 10 seconds)
    for(r <- result)
      yield r match {
        case scalaz.Failure(failure) => fail(s"$failure")
        case scalaz.Success(value) => value
      }
  }

  def blockAndValidateSuccess[A](f: => scala.concurrent.Future[Validation[Error, A]]): A = {
    val future = f
    future.onComplete {
      case scala.util.Failure(failure) =>
        fail(failure)
      case scala.util.Success(result) =>
        validateSuccess(result)
    }
    val result = Await.result(future, 10 seconds)
    result match {
      case scalaz.Failure(failure) => fail(s"$failure")
      case scalaz.Success(value) => value
    }
  }

  def withStringResource[A](resource: String)(res: String => A) = {
    var source: scala.io.BufferedSource = null
    try {
      source = scala.io.Source.fromURL(getClass.getResource(resource))
      res(source.getLines() mkString "\n")
    } finally {
      if (source ne null)
        source.close()
    }
  }

  def withByteArrayResource[A](resource: String)(res: Array[Byte] => A) = {
    var source: scala.io.BufferedSource = null
    try {
      source = scala.io.Source.fromURL(getClass.getResource(resource))
      res(source.getLines().map(_.getBytes(Charset.forName("UTF-8"))).toArray.flatten)
    } finally {
      if (source ne null)
        source.close()
    }
  }

  def validateSuccess[A](v: Validation[Error, A]): Option[A] = v match {
    case scalaz.Success(x) =>
      Some(x)
    case scalaz.Failure(error) =>
      fail(s"$error"); None
    case _ => fail(s"Failed in validateSuccess()"); None
  }

  def validateFailure(v: Validation[Error, Boolean]): Option[String] = v match {
    case scalaz.Success(x) => fail(s"Expected failure")
    case scalaz.Failure(Error(_, error)) => Some(error)
    case _ => fail(s"Failed in validateFailure()"); None
  }

  implicit def VALIDATE_RESOURCE_PARSE_SUCCESS_NOOP[A]: A => Unit = (_: A) => { }

  def validateResourceParse[A](resource: String)(f: (Int, Array[Byte]) => Validation[Error, A])(implicit success: A => Unit): Option[A] =
    withByteArrayResource(resource) { res =>
      val validated = validateSuccess(f(200, res))
      validated match {
        case Some(result) => success(result)
        case _ =>
      }
      validated
    }

  def validateErrorResponseResourceParse(resource: String)(f: (Int, Array[Byte]) => Validation[Error, ServerResponse])(implicit success: String => Unit): Option[String] =
    validateErrorResponseResourceParseWithBoolean(resource)((statusCode, response) => f(statusCode, response) match {
      case scalaz.Success(value) if !value.isSuccess => scalaz.Failure(Error(statusCode, s"${HttpResponseStatus.valueOf(statusCode)}"))
      case scalaz.Failure(error) => scalaz.Failure(error)
      case scalaz.Success(value) => scalaz.Success(value.isSuccess)
    })(success)

  def validateErrorResponseResourceParseWithBoolean(resource: String)(f: (Int, Array[Byte]) => Validation[Error, Boolean])(implicit success: String => Unit): Option[String] =
    withByteArrayResource(resource) { res =>
      val validated = validateFailure(f(422, res))
      validated match {
        case Some(result) => success(result)
        case _ =>
      }
      validated
    }

  def ping(host: String, port: Int): Boolean = {
    import java.io._
    import java.net._

    var s: Socket = null
    try {
      s = new Socket(host, port)
      s.isConnected
    } catch {
      case iae: IllegalArgumentException =>
        fail(iae)
        false
      case _: Throwable =>
        false
    } finally {
      if (s ne null)
        s.close()
    }
  }

  def asyncPing(host: String, port: Int): scala.concurrent.Future[Boolean] = future {
    ping(host, port)
  }

  def onlyTestIfCanConnectToHost(host: String = DEFAULT_HOST, port: Int = DEFAULT_PORT)(success: (String, Int) => Unit): Unit = {
    val result = Await.result(asyncPing(host, port), 3 seconds)
    org.scalatest.Assertions.assume(result, s"""IGNORED: test requires "$host:$port" to be up""")
    if (result)
      success(host, port)
  }

  def ignoreIfHostNotUp(success: (String, Int) => Unit): Unit =
    onlyTestIfCanConnectToHost()(success)
}
