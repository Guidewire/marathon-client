package com.guidewire.tools.marathon.client

import scala.util._
import scala.concurrent._
import java.util.concurrent.{Callable, FutureTask}

object Utils {
  def toJavaFuture[TScalaType, TJavaType](f: Future[TScalaType])(map: (TScalaType => TJavaType))(implicit executor: ExecutionContext): java.util.concurrent.Future[TJavaType] = {
    val applied = f.map(map)

    val task = new FutureTask[TJavaType](new Callable[TJavaType] {
      def call(): TJavaType = applied.value.get match {
        case Success(v) => v
        case Failure(t) => throw new ExecutionException(s"Failure during execution", t)
        case _ => throw new ExecutionException(s"Failure during execution", null)
      }
    })

    applied.onComplete {
      case _ =>
        task.run()
    }

    task

    /*
    //Original attempt

    val latch = new CountDownLatch(1)
    val result = new AtomicReference[scala.util.Try[TJavaType]]()

    f.onComplete {
      case t =>
        result.set(t.map(map))
        latch.countDown()
    }

    def fetchResult =
      result.get match {
        case scala.util.Success(v) => v
        case scala.util.Failure(t) => throw new ExecutionException(s"Failure during execution", t)
        case _ => throw new ExecutionException(s"Failure during execution", null)
      }

    new java.util.concurrent.Future[TJavaType]() {
      def cancel(mayInterruptIfRunning: Boolean): Boolean = false
      def isCancelled: Boolean = false
      def isDone: Boolean = f.isCompleted
      def get(): TJavaType = {
        latch.await()
        fetchResult
      }
      def get(timeout: Long, unit: TimeUnit): TJavaType = {
        if (!latch.await(timeout, unit))
          throw new TimeoutException(s"Timeout while waiting for execution to complete")
        fetchResult
      }
    }
    */
  }

  def toJavaFutureWithValidation[TValidationType, TJavaType](f: Future[scalaz.Validation[Error, TValidationType]])(map: TValidationType => TJavaType)(implicit executor: ExecutionContext): java.util.concurrent.Future[TJavaType] =
    toJavaFuture(f)(_ match {
      case scalaz.Success(value) => map(value)
      case scalaz.Failure(fail) => throw new IllegalStateException(s"Error during execution: $fail", null)
    })

  def toJavaFutureWithValidationNoMap[TJavaType](f: Future[scalaz.Validation[Error, TJavaType]])(implicit executor: ExecutionContext): java.util.concurrent.Future[TJavaType] =
    toJavaFutureWithValidation[TJavaType, TJavaType](f)(x => x)(executor)
}
