package com.guidewire.tools.marathon.client;

public enum AppResponse implements ServerResponse {
    SUCCESS        (200, 299, true)
  , ALREADY_STARTED(422,      true)
  , UNKNOWN_APP    (404,      true)

  , UNKNOWN        (  -1,     false)
  ;

  private int status_code_range_start;
  private int status_code_range_end;
  private boolean consider_success;

  private AppResponse(final int status_code, final boolean consider_success) {
    this(status_code, status_code, consider_success);
  }

  private AppResponse(final int status_code_range_start, final int status_code_range_end, final boolean consider_success) {
    this.status_code_range_start = status_code_range_start;
    this.status_code_range_end = status_code_range_end;
    this.consider_success = consider_success;
  }

  @Override
  public int statusCode() {
    return statusCodeRangeStart();
  }

  @Override
  public int statusCodeRangeStart() {
    return status_code_range_start;
  }

  @Override
  public int statusCodeRangeEnd() {
    return status_code_range_end;
  }

  @Override
  public boolean success() {
    return isSuccess(this);
  }

  @Override
  public boolean validationAllowed() {
    return consider_success;
  }

  @Override
  public int getStatusCode() {
    return statusCode();
  }

  @Override
  public int getStatusCodeRangeStart() {
    return statusCodeRangeStart();
  }

  @Override
  public int getStatusCodeRangeEnd() {
    return statusCodeRangeEnd();
  }

  @Override
  public boolean isSuccess() {
    return success();
  }

  @Override
  public boolean isValidationAllowed() {
    return validationAllowed();
  }

  public static boolean isValidationAllowed(final AppResponse response) {
    return response.validationAllowed();
  }

  public static boolean isSuccess(final AppResponse response) {
    return SUCCESS == response;
  }

  public static AppResponse fromStatusCode(final int statusCode) {
    for(final AppResponse response : AppResponse.values())
      if (response.status_code_range_start <= statusCode && statusCode <= response.status_code_range_end)
        return response;
    return UNKNOWN;
  }
}