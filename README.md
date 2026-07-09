# 💰 FinAdvisor

> A full-stack microservices platform delivering AI-powered crypto investment advice, Indian tax optimisation, and credit card trap detection — built for the modern Indian investor.

[![React](https://img.shields.io/badge/React-19.0+-61DAFB?style=for-the-badge&logo=react&logoColor=white)](https://reactjs.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2+-6DB33F?style=for-the-badge&logo=spring&logoColor=white)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-21+-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?style=for-the-badge&logo=docker&logoColor=white)](https://www.docker.com/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16+-336791?style=for-the-badge&logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![RabbitMQ](https://img.shields.io/badge/RabbitMQ-3.13-FF6600?style=for-the-badge&logo=rabbitmq&logoColor=white)](https://www.rabbitmq.com/)
[![Kafka](https://img.shields.io/badge/Apache%20Kafka-7.6-231F20?style=for-the-badge&logo=apachekafka&logoColor=white)](https://kafka.apache.org/)
[![Gemini](https://img.shields.io/badge/Gemini-2.5%20Flash-4285F4?style=for-the-badge&logo=google&logoColor=white)](https://deepmind.google/technologies/gemini/)

---

## ✨ Features

### 🤖 AI Crypto Adviser
- Personalised investment recommendations for BTC, ETH, SOL, ADA, XRP, DOT and more
- Based on annual income, risk tolerance (1–10), and investment horizon
- Gemini 2.5 Flash powered analysis with confidence score
- Sync and async processing modes via RabbitMQ
- Full recommendation history

### 🧾 Tax Optimizer
- Old Regime vs New Regime comparison for FY 2024-25
- HRA exemption calculator using Least-of-3 rule
- Deduction analysis — 80C, 80D, 80CCD(1B), 80EEA, 80G, 80TTA, Section 24b
- Gemini AI investment suggestions to reduce future tax liability
- Results saved asynchronously via RabbitMQ with Dead Letter Queue

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
- Filter by cryptocurrency (BTC, ETH, SOL, ADA, DOT)
- Source attribution, image previews, full article links
- Automatic deduplication

### 📊 Live Crypto Price Ticker
- Real-time scrolling price ticker via WebSocket
- 15+ major cryptocurrencies tracked
- 24-hour percentage change indicators

### 👤 User Management
- JWT-based authentication via API Gateway
- User registration, login, profile management
- All protected routes validated at gateway level

---

## 🏗️ Architecture

### Microservices Overview

```
┌──────────────────────────────────────────────────────────────────┐
│                     React Frontend (Port 5173)                   │
└───────────────────────────────┬──────────────────────────────────┘
                                │
                                ▼
┌──────────────────────────────────────────────────────────────────┐
│                  API Gateway  (Port 5051)                        │
│            JWT Validation · Request Routing · CORS               │
└──┬──────────────┬──────────────┬────────────────┬───────────────┘
   │              │              │                │
   ▼              ▼              ▼                ▼
┌──────┐   ┌──────────┐  ┌────────────┐  ┌─────────────────┐
│Auth  │   │  User    │  │Tax Optimiz.│  │Credit Card Svc  │
│Svc   │   │  Svc     │  │(Port 5057) │  │(Port 5058)      │
│5054  │   │  5053    │  │WebFlux/R2DBC│  │WebFlux/R2DBC   │
└──────┘   └──────────┘  └─────┬──────┘  └───────┬─────────┘
                                │                 │
                            RabbitMQ ←────────────┘
                                │
                         async save to PostgreSQL

┌──────────────────────────┐   ┌────────────────────────┐
│ AI Recommendation Svc    │   │ Crypto News Svc        │
│ (Port 5055)              │   │ (Port 5056)            │
│ WebFlux · R2DBC · AMQP   │   │ WebSocket · JPA · Kafka│
│ Gemini 2.5 Flash         │   │ Live prices & news     │
└──────────────────────────┘   └────────────────────────┘

┌──────────────────────────┐   ┌────────────────────────┐
│  Eureka Discovery Server │   │  PostgreSQL (Port 2526)│
│       (Port 8761)        │   │  6 databases           │
└──────────────────────────┘   └────────────────────────┘

┌──────────────────────────┐   ┌────────────────────────┐
│  RabbitMQ  (Port 5672)   │   │  Apache Kafka (9092)   │
│  Mgmt UI   (Port 15672)  │   │  Zookeeper    (2181)   │
└──────────────────────────┘   └────────────────────────┘
```

### Tech Stack

#### Frontend
- **React 19** — UI Framework
- **Vite** — Build Tool
- **Tailwind CSS** — Styling
- **React Router v7** — Navigation
- **Axios** — HTTP Client
- **STOMP.js + SockJS** — WebSocket Communication
- **Context API** — Auth State Management

#### Backend
- **Spring Boot 3.2** — Application Framework
- **Spring WebFlux** — Reactive programming (AI, Tax, Credit Card services)
- **Spring Cloud Gateway** — API Gateway + JWT filter
- **Spring Cloud Netflix Eureka** — Service Discovery
- **Spring AMQP** — RabbitMQ integration with DLQ
- **Spring Kafka** — Kafka producer/consumer (News Service)
- **R2DBC + Spring Data Relational** — Non-blocking DB access
- **Spring Data JPA** — Blocking DB access (Auth, User, News)
- **JWT (jjwt 0.12.3)** — Token-based authentication

#### AI & External
- **Google Gemini 2.5 Flash** — Tax optimisation + Crypto advice + Credit card analysis

#### Infrastructure
- **PostgreSQL 16** — Primary database (6 separate databases)
- **RabbitMQ 3.13** — Async message queue with Dead Letter Queue support
- **Apache Kafka 7.6** — Real-time event streaming for crypto news/prices
- **Docker Compose** — One-command full-stack startup

---

## 🚀 Getting Started

### ⚡ Quickstart with Docker (Recommended)

The easiest way to run the entire stack — no manual setup needed.

**Prerequisites:** [Docker Desktop](https://www.docker.com/products/docker-desktop/) installed and running.

```bash
# 1. Clone the repository
git clone https://github.com/AtharvGhumtane/Finance-Adviser.git
cd Finance-Adviser

# 2. Copy and configure environment variables
cp .env.example .env
# Edit .env and fill in your GEMINI_API_KEY

# 3. Start everything (first time — builds all images, takes ~5–10 min)
docker compose up --build

# 4. Subsequent starts (uses cached images, much faster)
docker compose up
```

**That's it!** All 13 services start automatically in the correct order.

| Service | URL |
|---|---|
| 🌐 Frontend | http://localhost:5173 |
| 🔀 API Gateway | http://localhost:5051 |
| 🔍 Eureka Dashboard | http://localhost:8761 |
| 🐰 RabbitMQ Management | http://localhost:15672 (guest/guest) |

```bash
# Run in background (detached)
docker compose up -d --build

# View logs for a specific service
docker compose logs -f auth-service

# Stop everything (keeps database data)
docker compose down

# Stop everything AND wipe all databases (clean slate)
docker compose down -v
```

---

### 🛠️ Manual Setup (Without Docker)

**Prerequisites:**
- Node.js 18+
- Java 21+
- Maven
- PostgreSQL 16 (running on port `2526`)
- RabbitMQ (running on port `5672`)
- Apache Kafka + Zookeeper (running on port `9092`)
- Gemini API Key → [Get one here](https://aistudio.google.com/)

#### 1. Create PostgreSQL Databases

```sql
CREATE DATABASE "AlexzAuth";
CREATE DATABASE "Alexz";
CREATE DATABASE "AlexzAiRecomm";
CREATE DATABASE crypto_news_db;
CREATE DATABASE tax_Alexz;
CREATE DATABASE credit_card_db;
```

#### 2. Set Environment Variables

```bash
export GEMINI_API_KEY=your_gemini_api_key_here
export JWT_SECRET=your_jwt_secret_here
```

#### 3. Start Backend Services (in order)

```bash
# 1. Eureka Server — start first
cd eureka && mvn spring-boot:run

# 2. Auth Service
cd auth-serviceAlex && mvn spring-boot:run

# 3. User Service
cd user-serviceAlex && mvn spring-boot:run

# 4. AI Recommendation Service
cd ai-serviceAlex && mvn spring-boot:run

# 5. Crypto News Service
cd cryptonewsAlexz && mvn spring-boot:run

# 6. Tax Optimizer Service
cd tax-optimizerAlexz && mvn spring-boot:run

# 7. Credit Card Service
cd credit-card-service && mvn spring-boot:run

# 8. API Gateway — start last
cd api-gatewayAlexz && mvn spring-boot:run
```

#### 4. Start Frontend

```bash
cd crypto-adviser-frontend
npm install
npm run dev
```

App runs at `http://localhost:5173`

---

## ⚙️ Environment Variables

Copy `.env.example` to `.env` and fill in your values:

```env
# PostgreSQL
POSTGRES_USER=postgres
POSTGRES_PASSWORD=2526

# JWT Secret (shared between auth-service and api-gateway)
JWT_SECRET=your_jwt_secret_here

# Gemini AI — get yours at https://aistudio.google.com/
GEMINI_API_KEY=your_gemini_api_key_here
```

---

## 📡 API Endpoints

All endpoints are routed through the **API Gateway** at `http://localhost:5051`.

### Auth
```
POST /api/auth/signup         — Register new user
POST /api/auth/login          — Login user
GET  /api/auth/validate       — Validate JWT token
```

### User Management
```
GET  /api/users/profile       — Get user profile
PUT  /api/users/profile       — Update user profile
```

### Tax Optimizer
```
POST /api/v1/tax/optimize             — Full AI analysis + async RabbitMQ save
POST /api/v1/tax/compare-regimes      — Quick Old vs New Regime comparison
POST /api/v1/tax/hra-exemption        — HRA exemption calculator
GET  /api/v1/tax/health               — Health check
```

### Credit Card
```
POST /api/v1/credit/quick-check       — Trap detection only (no AI)
POST /api/v1/credit/risk              — ML risk classification
POST /api/v1/credit/analyze           — Full AI analysis
```

### AI Crypto Recommendations
```
POST /api/v1/recommendations/generate          — Generate recommendation (sync)
POST /api/v1/recommendations/generate-async    — Generate recommendation (async)
GET  /api/v1/recommendations/history           — Get user recommendation history
GET  /api/v1/recommendations/history/{crypto}  — History by cryptocurrency
GET  /api/v1/recommendations/recent?days=30    — Recent recommendations
```

### Crypto News & Prices
```
GET  /api/news/latest?limit=20        — Get latest news
GET  /api/prices/all                  — Get all live prices
WS   /ws/crypto-news                  — WebSocket endpoint
     /topic/crypto-news               — Live news topic
     /topic/crypto-prices             — Live prices topic
```

---

## 🗂️ Project Structure

```
Finance-Adviser/
├── docker-compose.yml               # One-command full-stack startup
├── docker/
│   └── init-db.sql                  # Auto-creates all 6 PostgreSQL databases
├── .env.example                     # Environment variable template
│
├── eureka/                          # Eureka Service Discovery (Port 8761)
├── api-gatewayAlexz/                # API Gateway + JWT Filter (Port 5051)
├── auth-serviceAlex/                # Authentication Service (Port 5054)
├── user-serviceAlex/                # User Management Service (Port 5053)
├── ai-serviceAlex/                  # AI Crypto Recommendation (Port 5055)
├── cryptonewsAlexz/                 # Crypto News + Prices via Kafka (Port 5056)
├── tax-optimizerAlexz/              # Tax Optimization Service (Port 5057)
├── credit-card-service/             # Credit Card Trap Analyser (Port 5058)
│
└── crypto-adviser-frontend/         # React + Vite Frontend (Port 5173)
    └── src/
        ├── components/              # Navbar, Ticker, NewsFeed, InfoModal
        ├── pages/                   # Dashboard, Tax, Credit, History, Login, Signup
        ├── context/                 # AuthContext
        ├── hooks/                   # useWebSocket
        └── services/                # api.js
```

---

## 👨‍💻 Author

- **Atharv Ghumtane** — [github.com/AtharvGhumtane](https://github.com/AtharvGhumtane)

## 🙏 Acknowledgments

- [Google Gemini](https://deepmind.google/technologies/gemini/) for AI capabilities
- [Spring Boot](https://spring.io/projects/spring-boot) for backend framework
- [React](https://reactjs.org/) for frontend framework
- [RabbitMQ](https://www.rabbitmq.com/) for async messaging
- [Apache Kafka](https://kafka.apache.org/) for real-time event streaming
- [CoinGecko](https://www.coingecko.com/) for cryptocurrency price data

---

> ⚠️ Not financial advice. For educational and demonstration purposes only.
