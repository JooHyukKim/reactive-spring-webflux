package com.learnreactiveprogramming.service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.function.Function;

public class FluxAndMonoGeneratorService {
  public Flux<String> namesFlux() {
    return Flux.fromIterable(List.of("alex", "ben", "chloe")).log("namesFlux"); // db or a servicefd
  }

  public Mono<String> nameMono() {
    return Mono.just("주혁이 <3").log("namesMono");
  }

  public static void main(String[] args) {
    var service = new FluxAndMonoGeneratorService();

    service
        .namesFlux()
        .subscribe(
            name -> {
              System.out.println("Flux name is : " + name);
            });

    service
        .nameMono()
        .subscribe(
            name -> {
              System.out.println("Mono name is : " + name);
            });
  }

  public Flux<String> namesFluxMap(int stringLength) {
    return Flux.fromIterable(List.of("alex", "ben", "chloe"))
        .map(String::toUpperCase)
        //        .map(s -> s.toUpperCase())
        .filter(s -> s.length() > stringLength)
        .map(s -> s.length() + "-" + s)
        .log("namesFlux");
  }

  public Flux<String> namesFluxImmutability() {
    var namesFlux = Flux.fromIterable(List.of("alex", "ben", "chloe"));
    namesFlux.map(String::toUpperCase);
    return namesFlux;
  }

  public Flux<String> namesFluxFlatMap(int stringLength) {
    return Flux.fromIterable(List.of("alex", "ben", "chloe"))
        .map(s -> s.toUpperCase())
        .filter(s -> s.length() > stringLength)
        .flatMap(this::splitString)
        .log("namesFlux");
  }

  public Flux<String> namesFluxFlatMapAsync(int stringLength) {
    return Flux.fromIterable(List.of("alex", "ben", "chloe"))
        .map(s -> s.toUpperCase())
        .filter(s -> s.length() > stringLength)
        .flatMap(s -> splitStringWithDelay(s))
        .log("namesFlux");
  }

  public Flux<String> splitString(String name) {
    var charArray = name.split("");
    return Flux.fromArray(charArray);
  }

  public Flux<String> splitStringWithDelay(String name) {
    var charArray = name.split("");
    var delay = new Random().nextInt(1000);
    return Flux.fromArray(charArray).delayElements(Duration.ofMillis(delay)).log();
  }

  public Flux<String> namesFluxConcatMapAsync(int stringLength) {
    return Flux.fromIterable(List.of("alex", "ben", "chloe"))
        .map(s -> s.toUpperCase())
        .filter(s -> s.length() > stringLength)
        .concatMap(s -> splitStringWithDelay(s))
        .log("namesFlux");
  }

  public Mono<List<String>> namesMonoFlatMap(int stringlen) {
    return Mono.just("alex")
        .map(String::toUpperCase)
        .filter(s -> s.length() > stringlen)
        .flatMap(this::splitStringMono);
  }

  private Mono<List<String>> splitStringMono(String s) {
    var charList = List.of(s.split(""));
    return Mono.just(charList);
  }

  public Flux<String> namesMonoFlatMapMany(int stringlen) {
    return Mono.just("alex")
        .map(String::toUpperCase)
        .filter(s -> s.length() > stringlen)
        .flatMapMany(this::splitString)
        .log();
  }

  public Flux<String> namesFluxTransform(int stringLength) {

    Function<Flux<String>, Flux<String>> filterMap =
        name -> name.map(String::toUpperCase).filter(s -> s.length() > stringLength);

    return Flux.fromIterable(List.of("alex", "ben", "chloe"))
        .transform(filterMap)
        .map(s -> s.toUpperCase())
        .flatMap(this::splitString)
        .defaultIfEmpty("default")
        .log("namesFlux");
  }

  public Flux<String> namesFluxSwitchIfEmpty(int stringLength) {

    Function<Flux<String>, Flux<String>> filterMap =
        name ->
            name.map(String::toUpperCase)
                .filter(s -> s.length() > stringLength)
                .flatMap(this::splitString);

    var defaultFlux = Flux.just("default").transform(filterMap);

    return Flux.fromIterable(List.of("alex", "ben", "chloe"))
        .transform(filterMap)
        .switchIfEmpty(defaultFlux)
        .log("namesFlux");
  }

  public Flux<String> exploreConcat() {
    var abc = Flux.just("A", "B", "C");
    var def = Flux.just("D", "E", "F");

    return Flux.concat(abc, def).log();
  }

  public Flux<String> exploreConcatWith() {
    var a = Mono.just("A");
    var b = Mono.just("B");

    return a.concatWith(b).log();
  }

  public Flux<String> exploreMerge() {
    var abc = Flux.just("A", "B", "C").delayElements(Duration.ofMillis(100));
    var def = Flux.just("D", "E", "F").delayElements(Duration.ofMillis(125));

    return Flux.merge(abc, def).log("exploreMerge");
  }

  public Flux<String> exploreMergeWith() {
    var abc = Flux.just("A", "B", "C").delayElements(Duration.ofMillis(100));
    var def = Flux.just("D", "E", "F").delayElements(Duration.ofMillis(125));

    return abc.mergeWith(def).log("exploreMergeWith");
  }

  public Flux<String> exploreMergeWithMono() {
    var a = Mono.just("A");
    var b = Mono.just("B");

    return a.mergeWith(b).log("exploreMergeWithMono");
  }

  public Flux<String> exploreMergeSequential() {
    var abc = Flux.just("A", "B", "C").delayElements(Duration.ofMillis(100));
    var def = Flux.just("D", "E", "F").delayElements(Duration.ofMillis(125));

    return Flux.mergeSequential(abc, def).log();
  }

  public Flux<String> exploreZip() {
    var abc = Flux.just("A", "B", "C");
    var def = Flux.just("D", "E", "F");

    return Flux.zip(abc, def, (first, second) -> first.concat(second)).log();
  }

  public Flux<String> exploreZip1() {
    var abc = Flux.just("A", "B", "C");
    var def = Flux.just("D", "E", "F");
    var _123 = Flux.just("1", "2", "3");
    var _456 = Flux.just("4", "5", "6");

    return Flux.zip(abc, def, _123, _456)
        .map(t4 -> t4.getT1() + t4.getT2() + t4.getT3() + t4.getT4())
        .log();
  }

  public Flux<String> exploreZipWith() {
    var abc = Flux.just("A", "B", "C");
    var def = Flux.just("D", "E", "F");

    return abc.zipWith(def, (first, second) -> first + second).log();
  }

  public Mono<String> exploreZipWithMono() {
    var a = Mono.just("A");
    var b = Mono.just("B");

    return a.zipWith(b).map(t2 -> t2.getT1() + t2.getT2()).log("exploreZipWithMono");
  }
}
