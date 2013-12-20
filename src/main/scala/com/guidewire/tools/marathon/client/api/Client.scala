package com.guidewire.tools.marathon.client.api

import scalaz._
import dispatch._
import play.api.libs.json._
import scala.concurrent.ExecutionContext
import com.ning.http.client.{Response}
import org.jboss.netty.handler.codec.http.HttpResponseStatus

import com.guidewire.tools.marathon.client._

import scala.language.implicitConversions

/**
 * Provides versionable access to the Marathon client API.
 *
 * Specific versions provide a small number of properties and override anything else that may
 * be specific to them, otherwise the default behavior should be sufficient.
 */
trait Client
extends ClientVersion
with ClientApps
with ClientDebug
with ClientTasks
with ClientEndpoints

object Client {
  /**
   * Takes a [[play.api.libs.json.Format]] and maps between a more general representation to a version-specific representation.
   * This is necessary because [[play.api.libs.json.Writes]] is contravariant.
   *
   * @param specific existing [[play.api.libs.json.Format]] that will be mapped
   * @tparam TGeneral type that will be mapped to
   * @tparam TSpecific type that will be mapped from
   * @return new [[play.api.libs.json.Format]] that reads and writes the TGeneral type
   */
  def mapJsonFormat[TGeneral, TSpecific <: TGeneral](specific: Format[TSpecific]): Format[TGeneral] = new Format[TGeneral] {
    def reads(json: JsValue): JsResult[TGeneral] = specific.reads(json)
    def writes(o: TGeneral) = specific.writes(o.asInstanceOf[TSpecific])
  }

  /**
   * Performs an HTTP `GET` using a URL derived from the provided `endpoint`. The result is given to the provided `resultProcessor`.
   *
   * @param connection [[com.guidewire.tools.marathon.client.Connection]] describing the host and port to connect to
   * @param endpoint function that given a [[com.guidewire.tools.marathon.client.Connection]] will provide a URL as a [[scala.Predef.String]]
   * @param resultProcessor function that process the server response and produces a [[scalaz.Validation]]
   * @param executor [[scala.concurrent.ExecutionContext]] that will execute the HTTP request
   * @tparam TResult type of result that the `resultProcessor` should produce and validate
   * @return instance of [[scala.concurrent.Future]] whose promise is fulfilled upon receipt of the server response
   */
  def httpGet[TResult](connection: Connection)(endpoint: Connection => String)(resultProcessor: (Int, Array[Byte]) => Validation[Error, TResult])(implicit executor: ExecutionContext): Future[Validation[Error, TResult]] = {
    val GET =
      url(endpoint(connection))
        .addHeader("Accept", "application/json")
        .GET

    def processResponse(response: Response) = {
      val status_code = response.getStatusCode
      val status_code_class = status_code / 100

      status_code_class match {
        case 2 | 4 =>
          resultProcessor(status_code, response.getResponseBodyAsBytes)
        case _ =>
          throw StatusCode(status_code)
      }
    }

    Http(GET > processResponse _)
  }

  /**
   * Performs an HTTP `POST` using a URL derived from the provided `endpoint`. The result is given to the provided `resultProcessor`.
   * 
   * @param obj instance that will be serialized into JSON using the provided implicit [[play.api.libs.json.Writes]] instance
   * @param connection [[com.guidewire.tools.marathon.client.Connection]] describing the host and port to connect to
   * @param endpoint function that given a [[com.guidewire.tools.marathon.client.Connection]] will provide a URL as a [[scala.Predef.String]]
   * @param resultProcessor function that process the server response and produces a [[scalaz.Validation]]
   * @param executor [[scala.concurrent.ExecutionContext]] that will execute the HTTP request
   * @tparam TObject type of object that will be serialized into JSON using the provided implicit [[play.api.libs.json.Writes]] instance
   * @tparam TResult type of result that the `resultProcessor` should produce and validate
   * @return instance of [[scala.concurrent.Future]] whose promise is fulfilled upon receipt of the server response
   */
  def httpPostEmpty[TObject, TResult](obj: TObject, connection: Connection)(endpoint: Connection => String)(resultProcessor: (Int, Array[Byte]) => Validation[Error, TResult])(implicit executor: ExecutionContext): Future[Validation[Error, TResult]] = {
    val POST =
      url(endpoint(connection))
        .POST
        .addHeader("Content-Type", "application/json")
        .addHeader("Accept", "application/json")

    def processResponse(response: Response) = {
      val status_code = response.getStatusCode
      val status_code_class = status_code / 100

      status_code_class match {
        case 2 | 4 =>
          resultProcessor(status_code, response.getResponseBodyAsBytes)
        case _ =>
          throw StatusCode(status_code)
      }
    }

    Http(POST > processResponse _)
  }

