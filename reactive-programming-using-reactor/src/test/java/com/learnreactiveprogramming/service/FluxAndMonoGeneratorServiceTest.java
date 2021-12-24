package com.learnreactiveprogramming.service;

import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.function.Consumer;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.in;
import static org.junit.jupiter.api.Assertions.*;

class FluxAndMonoGeneratorServiceTest {

  FluxAndMonoGeneratorService fluxAndMonoGeneratorService = new FluxAndMonoGeneratorService();

  @Test
  void namesFlux1() {
    // given

    // when
    var namesFlux = fluxAndMonoGeneratorService.namesFlux();
    // then
    StepVerifier.create(namesFlux).expectNext("alex", "ben", "chloe").verifyComplete();
  }

  @Test
  void namesFlux2() {
    // given

    // when
    var namesFlux = fluxAndMonoGeneratorService.namesFlux();
    // then
    StepVerifier.create(namesFlux).expectNextCount(3).verifyComplete();
  }

  @Test
  void fluxTest() {
    // given

    // when
    var flow =
        fluxAndMonoGeneratorService
            .namesFlux()
            .map(
                name -> {
                  if (name.equals("chloe")) throw new RuntimeException("errorMessage");
                  return name;
                });
    // then
    StepVerifier.create(flow)
        .expectNext("alex", "ben")
        .expectErrorSatisfies(this::assertHiHiRuntimeException)
        .verify();
  }

  private Consumer<Throwable> assertHiHiRuntimeException(Throwable error) {
    return e -> assertThat(e).isInstanceOf(RuntimeException.class).hasMessageContaining("hihi");
  }

  @Test
  void namesFlux3() {
    // given

    // when
    var namesFlux = fluxAndMonoGeneratorService.namesFlux();
    // then
    StepVerifier.create(namesFlux).expectNext("alex").expectNextCount(2).verifyComplete();
  }

  @Test
  void namesFluxMap() {
    // given
    int stringLen = 3;
    // when
    var namesFlux = fluxAndMonoGeneratorService.namesFluxMap(stringLen);
    // then
    StepVerifier.create(namesFlux).expectNext("4-ALEX", "5-CHLOE").verifyComplete();
  }

  @Test
  void namesFluxMap2() {
    // given
    int stringLen = 2;
    // when
    var namesFlux = fluxAndMonoGeneratorService.namesFluxMap(stringLen);
    // then
    StepVerifier.create(namesFlux).expectNext("4-ALEX", "3-BEN", "5-CHLOE").verifyComplete();
  }

  @Test
  void namesFluxImmutability() {
    // given

    // when
    var namesFlux = fluxAndMonoGeneratorService.namesFluxImmutability();
    // then
    StepVerifier.create(namesFlux).expectNextCount(3).verifyComplete();
  }

  @Test
  void namesFluxFlatMap() {
    // given
    int stringLen = 3;
    String[] expected = "ALEXCHLOE".split("");
    // when
    var namesFlux = fluxAndMonoGeneratorService.namesFluxFlatMap(stringLen);
    // then
    StepVerifier.create(namesFlux).expectNext(expected).verifyComplete();
  }

  @Test
  void namesFluxFlatMapAsync() {
    // given
    int stringLen = 3;
    List<String> expected = List.of("ALEXCHLOE".split(""));
    // when
    var namesFlux = fluxAndMonoGeneratorService.namesFluxFlatMapAsync(stringLen);
    // then
    StepVerifier.create(namesFlux)
        .expectNextCount(9)
        .thenConsumeWhile(
            c -> {
              assertThat(expected.contains(c));
              return true;
            })
        .verifyComplete();
  }

  @Test
  void namesFluxFlatMapAsync2() {
    // given
    int stringLen = 3;
    List<String> expected = List.of("ZZZZZZZZ".split(""));
    // when
    var namesFlux = fluxAndMonoGeneratorService.namesFluxFlatMapAsync(stringLen);
    // then
    StepVerifier.create(namesFlux)
        .thenConsumeWhile(
            c -> {
              assertThat(expected.contains(c)).isFalse();
              return true;
            })
        .verifyComplete();
  }

  @Test
  void namesFluxFlatMapAsync1() {
    // given
    int stringLen = 3;
    // when
    var namesFlux = fluxAndMonoGeneratorService.namesFluxFlatMapAsync(stringLen);
    // then
    StepVerifier.create(namesFlux).expectNextCount(9).verifyComplete();
  }

  @Test
  void namesFluxConcatMapAsync() {
    // given
    int stringLen = 3;
    String[] expected = "ALEXCHLOE".split("");
    // when
    var namesFlux = fluxAndMonoGeneratorService.namesFluxConcatMapAsync(stringLen);
    // then
    StepVerifier.create(namesFlux).expectNext(expected).verifyComplete();
  }

