-- ============================================================
-- PostgreSQL Initialization Script
-- Creates all databases needed by the microservices
-- This script runs automatically when the postgres container
-- starts for the first time.
-- ============================================================

-- Auth Service database
CREATE DATABASE "AlexzAuth";

-- User Service database
CREATE DATABASE "Alexz";

-- AI Recommendation Service database
CREATE DATABASE "AlexzAiRecomm";

-- Crypto News Service database
CREATE DATABASE "crypto_news_db";

-- Tax Optimizer Service database
CREATE DATABASE "tax_Alexz";

-- Credit Card Service database
CREATE DATABASE "credit_card_db";

-- Connect and create tables for AI Recommendation Service
\c "AlexzAiRecomm"

CREATE TABLE IF NOT EXISTS recommendations (
    id BIGSERIAL PRIMARY KEY,
    user_id UUID NOT NULL,
    target_cryptocurrency VARCHAR(10) NOT NULL,
    annual_income NUMERIC(20, 2) NOT NULL,
    risk_tolerance INTEGER NOT NULL,
    investment_horizon VARCHAR(20) NOT NULL,
    recommendation_text TEXT NOT NULL,
    confidence_score DOUBLE PRECISION NOT NULL,
    risk_assessment TEXT NOT NULL,
    processing_time_ms BIGINT,
    ai_model_version VARCHAR(50),
    created_at TIMESTAMP NOT NULL
);

-- Connect and create tables for Tax Optimizer Service
\c "tax_Alexz"

