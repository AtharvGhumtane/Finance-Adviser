import React, { useState, useEffect, useRef } from 'react';
import { useWebSocket } from '../hooks/useWebSocket';
import axios from 'axios';
import './CryptoPriceTicker.css';

const NEWS_SERVICE_URL = 'http://localhost:5056';
const WS_URL = `${NEWS_SERVICE_URL}/ws/crypto-news`;

export const CryptoPriceTicker = () => {
  const { messages: priceUpdates, isConnected } = useWebSocket(WS_URL, '/topic/crypto-prices');
  const [prices, setPrices] = useState({});
  const [loading, setLoading] = useState(true);
  const updateTimeoutRef = useRef(null);

  const fetchInitialPrices = async () => {
    try {
      console.log('🔍 Fetching initial prices from:', `${NEWS_SERVICE_URL}/api/prices/all`);
      const response = await axios.get(`${NEWS_SERVICE_URL}/api/prices/all`);
      
      console.log('📊 Initial prices response:', response.data);
      console.log('📊 Number of prices:', response.data?.length);
      
      const pricesMap = {};
      response.data.forEach(price => {
        pricesMap[price.symbol] = price;
      });
      
      console.log('📊 Prices map:', pricesMap);
      setPrices(pricesMap);
      setLoading(false);
    } catch (error) {
      console.error('❌ Error fetching initial prices:', error);
      console.error('❌ Error details:', error.response?.data);
      setLoading(false);
    }
  };

  useEffect(() => {
    const loadPrices = async () => {
      await fetchInitialPrices();
    };
    loadPrices();
  }, []);

  useEffect(() => {
    console.log('📈 Price updates received:', priceUpdates.length);
    if (priceUpdates.length > 0) {
      const latestPrice = priceUpdates[0];
      console.log('📈 Latest price update:', latestPrice);
      
      if (updateTimeoutRef.current) {
        clearTimeout(updateTimeoutRef.current);
      }
      updateTimeoutRef.current = setTimeout(() => {
        setPrices(prev => ({
          ...prev,
          [latestPrice.symbol]: latestPrice
        }));
        console.log(`✅ ${latestPrice.symbol} updated: $${latestPrice.price}`);
      }, 0);
    }
    return () => {
      if (updateTimeoutRef.current) {
        clearTimeout(updateTimeoutRef.current);
      }
    };
  }, [priceUpdates]);

  const formatPrice = (price) => {
    if (!price) return '$0.00';
    const num = parseFloat(price);
    if (num >= 1000) return `$${num.toLocaleString('en-US', { maximumFractionDigits: 0 })}`;
    if (num >= 1) return `$${num.toFixed(2)}`;
    return `$${num.toFixed(4)}`;
  };

  const formatChange = (change) => {
    if (!change) return '0.00%';
    const num = parseFloat(change);
    const sign = num >= 0 ? '+' : '';
    return `${sign}${num.toFixed(2)}%`;
  };

  console.log('🎨 Rendering ticker. Loading:', loading, 'Prices count:', Object.keys(prices).length);

  if (loading) {
    return (
      <div className="crypto-ticker-container">
        <div className="ticker-loading">
          <span className="loading-spinner"></span>
          Loading live prices...
        </div>
      </div>
    );
  }

  const priceList = Object.values(prices);
  console.log('🎨 Price list for rendering:', priceList);

  if (priceList.length === 0) {
    return (
      <div className="crypto-ticker-container">
        <div className={`connection-indicator ${isConnected ? 'connected' : 'disconnected'}`}>
          <span className="indicator-dot"></span>
          {isConnected ? 'LIVE' : 'OFFLINE'}
        </div>
        <div className="ticker-loading">
          ⚠️ No price data available. Waiting for updates...
        </div>
      </div>
    );
  }

  const tickerItems = [...priceList, ...priceList];

  return (
    <div className="crypto-ticker-container sticky top-16 z-40">
      <div className={`connection-indicator ${isConnected ? 'connected' : 'disconnected'}`}>
        <span className="indicator-dot"></span>
        {isConnected ? 'LIVE' : 'OFFLINE'}
      </div>

      <div className="ticker-wrapper">
        <div className="ticker-track">
          {tickerItems.map((crypto, index) => (
            <div 
              key={`${crypto.symbol}-${index}`} 
              className="ticker-item"
              data-symbol={crypto.symbol}
            >
              <span className="crypto-icon">{crypto.icon || '💰'}</span>
              <div className="crypto-info">
                <span className="crypto-symbol">{crypto.symbol}</span>
                <span className="crypto-name">{crypto.name}</span>
              </div>
              <span className="crypto-price">{formatPrice(crypto.price)}</span>
              <span className={`crypto-change ${parseFloat(crypto.priceChangePercentage24h) >= 0 ? 'positive' : 'negative'}`}>
                {formatChange(crypto.priceChangePercentage24h)}
              </span>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};