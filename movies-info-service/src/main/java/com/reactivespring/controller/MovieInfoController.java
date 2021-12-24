package com.reactivespring.controller;

import com.reactivespring.domain.MovieInfo;
import com.reactivespring.service.MovieInfoSerivce;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import javax.validation.Valid;

@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
public class MovieInfoController {

  private final MovieInfoSerivce movieInfoSerivce;

  Sinks.Many<MovieInfo> movieInfoSink = Sinks.many().replay().latest();
  //  Sinks.Many<MovieInfo> movieInfoSink = Sinks.many().replay().all();

  @GetMapping(value = "/movieinfos/stream", produces = MediaType.APPLICATION_NDJSON_VALUE)
  public Flux<MovieInfo> getMovieInfoById() {
    return movieInfoSink.asFlux().log();
  }

  @PostMapping("/movieinfos")
  @ResponseStatus(HttpStatus.CREATED)
  public Mono<MovieInfo> addMovieInfo(@RequestBody @Valid MovieInfo movieInfo) {
    return movieInfoSerivce
        .addMovieInfo(movieInfo)
        .doOnNext(savedMovieInfo -> movieInfoSink.tryEmitNext(savedMovieInfo))
        .log();
  }

  @GetMapping("/movieinfos")
  public Flux<MovieInfo> getAllMovieInfos(
      @RequestParam(value = "year", required = false) Integer year) {
    if (year != null) {
      return movieInfoSerivce.getMovieInfoByYear(year).log();
    }
    return movieInfoSerivce.getAllMovieInfos().log();
  }

  @GetMapping("/movieinfos/{id}")
  public Mono<ResponseEntity<MovieInfo>> getMovieInfoById(@PathVariable String id) {
    return movieInfoSerivce
        .getMovieInfoById(id)
        .map(ResponseEntity.ok()::body)
        .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()))
        .log();
  }

  @PutMapping("/movieinfos/{id}")
  public Mono<ResponseEntity<MovieInfo>> updateMovieInfo(
      @RequestBody MovieInfo updateMovieInfo, @PathVariable String id) {
    return movieInfoSerivce
        .updateMovieInfo(updateMovieInfo, id)
        .map(ResponseEntity.ok()::body)
        .switchIfEmpty(Mono.just(ResponseEntity.noContent().build()))
        .log();
  }

  @DeleteMapping("/movieinfos/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public Mono<Void> deleteMovieInfo(@PathVariable String id) {
    return movieInfoSerivce.deleteMovieInfo(id).log();
  }
}
