package com.creditcard.credit_card_service.repo;

import com.creditcard.credit_card_service.model.CreditAnalysis;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface CreditAnalysisRepository extends ReactiveCrudRepository<CreditAnalysis, Long> {

    Flux<CreditAnalysis> findByUserIdOrderByCreatedAtDesc(String userId);

    Mono<CreditAnalysis> findByProfileId(Long profileId);

    Flux<CreditAnalysis> findByRiskLevel(String riskLevel);
}