CREATE TABLE IF NOT EXISTS tax_profiles (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    age INTEGER NOT NULL,
    dependents INTEGER NOT NULL,
    gross_salary DOUBLE PRECISION NOT NULL,
    basic_salary DOUBLE PRECISION NOT NULL,
    hra DOUBLE PRECISION NOT NULL,
    da DOUBLE PRECISION NOT NULL,
    special_allowance DOUBLE PRECISION NOT NULL,
    other_income DOUBLE PRECISION NOT NULL,
    rent_paid DOUBLE PRECISION NOT NULL,
    city_type VARCHAR(20) NOT NULL,
    section_80c DOUBLE PRECISION NOT NULL,
    section_80d DOUBLE PRECISION NOT NULL,
    section_80d_parents DOUBLE PRECISION NOT NULL,
    section_80ccd1b DOUBLE PRECISION NOT NULL,
    section_80eea DOUBLE PRECISION NOT NULL,
    section_80g DOUBLE PRECISION NOT NULL,
    section_80tta DOUBLE PRECISION NOT NULL,
    home_loan_interest DOUBLE PRECISION NOT NULL,
    home_loan_principal DOUBLE PRECISION NOT NULL,
    risk_appetite VARCHAR(20) NOT NULL,
    liquidity_need VARCHAR(20) NOT NULL,
    created_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS tax_recommendations (
    id BIGSERIAL PRIMARY KEY,
    profile_id BIGINT NOT NULL REFERENCES tax_profiles(id) ON DELETE CASCADE,
    user_id VARCHAR(255) NOT NULL,
    recommended_regime VARCHAR(50) NOT NULL,
    old_regime_tax DOUBLE PRECISION NOT NULL,
    new_regime_tax DOUBLE PRECISION NOT NULL,
    tax_savings_regime DOUBLE PRECISION NOT NULL,
    potential_savings DOUBLE PRECISION NOT NULL,
    ai_recommendation TEXT NOT NULL,
    strategies_json TEXT NOT NULL,
    financial_year VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP
);

-- Connect and create tables for Credit Card Service
\c "credit_card_db"

CREATE TABLE IF NOT EXISTS credit_profiles (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    monthly_income DOUBLE PRECISION NOT NULL,
    monthly_expenses DOUBLE PRECISION NOT NULL,
    total_credit_limit DOUBLE PRECISION NOT NULL,
    total_outstanding_balance DOUBLE PRECISION NOT NULL,
    number_of_cards INTEGER NOT NULL,
    credit_score INTEGER NOT NULL,
    pays_minimum_only BOOLEAN NOT NULL,
    late_payments_last_year INTEGER NOT NULL,
    missed_payments_last_year INTEGER NOT NULL,
    total_emi_per_month DOUBLE PRECISION NOT NULL,
    number_of_active_emis INTEGER NOT NULL,
    cash_advance_amount DOUBLE PRECISION NOT NULL,
    cash_advance_frequency INTEGER NOT NULL,
    annual_interest_rate DOUBLE PRECISION NOT NULL,
    late_payment_fee DOUBLE PRECISION NOT NULL,
    other_loan_emi DOUBLE PRECISION NOT NULL,
    created_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS credit_analyses (
    id BIGSERIAL PRIMARY KEY,
    profile_id BIGINT NOT NULL REFERENCES credit_profiles(id) ON DELETE CASCADE,
    user_id VARCHAR(255) NOT NULL,
    risk_level VARCHAR(50) NOT NULL,
    risk_score INTEGER NOT NULL,
    risk_reasoning TEXT NOT NULL,
    traps_detected_count INTEGER NOT NULL,
    traps_json TEXT NOT NULL,
    credit_utilization_pct DOUBLE PRECISION NOT NULL,
    debt_to_income_ratio DOUBLE PRECISION NOT NULL,
    emi_burden_ratio DOUBLE PRECISION NOT NULL,
    estimated_annual_interest DOUBLE PRECISION NOT NULL,
    free_cash_flow DOUBLE PRECISION NOT NULL,
    ai_recommendation TEXT NOT NULL,
    ai_tips_json TEXT NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP
);

-- Connect and create tables for Crypto News Service
\c "crypto_news_db"

CREATE TABLE IF NOT EXISTS crypto_news (
    id BIGSERIAL PRIMARY KEY,
    news_id VARCHAR(255) NOT NULL UNIQUE,
    title VARCHAR(500) NOT NULL,
    body TEXT,
    image_url VARCHAR(1000),
    source VARCHAR(255) NOT NULL,
    source_url TEXT,
    published_at TIMESTAMP,
    related_cryptos VARCHAR(500),
    category VARCHAR(100),
    created_at TIMESTAMP DEFAULT NOW()
);

-- Seed mock news so the frontend always has content even when external APIs are down
INSERT INTO crypto_news (news_id, title, body, image_url, source, source_url, published_at, related_cryptos, category)
SELECT * FROM (VALUES
    ('MOCK_001', 'Bitcoin Surges Past $63,000 as Institutional Demand Grows', 'Bitcoin has rallied strongly this week, breaking past the $63,000 level as major institutions continue to accumulate. Analysts point to growing ETF inflows as key drivers.', 'https://images.unsplash.com/photo-1516245834210-c4c142787335?w=500', 'CryptoNews', 'https://cryptonews.com', NOW() - INTERVAL '1 hour', 'BTC', 'NEWS'),
    ('MOCK_002', 'Ethereum Layer-2 Ecosystem Sees Record Transaction Volumes', 'Ethereum layer-2 networks including Arbitrum and Optimism have recorded their highest ever transaction volumes, with total value locked crossing $40 billion.', 'https://images.unsplash.com/photo-1622630998477-20aa696ecb05?w=500', 'Decrypt', 'https://decrypt.co', NOW() - INTERVAL '2 hours', 'ETH', 'NEWS'),
    ('MOCK_003', 'Solana DeFi Activity Reaches All-Time High', 'Solanas decentralized finance ecosystem has hit new all-time highs with over $8 billion in TVL and daily DEX volumes surpassing $1.5 billion.', 'https://images.unsplash.com/photo-1639762681485-074b7f938ba0?w=500', 'CoinDesk', 'https://coindesk.com', NOW() - INTERVAL '3 hours', 'SOL', 'NEWS'),
    ('MOCK_004', 'SEC Approves New Spot Bitcoin ETFs, Market Reacts Positively', 'The US SEC has given the green light to several new spot Bitcoin ETF applications. The approval is expected to bring billions of new institutional capital into crypto.', 'https://images.unsplash.com/photo-1589829545856-d10d557cf95f?w=500', 'The Block', 'https://theblock.co', NOW() - INTERVAL '4 hours', 'BTC', 'REGULATION'),
    ('MOCK_005', 'Cardano Smart Contract Adoption Grows 300% Year Over Year', 'Cardano has seen a dramatic increase in smart contract activity, with the number of deployed Plutus scripts growing 300% compared to the same period last year.', 'https://images.unsplash.com/photo-1621761191319-c6fb62004040?w=500', 'CryptoNews', 'https://cryptonews.com', NOW() - INTERVAL '5 hours', 'ADA', 'NEWS'),
    ('MOCK_006', 'Polkadot Parachain Auction Results in Record Bids', 'The latest Polkadot parachain auction concluded with record-breaking DOT bids, signaling strong developer and community confidence in the ecosystem.', 'https://images.unsplash.com/photo-1605792657660-596af9009e82?w=500', 'Decrypt', 'https://decrypt.co', NOW() - INTERVAL '6 hours', 'DOT', 'NEWS'),
    ('MOCK_007', 'Global Crypto Market Cap Approaches $2.5 Trillion Milestone', 'The total cryptocurrency market capitalization is approaching the $2.5 trillion mark as Bitcoin and altcoins continue their upward trajectory.', 'https://images.unsplash.com/photo-1590283603385-17ffb3a7f29f?w=500', 'CoinMarketCap', 'https://coinmarketcap.com', NOW() - INTERVAL '7 hours', 'BTC,ETH', 'MARKET'),
    ('MOCK_008', 'DeFi Protocol Aave Launches New Risk Management Features', 'Leading decentralized lending protocol Aave has introduced advanced risk management features including dynamic liquidation thresholds and improved oracle integrations.', 'https://images.unsplash.com/photo-1639762681057-408e52192e55?w=500', 'The Block', 'https://theblock.co', NOW() - INTERVAL '8 hours', 'ETH', 'DEFI'),
    ('MOCK_009', 'Crypto Adoption in Emerging Markets Surges as Dollar Weakens', 'Cryptocurrency adoption in emerging markets including Latin America, Africa, and Southeast Asia continues to accelerate as local currencies face inflation pressures.', 'https://images.unsplash.com/photo-1526304640581-d334cdbbf45e?w=500', 'CoinDesk', 'https://coindesk.com', NOW() - INTERVAL '10 hours', 'BTC', 'ADOPTION'),
    ('MOCK_010', 'NFT Market Shows Signs of Recovery with Blue-Chip Collections Rising', 'The NFT market is showing signs of recovery, with blue-chip collections like CryptoPunks and Bored Ape Yacht Club seeing significant price increases.', 'https://images.unsplash.com/photo-1620641788421-7a1c342ea42e?w=500', 'NFT Plazas', 'https://nftplazas.com', NOW() - INTERVAL '12 hours', 'ETH', 'NFT')
) AS t(news_id, title, body, image_url, source, source_url, published_at, related_cryptos, category)
WHERE NOT EXISTS (SELECT 1 FROM crypto_news WHERE crypto_news.news_id = t.news_id);

