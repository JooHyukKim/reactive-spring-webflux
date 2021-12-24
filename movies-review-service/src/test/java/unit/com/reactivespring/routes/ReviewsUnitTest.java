package com.reactivespring.routes;

import com.reactivespring.domain.Review;
import com.reactivespring.exceptionhandler.GlobalErrorHandler;
import com.reactivespring.handler.ReviewHandler;
import com.reactivespring.repository.ReviewReactiveRepository;
import com.reactivespring.router.ReviewRouter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@WebFluxTest
@ContextConfiguration(
    classes = {
      ReviewRouter.class,
      ReviewHandler.class,
      GlobalErrorHandler.class
    }) // can inject beans
@AutoConfigureWebTestClient
public class ReviewsUnitTest {
  @MockBean private ReviewReactiveRepository repository;

  @Autowired WebTestClient testClient;

  String REVIEWS_URL = "/v1/reviews";

  @Test
  void addReview() {
    // given
    var expected = new Review(null, 1L, "Awesome Movie", 9.0);
    // when
    when(repository.save(isA(Review.class)))
        .thenReturn(Mono.just(new Review("abc", 1L, "Awesome Movie", 9.0)));
    // then
    testClient
        .post()
        .uri(REVIEWS_URL)
        .bodyValue(expected)
        .exchange()
        .expectStatus()
        .isCreated()
        .expectBody(Review.class)
        .consumeWith(
            res -> {
              var actual = res.getResponseBody();
              assertNotNull(actual.getReviewId());
              assertEquals(expected.getComment(), actual.getComment());
              assertEquals(expected.getMovieInfoId(), actual.getMovieInfoId());
              assertEquals(expected.getRating(), actual.getRating());
            });
  }

  @Test
  void addReview_validation() {
    // given
    var expected = new Review("abc", null, "Awesome Movie", -1.0);
    var expectedErrorMessage =
        "rating must not be null,rating.negative : please pass a non-negative value";
    // when
    when(repository.save(isA(Review.class)))
        .thenReturn(Mono.just(new Review(null, 1L, "Awesome Movie", 9.0)));
    // then
    testClient
        .post()
        .uri(REVIEWS_URL)
        .bodyValue(expected)
        .exchange()
        .expectStatus()
        .isBadRequest()
        .expectBody(String.class)
        .isEqualTo(expectedErrorMessage);
  }

  @Test
  void getReviews() {
    // given
    var reviewsList =
        List.of(
            new Review(null, 1L, "Awesome Movie", 9.0),
            new Review(null, 1L, "Awesome Movie1", 9.0),
            new Review(null, 2L, "Excellent Movie", 8.0));
    // when
    when(repository.findAll()).thenReturn(Flux.fromIterable(reviewsList));
    // then
    testClient.get().uri(REVIEWS_URL).exchange().expectBodyList(Review.class).hasSize(3);
  }

  @Test
  void getReviewsByMovieInfoId() {
    // given
    var reviewsList =
        List.of(
            new Review(null, 1L, "Awesome Movie", 9.0),
            new Review(null, 1L, "Awesome Movie1", 9.0));
    var uri =
        UriComponentsBuilder.fromUriString(REVIEWS_URL)
            .queryParam("movieInfoId", 1L)
            .buildAndExpand()
            .toUri();
    // when
    when(repository.findReviewsByMovieInfoId(1L)).thenReturn(Flux.fromIterable(reviewsList));
    // then
    testClient.get().uri(uri).exchange().expectBodyList(Review.class).hasSize(2);
  }

  @Test
  void getReviewsByMovieInfoId_NOTFOUND() {
    // given
    var reviewsList =
        List.of(
            new Review(null, 1L, "Awesome Movie", 9.0),
            new Review(null, 1L, "Awesome Movie1", 9.0));
    var uri =
        UriComponentsBuilder.fromUriString(REVIEWS_URL)
            .queryParam("movieInfoId", 4L)
            .buildAndExpand()
            .toUri();
    // when
    when(repository.findReviewsByMovieInfoId(isA(Long.class))).thenReturn(Flux.empty());
    // then
    testClient.get().uri(uri).exchange().expectBodyList(Review.class).hasSize(0);
  }

  @Test
  void updateReview() {
    // given
    var reviewsList =
        List.of(
            new Review("a", 1L, "Awesome Movie", 9.0), new Review("b", 1L, "Awesome Movie1", 9.0));

    final var expected = reviewsList.get(0);
    final var updated = new Review("a", 1L, "Awesome Movies.com", 9.0);
    // when
    when(repository.findById(isA(String.class))).thenReturn(Mono.just(reviewsList.get(0)));
    when(repository.save(isA(Review.class))).thenReturn(Mono.just(expected));
    // then
    testClient
        .put()
        .uri(REVIEWS_URL + "/{id}", reviewsList.get(0).getReviewId())
        .bodyValue(new Review("a", 1L, "Awesome Movies.com", 9.0))
        .exchange()
        .expectStatus()
        .is2xxSuccessful()
        .expectBody(Review.class)
        .consumeWith(
            res -> {
              var actual = res.getResponseBody();
              assertEquals(expected, actual);
            });
  }

  @Test
  void updateReview_nosuchId() {
    // given
    // when
    when(repository.findById("a")).thenReturn(Mono.empty());
    when(repository.save(isA(Review.class))).thenReturn(Mono.empty());
    // then
    testClient
        .put()
        .uri(REVIEWS_URL + "/{id}", "a")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .isEmpty();
  }

  @Test
  void updateReview_() {
    // given
    var reviewsList =
        List.of(
            new Review("a", 1L, "Awesome Movie", 9.0), new Review("b", 1L, "Awesome Movie1", 9.0));
    final var actual = reviewsList.get(0);
    // when
    when(repository.findById("a")).thenReturn(Mono.just(reviewsList.get(0)));
    when(repository.save(isA(Review.class))).thenReturn(Mono.empty());
    // then
    testClient
        .put()
        .uri(REVIEWS_URL + "/{id}", reviewsList.get(0).getReviewId())
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .isEmpty();
  }

  @Test
  void deleteReview() {
    // given

    // when

    // then
  }

  @Test
  void updateReview_not_found() {
    // given
    final var expected = new Review("a", 1L, "Awesome Movie", 9.0);
    // when
    when(repository.findById(isA(String.class))).thenReturn(Mono.empty());
    when(repository.save(isA(Review.class))).thenReturn(Mono.just(expected));
    // then
    testClient
        .put()
        .uri(REVIEWS_URL + "/{id}", expected.getReviewId())
        .bodyValue(new Review("a", 1L, "Awesome Movies.com", 9.0))
        .exchange()
        .expectStatus()
        .isNotFound()
        .expectBody(String.class)
        .consumeWith(
            r -> {
              r.getResponseBody().contains("Review not found for the given Review id of");
            });

    verify(repository, times(1)).findById(any(String.class));
    verify(repository, times(0)).save(any());
  }
}
