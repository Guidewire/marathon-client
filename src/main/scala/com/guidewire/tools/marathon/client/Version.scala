package com.guidewire.tools.marathon.client

/**
 * Provide access to version-specific client implementations for Java.
 */
object Version {
  val getDefault = DEFAULT_MARATHON_VERSION

  val v1 = api.v1
  val getVersion1 = api.v1
}
