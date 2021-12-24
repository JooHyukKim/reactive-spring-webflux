package com.reactivespring.controller;

import com.reactivespring.domain.MovieInfo;
import com.reactivespring.service.MovieInfoSerivce;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.*;

@WebFluxTest(MovieInfoController.class)
@AutoConfigureWebTestClient
public class MoviesInfoControllerUnitTest {
  @Autowired private WebTestClient webTestClient;

  @MockBean private MovieInfoSerivce movieInfoSerivce;

  static String MOVIES_INFO_URL = "/v1/movieinfos";
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

  @Test
  void getAllMoviesINfo() {
    // given

    // when
    when(movieInfoSerivce.getAllMovieInfos()).thenReturn(Flux.fromIterable(movieinfos));
    // then
    webTestClient
        .get()
        .uri(MOVIES_INFO_URL)
        .exchange()
        .expectStatus()
        .is2xxSuccessful()
        .expectBodyList(MovieInfo.class)
        .hasSize(3)
        .consumeWith(
            rest -> {
              var actual = rest.getResponseBody();
              actual.get(0).equals(movieinfos.get(0));
            });
  }

  @Test
  void getMovieInfoById() {
    // given
    var presetMovieId = "abc";
    // when
    when(movieInfoSerivce.getMovieInfoById(presetMovieId)).thenReturn(Mono.just(movieinfos.get(2)));
    // then
    webTestClient
        .get()
        .uri(MOVIES_INFO_URL + "/{id}", presetMovieId)
        .exchange()
        .expectStatus()
        .is2xxSuccessful()
        .expectBody(MovieInfo.class)
        .isEqualTo(movieinfos.get(2));
  }

  @Test
  void getMovieInfoById2() {
    // given
    var presetMovieId = "22222fdafea";
    // when
    when(movieInfoSerivce.getMovieInfoById(presetMovieId)).thenReturn(Mono.empty());
    // then
    webTestClient
        .get()
        .uri(MOVIES_INFO_URL + "/{id}", presetMovieId)
        .exchange()
        .expectBody()
        .isEmpty();
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
    var savedInfo =
        new MovieInfo(
            "mockId",
            "The Dark Knight goes Home",
            2008,
            List.of("Christian Bale", "HeathLedger"),
            LocalDate.now());
    final String MOVIES_INFO_URL = "/v1/movieinfos";
    // when
    when(movieInfoSerivce.addMovieInfo(isA(MovieInfo.class))).thenReturn(Mono.just(savedInfo));
    // then
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
              Assertions.assertEquals("mockId", savedInfo.getMovieInfoId());
            });
  }

  @Test
  void updateMovieInfo() {
    // given
    var movieInfo =
        new MovieInfo(
            "abc",
            "The Dark Knight goes to the Moon",
            2108,
            List.of("JooHyuk Bale", "HeathLedger", "Ma Dong Suck"),
            LocalDate.now());
    final String MOVIES_INFO_URL = "/v1/movieinfos";
    // when
    when(movieInfoSerivce.updateMovieInfo(isA(MovieInfo.class), isA(String.class)))
        .thenReturn(
            Mono.just(
                new MovieInfo(
                    "abc",
                    "The Dark Knight goes to the Moon",
                    2108,
                    List.of("JooHyuk Bale", "HeathLedger", "Ma Dong Suck"),
                    LocalDate.now())));
    // then
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

    verify(movieInfoSerivce, times(1)).updateMovieInfo(any(), any());
  }

  @Test
  void deleteMovieInfo() {
    // given
    var movieId = "abc";
    // when
    when(movieInfoSerivce.deleteMovieInfo(isA(String.class))).thenReturn(Mono.empty());
    // then
    webTestClient
        .delete()
        .uri(MOVIES_INFO_URL + "/{id}", movieId)
        .exchange()
        .expectStatus()
        .is2xxSuccessful()
        .expectBody()
        .isEmpty();
  }

  @Test
  void addMovieInfo2() {
    // given
    var movieInfo =
        new MovieInfo(
            "abc",
            "",
            -2005,
            //                    List.of("JooHyuk Bale", "HeathLedger", "Ma Dong Suck"),
            List.of(""),
            LocalDate.now());

    // when
    when(movieInfoSerivce.updateMovieInfo(isA(MovieInfo.class), isA(String.class)))
        .thenReturn(
            Mono.just(
                new MovieInfo(
                    "abc",
                    "The Dark Knight goes to the Moon",
                    2108,
                    //                    List.of("JooHyuk Bale", "HeathLedger", "Ma Dong Suck"),
                    List.of(""),
                    LocalDate.now())));
    // then
    webTestClient
        .post()
        .uri(MOVIES_INFO_URL)
        .bodyValue(movieInfo)
        .exchange()
        .expectStatus()
        .isNotFound()
        .expectBody(String.class)
        .consumeWith(
            res -> {
              var responseBody = res.getResponseBody();
              assertTrue(responseBody.contains(".name must"));
              assertTrue(responseBody.contains(".year must"));
              assertTrue(responseBody.contains(".cast must"));
            });
    verify(movieInfoSerivce, times(0)).updateMovieInfo(any(), any());
  }
}
