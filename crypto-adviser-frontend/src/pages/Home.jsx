import { Link } from 'react-router-dom';
import {
  TrendingUp, ArrowRight, Brain, Calculator,
  CreditCard, Newspaper, Github, Mail, Phone,
  Instagram, Facebook, Linkedin, Sparkles, ChevronDown
} from 'lucide-react';

// ─── Inline styles for custom animations ─────────────────────────────────────
const styles = `
  @import url('https://fonts.googleapis.com/css2?family=Syne:wght@400;700;800&family=DM+Sans:wght@300;400;500&display=swap');

  .home-root { font-family: 'DM Sans', sans-serif; }
  .display-font { font-family: 'Syne', sans-serif; }

  @keyframes float {
    0%, 100% { transform: translateY(0px) rotate(0deg); }
    33%       { transform: translateY(-18px) rotate(1deg); }
    66%       { transform: translateY(-8px) rotate(-1deg); }
  }
  @keyframes drift {
    0%, 100% { transform: translateX(0px); }
    50%       { transform: translateX(30px); }
  }
  @keyframes gridMove {
    0%   { background-position: 0 0; }
    100% { background-position: 60px 60px; }
  }
  @keyframes fadeUp {
    from { opacity: 0; transform: translateY(30px); }
    to   { opacity: 1; transform: translateY(0); }
  }
  @keyframes glowPulse {
    0%, 100% { box-shadow: 0 0 20px rgba(234,179,8,0.3), 0 0 60px rgba(234,179,8,0.1); }
    50%       { box-shadow: 0 0 40px rgba(234,179,8,0.6), 0 0 100px rgba(234,179,8,0.2); }
  }
  @keyframes scanline {
    0%   { transform: translateY(-100%); }
    100% { transform: translateY(100vh); }
  }
  @keyframes ticker {
    0%   { transform: translateX(0); }
    100% { transform: translateX(-50%); }
  }

  .float-1 { animation: float 7s ease-in-out infinite; }
  .float-2 { animation: float 9s ease-in-out infinite 2s; }
  .float-3 { animation: drift 12s ease-in-out infinite; }

  .fade-up-1 { animation: fadeUp 0.8s ease forwards; }
  .fade-up-2 { animation: fadeUp 0.8s ease 0.15s forwards; opacity: 0; }
  .fade-up-3 { animation: fadeUp 0.8s ease 0.3s forwards; opacity: 0; }
  .fade-up-4 { animation: fadeUp 0.8s ease 0.45s forwards; opacity: 0; }
  .fade-up-5 { animation: fadeUp 0.8s ease 0.6s forwards; opacity: 0; }

  .glow-btn { animation: glowPulse 3s ease-in-out infinite; }

  .grid-bg {
    background-image: linear-gradient(rgba(234,179,8,0.04) 1px, transparent 1px),
                      linear-gradient(90deg, rgba(234,179,8,0.04) 1px, transparent 1px);
    background-size: 60px 60px;
    animation: gridMove 8s linear infinite;
  }

  .service-card:hover .service-icon { transform: scale(1.1) rotate(-3deg); }
  .service-icon { transition: transform 0.3s ease; }

  .social-link:hover svg { transform: scale(1.2); }
  .social-link svg { transition: transform 0.2s ease; }

  .ticker-track { animation: ticker 30s linear infinite; }
  .ticker-track:hover { animation-play-state: paused; }
`;

