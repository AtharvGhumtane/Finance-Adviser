package com.alexz.cryptonewsAlexz.repo;

import com.alexz.cryptonewsAlexz.model.CryptoNews;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface NewsRepository extends JpaRepository<CryptoNews, Long> {

    // Prevent duplicates
    boolean existsByNewsId(String newsId);

    Optional<CryptoNews> findByNewsId(String newsId);

    // Latest news
    List<CryptoNews> findTop50ByOrderByPublishedAtDesc();

    // Filter by crypto (from relatedCryptos field)
    @Query("""
        SELECT n FROM CryptoNews n
        WHERE UPPER(n.relatedCryptos) LIKE UPPER(CONCAT('%', :crypto, '%'))
        ORDER BY n.publishedAt DESC
    """)
    Page<CryptoNews> findByRelatedCryptosContainingIgnoreCase(
            @Param("crypto") String crypto,
            Pageable pageable
    );

    // Recent news (last N hours)
    @Query("""
        SELECT n FROM CryptoNews n
        WHERE n.publishedAt >= :since
        ORDER BY n.publishedAt DESC
    """)
    List<CryptoNews> findRecentNews(@Param("since") LocalDateTime since);

    // Used by service
    List<CryptoNews> findByPublishedAtAfterOrderByPublishedAtDesc(LocalDateTime since);
}
