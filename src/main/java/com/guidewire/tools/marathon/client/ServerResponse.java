package com.guidewire.tools.marathon.client;

public interface ServerResponse {
  int statusCode();
  int statusCodeRangeStart();
  int statusCodeRangeEnd();
  boolean success();
  boolean validationAllowed();

  int getStatusCode();
  int getStatusCodeRangeStart();
  int getStatusCodeRangeEnd();
  boolean isSuccess();
  boolean isValidationAllowed();
}