// ─── Service data ─────────────────────────────────────────────────────────────
const SERVICES = [
  {
    icon: <Brain className="w-6 h-6 text-black" />,
    gradient: 'from-yellow-400 to-amber-500',
    shadow: 'shadow-yellow-500/25',
    tag: 'GEMINI AI',
    title: 'Crypto Adviser',
    desc: 'Personalised BTC, ETH, SOL investment strategies powered by Gemini AI — tailored to your risk profile and investment horizon.',
  },
  {
    icon: <Calculator className="w-6 h-6 text-black" />,
    gradient: 'from-blue-400 to-cyan-500',
    shadow: 'shadow-blue-500/25',
    tag: 'FY 2024-25',
    title: 'Tax Optimizer',
    desc: 'Old vs New Regime comparison, HRA exemption calculator, 80C/80D deductions — with AI investment tips to slash your tax bill.',
  },
  {
    icon: <CreditCard className="w-6 h-6 text-black" />,
    gradient: 'from-emerald-400 to-green-500',
    shadow: 'shadow-emerald-500/25',
    tag: '6 TRAPS',
    title: 'Credit Analyser',
    desc: 'ML-powered debt trap detection, CIBIL impact scoring, and actionable steps to escape the minimum payment cycle for good.',
  },
  {
    icon: <Newspaper className="w-6 h-6 text-black" />,
    gradient: 'from-purple-400 to-violet-500',
    shadow: 'shadow-purple-500/25',
    tag: 'LIVE',
    title: 'Crypto News',
    desc: 'Real-time crypto news streamed via WebSocket — filtered by coin, sourced from top publications, updated every minute.',
  },
];

// ─── Ticker items ─────────────────────────────────────────────────────────────
const TICKER = [
  'Gemini AI · Tax Optimisation',
  'Live Crypto Prices · WebSocket',
  'Old vs New Regime · FY 2024-25',
  'Credit Trap Detection · ML Risk',
  'Section 80C · 80D · NPS · HRA',
  'BTC · ETH · SOL · ADA · XRP',
];

// ─── Social links ─────────────────────────────────────────────────────────────
const SOCIALS = [
  { icon: <Github className="w-5 h-5" />,    label: 'GitHub',    href: '#' },
  { icon: <Linkedin className="w-5 h-5" />,  label: 'LinkedIn',  href: '#' },
  { icon: <Instagram className="w-5 h-5" />, label: 'Instagram', href: '#' },
  { icon: <Facebook className="w-5 h-5" />,  label: 'Facebook',  href: '#' },
  { icon: <Mail className="w-5 h-5" />,      label: 'Email',     href: '#' },
  { icon: <Phone className="w-5 h-5" />,     label: 'Phone',     href: '#' },
];

