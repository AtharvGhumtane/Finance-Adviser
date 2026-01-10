import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { recommendationAPI } from '../services/api';
import Navbar from '../components/Navbar';
import { History, TrendingUp, Target, AlertTriangle, FileText, Calendar, X, Sparkles } from 'lucide-react';

const HistoryPage = () => {
  const [recommendations, setRecommendations] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [selectedRec, setSelectedRec] = useState(null);

  useEffect(() => {
    loadHistory();
  }, []);

  const loadHistory = async () => {
    try {
      const response = await recommendationAPI.getHistory();
      setRecommendations(response.data);
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Failed to load history');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-[#0A0E27]">
      <Navbar />

      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Header */}
        <div className="mb-8">
          <div className="flex items-center gap-3 mb-2">
            <div className="w-12 h-12 rounded-xl bg-yellow-500/10 flex items-center justify-center">
              <History className="w-6 h-6 text-yellow-400" />
            </div>
            <h1 className="text-4xl font-bold text-white">Recommendation History</h1>
          </div>
          <p className="text-gray-400 text-lg ml-15">
            View and manage all your past AI recommendations
          </p>
        </div>

        {loading && (
          <div className="flex justify-center py-20">
            <div className="relative w-16 h-16">
              <div className="absolute inset-0 border-4 border-yellow-500/20 rounded-full"></div>
              <div className="absolute inset-0 border-4 border-yellow-400 border-t-transparent rounded-full animate-spin"></div>
            </div>
          </div>
        )}

        {error && (
          <div className="bg-red-500/10 border border-red-500/30 text-red-400 p-4 rounded-xl flex items-center gap-2">
            <span>⚠️</span>
            <span>{error}</span>
          </div>
        )}

        {!loading && recommendations.length === 0 && (
          <div className="bg-[#141824] rounded-2xl border border-white/10 p-16 text-center shadow-xl">
            <div className="w-24 h-24 bg-white/5 rounded-2xl flex items-center justify-center mx-auto mb-6">
              <span className="text-6xl">📭</span>
            </div>
            <h3 className="text-2xl font-bold text-white mb-3">
              No recommendations yet
            </h3>
            <p className="text-gray-400 mb-8 text-lg">
              Get started by creating your first AI-powered recommendation
            </p>
            <Link
              to="/dashboard"
              className="inline-flex items-center gap-2 bg-gradient-to-r from-yellow-400 to-yellow-600 text-black px-8 py-4 rounded-lg font-bold hover:from-yellow-500 hover:to-yellow-700 transition-all shadow-lg shadow-yellow-500/30"
            >
              <Sparkles className="w-5 h-5" />
              Create Recommendation
            </Link>
          </div>
        )}

        {!loading && recommendations.length > 0 && (
          <>
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
              {recommendations.map((rec) => (
                <RecommendationCard 
                  key={rec.recommendationId} 
                  recommendation={rec}
                  onClick={() => setSelectedRec(rec)}
                />
              ))}
            </div>
          </>
        )}
      </div>

      {/* Modal for Full Details */}
      {selectedRec && (
        <RecommendationModal 
          recommendation={selectedRec} 
          onClose={() => setSelectedRec(null)} 
        />
      )}
    </div>
  );
};

const RecommendationCard = ({ recommendation, onClick }) => {
  return (
    <div
      onClick={onClick}
      className="bg-[#141824] rounded-xl border border-white/10 p-6 cursor-pointer transition-all hover:border-yellow-500/30 hover:shadow-xl hover:shadow-yellow-500/10 hover:-translate-y-1 group"
    >
      {/* Header */}
      <div className="flex justify-between items-start mb-4">
        <div className="flex items-center gap-3">
          <div className="w-12 h-12 rounded-lg bg-yellow-500/10 flex items-center justify-center">
            <TrendingUp className="w-6 h-6 text-yellow-400" />
          </div>
          <div>
            <h3 className="text-2xl font-bold text-white">{recommendation.targetCryptocurrency}</h3>
            <p className="text-xs text-gray-500">Investment Target</p>
          </div>
        </div>
        <div className="bg-yellow-500/10 border border-yellow-500/30 text-yellow-400 text-sm px-3 py-1.5 rounded-lg font-bold">
          {(recommendation.confidenceScore * 100).toFixed(0)}%
        </div>
      </div>

      {/* Strategy */}
      <div className="bg-[#1A1F37] p-3 rounded-lg mb-4 border border-white/5">
        <p className="text-xs text-gray-400 mb-1">Strategy</p>
        <p className="text-sm text-white font-semibold">
          {recommendation.investmentStrategy || recommendation.timeframe}
        </p>
      </div>

      {/* Risk Assessment Preview */}
      {recommendation.riskAssessment && (
        <div className="mb-4">
          <p className="text-xs text-gray-500 line-clamp-2">
            {recommendation.riskAssessment}
          </p>
        </div>
      )}

      {/* Footer */}
      <div className="flex items-center justify-between pt-4 border-t border-white/10">
        <div className="flex items-center gap-2 text-gray-500 text-xs">
          <Calendar className="w-4 h-4" />
          {new Date(recommendation.createdAt).toLocaleDateString('en-US', {
            month: 'short',
            day: 'numeric',
            year: 'numeric',
          })}
        </div>
        <span className="text-yellow-400 text-sm font-semibold group-hover:translate-x-1 transition-transform">
          View Details →
        </span>
      </div>
    </div>
  );
};

