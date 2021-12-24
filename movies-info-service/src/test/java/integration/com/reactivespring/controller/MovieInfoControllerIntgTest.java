package com.reactivespring.controller;

import com.reactivespring.domain.MovieInfo;
import com.reactivespring.repository.MovieInfoRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureWebTestClient
class MovieInfoControllerIntgTest {

  @Autowired WebTestClient webTestClient;

  @Autowired MovieInfoRepository movieInfoRepository;

  final String MOVIES_INFO_URL = "/v1/movieinfos";
  List<MovieInfo> movieinfos =
      List.of(
          new MovieInfo(
              null,
              "Batman Begins",
              2005,
              List.of("Christian Bale", "Michael Cane"),
              LocalDate.parse("2005-06-15")),
          new MovieInfo(
              null,
              "The Dark Knight",
              2008,
              List.of("Christian Bale", "HeathLedger"),
              LocalDate.parse("2008-07-18")),
          new MovieInfo(
              "abc",
              "Dark Knight Rises",
              2012,
              List.of("Christian Bale", "Tom Hardy"),
              LocalDate.parse("2012-07-20")));

  @BeforeEach
  void setUp() {

    movieInfoRepository.deleteAll().thenMany(movieInfoRepository.saveAll(movieinfos)).blockLast();
  }

  @Test
  void addMovieInfo() {
    // given
    var movieInfo =
        new MovieInfo(
            null,
            "The Dark Knight goes Home",
            2008,
            List.of("Christian Bale", "HeathLedger"),
            LocalDate.now());
    final String MOVIES_INFO_URL = "/v1/movieinfos";
    // when

    webTestClient
        .post()
        .uri(MOVIES_INFO_URL)
        .bodyValue(movieInfo)
        .exchange()
        .expectStatus()
        .is2xxSuccessful()
        .expectBody(MovieInfo.class)
        .consumeWith(
            movieInfoEntityExchangeResult -> {
              var actual = movieInfoEntityExchangeResult.getResponseBody();
              assert actual != null;
              assert actual.getName().equals(movieInfo.getName());
            });

    // then
  }

  @Test
  void getAllMovieInfos() {
    // given

    // when
    webTestClient
        .get()
        .uri(MOVIES_INFO_URL)
        .exchange()
        .expectStatus()
        .is2xxSuccessful()
        .expectBodyList(MovieInfo.class)
        .hasSize(3);

    // then
  }

  @Test
  void getMovieInfoById() {
    // given
    var movieInfoId = "abc";
    // when
    webTestClient
        .get()
        .uri(MOVIES_INFO_URL + "/{id}", movieInfoId)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(MovieInfo.class)
        .consumeWith(
            movieInfoEntityExchangeResult -> {
              var movieInfo = movieInfoEntityExchangeResult.getResponseBody();
              assertNotNull(movieInfo);
              assertEquals(movieInfoId, movieInfo.getMovieInfoId());
            });
    // then
  }

  @Test
  void getMovieInfoById2() {
    // given
    var movieInfoId = "abc";
    // when
    webTestClient
        .get()
        .uri(MOVIES_INFO_URL + "/{id}", movieInfoId)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.name")
        .isEqualTo("Dark Knight Rises");
  }

  @Test
  void updateMovieInfo() {
    // given
    var movieInfo =
        new MovieInfo(
            "abc",
            "The Dark Knight goes Home 2",
            2008,
            List.of("Christian Bale", "HeathLedger", "Ma Dong Suck"),
            LocalDate.now());
    final String MOVIES_INFO_URL = "/v1/movieinfos";
    // when

    webTestClient
        .put()
        .uri(MOVIES_INFO_URL + "/{id}", movieInfo.getMovieInfoId())
        .bodyValue(movieInfo)
        .exchange()
        .expectStatus()
        .is2xxSuccessful()
        .expectBody(MovieInfo.class)
        .consumeWith(
            movieInfoEntityExchangeResult -> {
              var actual = movieInfoEntityExchangeResult.getResponseBody();
              assert actual != null;
              assert actual.getName().equals(movieInfo.getName());
              assertEquals(movieInfo, actual);
            });
    // then
  }

