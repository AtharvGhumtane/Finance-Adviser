# Start_Of_NewWorld
# 💰 Crypto World Adviser

> An intelligent cryptocurrency investment advisory platform powered by AI, featuring real-time crypto news, live price tracking, and personalized investment recommendations.

[![React](https://img.shields.io/badge/React-18.0+-61DAFB?style=for-the-badge&logo=react&logoColor=white)](https://reactjs.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.0+-6DB33F?style=for-the-badge&logo=spring&logoColor=white)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-17+-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![WebSocket](https://img.shields.io/badge/WebSocket-STOMP-010101?style=for-the-badge&logo=socket.io&logoColor=white)](https://stomp.github.io/)



## ✨ Features

### 🤖 AI-Powered Investment Recommendations
- Personalized cryptocurrency investment advice based on:
  - Annual income
  - Risk tolerance (1-10 scale)
  - Investment horizon (Short/Medium/Long term)
  - Target cryptocurrency preferences
- Real-time AI analysis using advanced algorithms
- Confidence score for each recommendation
- Detailed risk assessment

### 📰 Live Crypto News Feed
- Real-time cryptocurrency news via WebSocket
- Filter news by cryptocurrency (BTC, ETH, SOL, ADA, DOT)
- Source attribution and timestamp
- Image previews and full article links
- Automatic duplicate removal

### 📊 Live Crypto Price Ticker
- Real-time scrolling price ticker
- 15+ major cryptocurrencies tracked
- Live price updates via WebSocket
- 24-hour percentage change indicators
- Beautiful animated scrolling interface

### 👤 User Management
- Secure JWT-based authentication
- User registration and login
- Profile management
- Recommendation history tracking
- Session management

### ⚡ Async Processing
- Background recommendation generation
- Queue-based processing for heavy workloads
- Non-blocking UI experience

## 🏗️ Architecture

### Microservices Architecture

```
┌─────────────────┐
│   React Frontend │
│   (Port 5173)    │
└────────┬─────────┘
         │
         ↓
┌─────────────────────────┐
│   API Gateway           │
│   (Port 5051)           │
│   - JWT Authentication  │
│   - Request Routing     │
│   - CORS Handling       │
└────────┬────────────────┘
         │
    ┌────┴──────────────────────────┐
    ↓                                ↓
┌────────────────┐        ┌──────────────────────┐
│  Auth Service  │        │  User Service        │
│  (Port 8081)   │        │  (Port 8082)         │
└────────────────┘        └──────────────────────┘
    │                                │
    ↓                                ↓
┌────────────────────────────────────────────────┐
│         Eureka Discovery Server                │
│              (Port 8761)                       │
└────────────────────────────────────────────────┘
    │                                │
    ↓                                ↓
┌─────────────────────┐   ┌──────────────────────┐
│ AI Recommendation   │   │ Crypto News Service  │
│ Service             │   │ (Port 5056)          │
│ (Port 8084)         │   │ - Live News Feed     │
│ - OpenAI GPT        │   │ - Price Tracking     │
│ - Sync/Async        │   │ - WebSocket STOMP    │
└─────────────────────┘   └──────────────────────┘
```

### Tech Stack

#### Frontend
- **React 18** - UI Framework
- **React Router** - Navigation
- **Axios** - HTTP Client
- **STOMP.js & SockJS** - WebSocket Communication
- **Tailwind CSS** - Styling
- **Context API** - State Management

#### Backend
- **Spring Boot 3.x** - Application Framework
- **Spring Cloud Gateway** - API Gateway
- **Spring Cloud Netflix Eureka** - Service Discovery
- **Spring Security** - Authentication & Authorization
- **Spring WebSocket (STOMP)** - Real-time Communication
- **JWT** - Token-based Authentication
- **OpenAI API** - AI Recommendations
- **RestTemplate** - External API Communication
- **MySQL** - Database
- **Kafka** (Optional) - Message Broker

#### External APIs
- **CoinGecko API** - Cryptocurrency Prices
- **CryptoPanic API** - Crypto News
- **Gemini API** - AI-powered Recommendations

## 🚀 Getting Started

### Prerequisites

- **Node.js** 
- **Java** 
- **Maven** 
- **PostgreSQL** 
- **Gemini API Key**



## 📡 API Endpoints

### Authentication
```
POST /api/auth/signup      - Register new user
POST /api/auth/login       - Login user
GET  /api/auth/validate    - Validate JWT token
```

### User Management
```
GET  /api/users/profile    - Get user profile
PUT  /api/users/profile    - Update user profile
```

### AI Recommendations
```
POST /api/v1/recommendations/generate       - Generate recommendation (sync)
POST /api/v1/recommendations/generate-async - Generate recommendation (async)
GET  /api/v1/recommendations/history        - Get user's history
GET  /api/v1/recommendations/history/{crypto} - Get history by crypto
GET  /api/v1/recommendations/recent?days=30   - Get recent recommendations
```

### Crypto News & Prices
```
GET  /api/news/latest?limit=20              - Get latest news
WS   /ws/crypto-news                        - WebSocket endpoint
     /topic/crypto-news                     - News topic
     /topic/crypto-prices                   - Prices topic
```

## 🛠️ Development

### Project Structure

```
crypto-world-adviser/
├── eureka-server/              # Service Discovery
├── api-gateway/                # API Gateway
├── auth-service/               # Authentication Service
├── user-service/               # User Management Service
├── ai-recommendation-service/  # AI Recommendation Service
├── crypto-news-service/        # News & Price Service
└── crypto-adviser-frontend/    # React Frontend
    ├── src/
    │   ├── components/         # React Components
    │   │   ├── LiveNewsFeed.jsx
    │   │   ├── CryptoPriceTicker.jsx
    │   │   └── Navbar.jsx
    │   ├── pages/              # Page Components
    │   │   ├── Dashboard.jsx
    │   │   ├── History.jsx
    │   │   ├── Login.jsx
    │   │   └── Signup.jsx
    │   ├── context/            # React Context
    │   │   └── AuthContext.jsx
    │   ├── hooks/              # Custom Hooks
    │   │   └── useWebSocket.js
    │   └── services/           # API Services
    │       └── api.js
    └── package.json
```

## 👨‍💻 Author

 - [Atharv Ghumtane](https://github.com/yourusername)

## 🙏 Acknowledgments

- [CoinGecko](https://www.coingecko.com/) for cryptocurrency price data
- [CryptoPanic](https://cryptopanic.com/) for crypto news
- [Gemini ](https://openai.com/) for AI capabilities
- [Spring Boot](https://spring.io/projects/spring-boot) for backend framework
- [React](https://reactjs.org/) for frontend framework



