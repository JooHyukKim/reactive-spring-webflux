package com.reactivespring.repository;

import com.reactivespring.domain.Review;
import com.reactivespring.router.ReviewRouter;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface ReviewReactiveRepository extends ReactiveMongoRepository<Review, String> {
  Flux<Review> findReviewsByMovieInfoId(Long movieInfoId);
}
