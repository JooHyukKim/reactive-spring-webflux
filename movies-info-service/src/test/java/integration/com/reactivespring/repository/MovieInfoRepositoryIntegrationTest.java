package com.reactivespring.repository;

import com.reactivespring.domain.MovieInfo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataMongoTest // look for repository class, class available no need to  spin up whole context
@ActiveProfiles("test")
class MovieInfoRepositoryIntegrationTest {

  @Autowired MovieInfoRepository movieInfoRepository;

  @BeforeEach
  void setUp() {
    var movieinfos =
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
                2005,
                List.of("Christian Bale", "HeathLedger"),
                LocalDate.parse("2008-07-18")),
            new MovieInfo(
                "abc",
                "Dark Knight Rises",
                2012,
                List.of("Christian Bale", "Tom Hardy"),
                LocalDate.parse("2012-07-20")));

    movieInfoRepository.saveAll(movieinfos).blockLast();
  }

  @AfterEach
  void tearDown() {
    movieInfoRepository.deleteAll().block();
  }

  @Test
  void findAll() {
    // given

    // when
    var movieInfoFlux = movieInfoRepository.findAll().log();
    // then
    StepVerifier.create(movieInfoFlux).expectNextCount(3).verifyComplete();
  }

  @Test
  void findById() {
    // given
    var expected =
        new MovieInfo(
            "abc",
            "Dark Knight Rises",
            2012,
            List.of("Christian Bale", "Tom Hardy"),
            LocalDate.parse("2012-07-20"));
    // when
    var movieInfoMono = movieInfoRepository.findById("abc").log();
    // then
    StepVerifier.create(movieInfoMono)
        .assertNext(
            actual -> {
              assertEquals(expected, actual);
            })
        .verifyComplete();
  }

  @Test
  void saveMovieInfo() {
    // given
    var expected =
        new MovieInfo(
            null,
            "Dark Knight goes to bed",
            2012,
            List.of("Christian Bale", "Tom Hardy"),
            LocalDate.parse("2012-07-20"));
    // when
    var movieInfoMono = movieInfoRepository.save(expected).log();
    // then
    StepVerifier.create(movieInfoMono)
        .assertNext(
            actual -> {
              assertNotNull(actual.getMovieInfoId());
              assertEquals(expected.getName(), actual.getName());
            })
        .verifyComplete();
  }

  @Test
  void updateMovieInfo() {
    // given
    var movieInfo = movieInfoRepository.findById("abc").block();
    movieInfo.setYear(movieInfo.getYear() + 1000);
    // when
    var movieInfoMono = movieInfoRepository.save(movieInfo).log();
    // then
    StepVerifier.create(movieInfoMono)
        .assertNext(
            actual -> {
              assertEquals(movieInfo.getName(), actual.getName());
              assertEquals(3012, actual.getYear());
            })
        .verifyComplete();
  }

  @Test
  void deleteMovieInfo() {
    // given

    // when
    movieInfoRepository.deleteById("abc").block();
    var movieInfoFlux = movieInfoRepository.findAll().log();
    // then
    StepVerifier.create(movieInfoFlux).expectNextCount(2).verifyComplete();
  }

  @Test
  void findByYear() {
    // given

    // when
    var moviesInfoFlux = movieInfoRepository.findByYear(2005);
    // then
    StepVerifier.create(moviesInfoFlux).expectNextCount(2).verifyComplete();
  }

  @Test
  void findByName() {
    // given

    // when
    var moviesInfo = movieInfoRepository.findByName("Batman Begins");
    // then
    StepVerifier.create(moviesInfo)
        .expectNextMatches(info -> info.getName().equals("Batman Begins"));
  }
}
