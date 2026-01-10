import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { recommendationAPI } from '../services/api';
import Navbar from '../components/Navbar';
import { TrendingUp, Target, AlertTriangle, FileText, Sparkles, ArrowLeft } from 'lucide-react';

const Recommendations = () => {
  const { user } = useAuth();
  const [formData, setFormData] = useState({
    annualIncome: '',
    riskTolerance: 5,
    investmentHorizon: 'MEDIUM_TERM',
    targetCryptocurrency: 'BTC',
    additionalContext: '',
  });
  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState(null);
  const [error, setError] = useState('');
  const [useAsync, setUseAsync] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    setResult(null);

    try {
      const payload = {
        ...formData,
        annualIncome: parseFloat(formData.annualIncome),
      };

      if (useAsync) {
        const response = await recommendationAPI.generateAsync(payload);
        setResult({ 
          async: true, 
          message: response.data || 'Recommendation queued! Check history in a few seconds.' 
        });
      } else {
        const response = await recommendationAPI.generate(payload);
        setResult(response.data);
      }
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Failed to generate recommendation');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-[#0A0E27]">
      <Navbar />

      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Back Button */}
        <Link
          to="/dashboard"
          className="inline-flex items-center gap-2 text-gray-400 hover:text-yellow-400 transition mb-6 font-medium"
        >
          <ArrowLeft className="w-5 h-5" />
          Back to Dashboard
        </Link>

        {/* Header */}
        <div className="mb-8">
          <h1 className="text-4xl font-bold text-white mb-2">
            AI Investment Recommendations
          </h1>
          <p className="text-gray-400 text-lg">
            Get personalized crypto investment advice based on your profile
          </p>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
          {/* Input Form */}
          <div className="bg-[#141824] rounded-2xl border border-white/10 p-8 shadow-2xl">
            <div className="flex items-center gap-3 mb-6">
              <div className="w-10 h-10 rounded-lg bg-yellow-500/10 flex items-center justify-center">
                <TrendingUp className="w-5 h-5 text-yellow-400" />
              </div>
              <h2 className="text-2xl font-bold text-white">Investment Profile</h2>
            </div>

            {error && (
              <div className="bg-red-500/10 border border-red-500/30 text-red-400 p-4 rounded-lg mb-6 text-sm flex items-center gap-2">
                <AlertTriangle className="w-5 h-5 flex-shrink-0" />
                <span>{error}</span>
              </div>
            )}

            <form onSubmit={handleSubmit} className="space-y-6">
              {/* Annual Income */}
              <div>
                <label className="block text-sm font-semibold text-gray-300 mb-2">
                  💰 Annual Income (USD)
                </label>
                <input
                  type="number"
                  value={formData.annualIncome}
                  onChange={(e) => setFormData({ ...formData, annualIncome: e.target.value })}
                  className="w-full px-4 py-3 bg-[#1A1F37] border border-white/10 rounded-lg text-white placeholder-gray-500 focus:ring-2 focus:ring-yellow-500 focus:border-transparent outline-none transition"
                  required
                  min="0"
                  step="1000"
                  placeholder="75000"
                />
              </div>

              {/* Risk Tolerance Slider */}
              <div>
                <label className="block text-sm font-semibold text-gray-300 mb-3">
                  📊 Risk Tolerance: <span className="text-yellow-400 text-lg">{formData.riskTolerance}/10</span>
                </label>
                <input
                  type="range"
                  min="1"
                  max="10"
                  value={formData.riskTolerance}
                  onChange={(e) => setFormData({ ...formData, riskTolerance: parseInt(e.target.value) })}
                  className="w-full h-3 bg-gray-700 rounded-lg appearance-none cursor-pointer accent-yellow-500"
                  style={{
                    background: `linear-gradient(to right, #EAB308 0%, #EAB308 ${formData.riskTolerance * 10}%, #374151 ${formData.riskTolerance * 10}%, #374151 100%)`
                  }}
                />
                <div className="flex justify-between text-xs text-gray-500 mt-2">
                  <span>🛡️ Conservative</span>
                  <span>🚀 Aggressive</span>
                </div>
              </div>

              {/* Investment Horizon */}
              <div>
                <label className="block text-sm font-semibold text-gray-300 mb-2">
                  ⏱️ Investment Horizon
                </label>
                <select
                  value={formData.investmentHorizon}
                  onChange={(e) => setFormData({ ...formData, investmentHorizon: e.target.value })}
                  className="w-full px-4 py-3 bg-[#1A1F37] border border-white/10 rounded-lg text-white focus:ring-2 focus:ring-yellow-500 outline-none cursor-pointer"
                >
                  <option value="SHORT_TERM">Short Term (&lt; 1 year)</option>
                  <option value="MEDIUM_TERM">Medium Term (1-3 years)</option>
                  <option value="LONG_TERM">Long Term (&gt; 3 years)</option>
                </select>
              </div>

              {/* Target Cryptocurrency */}
              <div>
                <label className="block text-sm font-semibold text-gray-300 mb-2">
                  🎯 Target Cryptocurrency
                </label>
                <select
                  value={formData.targetCryptocurrency}
                  onChange={(e) => setFormData({ ...formData, targetCryptocurrency: e.target.value })}
                  className="w-full px-4 py-3 bg-[#1A1F37] border border-white/10 rounded-lg text-white focus:ring-2 focus:ring-yellow-500 outline-none cursor-pointer"
                >
                  <option value="BTC">₿ Bitcoin (BTC)</option>
                  <option value="ETH">Ξ Ethereum (ETH)</option>
                  <option value="BNB">🔶 Binance Coin (BNB)</option>
                  <option value="SOL">◎ Solana (SOL)</option>
                  <option value="ADA">₳ Cardano (ADA)</option>
                  <option value="XRP">✕ Ripple (XRP)</option>
                  <option value="DOT">● Polkadot (DOT)</option>
                </select>
              </div>

              {/* Additional Context */}
              <div>
                <label className="block text-sm font-semibold text-gray-300 mb-2">
                  📝 Additional Context (Optional)
                </label>
                <textarea
                  value={formData.additionalContext}
                  onChange={(e) => setFormData({ ...formData, additionalContext: e.target.value })}
                  className="w-full px-4 py-3 bg-[#1A1F37] border border-white/10 rounded-lg text-white placeholder-gray-500 focus:ring-2 focus:ring-yellow-500 outline-none resize-none"
                  rows="3"
                  placeholder="Any specific goals, preferences, or concerns..."
                />
              </div>

              {/* Async Checkbox */}
              <div className="bg-yellow-500/10 p-4 rounded-lg border border-yellow-500/20">
                <label className="flex items-start space-x-3 cursor-pointer">
                  <input
                    type="checkbox"
                    checked={useAsync}
                    onChange={(e) => setUseAsync(e.target.checked)}
                    className="w-5 h-5 mt-0.5 text-yellow-500 bg-gray-700 border-gray-600 rounded focus:ring-yellow-500 cursor-pointer"
                  />
                  <div>
                    <span className="text-sm font-semibold text-white block">⚡ Use Async Processing</span>
                    <p className="text-xs text-gray-400 mt-1">Queue recommendation in background (faster response, check history later)</p>
                  </div>
                </label>
              </div>

              {/* Submit Button */}
              <button
                type="submit"
                disabled={loading}
                className="w-full bg-gradient-to-r from-yellow-400 to-yellow-600 text-black py-4 rounded-lg font-bold text-lg hover:from-yellow-500 hover:to-yellow-700 transition-all disabled:opacity-50 disabled:cursor-not-allowed shadow-lg shadow-yellow-500/30 transform hover:scale-[1.02] flex items-center justify-center gap-2"
              >
                {loading ? (
                  <>
                    <div className="w-5 h-5 border-2 border-black/30 border-t-black rounded-full animate-spin"></div>
                    Generating...
                  </>
                ) : (
                  <>
                    <Sparkles className="w-5 h-5" />
                    {useAsync ? 'Queue Recommendation' : 'Get AI Recommendation'}
                  </>
                )}
              </button>
            </form>
          </div>

          {/* Results Panel */}
          <div className="bg-[#141824] rounded-2xl border border-white/10 p-8 shadow-2xl">
            <div className="flex items-center gap-3 mb-6">
              <div className="w-10 h-10 rounded-lg bg-yellow-500/10 flex items-center justify-center">
                <Target className="w-5 h-5 text-yellow-400" />
              </div>
              <h2 className="text-2xl font-bold text-white">AI Recommendation</h2>
            </div>

            {loading && (
              <div className="flex flex-col items-center justify-center py-16">
                <div className="relative w-20 h-20 mb-6">
                  <div className="absolute inset-0 border-4 border-yellow-500/20 rounded-full"></div>
                  <div className="absolute inset-0 border-4 border-yellow-400 border-t-transparent rounded-full animate-spin"></div>
                </div>
                <p className="text-gray-400 text-lg">Analyzing your investment profile...</p>
                <p className="text-gray-500 text-sm mt-2">This may take a few moments</p>
              </div>
            )}

            {result && !loading && (
              <div className="space-y-4">
                {result.async ? (
                  <div className="bg-green-500/10 border border-green-500/30 p-8 rounded-xl text-center">
                    <div className="w-16 h-16 bg-green-500/20 rounded-full flex items-center justify-center mx-auto mb-4">
                      <span className="text-4xl">✅</span>
                    </div>
                    <h3 className="text-xl font-bold text-green-400 mb-2">
                      Recommendation Queued!
                    </h3>
                    <p className="text-green-300 mb-6">{result.message}</p>
                    <Link
                      to="/history"
                      className="inline-flex items-center gap-2 bg-green-500 text-white px-6 py-3 rounded-lg hover:bg-green-600 transition font-semibold"
                    >
                      View History →
                    </Link>
                  </div>
                ) : (
                  <>
                    {/* Confidence Score */}
                    <div className="bg-gradient-to-r from-yellow-500/20 to-yellow-600/10 p-6 rounded-xl border border-yellow-500/30">
                      <div className="flex items-center justify-between mb-3">
                        <span className="text-sm font-semibold text-white">Confidence Score</span>
                        <span className="text-3xl font-bold text-yellow-400">
                          {(result.confidenceScore * 100).toFixed(0)}%
                        </span>
                      </div>
                      <div className="w-full bg-gray-700 rounded-full h-3 overflow-hidden">
                        <div
                          className="bg-gradient-to-r from-yellow-400 to-yellow-600 h-3 rounded-full transition-all duration-1000 ease-out shadow-lg shadow-yellow-500/50"
                          style={{ width: `${result.confidenceScore * 100}%` }}
                        ></div>
                      </div>
                    </div>

                    {/* Target Crypto */}
                    <div className="bg-[#1A1F37] p-6 rounded-xl border border-white/10">
                      <h3 className="font-semibold text-gray-400 mb-2 text-sm flex items-center gap-2">
                        <Target className="w-4 h-4" />
                        Target Cryptocurrency
                      </h3>
                      <p className="text-3xl font-bold text-yellow-400">{result.targetCryptocurrency}</p>
                    </div>

                    {/* Risk Assessment */}
                    {result.riskAssessment && (
                      <div className="bg-[#1A1F37] p-6 rounded-xl border border-white/10">
                        <h3 className="font-semibold text-gray-400 mb-3 text-sm flex items-center gap-2">
                          <AlertTriangle className="w-4 h-4" />
                          Risk Assessment
                        </h3>
                        <p className="text-gray-300 text-sm leading-relaxed">{result.riskAssessment}</p>
                      </div>
                    )}

                    {/* Full Recommendation */}
                    <div className="bg-[#1A1F37] p-6 rounded-xl max-h-96 overflow-y-auto border border-white/10">
                      <h3 className="font-semibold text-gray-400 mb-3 text-sm flex items-center gap-2">
                        <FileText className="w-4 h-4" />
                        Full Recommendation
                      </h3>
                      <p className="text-gray-300 text-sm whitespace-pre-line leading-relaxed">
                        {result.recommendationText}
                      </p>
                    </div>

                    {/* Metadata */}
                    <div className="text-xs text-gray-500 text-center pt-4 border-t border-white/10 flex items-center justify-center gap-3">
                      <span>⚡ Generated in {result.processingTimeMs}ms</span>
                      <span>•</span>
                      <span>📅 {new Date(result.createdAt).toLocaleString()}</span>
                    </div>
                  </>
                )}
              </div>
            )}

            {!result && !loading && (
              <div className="flex flex-col items-center justify-center py-20 text-gray-500">
                <div className="w-24 h-24 bg-white/5 rounded-2xl flex items-center justify-center mb-4">
                  <span className="text-5xl">🤖</span>
                </div>
                <p className="text-center text-gray-400 text-lg">
                  Fill out the form to get your<br />
                  <span className="text-yellow-400 font-semibold">AI-powered recommendation</span>
                </p>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default Recommendations;