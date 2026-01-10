// src/hooks/useWebSocket.js
import { useEffect, useState, useCallback } from 'react';
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';

export const useWebSocket = (url, topic) => {
  const [messages, setMessages] = useState([]);
  const [isConnected, setIsConnected] = useState(false);

  useEffect(() => {
    // Create STOMP client
    const stompClient = new Client({
      webSocketFactory: () => new SockJS(url),
      debug: (str) => console.log('STOMP:', str),
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
    });

    // On connect
    stompClient.onConnect = () => {
      console.log('✅ WebSocket connected');
      setIsConnected(true);

      // Subscribe to topic
      stompClient.subscribe(topic, (message) => {
        try {
          const newsItem = JSON.parse(message.body);
          console.log('📰 New news received:', newsItem.title);
          
          setMessages((prev) => {
            // Prevent duplicates
            if (prev.some(item => item.newsId === newsItem.newsId)) {
              return prev;
            }
            return [newsItem, ...prev].slice(0, 50); // Keep last 50 items
          });
        } catch (error) {
          console.error('Error parsing message:', error);
        }
      });
    };

    // On disconnect
    stompClient.onDisconnect = () => {
      console.log('❌ WebSocket disconnected');
      setIsConnected(false);
    };

    // On error
    stompClient.onStompError = (frame) => {
      console.error('STOMP error:', frame);
      setIsConnected(false);
    };

    // Activate connection
    stompClient.activate();

    // Cleanup on unmount
    return () => {
      if (stompClient) {
        stompClient.deactivate();
      }
    };
  }, [url, topic]);

  const clearMessages = useCallback(() => {
    setMessages([]);
  }, []);

    return {
      messages,
      isConnected,
      clearMessages
    };
  };
