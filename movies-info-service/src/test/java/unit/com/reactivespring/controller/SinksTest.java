package com.reactivespring.controller;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Sinks;
import reactor.test.StepVerifier;

public class SinksTest {
  @Test
  void sink() {
    // given
    Sinks.Many<Integer> replaySink = Sinks.many().replay().all();
    // when
    replaySink.emitNext(1, Sinks.EmitFailureHandler.FAIL_FAST);
    replaySink.emitNext(2, Sinks.EmitFailureHandler.FAIL_FAST);
    replaySink.emitNext(3, Sinks.EmitFailureHandler.FAIL_FAST);
    // then
    var f = replaySink.asFlux();
    f.subscribe(
        (integer) -> {
          System.out.println("subscriber 1 : " + integer);
        });
    var f1 = replaySink.asFlux();
    f1.subscribe(
        (integer) -> {
          System.out.println("subscriber 2: " + integer);
        });

    replaySink.tryEmitNext(4);

    var f3 = replaySink.asFlux();
    f1.subscribe(
        (integer) -> {
          System.out.println("subscriber 3: " + integer);
        });
  }

  @Test
  void sink_multicast() {
    // given
    Sinks.Many<Integer> replaySink = Sinks.many().multicast().onBackpressureBuffer();
    // when
    replaySink.emitNext(1, Sinks.EmitFailureHandler.FAIL_FAST);
    replaySink.emitNext(2, Sinks.EmitFailureHandler.FAIL_FAST);
    replaySink.emitNext(3, Sinks.EmitFailureHandler.FAIL_FAST);
    // then
    var f = replaySink.asFlux();
    f.subscribe(
        (integer) -> {
          System.out.println("subscriber 1 : " + integer);
        });
    var f1 = replaySink.asFlux();
    f1.subscribe(
        (integer) -> {
          System.out.println("subscriber 2: " + integer);
        });

    replaySink.tryEmitNext(4);

    var f3 = replaySink.asFlux();
    f1.subscribe(
        (integer) -> {
          System.out.println("subscriber 3: " + integer);
        });
  }

  @Test
  void sink_unicast() {
    // given
    Sinks.Many<Integer> replaySink = Sinks.many().unicast().onBackpressureBuffer();
    // when
    replaySink.emitNext(1, Sinks.EmitFailureHandler.FAIL_FAST);
    replaySink.emitNext(2, Sinks.EmitFailureHandler.FAIL_FAST);
    replaySink.emitNext(3, Sinks.EmitFailureHandler.FAIL_FAST);
    // then
    var f = replaySink.asFlux();
    f.subscribe(
        (integer) -> {
          System.out.println("subscriber 1 : " + integer);
        });
    replaySink.emitNext(4, Sinks.EmitFailureHandler.FAIL_FAST);
  }
}
