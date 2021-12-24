package com.reactivespring.controller;

import com.reactivespring.client.MovieInfoRestClient;
import com.reactivespring.client.ReviewsRestClient;
import com.reactivespring.domain.Movie;
import com.reactivespring.domain.MovieInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/v1/movies")
@RequiredArgsConstructor
public class MoviesController {
  private final MovieInfoRestClient moviesClient;
  private final ReviewsRestClient reviewClient;

  @GetMapping("/{id}")
  public Mono<Movie> getMovieById(@PathVariable("id") String movieId) {
    return moviesClient
        .retrieveMovieInfo(movieId)
        .flatMap(
            movieInfo -> {
              var reviewlistMono = reviewClient.getReviews(movieId).collectList();
              return reviewlistMono.map(reviews -> new Movie(movieInfo, reviews));
            });
  }

  @GetMapping(value = "/stream", produces = MediaType.APPLICATION_NDJSON_VALUE)
  public Flux<MovieInfo> streamMovieInfos() {
    return moviesClient.retrieveMovieInfoStream();
  }
}
