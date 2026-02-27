package com.creditcard.credit_card_service.repo;

import com.creditcard.credit_card_service.model.CreditProfile;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface CreditProfileRepository extends ReactiveCrudRepository<CreditProfile, Long> {

    Flux<CreditProfile> findByUserIdOrderByCreatedAtDesc(String userId);

    Mono<CreditProfile> findTopByUserIdOrderByCreatedAtDesc(String userId);
}