  @Test
  void namesFlux() {
    // given

    // when

    // then
  }

  @Test
  void nameMono() {
    // given

    // when

    // then
  }

  @Test
  void main() {
    // given

    // when

    // then
  }

  @Test
  void splitString() {
    // given

    // when

    // then
  }

  @Test
  void splitStringWithDelay() {
    // given

    // when

    // then
  }

  @Test
  void namesMonoFlatMap() {
    // given
    int stringlen = 3;
    var expected = List.of("ALEX".split(""));
    // when
    var value = fluxAndMonoGeneratorService.namesMonoFlatMap(stringlen);
    // then
    StepVerifier.create(value).expectNext(expected).verifyComplete();
  }

  @Test
  void namesMonoFlatMapMany() {
    // given
    int stringlen = 3;
    // when
    var value = fluxAndMonoGeneratorService.namesMonoFlatMapMany(stringlen);
    // then
    StepVerifier.create(value).expectNext("A", "L", "E", "X").verifyComplete();
  }

  @Test
  void namesFluxTransform() {
    // given
    int stringLen = 3;
    String[] expected = "ALEXCHLOE".split("");
    // when
    var namesFlux = fluxAndMonoGeneratorService.namesFluxTransform(stringLen).log("transform");
    // then
    StepVerifier.create(namesFlux).expectNext(expected).verifyComplete();
  }

  @Test
  void namesFluxTransform1() {
    // given
    int stringLen = 6;
    var expected = "default";
    // when
    var namesFlux = fluxAndMonoGeneratorService.namesFluxTransform(stringLen).log("transform");
    // then
    StepVerifier.create(namesFlux).expectNext(expected).verifyComplete();
  }

  @Test
  void namesFluxSwitchIfEmpty() {
    // given
    int stringLen = 6;
    var expected = "DEFAULT".split("");
    // when
    var namesFlux =
        fluxAndMonoGeneratorService.namesFluxSwitchIfEmpty(stringLen).log("switchIfEmpty");
    // then
    StepVerifier.create(namesFlux).expectNext(expected).verifyComplete();
  }

  @Test
  void exploreConcat() {
    // given

    // when
    var result = fluxAndMonoGeneratorService.exploreConcat();
    // then
    StepVerifier.create(result).expectNext("A", "B", "C", "D", "E", "F").verifyComplete();
  }

  @Test
  void exploreConcatWith() {
    // given

    // when
    var result = fluxAndMonoGeneratorService.exploreConcatWith();
    // then
    StepVerifier.create(result).expectNext("A", "B").verifyComplete();
  }

  @Test
  void exploreMerge() {
    // given
    var expected = "ADBECF".split("");
    // when
    var namesFlux = fluxAndMonoGeneratorService.exploreMerge();
    // then
    StepVerifier.create(namesFlux).expectNext(expected).verifyComplete();
  }

  @Test
  void exploreMergeWith() {
    // given
    var expected = "ADBECF".split("");
    // when
    var result = fluxAndMonoGeneratorService.exploreMergeWith().log("exploreMergeWith");
    // then
    StepVerifier.create(result).expectNext(expected).verifyComplete();
  }

  @Test
  void exploreMergeWithMono() {
    // given
    var expected = "AB".split("");
    // when
    var result = fluxAndMonoGeneratorService.exploreMergeWithMono().log("exploreMergeWithMono");
    // then
    StepVerifier.create(result).expectNext(expected).verifyComplete();
  }

  @Test
  void exploreMergeSequential() {
    // given
    var expected = "ABCDEF".split("");
    // when
    var result = fluxAndMonoGeneratorService.exploreMergeSequential().log("exploreMergeSequential");
    // then
    StepVerifier.create(result).expectNext(expected).verifyComplete();
  }

  @Test
  void exploreZip() {
    // given
    var expected = new String[] {"AD", "BE", "CF"};
    // when
    var result = fluxAndMonoGeneratorService.exploreZip();
    // then
    StepVerifier.create(result).expectNext(expected).verifyComplete();
  }

  @Test
  void exploreZip1() {
    // given
    var expected = new String[] {"AD14", "BE25", "CF36"};
    // when
    var result = fluxAndMonoGeneratorService.exploreZip1();
    // then
    StepVerifier.create(result).expectNext(expected).verifyComplete();
  }

  @Test
  void exploreZipWith() {
    // given

    // when

    // then
  }

  @Test
  void exploreZipWithMono() {
    // given

    // when

    // then
  }
}