  @Test
  void deleteMovieInfo() {
    // given
    var movieId = "abc";
    // when
    webTestClient
        .delete()
        .uri(MOVIES_INFO_URL + "/{id}", movieId)
        .exchange()
        .expectStatus()
        .isNoContent();
    // then
    var allMovies = movieInfoRepository.findAll();
    StepVerifier.create(allMovies).expectNextCount(2).verifyComplete();
  }

  @Test
  void updateMovieInfoNotFOund() {
    // given
    var movieInfo =
        new MovieInfo(
            "def",
            "The Dark Knight goes to the Moon",
            2108,
            List.of("JooHyuk Bale", "HeathLedger", "Ma Dong Suck"),
            LocalDate.now());
    final String MOVIES_INFO_URL = "/v1/movieinfos";

    // then
    webTestClient
        .put()
        .uri(MOVIES_INFO_URL + "/{id}", movieInfo.getMovieInfoId())
        .bodyValue(movieInfo)
        .exchange()
        .expectStatus()
        .isNoContent();
  }

  @Test
  void getMovieInfoById_NOTEXIST() {
    // given
    var movieInfoId = "-1";
    // when
    webTestClient
        .get()
        .uri(MOVIES_INFO_URL + "/{id}", movieInfoId)
        .exchange()
        .expectStatus()
        .isNoContent();
    // then
  }

  @Test
  void getMovieInfoByYear() {
    // given
    var uri =
        UriComponentsBuilder.fromUriString(MOVIES_INFO_URL)
            .queryParam("year", 2005)
            .buildAndExpand()
            .toUri();
    // when
    // then
    webTestClient
        .get()
        .uri(uri)
        .exchange()
        .expectStatus()
        .is2xxSuccessful()
        .expectBodyList(MovieInfo.class)
        .hasSize(1);
  }

  @Test
  void getMovieInfoByName() {
    // given
    var uri =
        UriComponentsBuilder.fromUriString(MOVIES_INFO_URL)
            .queryParam("name", "Batman Begins")
            .buildAndExpand()
            .toUri();
    // when

    // then
    webTestClient
        .get()
        .uri(uri)
        .exchange()
        .expectStatus()
        .is2xxSuccessful()
        .expectBodyList(MovieInfo.class)
        .hasSize(1)
        .consumeWith(
            result -> {
              var body = result.getResponseBody();
              var expected = movieinfos.get(0);
              var actual = body.get(0);
              assertEquals(expected.getName(), actual.getName());
              assertEquals(expected.getYear(), actual.getYear());
              assertEquals(expected.getRelease_date(), actual.getRelease_date());
              assertEquals(expected.getCast(), actual.getCast());
            });
  }

  @Test
  void getAllMovie_stream() {
    // given
    final String MOVIES_INFO_URL = "/v1/movieinfos";
    var expected =
        new MovieInfo(
            null,
            "The Dark Knight goes Home",
            2008,
            List.of("Christian Bale", "HeathLedger"),
            LocalDate.now());
    String newMovieTitle = "The Dark Knight rose again for american breakfast!";

    webTestClient
        .post()
        .uri(MOVIES_INFO_URL)
        .bodyValue(expected)
        .exchange()
        .expectStatus()
        .is2xxSuccessful()
        .expectBody(MovieInfo.class)
        .consumeWith(
            movieInfoEntityExchangeResult -> {
              var actual = movieInfoEntityExchangeResult.getResponseBody();
              assert actual != null;
              assert actual.getName().equals(expected.getName());
            });

    // when
    var stream =
        webTestClient
            .get()
            .uri(MOVIES_INFO_URL.concat("/stream"))
            .exchange()
            .expectStatus()
            .is2xxSuccessful()
            .returnResult(MovieInfo.class)
            .getResponseBody();
    // then
    StepVerifier.create(stream)
        .assertNext(
            actual -> {
              Assertions.assertNotNull(actual.getMovieInfoId());
              Assertions.assertEquals(expected.getName(), actual.getName());
            })
        .thenCancel()
        .verify();
  }
}
