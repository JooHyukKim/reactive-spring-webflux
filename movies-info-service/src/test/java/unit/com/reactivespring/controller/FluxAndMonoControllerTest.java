package com.reactivespring.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.test.StepVerifier;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

// access to endpoints in the controller
@WebFluxTest(controllers = FluxAndMonoController.class)
@AutoConfigureWebTestClient // injected
class FluxAndMonoControllerTest {

  @Autowired WebTestClient testClient;

  @Test
  void flux() {
    // given

    // when
    testClient
        .get()
        .uri("/flux")
        .exchange()
        .expectStatus()
        .is2xxSuccessful()
        .expectBodyList(Integer.class)
        .hasSize(3);
    // then
  }

  @Test
  void flux2() {
    // given

    // when
    var flux =
        testClient
            .get()
            .uri("/flux")
            .exchange()
            .expectStatus()
            .is2xxSuccessful()
            .returnResult(Integer.class)
            .getResponseBody();
    // then
    StepVerifier.create(flux).expectNext(1, 2, 3).verifyComplete();
  }

  @Test
  void flux3() {
    // given

    // when
    var flux =
        testClient
            .get()
            .uri("/flux")
            .exchange()
            .expectStatus()
            .is2xxSuccessful()
            .expectBodyList(Integer.class)
            .consumeWith(
                listEntityExchangeResult -> {
                  var responseBody = listEntityExchangeResult.getResponseBody();
                  assertEquals(responseBody.size(), 3);
                });
  }

  @Test
  void helloWorldMono() {
    // given

    // when
    testClient
        .get()
        .uri("/mono")
        .exchange()
        .expectStatus()
        .is2xxSuccessful()
        .expectBody(String.class)
        .consumeWith(
            stringEntityExchangeResult -> {
              var responseBody = stringEntityExchangeResult.getResponseBody();
              assertEquals("hello-world!", responseBody);
            });
    // then
  }

  @Test
  void stream() {
    // given

    // when
    var flux =
        testClient
            .get()
            .uri("/stream")
            .exchange()
            .expectStatus()
            .is2xxSuccessful()
            .returnResult(Long.class)
            .getResponseBody();
    // then
    StepVerifier.create(flux).expectNext(0L, 1L, 2L, 3L).thenCancel().verify();
  }
}
