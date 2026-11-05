package com.capgemini.test.code.service;

public class ExternalServiceException extends RuntimeException {

  public ExternalServiceException(String message, Throwable cause) {
    super(message, cause);
  }
}
