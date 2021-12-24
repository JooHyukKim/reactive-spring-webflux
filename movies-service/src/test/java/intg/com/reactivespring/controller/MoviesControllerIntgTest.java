package com.reactivespring.controller;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.reactivespring.domain.Movie;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Objects;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureWebTestClient
@AutoConfigureWireMock(port = 8084)
@TestPropertySource(
    properties = {
      "restClient.moviesInfoUrl=http://localhost:8084/v1/movieinfos",
      "restClient.reviewsInfoUrl=http://localhost:8084/v1/reviews"
    })
public class MoviesControllerIntgTest {
  @Autowired WebTestClient testClient;

  @Test
  void getMovieById() {
    // given
    var movieId = "abc";
    stubFor(
        get(urlEqualTo("/v1/movieinfos/" + movieId))
            .willReturn(
                aResponse()
                    .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .withBodyFile(
                        "movieinfo.json"))); // goto resources directory and file __files directory
    stubFor(
        get(urlPathEqualTo("/v1/reviews"))
            .willReturn(
                aResponse()
                    .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .withBodyFile(
                        "reviews.json"))); // goto resources directory and file __files directory
    // when
    testClient
        .get()
        .uri("/v1/movies/{id}", movieId)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(Movie.class)
        .consumeWith(
            exResult -> {
              var movie = exResult.getResponseBody();
              Assertions.assertEquals(2, Objects.requireNonNull(movie).getReviewList().size());
              Assertions.assertEquals("Batman Begins", movie.getMovieInfo().getName());
            });
    // then
  }

  @Test
  void getMovieById_moviesInfo_404() {
    // given
    var movieId = "abc";
    stubFor(
        get(urlEqualTo("/v1/movieinfos/" + movieId))
            .willReturn(
                aResponse()
                    .withStatus(404))); // goto resources directory and file __files directory
    stubFor(
        get(urlPathEqualTo("/v1/reviews"))
            .willReturn(
                aResponse()
                    .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .withBodyFile(
                        "reviews.json"))); // goto resources directory and file __files directory
    // when
    testClient
        .get()
        .uri("/v1/movies/{id}", movieId)
        .exchange()
        .expectStatus()
        .isNotFound()
        .expectBody(String.class)
        .consumeWith(r -> r.getResponseBody().contains("There is no movieinfo"));
    // then
    WireMock.verify(1, getRequestedFor(urlEqualTo("/v1/movieinfos/" + movieId)));
  }

  @Test
  void getMovieById_reviews_404() {
    // given
    var movieId = "abc";
    stubFor(
        get(urlEqualTo("/v1/movieinfos/" + movieId))
            .willReturn(
                aResponse()
                    .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .withBodyFile(
                        "movieinfo.json"))); // goto resources directory and file __files directory
    stubFor(
        get(urlPathEqualTo("/v1/reviews"))
            .willReturn(
                aResponse()
                    .withStatus(404))); // goto resources directory and file __files directory
    // when
    testClient
        .get()
        .uri("/v1/movies/{id}", movieId)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(Movie.class)
        .consumeWith(
            res -> {
              var movie = Objects.requireNonNull(res.getResponseBody());
              Assertions.assertEquals(0, movie.getReviewList().size());
              Assertions.assertEquals("Batman Begins", movie.getMovieInfo().getName());
            });
    // then
  }

  @Test
  void getMovieById_moviesInfo_5xx() {
    // given
    var movieId = "abc";
    stubFor(
        get(urlEqualTo("/v1/movieinfos/" + movieId))
            .willReturn(aResponse().withStatus(500).withBody("MovieInfo service unavailable")));
    // when
    testClient
        .get()
        .uri("/v1/movies/{id}", movieId)
        .exchange()
        .expectStatus()
        .is5xxServerError()
        .expectBody(String.class)
        .consumeWith(r -> r.getResponseBody().contains("Unavailable"));
    // then
  }

  @Test
  void getMovieById_retry() {
    // given
    var movieId = "abc";
    stubFor(
        get(urlEqualTo("/v1/movieinfos/" + movieId))
            .willReturn(aResponse().withStatus(500).withBody("MovieInfo service unavailable")));
    // when
    testClient
        .get()
        .uri("/v1/movies/{id}", movieId)
        .exchange()
        .expectStatus()
        .is5xxServerError()
        .expectBody(String.class)
        .consumeWith(r -> r.getResponseBody().contains("Unavailable"));
    // then
    WireMock.verify(4, getRequestedFor(urlEqualTo("/v1/movieinfos/" + movieId)));
  }

  @Test
  void getMovieById_retry_reviews() {
    // given
    var movieId = "abc";
    stubFor(
        get(urlEqualTo("/v1/movieinfos/" + movieId))
            .willReturn(
                aResponse()
                    .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .withBodyFile(
                        "movieinfo.json"))); // goto resources directory and file __files directory
    stubFor(
        get(urlPathEqualTo("/v1/reviews"))
            .willReturn(
                aResponse()
                    .withStatus(500)
                    .withBody(
                        "Review Service Not Available"))); // goto resources directory and file
    // __files directory
    // when
    testClient
        .get()
        .uri("/v1/movies/{id}", movieId)
        .exchange()
        .expectStatus()
        .is5xxServerError()
        .expectBody(String.class)
        .consumeWith(r -> r.getResponseBody().contains("Review Service Not Available"));
    // then
    WireMock.verify(4, getRequestedFor(urlPathMatching("/v1/reviews*")));
  }
}
