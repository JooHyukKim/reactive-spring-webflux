package com.reactivespring.handler;

import com.mongodb.internal.connection.Server;
import com.reactivespring.domain.Review;
import com.reactivespring.exception.ReviewDataException;
import com.reactivespring.exception.ReviewNotFoundException;
import com.reactivespring.repository.ReviewReactiveRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReviewHandler {
  private final ReviewReactiveRepository repository;
  private final Validator validator;

  Sinks.Many<Review> reviewsInfoSink = Sinks.many().replay().latest();

  public Mono<ServerResponse> addReview(ServerRequest request) {
    return request
        .bodyToMono(Review.class)
        .doOnNext(this::validate)
        .flatMap(repository::save)
        .doOnNext(reviewsInfoSink::tryEmitNext)
        .flatMap(ServerResponse.status(HttpStatus.CREATED)::bodyValue);
  }

  private void validate(Review review) {
    var violations = validator.validate(review);
    log.info("constraint violations : {}", violations);
    if (violations.size() > 0) {
      var errorMessage =
          violations.stream()
              .map(ConstraintViolation::getMessage)
              .sorted()
              .collect(Collectors.joining(","));
      throw new ReviewDataException(errorMessage);
    }
  }

  public Mono<ServerResponse> getReviews(ServerRequest request) {
    var movieInfoId = request.queryParam("movieInfoId");
    if (movieInfoId.isPresent()) {
      var reviews = repository.findReviewsByMovieInfoId(Long.valueOf(movieInfoId.get())).log();
      return buildReviewsResponse(reviews);
    } else {
      var reviews = repository.findAll().log();
      return buildReviewsResponse(reviews);
    }
  }

  private Mono<ServerResponse> buildReviewsResponse(Flux<Review> reviews) {
    return ServerResponse.ok().body(reviews, Review.class);
  }

  public Mono<ServerResponse> updateReview(ServerRequest request) {
    var reviewid = request.pathVariable("id");
    var existingReview = repository.findById(reviewid);
    var errorMessage = "Review not found for the given Review id of".concat(reviewid);
    //            .switchIfEmpty(
    //                Mono.error(
    //                    new ReviewNotFoundException(
    //                        )));

    return existingReview
        .flatMap(
            review ->
                request
                    .bodyToMono(Review.class)
                    .map(
                        reqReview -> {
                          review.setComment(reqReview.getComment());
                          review.setRating(reqReview.getRating());
                          return review;
                        }))
        .flatMap(repository::save)
        .flatMap(ServerResponse.ok()::bodyValue)
        .switchIfEmpty(ServerResponse.status(HttpStatus.NOT_FOUND).bodyValue(errorMessage))
        .log();
  }

  public Mono<ServerResponse> deleteReview(ServerRequest request) {
    var reviewid = request.pathVariable("id");
    var existingReview = repository.findById(reviewid);

    return existingReview
        .flatMap(review -> repository.deleteById(reviewid))
        .then(ServerResponse.noContent().build());
  }

  public Mono<ServerResponse> getReviewsStream(ServerRequest request) {
    return ServerResponse.ok()
        .contentType(MediaType.APPLICATION_NDJSON)
        .body(reviewsInfoSink.asFlux(), Review.class)
        .log();
  }
}
