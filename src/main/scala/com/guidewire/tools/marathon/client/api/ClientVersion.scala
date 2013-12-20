package com.guidewire.tools.marathon.client.api

trait ClientVersion {
  /** Provides a version for forming a URI to make an HTTP request to. */
  def uriVersionPart: String
}
