import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import Navbar from '../components/Navbar';
import { LiveNewsFeed } from '../components/LiveNewsFeed';
import { CryptoPriceTicker } from '../components/CryptoPriceTicker';
import {
  Sparkles, TrendingUp, ArrowRight,
  Calculator, CreditCard, Brain
} from 'lucide-react';

// ─── Service Card ─────────────────────────────────────────────────────────────
const ServiceCard = ({ to, icon, title, description, badge, accent = 'yellow' }) => {
  const accents = {
    yellow: {
      icon: 'from-yellow-400 to-yellow-600',
      shadow: 'shadow-yellow-500/20',
      border: 'hover:border-yellow-500/40',
      badge: 'bg-yellow-500/10 text-yellow-400 border-yellow-500/20',
      arrow: 'text-yellow-400',
      glow: 'group-hover:shadow-yellow-500/10',
    },
    blue: {
      icon: 'from-blue-400 to-blue-600',
      shadow: 'shadow-blue-500/20',
      border: 'hover:border-blue-500/40',
      badge: 'bg-blue-500/10 text-blue-400 border-blue-500/20',
      arrow: 'text-blue-400',
      glow: 'group-hover:shadow-blue-500/10',
    },
    emerald: {
      icon: 'from-emerald-400 to-emerald-600',
      shadow: 'shadow-emerald-500/20',
      border: 'hover:border-emerald-500/40',
      badge: 'bg-emerald-500/10 text-emerald-400 border-emerald-500/20',
      arrow: 'text-emerald-400',
      glow: 'group-hover:shadow-emerald-500/10',
    },
  };
  const a = accents[accent];

  return (
    <Link
      to={to}
      className={`group flex items-center gap-5 bg-[#141824] rounded-2xl border border-white/8 p-5 transition-all duration-300 ${a.border} hover:shadow-xl ${a.glow} hover:-translate-y-0.5`}
    >
      {/* Icon */}
      <div className={`w-14 h-14 flex-shrink-0 rounded-xl bg-gradient-to-br ${a.icon} flex items-center justify-center shadow-lg ${a.shadow} group-hover:scale-105 transition-transform duration-300`}>
        {icon}
      </div>

      {/* Text */}
      <div className="flex-1 min-w-0">
        <div className="flex items-center gap-2 mb-1">
          <h3 className="text-white font-bold text-base">{title}</h3>
          {badge && (
            <span className={`text-[10px] font-bold px-2 py-0.5 rounded-full border ${a.badge}`}>
              {badge}
            </span>
          )}
        </div>
        <p className="text-gray-500 text-xs leading-relaxed truncate">{description}</p>
      </div>

      {/* Arrow */}
      <ArrowRight className={`w-5 h-5 flex-shrink-0 ${a.arrow} opacity-50 group-hover:opacity-100 group-hover:translate-x-1 transition-all duration-300`} />
    </Link>
  );
};

// ─── Dashboard ────────────────────────────────────────────────────────────────
const Dashboard = () => {
  const { user } = useAuth();

  return (
    <div className="min-h-screen bg-[#0A0E27]">
      <Navbar />
      <CryptoPriceTicker />

      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">

        {/* ── Welcome Header ──────────────────────────────────────────────── */}
        <div className="mb-6">
          <h1 className="text-3xl font-bold text-white">
            Welcome back,{' '}
            <span className="bg-gradient-to-r from-yellow-400 to-yellow-200 bg-clip-text text-transparent">
              {user?.username}
            </span>{' '}
            👋
          </h1>
          <p className="text-gray-500 mt-1 text-sm">
            Live crypto news, AI-powered tax optimization, and smart financial tools — all in one place.
          </p>
        </div>

        {/* ── Main 2-column layout ─────────────────────────────────────────── */}
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">

          {/* LEFT — News Feed (2/3 width) */}
          <div className="lg:col-span-2">
            <LiveNewsFeed />
          </div>

          {/* RIGHT — Services (1/3 width) */}
          {/* RIGHT — Services (1/3 width) */}
          <div className="flex flex-col gap-4 sticky top-24 self-start">

            {/* Section Label */}
            <div className="flex items-center gap-2">
              <Sparkles className="w-4 h-4 text-yellow-400" />
              <span className="text-xs font-bold text-gray-400 uppercase tracking-widest">AI Services</span>
            </div>

            {/* Card 1 — Crypto Recommendation */}
            <ServiceCard
              to="/recommendations"
              icon={<Brain className="w-7 h-7 text-black" />}
              title="Crypto Adviser"
              description="Personalized AI investment recommendations based on your risk profile"
              badge="AI"
              accent="yellow"
            />

            {/* Card 2 — Tax Optimizer */}
            <ServiceCard
              to="/tax"
              icon={<Calculator className="w-7 h-7 text-black" />}
              title="Tax Optimizer"
              description="Compare Old vs New regime, HRA exemptions & AI tax-saving tips"
              badge="FY 2024-25"
              accent="blue"
            />

            {/* Card 3 — Credit Card */}
            <ServiceCard
              to="/credit"
              icon={<CreditCard className="w-7 h-7 text-black" />}
              title="Credit Card Analyser"
              description="Detect hidden debt traps, ML risk scoring & CIBIL impact analysis"
              badge="6 Traps"
              accent="emerald"
            />

            {/* History quick link */}
            <Link
              to="/history"
              className="flex items-center justify-between px-5 py-3.5 bg-white/3 hover:bg-white/6 border border-white/8 hover:border-white/15 rounded-xl transition-all group"
            >
              <div className="flex items-center gap-3">
                <TrendingUp className="w-4 h-4 text-gray-500" />
                <span className="text-sm text-gray-400 font-medium">Past Recommendations</span>
              </div>
              <ArrowRight className="w-4 h-4 text-gray-600 group-hover:text-gray-400 group-hover:translate-x-1 transition-all" />
            </Link>

            {/* Quick Stats */}
            

          </div>
        </div>
      </div>
    </div>
  );
};

export default Dashboard;