const RecommendationModal = ({ recommendation, onClose }) => {
  return (
    <div
      className="fixed inset-0 bg-black/80 backdrop-blur-sm flex items-center justify-center p-4 z-50 animate-in fade-in duration-200"
      onClick={onClose}
    >
      <div
        className="bg-[#141824] border border-white/10 rounded-2xl max-w-3xl w-full max-h-[90vh] overflow-y-auto shadow-2xl"
        onClick={(e) => e.stopPropagation()}
      >
        {/* Header */}
        <div className="sticky top-0 bg-[#141824] border-b border-white/10 p-6 flex justify-between items-start">
          <div className="flex items-center gap-4">
            <div className="w-14 h-14 rounded-xl bg-yellow-500/10 flex items-center justify-center">
              <TrendingUp className="w-7 h-7 text-yellow-400" />
            </div>
            <div>
              <h2 className="text-3xl font-bold text-white mb-1">
                {recommendation.targetCryptocurrency} Recommendation
              </h2>
              <p className="text-sm text-gray-400 flex items-center gap-2">
                <Calendar className="w-4 h-4" />
                {new Date(recommendation.createdAt).toLocaleString('en-US', {
                  month: 'long',
                  day: 'numeric',
                  year: 'numeric',
                  hour: 'numeric',
                  minute: '2-digit'
                })}
              </p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="w-10 h-10 rounded-lg bg-white/5 hover:bg-white/10 text-gray-400 hover:text-white transition-all flex items-center justify-center"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Content */}
        <div className="p-6 space-y-6">
          {/* Confidence Score */}
          <div className="bg-gradient-to-r from-yellow-500/20 to-yellow-600/10 p-6 rounded-xl border border-yellow-500/30">
            <div className="flex items-center justify-between mb-3">
              <span className="text-sm font-semibold text-white flex items-center gap-2">
                <Target className="w-4 h-4" />
                Confidence Score
              </span>
              <span className="text-3xl font-bold text-yellow-400">
                {(recommendation.confidenceScore * 100).toFixed(0)}%
              </span>
            </div>
            <div className="w-full bg-gray-700 rounded-full h-3">
              <div
                className="bg-gradient-to-r from-yellow-400 to-yellow-600 h-3 rounded-full transition-all duration-500 shadow-lg shadow-yellow-500/50"
                style={{ width: `${recommendation.confidenceScore * 100}%` }}
              ></div>
            </div>
          </div>

          {/* Investment Strategy */}
          <div className="bg-[#1A1F37] p-6 rounded-xl border border-white/10">
            <h3 className="font-bold text-white mb-3 flex items-center gap-2">
              <TrendingUp className="w-5 h-5 text-yellow-400" />
              Investment Strategy
            </h3>
            <p className="text-gray-300 text-sm">
              {recommendation.investmentStrategy || recommendation.timeframe}
            </p>
          </div>

          {/* Risk Assessment */}
          {recommendation.riskAssessment && (
            <div className="bg-[#1A1F37] p-6 rounded-xl border border-white/10">
              <h3 className="font-bold text-white mb-3 flex items-center gap-2">
                <AlertTriangle className="w-5 h-5 text-orange-400" />
                Risk Assessment
              </h3>
              <p className="text-gray-300 text-sm leading-relaxed">
                {recommendation.riskAssessment}
              </p>
            </div>
          )}

          {/* Full Recommendation */}
          <div className="bg-[#1A1F37] p-6 rounded-xl border border-white/10">
            <h3 className="font-bold text-white mb-3 flex items-center gap-2">
              <FileText className="w-5 h-5 text-blue-400" />
              Full Recommendation
            </h3>
            <p className="text-gray-300 text-sm whitespace-pre-line leading-relaxed">
              {recommendation.recommendationText}
            </p>
          </div>

          {/* Metadata */}
          <div className="text-xs text-gray-500 text-center pt-4 border-t border-white/10 flex items-center justify-center gap-3">
            <span>⚡ Processing Time: {recommendation.processingTimeMs}ms</span>
          </div>
        </div>

        {/* Footer */}
        <div className="sticky bottom-0 bg-[#141824] border-t border-white/10 p-6">
          <button
            onClick={onClose}
            className="w-full bg-gray-700/50 hover:bg-gray-700 text-white py-3 rounded-lg font-bold transition-all border border-white/10"
          >
            Close
          </button>
        </div>
      </div>
    </div>
  );
};

export default HistoryPage;