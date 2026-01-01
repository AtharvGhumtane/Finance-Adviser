package com.alex.ai_serviceAlex.repo;

import com.alex.ai_serviceAlex.model.RecommendationEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface RecommendationRepository extends ReactiveCrudRepository<RecommendationEntity, Long> {

    /**
     * Find all recommendations for a specific user
     */
    Flux<RecommendationEntity> findByUserIdOrderByCreatedAtDesc(UUID userId);

    /**
     * Find recommendations by user and cryptocurrency
     */
    Flux<RecommendationEntity> findByUserIdAndTargetCryptocurrency(UUID userId, String targetCryptocurrency);

    /**
     * Find recent recommendations for a user (last N days)
     */
    @Query("SELECT * FROM recommendations WHERE user_id = :userId AND created_at >= :since ORDER BY created_at DESC")
    Flux<RecommendationEntity> findRecentByUserId(UUID userId, LocalDateTime since);

    /**
     * Count total recommendations for a user
     */
    Mono<Long> countByUserId(UUID userId);

    /**
     * Find latest recommendation for user and crypto
     */
    @Query("SELECT * FROM recommendations WHERE user_id = :userId AND target_cryptocurrency = :crypto ORDER BY created_at DESC LIMIT 1")
    Mono<RecommendationEntity> findLatestByUserIdAndCrypto(UUID userId, String crypto);

    /**
     * Delete old recommendations (data retention policy)
     */
    @Query("DELETE FROM recommendations WHERE created_at < :before")
    Mono<Void> deleteOlderThan(LocalDateTime before);
}
