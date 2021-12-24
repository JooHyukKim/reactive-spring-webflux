package com.reactivespring.router;

import com.reactivespring.handler.ReviewHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.path;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class ReviewRouter {

  @Bean
  public RouterFunction<ServerResponse> reviesRoute(ReviewHandler reviewHandler) {
    return route()
        .nest(
            path("/v1/reviews"),
            builder -> {
              builder
                  .GET("/stream", request -> reviewHandler.getReviewsStream(request))
                  .POST(reviewHandler::addReview)
                  .GET(reviewHandler::getReviews)
                  .PUT("/{id}", reviewHandler::updateReview)
                  .DELETE("/{id}", reviewHandler::deleteReview);
            })
        .GET("/v1/helloworld", (request) -> ServerResponse.ok().bodyValue("helloworld"))
        //        .POST("/v1/reviews", reviewHandler::addReview)
        //        .GET("/v1/reviews", reviewHandler::getReviews)
        .build();
  }
}
