package com.reactivespring.errorhandler;

import com.reactivespring.exception.MoviesInfoClientException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Slf4j
public class GlobalErrorHandler {
  @ExceptionHandler(MoviesInfoClientException.class)
  public ResponseEntity<String> handleClientException(MoviesInfoClientException exception) {
    log.error("Exception caught in handleClientException : {}", exception.getMessage());
    return ResponseEntity.status(exception.getStatusCode()).body(exception.getMessage());
  }
}
