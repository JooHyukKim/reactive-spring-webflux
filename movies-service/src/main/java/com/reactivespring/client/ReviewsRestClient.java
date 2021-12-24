package com.reactivespring.client;

import com.reactivespring.domain.Review;
import com.reactivespring.exception.MoviesInfoClientException;
import com.reactivespring.exception.MoviesInfoServerException;
import com.reactivespring.exception.ReviewsClientException;
import com.reactivespring.exception.ReviewsServerException;
import com.reactivespring.util.RetryUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReviewsRestClient {
  private final WebClient webClient;

  @Value("${restClient.reviewsInfoUrl}")
  private String reviewsUrl;

  public Flux<Review> getReviews(String movieId) {
    var uri =
        UriComponentsBuilder.fromHttpUrl(reviewsUrl)
            .queryParam("movieInfoId", movieId)
            .buildAndExpand()
            .toUri();
    return webClient
        .get()
        .uri(uri)
        .retrieve()
        .onStatus(
            HttpStatus::is4xxClientError,
            clientResponse -> {
              log.info("status code 4xx is : {}", clientResponse.statusCode().value());
              if (clientResponse.statusCode().equals(HttpStatus.NOT_FOUND)) {
                return Mono.empty();
              }
              return clientResponse
                  .bodyToMono(String.class)
                  .flatMap(msg -> Mono.error(new ReviewsClientException(msg)));
            })
        .onStatus(
            HttpStatus::is5xxServerError,
            clientResponse -> {
              log.info("status code 5xx is : {}", clientResponse.statusCode().value());
              return clientResponse
                  .bodyToMono(String.class)
                  .flatMap(
                      msg ->
                          Mono.error(
                              new ReviewsServerException(
                                  msg.concat(" Review Service Not Available"))));
            })
        .bodyToFlux(Review.class)
        .retryWhen(RetryUtil.retrySpec());
  }
}