// ─── Component ────────────────────────────────────────────────────────────────
const Home = () => {
  return (
    <div className="home-root min-h-screen bg-[#07090F] text-white relative overflow-x-hidden">
      <style>{styles}</style>

      {/* ── Animated grid background ── */}
      <div className="grid-bg fixed inset-0 pointer-events-none opacity-60" />

      {/* ── Floating orbs ── */}
      <div className="fixed inset-0 pointer-events-none overflow-hidden">
        <div className="float-1 absolute top-20 left-[10%] w-72 h-72 bg-yellow-500/8 rounded-full blur-3xl" />
        <div className="float-2 absolute top-[40%] right-[8%] w-96 h-96 bg-blue-500/6 rounded-full blur-3xl" />
        <div className="float-3 absolute bottom-[20%] left-[30%] w-64 h-64 bg-emerald-500/5 rounded-full blur-3xl" />
      </div>

      {/* ── Scanline effect ── */}
      <div
        className="fixed inset-0 pointer-events-none"
        style={{
          background: 'repeating-linear-gradient(0deg, transparent, transparent 2px, rgba(0,0,0,0.015) 2px, rgba(0,0,0,0.015) 4px)',
        }}
      />

      {/* ════════════════════════════════════════════
          NAVBAR
      ════════════════════════════════════════════ */}
      <nav className="relative z-50 px-6 py-5 border-b border-white/5">
        <div className="max-w-7xl mx-auto flex justify-between items-center">
          <div className="flex items-center gap-3">
            <div className="w-11 h-11 rounded-xl bg-gradient-to-br from-yellow-400 to-yellow-600 flex items-center justify-center shadow-lg shadow-yellow-500/30">
              <TrendingUp className="w-5 h-5 text-black" />
            </div>
            <div>
              <span className="display-font text-xl font-bold bg-gradient-to-r from-yellow-400 to-yellow-200 bg-clip-text text-transparent">
                FinAdvisor
              </span>
              <p className="text-[10px] text-gray-500 -mt-0.5 tracking-widest uppercase">AI Finance Platform</p>
            </div>
          </div>

          <div className="flex items-center gap-3">
            <Link to="/login"
              className="px-5 py-2 text-gray-400 hover:text-yellow-400 transition text-sm font-medium">
              Sign In
            </Link>
            <Link to="/signup"
              className="glow-btn px-6 py-2.5 bg-gradient-to-r from-yellow-400 to-yellow-500 text-black rounded-xl font-bold text-sm hover:from-yellow-300 hover:to-yellow-400 transition-all">
              Get Started →
            </Link>
          </div>
        </div>
      </nav>

      {/* ════════════════════════════════════════════
          HERO
      ════════════════════════════════════════════ */}
      <section className="relative z-10 max-w-7xl mx-auto px-6 pt-24 pb-20 text-center">

        {/* Badge */}
        <div className="fade-up-1 inline-flex items-center gap-2 px-4 py-2 bg-yellow-500/8 border border-yellow-500/20 rounded-full mb-8">
          <Sparkles className="w-3.5 h-3.5 text-yellow-400" />
          <span className="text-yellow-400 text-xs font-semibold tracking-widest uppercase">
            Powered by AI
          </span>
        </div>

        {/* Headline */}
        <h1 className="display-font fade-up-2 text-5xl md:text-7xl lg:text-8xl font-extrabold leading-[1.05] mb-6">
          <span className="text-white">Your Money.</span>
          <br />
          <span
            className="bg-gradient-to-r from-yellow-300 via-yellow-400 to-amber-500 bg-clip-text text-transparent"
            style={{ WebkitTextStroke: '1px transparent' }}
          >
            Optimised.
          </span>
        </h1>

        {/* Subheadline */}
        <p className="fade-up-3 text-gray-400 text-lg md:text-xl max-w-2xl mx-auto leading-relaxed mb-12">
          AI-powered crypto recommendations, Indian tax optimisation, and credit card trap detection — 
          all in one platform built for the modern investor.
        </p>

        {/* CTAs */}
        <div className="fade-up-4 flex items-center justify-center gap-4 flex-wrap">
          <Link to="/signup"
            className="group px-8 py-4 bg-gradient-to-r from-yellow-400 to-amber-500 text-black rounded-xl font-bold text-base hover:from-yellow-300 hover:to-amber-400 transition-all shadow-2xl shadow-yellow-500/30 flex items-center gap-2">
            Start for Free
            <ArrowRight className="w-4 h-4 group-hover:translate-x-1 transition-transform" />
          </Link>
          <Link to="/login"
            className="px-8 py-4 bg-white/4 border border-white/10 text-gray-300 rounded-xl font-medium text-base hover:bg-white/8 hover:border-white/20 hover:text-white transition-all">
            Sign In
          </Link>
        </div>

        {/* Scroll hint */}
        <div className="fade-up-5 mt-20 flex flex-col items-center gap-2 text-gray-600">
          <span className="text-xs tracking-widest uppercase">Explore</span>
          <ChevronDown className="w-4 h-4 animate-bounce" />
        </div>
      </section>

      {/* ════════════════════════════════════════════
          TICKER TAPE
      ════════════════════════════════════════════ */}
      <div className="relative z-10 border-y border-white/5 bg-white/2 py-3 overflow-hidden">
        <div className="ticker-track flex gap-12 whitespace-nowrap" style={{ width: 'max-content' }}>
          {[...TICKER, ...TICKER].map((item, i) => (
            <span key={i} className="text-xs font-medium text-gray-500 flex items-center gap-3">
              <span className="w-1 h-1 rounded-full bg-yellow-500 inline-block" />
              {item}
            </span>
          ))}
        </div>
      </div>

      {/* ════════════════════════════════════════════
          SERVICES
      ════════════════════════════════════════════ */}
      <section className="relative z-10 max-w-7xl mx-auto px-6 py-28">
        <div className="text-center mb-16">
          <p className="text-xs text-yellow-500 font-bold tracking-widest uppercase mb-3">What We Offer</p>
          <h2 className="display-font text-4xl md:text-5xl font-extrabold text-white">
            Four Tools. One Platform.
          </h2>
        </div>

        <div className="grid md:grid-cols-2 gap-5">
          {SERVICES.map((s, i) => (
            <div
              key={i}
              className="service-card group relative bg-[#0D1117] border border-white/6 rounded-2xl p-7 hover:border-white/15 transition-all duration-300 overflow-hidden"
            >
              {/* Hover glow */}
              <div className="absolute inset-0 bg-gradient-to-br from-white/2 to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-500 rounded-2xl" />

              <div className="relative flex items-start gap-5">
                {/* Icon */}
                <div className={`service-icon w-12 h-12 flex-shrink-0 rounded-xl bg-gradient-to-br ${s.gradient} flex items-center justify-center shadow-lg ${s.shadow}`}>
                  {s.icon}
                </div>

                <div className="flex-1">
                  <div className="flex items-center gap-2 mb-2">
                    <h3 className="display-font text-lg font-bold text-white">{s.title}</h3>
                    <span className="text-[10px] font-bold text-yellow-500 bg-yellow-500/10 border border-yellow-500/20 px-2 py-0.5 rounded-full tracking-wider">
                      {s.tag}
                    </span>
                  </div>
                  <p className="text-gray-500 text-sm leading-relaxed">{s.desc}</p>
                </div>
              </div>
            </div>
          ))}
        </div>
      </section>

      {/* ════════════════════════════════════════════
          HOW IT WORKS
      ════════════════════════════════════════════ */}
      <section className="relative z-10 border-y border-white/5 bg-white/1 py-28">
        <div className="max-w-7xl mx-auto px-6">
          <div className="text-center mb-16">
            <p className="text-xs text-yellow-500 font-bold tracking-widest uppercase mb-3">Simple Process</p>
            <h2 className="display-font text-4xl md:text-5xl font-extrabold text-white">
              Up & Running in Minutes
            </h2>
          </div>

          <div className="grid md:grid-cols-3 gap-6 relative">
            {/* connector line */}
            <div className="hidden md:block absolute top-10 left-[33%] right-[33%] h-px bg-gradient-to-r from-transparent via-yellow-500/30 to-transparent" />

            {[
              { step: '01', title: 'Create Account', desc: 'Sign up in seconds — no credit card, no verification delays. Just an email and password.' },
              { step: '02', title: 'Enter Your Profile', desc: 'Fill in your income, investments, or credit details. Every field has an explanation in our Guide.' },
              { step: '03', title: 'Get AI Insights', desc: 'Receive Gemini-powered recommendations, tax comparisons, and risk reports — instantly.' },
            ].map((s) => (
              <div key={s.step} className="relative text-center px-4">
                <div className="display-font text-6xl font-extrabold text-white/4 mb-4 leading-none">{s.step}</div>
                <h3 className="display-font text-xl font-bold text-white mb-3">{s.title}</h3>
                <p className="text-gray-500 text-sm leading-relaxed">{s.desc}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* ════════════════════════════════════════════
          CTA BANNER
      ════════════════════════════════════════════ */}
      <section className="relative z-10 max-w-7xl mx-auto px-6 py-28">
        <div
          className="relative rounded-3xl overflow-hidden border border-yellow-500/20 p-12 md:p-16 text-center"
          style={{
            background: 'radial-gradient(ellipse at 50% 0%, rgba(234,179,8,0.12) 0%, rgba(7,9,15,0.95) 70%)',
          }}
        >
          {/* Corner accents */}
          <div className="absolute top-0 left-0 w-32 h-32 border-t-2 border-l-2 border-yellow-500/30 rounded-tl-3xl" />
          <div className="absolute bottom-0 right-0 w-32 h-32 border-b-2 border-r-2 border-yellow-500/30 rounded-br-3xl" />

          <h2 className="display-font text-4xl md:text-6xl font-extrabold text-white mb-4 relative z-10">
            Take Control of<br />
            <span className="bg-gradient-to-r from-yellow-300 to-amber-500 bg-clip-text text-transparent">
              Your Financial Future
            </span>
          </h2>
          <p className="text-gray-400 text-lg mb-10 max-w-xl mx-auto relative z-10">
            Join FinAdvisor and start making smarter, data-driven financial decisions today.
          </p>
          <Link to="/signup"
            className="relative z-10 inline-flex items-center gap-2 px-10 py-4 bg-gradient-to-r from-yellow-400 to-amber-500 text-black rounded-xl font-bold text-lg hover:from-yellow-300 hover:to-amber-400 transition-all shadow-2xl shadow-yellow-500/40">
            <Sparkles className="w-5 h-5" />
            Create Free Account
          </Link>
        </div>
      </section>

      {/* ════════════════════════════════════════════
          FOOTER — Connect With Us
      ════════════════════════════════════════════ */}
      <footer className="relative z-10 border-t border-white/8 bg-[#07090F]">
        <div className="max-w-7xl mx-auto px-6 py-14">
          <div className="grid md:grid-cols-3 gap-10">

            {/* Brand */}
            <div>
              <div className="flex items-center gap-3 mb-4">
                <div className="w-10 h-10 rounded-xl bg-gradient-to-br from-yellow-400 to-yellow-600 flex items-center justify-center">
                  <TrendingUp className="w-5 h-5 text-black" />
                </div>
                <span className="display-font text-lg font-bold bg-gradient-to-r from-yellow-400 to-yellow-200 bg-clip-text text-transparent">
                  FinAdvisor
                </span>
              </div>
              <p className="text-gray-500 text-sm leading-relaxed max-w-xs">
                AI-powered financial tools for the modern Indian investor. Tax, crypto, and credit — simplified.
              </p>
            </div>

            {/* Quick Links */}
            <div>
              <h4 className="display-font text-sm font-bold text-white mb-4 tracking-widest uppercase">Platform</h4>
              <ul className="space-y-2.5">
                {[
                  { label: 'Crypto Adviser',   to: '/recommendations' },
                  { label: 'Tax Optimizer',    to: '/tax' },
                  { label: 'Credit Analyser',  to: '/credit' },
                  { label: 'Sign Up',          to: '/signup' },
                  { label: 'Sign In',          to: '/login' },
                ].map(l => (
                  <li key={l.label}>
                    <Link to={l.to} className="text-gray-500 hover:text-yellow-400 text-sm transition">
                      {l.label}
                    </Link>
                  </li>
                ))}
              </ul>
            </div>

            {/* Connect With Us */}
            <div>
              <h4 className="display-font text-sm font-bold text-white mb-4 tracking-widest uppercase">Connect With Us</h4>
              <div className="grid grid-cols-3 gap-3">
                {SOCIALS.map(s => (
                  <a
                    key={s.label}
                    href={s.href}
                    aria-label={s.label}
                    className="social-link flex flex-col items-center gap-2 p-3 bg-white/4 border border-white/8 rounded-xl hover:bg-yellow-500/10 hover:border-yellow-500/30 hover:text-yellow-400 text-gray-500 transition-all group"
                  >
                    {s.icon}
                    <span className="text-[10px] font-medium tracking-wide">{s.label}</span>
                  </a>
                ))}
              </div>
            </div>
          </div>

          {/* Bottom bar */}
          <div className="mt-12 pt-6 border-t border-white/6 flex flex-col md:flex-row items-center justify-between gap-3">
            <p className="text-gray-600 text-xs">
              © 2026 FinAdvisor. Built with ❤️ for smart Indian investors.
            </p>
            <p className="text-gray-700 text-xs">
              Not financial advice. For educational purposes only.
            </p>
          </div>
        </div>
      </footer>
    </div>
  );
};

export default Home;