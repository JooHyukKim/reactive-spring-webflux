package com.reactivespring.routes;

import com.reactivespring.domain.Review;
import com.reactivespring.repository.ReviewReactiveRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.test.StepVerifier;

import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureWebTestClient
public class ReviewsIntgTest {
  @Autowired WebTestClient webTestClient;
  @Autowired ReviewReactiveRepository reviewRepository;

  @BeforeEach
  void setUp() {

    var reviewsList =
        List.of(
            new Review(null, 1L, "Awesome Movie", 9.0),
            new Review(null, 1L, "Awesome Movie1", 9.0),
            new Review(null, 2L, "Excellent Movie", 8.0));
    var savedReviews = reviewRepository.saveAll(reviewsList).blockLast();
  }

  @AfterEach
  void tearDown() {
    reviewRepository.deleteAll().block();
  }

  String REVIEWS_URL = "/v1/reviews";

  @Test
  void addReview() {
    // given
    var expected = new Review(null, 1L, "Awesome Movie", 9.0);
    // when

    // then
    webTestClient
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
              Assertions.assertEquals(expected.getComment(), actual.getComment());
              Assertions.assertEquals(expected.getMovieInfoId(), actual.getMovieInfoId());
              Assertions.assertEquals(expected.getRating(), actual.getRating());
            });
  }

  @Test
  void getAllReviews() {
    // given

    // when

    // then
    webTestClient
        .get()
        .uri(REVIEWS_URL)
        .exchange()
        .expectStatus()
        .is2xxSuccessful()
        .expectBodyList(Review.class)
        .hasSize(3);
  }

  @Test
  void updateReview() {
    // given
    var expected = new Review(null, 3L, "New moview to be updated", 9.9);
    var reviewId = reviewRepository.findAll().blockLast().getReviewId();
    // when

    // then
    webTestClient
        .put()
        .uri(REVIEWS_URL + "/{id}", reviewId)
        .bodyValue(expected)
        .exchange()
        .expectStatus()
        .isCreated()
        .expectBody(Review.class)
        .consumeWith(
            res -> {
              var actual = res.getResponseBody();
              Assertions.assertEquals(expected.getComment(), actual.getComment());
              Assertions.assertEquals(expected.getMovieInfoId(), actual.getMovieInfoId());
              Assertions.assertEquals(expected.getRating(), actual.getRating());
            });
  }

  @Test
  void deleteReview() {
    var reviewId = reviewRepository.findAll().blockLast().getReviewId();
    StepVerifier.create(reviewRepository.findAll()).expectNextCount(3);

    // then
    webTestClient
        .delete()
        .uri(REVIEWS_URL + "/{id}", reviewId)
        .exchange()
        .expectStatus()
        .isNoContent()
        .expectBody()
        .consumeWith(
            entityExchangeResult -> {
              StepVerifier.create(reviewRepository.findAll()).expectNextCount(2);
            });
  }

  @Test
  void getAllReviewsByMovieInfoId() {
    // given
    var uri =
        UriComponentsBuilder.fromUriString(REVIEWS_URL)
            .queryParam("movieInfoId", 1L)
            .buildAndExpand()
            .toUri();
    // when

    // then
    webTestClient.get().uri(uri).exchange().expectBodyList(Review.class).hasSize(2);
  }

  @Test
  void getAllReviewsByMovieInfoId2() {
    // given
    var uri =
        UriComponentsBuilder.fromUriString(REVIEWS_URL)
            .queryParam("movieInfoId", 2L)
            .buildAndExpand()
            .toUri();
    // when

    // then
    webTestClient.get().uri(uri).exchange().expectBodyList(Review.class).hasSize(1);
  }
}
