import { Link } from 'react-router-dom';
import { TrendingUp, Sparkles, Shield, Zap, ArrowRight, BarChart3, Brain } from 'lucide-react';

const Home = () => {
  return (
    <div className="min-h-screen bg-[#0A0E27] relative overflow-hidden">
      {/* Animated Background */}
      <div className="absolute inset-0">
        <div className="absolute top-1/4 left-1/4 w-96 h-96 bg-yellow-500/10 rounded-full blur-3xl animate-pulse"></div>
        <div className="absolute bottom-1/4 right-1/4 w-96 h-96 bg-blue-500/10 rounded-full blur-3xl animate-pulse delay-1000"></div>
        <div className="absolute top-1/2 right-1/3 w-64 h-64 bg-purple-500/10 rounded-full blur-3xl animate-pulse delay-500"></div>
      </div>

      {/* Navigation */}
      <nav className="relative z-10 px-6 py-6">
        <div className="max-w-7xl mx-auto flex justify-between items-center">
          <div className="flex items-center gap-3">
            <div className="w-12 h-12 rounded-xl bg-gradient-to-br from-yellow-400 to-yellow-600 flex items-center justify-center shadow-lg shadow-yellow-500/30">
              <TrendingUp className="w-6 h-6 text-black" />
            </div>
            <div>
              <h1 className="text-2xl font-bold bg-gradient-to-r from-yellow-400 to-yellow-200 bg-clip-text text-transparent">
                Crypto Adviser
              </h1>
              <p className="text-[10px] text-gray-400 -mt-1">AI-Powered Investment Platform</p>
            </div>
          </div>

          <div className="flex items-center gap-4">
            <Link
              to="/login"
              className="px-6 py-2 text-white hover:text-yellow-400 transition font-medium"
            >
              Sign In
            </Link>
            <Link
              to="/signup"
              className="px-6 py-2 bg-gradient-to-r from-yellow-400 to-yellow-600 text-black rounded-lg font-bold hover:from-yellow-500 hover:to-yellow-700 transition shadow-lg shadow-yellow-500/30"
            >
              Get Started
            </Link>
          </div>
        </div>
      </nav>

      {/* Hero Section */}
      <div className="relative z-10 max-w-7xl mx-auto px-6 py-20">
        <div className="text-center mb-16">
          <div className="inline-flex items-center gap-2 px-4 py-2 bg-yellow-500/10 border border-yellow-500/30 rounded-full mb-6 animate-pulse">
            <Sparkles className="w-4 h-4 text-yellow-400" />
            <span className="text-yellow-400 text-sm font-semibold">AI-Powered Crypto Analysis</span>
          </div>

          <h1 className="text-6xl md:text-7xl font-bold text-white mb-6 leading-tight">
            Smart Investment
            <br />
            <span className="bg-gradient-to-r from-yellow-400 via-yellow-500 to-yellow-600 bg-clip-text text-transparent">
              Decisions Made Easy
            </span>
          </h1>

          <p className="text-xl text-gray-400 mb-12 max-w-3xl mx-auto leading-relaxed">
            Get personalized cryptocurrency investment recommendations powered by advanced AI. 
            Make informed decisions with real-time market analysis and expert insights.
          </p>

          <div className="flex items-center justify-center gap-4">
            <Link
              to="/signup"
              className="px-8 py-4 bg-gradient-to-r from-yellow-400 to-yellow-600 text-black rounded-lg font-bold text-lg hover:from-yellow-500 hover:to-yellow-700 transition shadow-2xl shadow-yellow-500/30 flex items-center gap-2 group"
            >
              Start Free Trial
              <ArrowRight className="w-5 h-5 group-hover:translate-x-1 transition" />
            </Link>
            <Link
              to="/login"
              className="px-8 py-4 bg-white/5 border border-white/10 text-white rounded-lg font-bold text-lg hover:bg-white/10 transition"
            >
              Watch Demo
            </Link>
          </div>
        </div>

        {/* Features Grid */}
        <div className="grid md:grid-cols-3 gap-8 mt-20">
          <FeatureCard
            icon={<Brain className="w-8 h-8" />}
            title="AI-Powered Analysis"
            description="Advanced machine learning algorithms analyze market trends and provide personalized recommendations."
            gradient="from-blue-500 to-cyan-500"
          />
          <FeatureCard
            icon={<BarChart3 className="w-8 h-8" />}
            title="Real-Time Data"
            description="Stay updated with live crypto prices, news, and market movements in real-time."
            gradient="from-yellow-500 to-orange-500"
          />
          <FeatureCard
            icon={<Shield className="w-8 h-8" />}
            title="Risk Management"
            description="Get detailed risk assessments tailored to your investment profile and risk tolerance."
            gradient="from-purple-500 to-pink-500"
          />
        </div>

        {/* Stats Section */}
        <div className="mt-32 grid md:grid-cols-4 gap-8">
          <StatCard number="10K+" label="Active Users" />
          <StatCard number="$50M+" label="Assets Analyzed" />
          <StatCard number="95%" label="Success Rate" />
          <StatCard number="24/7" label="Live Support" />
        </div>

        {/* CTA Section */}
        <div className="mt-32 bg-gradient-to-r from-yellow-500/10 to-yellow-600/10 border border-yellow-500/30 rounded-3xl p-12 text-center">
          <h2 className="text-4xl font-bold text-white mb-4">
            Ready to Make Smarter Investments?
          </h2>
          <p className="text-gray-400 text-lg mb-8">
            Join thousands of investors who trust our AI-powered platform
          </p>
          <Link
            to="/signup"
            className="inline-flex items-center gap-2 px-8 py-4 bg-gradient-to-r from-yellow-400 to-yellow-600 text-black rounded-lg font-bold text-lg hover:from-yellow-500 hover:to-yellow-700 transition shadow-2xl shadow-yellow-500/30"
          >
            <Zap className="w-5 h-5" />
            Get Started Now
          </Link>
        </div>
      </div>

      {/* Footer */}
      <footer className="relative z-10 border-t border-white/10 mt-20 py-8">
        <div className="max-w-7xl mx-auto px-6 text-center text-gray-500 text-sm">
          <p>© 2026 Crypto Adviser. All rights reserved. Built with ❤️ for smart investors.</p>
        </div>
      </footer>
    </div>
  );
};

const FeatureCard = ({ icon, title, description, gradient }) => (
  <div className="bg-[#141824] border border-white/10 rounded-2xl p-8 hover:border-yellow-500/30 transition group">
    <div className={`w-16 h-16 rounded-xl bg-gradient-to-br ${gradient} flex items-center justify-center mb-6 group-hover:scale-110 transition`}>
      <div className="text-white">{icon}</div>
    </div>
    <h3 className="text-xl font-bold text-white mb-3">{title}</h3>
    <p className="text-gray-400 leading-relaxed">{description}</p>
  </div>
);

const StatCard = ({ number, label }) => (
  <div className="text-center">
    <div className="text-4xl font-bold bg-gradient-to-r from-yellow-400 to-yellow-600 bg-clip-text text-transparent mb-2">
      {number}
    </div>
    <div className="text-gray-400 font-medium">{label}</div>
  </div>
);

export default Home;