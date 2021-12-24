package com.reactivespring.service;

import com.reactivespring.domain.MovieInfo;
import com.reactivespring.repository.MovieInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class MovieInfoSerivce {

  private final MovieInfoRepository movieInfoRepository;

  public Mono<MovieInfo> addMovieInfo(MovieInfo movieInfo) {
    return movieInfoRepository.save(movieInfo);
  }

  public Flux<MovieInfo> getAllMovieInfos() {
    return movieInfoRepository.findAll();
  }

  public Mono<MovieInfo> getMovieInfoById(String id) {
    return movieInfoRepository.findById(id);
  }

  public Mono<MovieInfo> updateMovieInfo(MovieInfo updateMovieInfo, String id) {
    return movieInfoRepository
        .findById(id)
        .flatMap(
            movieInfo -> {
              movieInfo.setCast(updateMovieInfo.getCast());
              movieInfo.setName(updateMovieInfo.getName());
              movieInfo.setRelease_date(updateMovieInfo.getRelease_date());
              movieInfo.setYear(updateMovieInfo.getYear());
              return movieInfoRepository.save(movieInfo);
            });
  }

  public Mono<Void> deleteMovieInfo(String id) {
    return movieInfoRepository.deleteById(id);
  }

  public Flux<MovieInfo> getMovieInfoByYear(Integer year) {
    return movieInfoRepository.findByYear(year);
  }
}
