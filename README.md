# 💰 FinAdvisor

> A full-stack microservices platform delivering AI-powered crypto investment advice, Indian tax optimisation, and credit card trap detection — built for the modern Indian investor.

[![React](https://img.shields.io/badge/React-18.0+-61DAFB?style=for-the-badge&logo=react&logoColor=white)](https://reactjs.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.1+-6DB33F?style=for-the-badge&logo=spring&logoColor=white)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-21+-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![WebSocket](https://img.shields.io/badge/WebSocket-STOMP-010101?style=for-the-badge&logo=socket.io&logoColor=white)](https://stomp.github.io/)
[![RabbitMQ](https://img.shields.io/badge/RabbitMQ-3.x-FF6600?style=for-the-badge&logo=rabbitmq&logoColor=white)](https://www.rabbitmq.com/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16+-336791?style=for-the-badge&logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Gemini](https://img.shields.io/badge/Gemini-2.5%20Flash-4285F4?style=for-the-badge&logo=google&logoColor=white)](https://deepmind.google/technologies/gemini/)

---

## ✨ Features

### 🤖 AI Crypto Adviser
- Personalised investment recommendations for BTC, ETH, SOL, ADA, XRP, DOT and more
- Based on annual income, risk tolerance (1–10), and investment horizon
- Gemini 2.5 Flash powered analysis with confidence score
- Sync and async processing modes
- Full recommendation history

### 🧾 Tax Optimizer
- Old Regime vs New Regime comparison for FY 2024-25
- HRA exemption calculator using Least-of-3 rule
- Deduction analysis — 80C, 80D, 80CCD(1B), 80EEA, 80G, 80TTA, Section 24b
- Gemini AI investment suggestions to reduce future tax liability
- Results saved asynchronously via RabbitMQ

### 💳 Credit Card Trap Analyser
- Detects 6 common credit card debt traps:
  - Minimum Payment Trap
  - High Credit Utilisation
  - Cash Advance Trap
  - Late Payment Trap
  - EMI Overload
  - Debt-to-Income Trap
- ML-based risk classification — LOW / MEDIUM / HIGH
- CIBIL score impact projection
- Annual interest cost estimation
- Gemini AI personalised recommendations

### 📰 Live Crypto News Feed
- Real-time news streamed via WebSocket (STOMP)
- Source attribution, image previews, full article links
- Automatic deduplication

### 📊 Live Crypto Price Ticker
- Real-time scrolling price ticker via WebSocket
- 15+ major cryptocurrencies
- 24-hour percentage change indicators

### 👤 User Management
- JWT-based authentication via API Gateway
- User registration, login, profile management
- All protected routes validated at gateway level

---

## 🏗️ Architecture

### Microservices Overview

```
┌──────────────────────────┐
│    React Frontend        │
│      (Port 5173)         │
└───────────┬──────────────┘
            │
            ▼
┌──────────────────────────┐
│      API Gateway         │
│       (Port 5051)        │
│  - JWT Validation        │
│  - Request Routing       │
│  - CORS Handling         │
└───────────┬──────────────┘
            │
   ┌────────┼──────────────────────────────┐
   ▼        ▼                              ▼
┌──────┐ ┌──────────────────┐   ┌─────────────────────┐
│ Auth │ │  Tax Optimizer   │   │  Credit Card        │
│ Svc  │ │  (Port 5057)     │   │  Service            │
└──────┘ │  WebFlux + R2DBC │   └─────────────────────┘
         └────────┬─────────┘
                  │
             RabbitMQ
                  │
         Consumer → PostgreSQL
            (async save)

┌──────────────────────────┐   ┌────────────────────────┐
│  AI Recommendation Svc   │   │  News Service          │
│  (Crypto advice)         │   │  (Port 5056)           │
│  Gemini 2.5 Flash        │   │  WebSocket + Prices    │
└──────────────────────────┘   └────────────────────────┘

┌──────────────────────────┐
│  Eureka Discovery Server │
│       (Port 8761)        │
└──────────────────────────┘
```

### Tech Stack

#### Frontend
- **React 18** — UI Framework
- **Vite** — Build Tool
- **Tailwind CSS** — Styling
- **React Router v6** — Navigation
- **Axios** — HTTP Client
- **STOMP.js + SockJS** — WebSocket Communication
- **Context API** — Auth State Management

#### Backend
- **Spring Boot 3.1** — Application Framework
- **Spring WebFlux** — Reactive programming (Tax Service)
- **Spring Cloud Gateway** — API Gateway + JWT filter
- **Spring Cloud Netflix Eureka** — Service Discovery
- **Spring AMQP** — RabbitMQ integration
- **R2DBC + Spring Data Relational** — Non-blocking DB access
- **JWT (jjwt)** — Token-based auth

#### AI & External
- **Google Gemini 2.5 Flash** — Tax recommendations + Crypto advice + Credit analysis

#### Infrastructure
- **PostgreSQL** — Primary database
- **RabbitMQ** — Async message queue (with Dead Letter Queue)
- **Docker** — RabbitMQ container

---

## 🚀 Getting Started

### Prerequisites
- Node.js 18+
- Java 21+
- Maven
- PostgreSQL
- Docker Desktop
- Gemini API Key

### 1. Start Infrastructure

```bash
# Start RabbitMQ (first time)
docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3-management

# Or if already created
docker start rabbitmq
```

### 2. Start Backend Services (in order)

```bash
# 1. Eureka Server
cd eureka-server && mvn spring-boot:run

# 2. Auth Service
cd auth-service && mvn spring-boot:run

# 3. Tax Optimizer Service
cd tax-optimizer-service && mvn spring-boot:run

# 4. Credit Card Service
cd credit-card-service && mvn spring-boot:run

# 5. AI Recommendation Service
cd ai-recommendation-service && mvn spring-boot:run

# 6. News Service
cd news-service && mvn spring-boot:run

# 7. API Gateway (last)
cd api-gateway && mvn spring-boot:run
```

### 3. Start Frontend

```bash
cd frontend
npm install
npm run dev
```

App runs at `http://localhost:5173`

---

## ⚙️ Environment Variables

```properties
# Gemini AI
GEMINI_API_KEY=your_gemini_api_key

# JWT (API Gateway)
jwt.secret=your_jwt_secret_key

# PostgreSQL
spring.r2dbc.url=r2dbc:postgresql://localhost:5432/your_db
spring.r2dbc.username=postgres
spring.r2dbc.password=your_password

# RabbitMQ
spring.rabbitmq.host=localhost
spring.rabbitmq.port=5672
spring.rabbitmq.username=guest
spring.rabbitmq.password=guest
```

---

## 📡 API Endpoints

### Auth
```
POST /api/auth/signup
POST /api/auth/login
```

### Tax Optimizer
```
POST /api/v1/tax/optimize             # Full AI analysis + RabbitMQ
POST /api/v1/tax/compare-regimes      # Quick Old vs New comparison
POST /api/v1/tax/hra-exemption        # HRA calculator
GET  /api/v1/tax/health
```

### Credit Card
```
POST /api/v1/credit/quick-check       # Trap detection only
POST /api/v1/credit/risk              # ML risk classification
POST /api/v1/credit/analyze           # Full AI analysis
```

### AI Recommendations (Crypto)
```
POST /api/v1/recommendations/generate
POST /api/v1/recommendations/generate-async
GET  /api/v1/recommendations/history
```

### News & Prices
```
GET  /api/news/latest?limit=20
GET  /api/prices/all
WS   /ws/crypto-news → /topic/crypto-news
WS   /ws/crypto-news → /topic/crypto-prices
```

---

## 🗂️ Project Structure

```
finAdvisor/
├── api-gateway/
├── auth-service/
├── tax-optimizer-service/
├── credit-card-service/
├── ai-recommendation-service/
├── news-service/
├── eureka-server/
└── frontend/
    └── src/
        ├── components/       # Navbar, Ticker, NewsFeed, InfoModal
        ├── pages/            # Dashboard, Tax, Credit, Recommendations
        ├── context/          # AuthContext
        ├── hooks/            # useWebSocket
        └── services/         # api.js
```

---

## 👨‍💻 Author

- [Atharv Ghumtane](https://github.com/yourusername)

## 🙏 Acknowledgments

- [Google Gemini](https://deepmind.google/technologies/gemini/) for AI capabilities
- [Spring Boot](https://spring.io/projects/spring-boot) for backend framework
- [React](https://reactjs.org/) for frontend framework
- [RabbitMQ](https://www.rabbitmq.com/) for async messaging

---

> Not financial advice. For educational purposes only.
