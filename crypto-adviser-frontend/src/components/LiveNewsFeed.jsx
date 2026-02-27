import React, { useState, useEffect } from 'react';
import { useWebSocket } from '../hooks/useWebSocket';
import axios from 'axios';
import './LiveNewsFeed.css';

const GATEWAY_URL = 'http://localhost:5051';
const NEWS_SERVICE_URL = 'http://localhost:5056';
const WS_URL = `${NEWS_SERVICE_URL}/ws/crypto-news`;

export const LiveNewsFeed = () => {
  const { messages: liveNews, isConnected } = useWebSocket(WS_URL, '/topic/crypto-news');
  const [historicalNews, setHistoricalNews] = useState([]);
  const [selectedCrypto, setSelectedCrypto] = useState('ALL');
  const [loading, setLoading] = useState(true);

  

  useEffect(() => {
    fetchHistoricalNews();
  }, []);

  const fetchHistoricalNews = async () => {
    try {
      setLoading(true);
      const response = await axios.get(`${NEWS_SERVICE_URL}/api/news/latest?limit=20`);
      setHistoricalNews(response.data);
    } catch (error) {
      console.error('Error fetching historical news:', error);
    } finally {
      setLoading(false);
    }
  };

  const allNews = [...liveNews, ...historicalNews];

  const filteredNews = selectedCrypto === 'ALL'
    ? allNews
    : allNews.filter(news => 
        news.relatedCryptos?.includes(selectedCrypto) || 
        news.title?.toUpperCase().includes(selectedCrypto)
      );

  const uniqueNews = Array.from(
    new Map(filteredNews.map(item => [item.newsId, item])).values()
  );

  return (
    <div className="live-news-container">
      {/* Header */}
      <div className="news-header">
        <div className="header-left">
          <h2>📰 Live Crypto News</h2>
          <div className={`connection-status ${isConnected ? 'connected' : 'disconnected'}`}>
            <span className="status-dot"></span>
            {isConnected ? 'Live' : 'Connecting...'}
          </div>
        </div>
      </div>

      {/* New Items Badge */}
      {liveNews.length > 0 && (
        <div className="new-items-badge">
          🔔 {liveNews.length} new {liveNews.length === 1 ? 'article' : 'articles'}
        </div>
      )}

      {/* News Feed */}
      {loading ? (
        <div className="loading-spinner">Loading news...</div>
      ) : (
        <div className="news-feed">
          {uniqueNews.length === 0 ? (
            <div className="no-news">No news available for {selectedCrypto}</div>
          ) : (
            uniqueNews.map((news) => (
              <NewsCard key={news.newsId} news={news} isNew={liveNews.includes(news)} />
            ))
          )}
        </div>
      )}
    </div>
  );
};

const NewsCard = ({ news, isNew }) => {
  const [imageError, setImageError] = useState(false);

  return (
    <div className={`news-card ${isNew ? 'new-item' : ''}`}>
      {isNew && <div className="new-badge">NEW</div>}
      
      <div className="news-content">
        {/* Image */}
        {news.imageUrl && !imageError && (
          <img
            src={news.imageUrl}
            alt={news.title}
            className="news-image"
            onError={() => setImageError(true)}
          />
        )}

        {/* Text Content */}
        <div className="news-text">
          <h3 className="news-title">{news.title}</h3>
          
          <p className="news-body">
            {news.body?.substring(0, 150)}
            {news.body?.length > 150 ? '...' : ''}
          </p>

          {/* Meta Info */}
          <div className="news-meta">
            <span className="news-source">{news.source}</span>
            <span className="news-time">{news.timeAgo}</span>
            {news.relatedCryptos && news.relatedCryptos !== 'GENERAL' && (
              <div className="crypto-tags">
                {news.relatedCryptos.split(',').slice(0, 3).map(crypto => (
                  <span key={crypto} className="crypto-tag">{crypto.trim()}</span>
                ))}
              </div>
            )}
          </div>

          {/* Read More Link */}
          {news.sourceUrl && (
            <a
              href={news.sourceUrl}
              target="_blank"
              rel="noopener noreferrer"
              className="read-more-link"
            >
              Read Full Article →
            </a>
          )}
        </div>
      </div>
    </div>
  );
};