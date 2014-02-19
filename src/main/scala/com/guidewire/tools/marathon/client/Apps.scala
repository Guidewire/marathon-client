package com.guidewire.tools.marathon.client

trait Container {
  def image  : String
  def options: Seq[String]
}

object Container {
  type Apply[TContainer <: Container] = (
      String
    , Seq[String]
  ) => TContainer

  def apply(
      image: String
    , options: Seq[String] = Seq()
  )
  (implicit version: api.Client)
  = validate(version.VersionSpecificContainerApply(image, options))

  def validate(container: Container): Container =
    container
}

/**
 * WARNING: This should never be a companion object or it will not be exposed to the Java API
 *          correctly.
 */
object Containers {
  def create(
      image  : String
  )
  = Container(image)(implicitly[api.Client])

  def create(
      image  : String
    , options: Seq[String]
  )
  = Container(image, options)(implicitly[api.Client])

  def create(
      image: String
    , version: api.Client
  )
  = Container(image)(version)

  def create(
      image: String
    , options: Seq[String]
    , version: api.Client
  )
  = Container(image, options)(version)
}

trait Constraint {
  def field   : String
  def operator: String
  def value   : Option[String]
}

object Constraint {
  type Apply[TConstraint <: Constraint] = (
      String
    , String
    , Option[String]
  ) => TConstraint

  def apply(
      field: String
    , operator: String
    , value: Option[String]
  )
  (implicit version: api.Client)
  = validate(version.VersionSpecificConstraintApply(field, operator, value))

  def validate(constraint: Constraint): Constraint =
    constraint
}

/**
 * WARNING: This should never be a companion object or it will not be exposed to the Java API
 *          correctly.
 */
object Constraints {
  def create(
      field   : String
    , operator: String
    , value   : String
  )
  = Constraint(field, operator, Option(value))(implicitly[api.Client])

  def create(
      field: String
    , operator: String
    , value: String
    , version: api.Client
  )
  = Constraint(field, operator, Option(value))(version)
}

trait App {
  def id           : String
  def cmd          : String
  def env          : Map[String, String]
  def instances    : Int
  def cpus         : Double
  def mem          : Double
  def executor     : String
  def constraints  : Seq[Constraint]
  def uris         : Seq[String]
  def ports        : Seq[Int]
  def container    : Option[Container]
  //def taskRateLimit: Option[Double]
}

object App {
  type Apply[TContainer <: Container, TConstraint <: Constraint, TApp <: App] = (
      String
    , String
    , Map[String, String]
    , Int
    , Double
    , Double
    , String
    , Seq[TConstraint]
    , Seq[String]
    , Seq[Int]
    , Option[TContainer]
  ) => TApp

  def apply(
      id           : String
    , cmd          : String
    , env          : Map[String, String] = Map.empty
    , instances    : Int                 = 0
    , cpus         : Double              = 1.0
    , mem          : Double              = 128.0
    , executor     : String              = ""
    , constraints  : Seq[Constraint]     = Seq()
    , uris         : Seq[String]         = Seq()
    , ports        : Seq[Int]            = Seq()
    , container    : Option[Container]   = None
    //, taskRateLimit: Option[Double]      = Some(1.0)
  )
  (implicit version: api.Client)
  = validate(version.VersionSpecificAppApply(id, cmd, env, instances, cpus, mem, executor, for(c <- constraints) yield version.VersionSpecificConstraintApply(c.field, c.operator, c.value), uris, ports, for(c <- container) yield version.VersionSpecificContainerApply(c.image, c.options)/*, taskRateLimit*/))

  val REGEX_VALIDATE_EXECUTOR = """(^//cmd$)|(^/[^/].*$)|""".r.pattern
  val REGEX_VALIDATE_ID = """^[A-Za-z0-9_.-]+$""".r.pattern

  def isValidID(id: String) =
    REGEX_VALIDATE_ID.matcher(id).matches()

  def isValidExecutor(executor: String) =
    REGEX_VALIDATE_EXECUTOR.matcher(executor).matches()

  def validate(app: App): App = {
    require(isValidExecutor(app.executor),                          "Incorrect executor parameter format")
    require(isValidID(app.id),                                      "Incorrect id parameter format")
    require((app.cmd ne null) && app.cmd != "",                     "Missing command parameter")
    require(app.constraints.forall(Constraint.validate(_) ne null), "Invalid constraint(s)")
    app
  }
}

import scala.collection.JavaConverters._

/**
 * WARNING: This should never be a companion object or it will not be exposed to the Java API
 *          correctly.
 */
object Apps {
  def create(
      id           : String
    , cmd          : String
    , env          : java.util.Map[String, String]
    , instances    : Int
    , cpus         : Double
    , mem          : Double
    , executor     : String
    , constraints  : java.util.List[Constraint]
    , uris         : java.util.List[String]
    , ports        : java.util.List[Int]
    //, taskRateLimit: Option[Double]
    , version      : api.Client
  )
  = App(id, cmd, env.asScala.toMap, instances, cpus, mem, executor, constraints.asScala.toSeq, uris.asScala.toSeq, ports.asScala.toSeq)(version)

  def create(
      id           : String
    , cmd          : String
    , instances    : Int
    , cpus         : Double
    , mem          : Double
    , executor     : String
    , ports        : java.util.List[Int]
    //, taskRateLimit: Option[Double]
    , version      : api.Client
  )
  = App(id = id, cmd = cmd, instances = instances, cpus = cpus, mem = mem, executor = executor, ports = ports.asScala.toSeq)(implicitly[api.Client])

  def create(
      id           : String
    , cmd          : String
    , instances    : Int
    , cpus         : Double
    , mem          : Double
    , executor     : String
    , ports        : java.util.List[Int]
  )
  = App(id = id, cmd = cmd, instances = instances, cpus = cpus, mem = mem, executor = executor, ports = ports.asScala.toSeq)(implicitly[api.Client])

  def create(
      id           : String
    , cmd          : String
  )
  = App(id = id, cmd = cmd)(implicitly[api.Client])

  def create(
      id           : String
    , cmd          : String
    , version      : api.Client
  )
  = App(id = id, cmd = cmd)(version)
}

trait AppScale {
  def id       : String
  def instances: Int
}

object AppScale {
  type Apply[TAppScale <: AppScale] = (
      String
    , Int
  ) => TAppScale

  def apply(
      id       : String
    , instances: Int
  )
  (implicit version: api.Client)
  = validate(version.VersionSpecificAppScaleApply(id, instances))

  def validate(scale: AppScale): AppScale =
    scale
}

/**
 * WARNING: This should never be a companion object or it will not be exposed to the Java API
 *          correctly.
 */
object AppScales {
  def create(
      id       : String
    , instances: Int
  )
  = AppScale(id, instances)(implicitly[api.Client])

  def create(
      id       : String
    , instances: Int
    , version  : api.Client
  )
  = AppScale(id, instances)(version)
}

trait AppDestroy {
  def id: String
}

object AppDestroy {
  type Apply[TAppDestroy <: AppDestroy] = (
      String
  ) => TAppDestroy

  def apply(
      id: String
  )
  (implicit version: api.Client)
  = validate(version.VersionSpecificAppDestroyApply(id))

  def validate(destroy: AppDestroy): AppDestroy =
    destroy
}

/**
 * WARNING: This should never be a companion object or it will not be exposed to the Java API
 *          correctly.
 */
object AppDestroys {
  def create(
      id: String
  )
  = AppDestroy(id)(implicitly[api.Client])

  def create(
      id     : String
    , version: api.Client
  )
  = AppDestroy(id)(version)
}
