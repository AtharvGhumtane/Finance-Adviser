import React from 'react';
import { TrendingUp, Calendar } from 'lucide-react';

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

export default RecommendationCard;