package com.guidewire.tools.marathon

import java.nio.charset.Charset
import scalaz.Validation

import scala.language.implicitConversions

package object client {
  implicit val DEFAULT_MARATHON_VERSION: api.Client = api.v1
  val DEFAULT_MARATHON_CHARSET = Charset.forName("UTF-8")
  val DEFAULT_MARATHON_PORT = 80

  implicit class ServerResponseExtensions(v: Validation[Error, ServerResponse]) {
    @inline def toBoolean: Validation[Error, Boolean] =
      v map(x => x.isSuccess)
  }
}
