package com.taxoptimizerAlexz.tax_optimizerAlexz.repo;

import com.taxoptimizerAlexz.tax_optimizerAlexz.model.TaxProfile;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Reactive R2DBC repository for TaxProfile.
 * All methods return Mono/Flux — non-blocking.
 */
@Repository
public interface TaxProfileRepository extends ReactiveCrudRepository<TaxProfile, Long> {

    /**
     * Get all profiles for a user, most recent first.
     */
    Flux<TaxProfile> findByUserIdOrderByCreatedAtDesc(String userId);

    /**
     * Get the latest profile for a user.
     */
    Mono<TaxProfile> findTopByUserIdOrderByCreatedAtDesc(String userId);

    /**
     * Find profiles within a salary range (analytics).
     */
    Flux<TaxProfile> findByGrossSalaryBetween(double min, double max);

    /**
     * Count profiles by city type.
     */
    @Query("SELECT COUNT(*) FROM tax_profiles WHERE city_type = :cityType")
    Mono<Long> countByCityType(String cityType);
}
