import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import Navbar from '../components/Navbar';
import { LiveNewsFeed } from '../components/LiveNewsFeed';
import { CryptoPriceTicker } from '../components/CryptoPriceTicker';
import { Sparkles, TrendingUp, ArrowRight, Brain, Shield, Zap } from 'lucide-react';

const Dashboard = () => {
  const { user } = useAuth();

  return (
    <div className="min-h-screen bg-[#0A0E27]">
      <Navbar />
      <CryptoPriceTicker />

      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Welcome Header */}
        <div className="mb-8">
          <h1 className="text-4xl font-bold text-white mb-2">
            Welcome back, <span className="text-yellow-400">{user?.username}</span>! 👋
          </h1>
          <p className="text-gray-400 text-lg">
            Stay updated with live crypto news and get AI-powered investment recommendations
          </p>
        </div>

        {/* Main Content - Two Columns */}
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          {/* LEFT: Live News Feed (2/3 width) */}
          <div className="lg:col-span-2">
            <LiveNewsFeed />
          </div>

          {/* RIGHT: AI Recommendation Card (1/3 width) */}
          <div className="lg:col-span-1">
            <div className="bg-[#141824] rounded-2xl border border-white/10 p-8 shadow-2xl sticky top-24">
              <div className="text-center mb-6">
                <div className="w-20 h-20 bg-gradient-to-br from-yellow-400 to-yellow-600 rounded-2xl flex items-center justify-center mx-auto mb-4 shadow-lg shadow-yellow-500/30 animate-pulse">
                  <Brain className="w-10 h-10 text-black" />
                </div>
                <h2 className="text-2xl font-bold text-white mb-2">
                  AI Investment Adviser
                </h2>
                <p className="text-gray-400 text-sm">
                  Get personalized crypto recommendations powered by advanced AI
                </p>
              </div>

              {/* Features List */}
              <div className="space-y-4 mb-6">
                <FeatureItem icon={<Sparkles className="w-5 h-5" />} text="Personalized Analysis" />
                <FeatureItem icon={<TrendingUp className="w-5 h-5" />} text="Market Insights" />
                <FeatureItem icon={<Shield className="w-5 h-5" />} text="Risk Assessment" />
                <FeatureItem icon={<Zap className="w-5 h-5" />} text="Instant Results" />
              </div>

              {/* CTA Button */}
              <Link
                to="/recommendations"
                className="w-full bg-gradient-to-r from-yellow-400 to-yellow-600 text-black py-4 rounded-lg font-bold text-lg hover:from-yellow-500 hover:to-yellow-700 transition-all shadow-lg shadow-yellow-500/30 transform hover:scale-[1.02] flex items-center justify-center gap-2 group"
              >
                <Sparkles className="w-5 h-5" />
                Get Recommendation
                <ArrowRight className="w-5 h-5 group-hover:translate-x-1 transition" />
              </Link>

              {/* Stats */}
              <div className="mt-6 pt-6 border-t border-white/10 grid grid-cols-2 gap-4">
                <div className="text-center">
                  <div className="text-2xl font-bold text-yellow-400">95%</div>
                  <div className="text-xs text-gray-500">Accuracy</div>
                </div>
                <div className="text-center">
                  <div className="text-2xl font-bold text-yellow-400">10K+</div>
                  <div className="text-xs text-gray-500">Users</div>
                </div>
              </div>

              {/* View History Link */}
              <Link
                to="/history"
                className="block text-center mt-4 text-yellow-400 text-sm font-semibold hover:text-yellow-300 transition"
              >
                View Past Recommendations →
              </Link>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

const FeatureItem = ({ icon, text }) => (
  <div className="flex items-center gap-3 text-gray-300">
    <div className="w-10 h-10 rounded-lg bg-yellow-500/10 flex items-center justify-center text-yellow-400">
      {icon}
    </div>
    <span className="font-medium">{text}</span>
  </div>
);

export default Dashboard;