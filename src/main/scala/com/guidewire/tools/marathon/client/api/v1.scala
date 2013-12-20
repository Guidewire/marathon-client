package com.guidewire.tools.marathon.client.api

object v1 extends Client {
  type VersionSpecificApp              = version1.App
  val  VersionSpecificAppFormat        = version1.App.fmt
  val  VersionSpecificAppApply         = version1.App.apply _

  type VersionSpecificAppScale         = version1.AppScale
  val  VersionSpecificAppScaleFormat   = version1.AppScale.fmt
  val  VersionSpecificAppScaleApply    = version1.AppScale.apply _

  type VersionSpecificAppDestroy       = version1.AppDestroy
  val  VersionSpecificAppDestroyFormat = version1.AppDestroy.fmt
  val  VersionSpecificAppDestroyApply  = version1.AppDestroy.apply _

  type VersionSpecificConstraint       = version1.Constraint
  val  VersionSpecificConstraintFormat = version1.Constraint.fmt
  val  VersionSpecificConstraintApply  = version1.Constraint.apply _

  type VersionSpecificTask             = version1.Task
  val  VersionSpecificTaskFormat       = version1.Task.fmt
  val  VersionSpecificTaskApply        = version1.Task.apply _

  type VersionSpecificTaskKill         = version1.TaskKill
  val  VersionSpecificTaskKillFormat   = version1.TaskKill.fmt
  val  VersionSpecificTaskKillApply    = version1.TaskKill.apply _

  type VersionSpecificEndpoint         = version1.Endpoint
  val  VersionSpecificEndpointFormat   = version1.Endpoint.fmt
  val  VersionSpecificEndpointApply    = version1.Endpoint.apply _

  val  uriVersionPart                  = "v1"
}
