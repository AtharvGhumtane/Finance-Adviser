import React, { useState, useEffect } from 'react';
import { useWebSocket } from '../hooks/useWebSocket';
import axios from 'axios';
import './LiveNewsFeed.css';

const GATEWAY_URL = 'http://localhost:5051';
const NEWS_SERVICE_URL = 'http://localhost:5056';
const WS_URL = `${NEWS_SERVICE_URL}/ws/crypto-news`;

const CRYPTO_FILTERS = ['ALL', 'BTC', 'ETH', 'SOL', 'ADA', 'DOT'];

const CATEGORY_COLORS = {
  NEWS: '#3b82f6',
  REGULATION: '#f59e0b',
  DEFI: '#10b981',
  NFT: '#8b5cf6',
  MARKET: '#6366f1',
  ADOPTION: '#ef4444',
};

export const LiveNewsFeed = () => {
  const { messages: liveNews, isConnected } = useWebSocket(WS_URL, '/topic/crypto-news');
  const [historicalNews, setHistoricalNews] = useState([]);
  const [selectedCrypto, setSelectedCrypto] = useState('ALL');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetchHistoricalNews();
  }, []);

  const fetchHistoricalNews = async () => {
    try {
      setLoading(true);
      setError(null);
      const response = await axios.get(`${NEWS_SERVICE_URL}/api/news/latest?limit=20`);
      setHistoricalNews(response.data);
    } catch (err) {
      console.error('Error fetching historical news:', err);
      setError('Could not load news. Please try again.');
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
    new Map(filteredNews.map(item => [item.newsId || item.id, item])).values()
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
        <button className="refresh-btn" onClick={fetchHistoricalNews} title="Refresh news">
          🔄
        </button>
      </div>

      {/* Crypto Filter Tabs */}
      <div className="crypto-filter-tabs">
        {CRYPTO_FILTERS.map(crypto => (
          <button
            key={crypto}
            className={`filter-tab ${selectedCrypto === crypto ? 'active' : ''}`}
            onClick={() => setSelectedCrypto(crypto)}
          >
            {crypto}
          </button>
        ))}
      </div>

      {/* New Items Badge */}
      {liveNews.length > 0 && (
        <div className="new-items-badge">
          🔔 {liveNews.length} new {liveNews.length === 1 ? 'article' : 'articles'}
        </div>
      )}

      {/* News Feed */}
      {loading ? (
        <div className="news-loading">
          <div className="loading-spinner-ring"></div>
          <span>Loading news...</span>
        </div>
      ) : error ? (
        <div className="news-error">
          <span>⚠️ {error}</span>
          <button onClick={fetchHistoricalNews}>Retry</button>
        </div>
      ) : (
        <div className="news-feed">
          {uniqueNews.length === 0 ? (
            <div className="no-news">
              <span className="no-news-icon">📭</span>
              <p>No news available for {selectedCrypto}</p>
              <button className="refresh-btn-inline" onClick={() => setSelectedCrypto('ALL')}>
                Show All News
              </button>
            </div>
          ) : (
            uniqueNews.map((news) => (
              <NewsCard key={news.newsId || news.id} news={news} isNew={liveNews.includes(news)} />
            ))
          )}
        </div>
      )}
    </div>
  );
};

const NewsCard = ({ news, isNew }) => {
  const [imageError, setImageError] = useState(false);
  const categoryColor = CATEGORY_COLORS[news.category] || '#6b7280';

  return (
    <div className={`news-card ${isNew ? 'new-item' : ''}`}>
      {isNew && <div className="new-badge">NEW</div>}

      <div className="news-content">
        {/* Image */}
        {news.imageUrl && !imageError && (
          <div className="news-image-wrapper">
            <img
              src={news.imageUrl}
              alt={news.title}
              className="news-image"
              onError={() => setImageError(true)}
            />
          </div>
        )}
        {(!news.imageUrl || imageError) && (
          <div className="news-image-placeholder">
            {news.relatedCryptos?.includes('BTC') ? '₿' :
             news.relatedCryptos?.includes('ETH') ? 'Ξ' :
             news.relatedCryptos?.includes('SOL') ? '◎' : '🪙'}
          </div>
        )}

        {/* Text Content */}
        <div className="news-text">
          {/* Category Badge */}
          {news.category && (
            <span
              className="category-badge"
              style={{ background: categoryColor + '22', color: categoryColor, borderColor: categoryColor + '44' }}
            >
              {news.category}
            </span>
          )}

          <h3 className="news-title">{news.title}</h3>

          <p className="news-body">
            {news.body?.substring(0, 160)}
            {news.body?.length > 160 ? '...' : ''}
          </p>

          {/* Meta Info */}
          <div className="news-meta">
            <span className="news-source">🗞 {news.source}</span>
            <span className="news-time">🕐 {news.timeAgo}</span>
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