package com.taxoptimizerAlexz.tax_optimizerAlexz.repo;


import com.taxoptimizerAlexz.tax_optimizerAlexz.model.TaxRecommendation;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Reactive R2DBC repository for TaxRecommendation.
 * Saved asynchronously by the RabbitMQ consumer.
 */
@Repository
public interface TaxRecommendationRepository extends ReactiveCrudRepository<TaxRecommendation, Long> {

    /**
     * All recommendations for a user, latest first.
     */
    Flux<TaxRecommendation> findByUserIdOrderByCreatedAtDesc(String userId);

    /**
     * Get recommendation linked to a specific profile.
     */
    Mono<TaxRecommendation> findByProfileId(Long profileId);

    /**
     * Get all recommendations by regime (analytics).
     */
    Flux<TaxRecommendation> findByRecommendedRegime(String recommendedRegime);

    /**
     * Update status after user views/applies recommendation.
     */
    @Query("UPDATE tax_recommendations SET status = :status WHERE id = :id")
    Mono<Void> updateStatus(Long id, String status);

    /**
     * Count recommendations per financial year.
     */
    @Query("SELECT COUNT(*) FROM tax_recommendations WHERE financial_year = :fy")
    Mono<Long> countByFinancialYear(String fy);
}