  /**
   * Performs an HTTP `POST` using a URL derived from the provided `endpoint`. The result is given to the provided `resultProcessor`.
   *
   * @param obj instance that will be serialized into JSON using the provided implicit [[play.api.libs.json.Writes]] instance
   * @param connection [[com.guidewire.tools.marathon.client.Connection]] describing the host and port to connect to
   * @param endpoint function that given a [[com.guidewire.tools.marathon.client.Connection]] will provide a URL as a [[scala.Predef.String]]
   * @param resultProcessor function that process the server response and produces a [[scalaz.Validation]]
   * @param writer instance of [[play.api.libs.json.Writes]] that can serialize an instance of `TObject` into a [[play.api.libs.json.JsValue]]
   * @param executor [[scala.concurrent.ExecutionContext]] that will execute the HTTP request
   * @tparam TObject type of object that will be serialized into JSON using the provided implicit [[play.api.libs.json.Writes]] instance
   * @tparam TResult type of result that the `resultProcessor` should produce and validate
   * @return instance of [[scala.concurrent.Future]] whose promise is fulfilled upon receipt of the server response
   */
  def httpPostAsJson[TObject, TResult](obj: TObject, connection: Connection)(endpoint: Connection => String)(resultProcessor: (Int, Array[Byte]) => Validation[Error, TResult])(implicit writer: Writes[TObject], executor: ExecutionContext): Future[Validation[Error, TResult]] = {
    val POST =
      url(endpoint(connection))
        .POST
        .addHeader("Content-Type", "application/json")
        .addHeader("Accept", "application/json")
        .setBody(Json.toJson(obj).toString().getBytes(DEFAULT_MARATHON_CHARSET))

    def processResponse(response: Response) = {
      val status_code = response.getStatusCode
      val status_code_class = status_code / 100

      status_code_class match {
        case 2 | 4 =>
          resultProcessor(status_code, response.getResponseBodyAsBytes)
        case _ =>
          throw StatusCode(status_code)
      }
    }

    Http(POST > processResponse _)
  }

  /**
   * Takes a [[play.api.libs.json.JsResult]] and maps it to a [[scalaz.Validation]].
   *
   * @param result the [[play.api.libs.json.JsResult]] to map
   * @tparam T type of [[play.api.libs.json.JsResult]] that will be mapped to a [[scalaz.Validation]]
   * @return a [[scalaz.Validation]] that can be composed using normal scalaz methods
   */
  def validateify[T](statusCode: Int, result: JsResult[T]): Validation[Error, T] =
    result
      .map { p =>
        Success(p)
      }
      .recoverTotal { e =>
        val (_, errors) = e.errors.head
        Failure(Error(statusCode, errors.head.message))
      }

