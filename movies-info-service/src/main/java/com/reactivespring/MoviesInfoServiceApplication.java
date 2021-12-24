package com.reactivespring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.core.publisher.Hooks;

@SpringBootApplication
public class MoviesInfoServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(MoviesInfoServiceApplication.class, args);
  }
}
