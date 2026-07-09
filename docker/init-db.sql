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
