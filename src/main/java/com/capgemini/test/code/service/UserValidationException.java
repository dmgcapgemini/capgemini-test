package com.capgemini.test.code.service;

public class UserValidationException extends RuntimeException {

  public UserValidationException(String field) {
    super("error validation " + field);
  }
}
