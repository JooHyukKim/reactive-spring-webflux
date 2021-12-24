package com.reactivespring.client;

import com.reactivespring.domain.MovieInfo;
import com.reactivespring.exception.MoviesInfoClientException;
import com.reactivespring.exception.MoviesInfoServerException;
import com.reactivespring.util.RetryUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

@Component
@RequiredArgsConstructor
@Slf4j
public class MovieInfoRestClient {
  private final WebClient webClient;

  @Value("${restClient.moviesInfoUrl}")
  private String movieInfoUrl;

  public Mono<MovieInfo> retrieveMovieInfo(String movieId) {

    var url = movieInfoUrl.concat("/{id}");
    return webClient
        .get()
        .uri(url, movieId)
        .retrieve()
        .onStatus(
            HttpStatus::is4xxClientError,
            clientResponse -> {
              log.info("status code is : {}", clientResponse.statusCode().value());
              if (clientResponse.statusCode().equals(HttpStatus.NOT_FOUND)) {
                return Mono.error(
                    new MoviesInfoClientException(
                        "There is no movieinfo with id".concat(movieId),
                        clientResponse.statusCode().value()));
              }
              return clientResponse
                  .bodyToMono(String.class)
                  .flatMap(
                      msg ->
                          Mono.error(
                              new MoviesInfoClientException(
                                  msg, clientResponse.statusCode().value())));
            })
        .onStatus(
            HttpStatus::is4xxClientError,
            clientResponse -> {
              log.info("status code is : {}", clientResponse.statusCode().value());

              return clientResponse
                  .bodyToMono(String.class)
                  .flatMap(
                      msg ->
                          Mono.error(
                              new MoviesInfoServerException(
                                  "ServerException in Movies info : " + msg)));
            })
        .bodyToMono(MovieInfo.class)
        //        .retry(3)
        .retryWhen(RetryUtil.retrySpec())
        .log();
  }

  public Flux<MovieInfo> retrieveMovieInfoStream() {
    var url = movieInfoUrl.concat("/stream");
    return webClient
        .get()
        .uri(url)
        .retrieve()
        .onStatus(
            HttpStatus::is4xxClientError,
            clientResponse -> {
              log.info("status code is : {}", clientResponse.statusCode().value());
              return clientResponse
                  .bodyToMono(String.class)
                  .flatMap(
                      msg ->
                          Mono.error(
                              new MoviesInfoClientException(
                                  msg, clientResponse.statusCode().value())));
            })
        .onStatus(
            HttpStatus::is4xxClientError,
            clientResponse -> {
              log.info("status code is : {}", clientResponse.statusCode().value());

              return clientResponse
                  .bodyToMono(String.class)
                  .flatMap(
                      msg ->
                          Mono.error(
                              new MoviesInfoServerException(
                                  "ServerException in Movies info : " + msg)));
            })
        .bodyToFlux(MovieInfo.class)
        //        .retry(3)
        .retryWhen(RetryUtil.retrySpec())
        .log();
  }
}