  /**
   * Takes `POST` responses that might indicate a problem and converts them into a [[scalaz.Validation]] instance.
   * Typical responses for a malformed JSON request might look like:
   *
   * <pre>
   *   {
   *     "message": "requirement failed: Already started app 'scalatest-start'"
   *   }
   * </pre>
   *
   * @param response should be a JSON payload that will be parsed and evaluated
   * @return instance of [[scalaz.Validation]] with the provided error message or `true` if the `POST` was successful
   */
  def processStandardHttpPostResponse[T <: ServerResponse](statusCode: Int, response: Array[Byte])(postProcess: => T): Validation[Error, T] = {
    val processed = postProcess
    if (processed.validationAllowed)
      Success(processed)
    else if (response.isEmpty)
      Failure(Error(statusCode, s"${HttpResponseStatus.valueOf(statusCode)}"))
    else {
      validateify(statusCode, Json.parse(response).validate[SingleResponseError]) match {
        case Failure(error) => Failure(error)
        case Success(responseError) => Failure(Error(statusCode, responseError.message))
      }
    }
  }

  /**
   * Takes `POST` responses that might indicate a problem and converts them into a [[scalaz.Validation]] instance.
   * Typical responses for a malformed JSON request might look like:
   *
   * <pre>
   *   {
   *     "errors": [
   *       {
   *         "attribute": "cmd",
   *         "error": "may not be empty"
   *       },
   *       {
   *         "attribute": "id",
   *         "error": "may not be empty"
   *       }
   *     ]
   *   }
   * </pre>
   *
   * @param response should be a JSON payload that will be parsed and evaluated
   * @return instance of [[scalaz.Validation]] with the provided error message or `true` if the `POST` was successful
   */
  def processStandardHttpPostResponseMultipleErrors[T <: ServerResponse](statusCode: Int, response: Array[Byte])(postProcess: => T): Validation[Error, T] = {
    val processed = postProcess
    if (processed.validationAllowed)
      Success(processed)
    else if (response.isEmpty)
      Failure(Error(statusCode, s"$statusCode: ${HttpResponseStatus.valueOf(statusCode)}"))
    else {
      validateify(statusCode, Json.parse(response).validate[MultipleResponseErrors]) match {
        case Failure(error) => Failure(error)
        case Success(responseErrors) => Failure(Error(statusCode, {
          val messages =
            for(e <- responseErrors.errors)
              yield s"${e.attribute}: ${e.error}"
          messages mkString "\n"
        }))
      }
    }
  }

  /**
   * Takes `GET` responses that are not valid JSON but are valid boolean values like `true` or `false` and converts
   * them into a [[scalaz.Validation]] instance.
   *
   * @param response should be a single [[scala.Predef.String]] payload that will be parsed and evaluated
   * @return instance of [[scalaz.Validation]] with an error message if unable to decode the value or the return value
   *         if the `GET` was successful
   */
  def processSingleBooleanStringHttpGetResponse(statusCode: Int, response: Array[Byte]): Validation[Error, Boolean] = {
    try {
      val parsed_response = DEFAULT_MARATHON_CHARSET.decode(java.nio.ByteBuffer.wrap(response)).toString.toLowerCase
      Success(parsed_response == "true")
    } catch {
      case t: Throwable => Failure(Error(statusCode, t.getMessage))
    }
  }

  /**
   * Takes `GET` responses that are not valid JSON but are valid [[scala.Predef.String]] values and converts
   * them into a [[scalaz.Validation]] instance.
   *
   * @param response should be a single [[scala.Predef.String]] payload that will be parsed and evaluated
   * @return instance of [[scalaz.Validation]] with an error message if unable to decode the value or the return value
   *         if the `GET` was successful
   */
  def processSingleStringHttpGetResponse(statusCode: Int, response: Array[Byte]): Validation[Error, String] = {
    try {
      val parsed_response = DEFAULT_MARATHON_CHARSET.decode(java.nio.ByteBuffer.wrap(response)).toString
      Success(parsed_response)
    } catch {
      case t: Throwable => Failure(Error(statusCode, t.getMessage))
    }
  }

  /**
   * Validates that an HTTP status code is in the OK range.
   *
   * @param statusCode HTTP status code to evaluate
   * @return `true` if the HTTP status code is OK
   */
  def isOK(statusCode: Int): Boolean =
    statusCode / 100 == 2
}