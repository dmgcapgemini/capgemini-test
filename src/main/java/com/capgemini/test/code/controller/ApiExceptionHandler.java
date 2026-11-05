package com.capgemini.test.code.controller;

import com.capgemini.test.code.dto.ErrorResponse;
import com.capgemini.test.code.service.ExternalServiceException;
import com.capgemini.test.code.service.UserValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

  @ExceptionHandler(UserValidationException.class)
  public ResponseEntity<ErrorResponse> handleValidation(UserValidationException exception) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(new ErrorResponse(HttpStatus.CONFLICT.value(), exception.getMessage()));
  }

  @ExceptionHandler(ExternalServiceException.class)
  public ResponseEntity<ErrorResponse> handleExternalService(ExternalServiceException exception) {
    return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
        .body(new ErrorResponse(HttpStatus.BAD_GATEWAY.value(), exception.getMessage()));
  }
}
