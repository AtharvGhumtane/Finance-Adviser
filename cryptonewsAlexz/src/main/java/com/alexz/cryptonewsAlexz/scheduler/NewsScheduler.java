package com.alexz.cryptonewsAlexz.scheduler;

import com.alexz.cryptonewsAlexz.service.NewsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NewsScheduler {

    private final NewsService newsService;

    @Value("${news.fetch.interval.minutes:5}")
    private int fetchInterval;

    /**
     * Fetch news every N minutes
     */
    @Scheduled(fixedDelayString = "${news.fetch.interval.minutes:5}00000")
    public void scheduleNewsFetch() {
        log.info("🔄 Scheduled news fetch triggered");
        try {
            newsService.fetchAndProcessNews();
            log.info("✅ Scheduled news fetch completed");
        } catch (Exception e) {
            log.error("❌ Error in scheduled news fetch", e);
        }
    }

    /**
     * Fetch news on startup (after 30 seconds)
     */
    @Scheduled(initialDelay = 30000, fixedRate = Long.MAX_VALUE)
    public void initialNewsFetch() {
        log.info("🚀 Initial news fetch on startup");
        scheduleNewsFetch();
    }
}
