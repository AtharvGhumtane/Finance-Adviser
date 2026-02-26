import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { creditAPI } from '../services/api';
import Navbar from '../components/Navbar';
import {
  CreditCard as CreditIcon, AlertTriangle, ShieldCheck,
  ArrowLeft, Sparkles, ChevronDown, ChevronUp, TrendingDown
} from 'lucide-react';

// ─── Helpers ──────────────────────────────────────────────────────────────────
const Field = ({ label, value, onChange, ph = '0' }) => (
  <div>
    <label className="block text-sm font-semibold text-gray-300 mb-1">{label}</label>
    <input
      type="number" value={value} onChange={e => onChange(e.target.value)}
      placeholder={ph} min="0"
      className="w-full px-3 py-2.5 bg-[#1A1F37] border border-white/10 rounded-lg text-white placeholder-gray-600 focus:ring-2 focus:ring-yellow-500 outline-none text-sm"
    />
  </div>
);

const Toggle = ({ label, value, onChange, desc }) => (
  <div className="flex items-start gap-3 bg-[#1A1F37] p-3 rounded-lg border border-white/10">
    <input type="checkbox" checked={value} onChange={e => onChange(e.target.checked)}
      className="w-5 h-5 mt-0.5 accent-yellow-500 cursor-pointer flex-shrink-0" />
    <div>
      <p className="text-sm font-semibold text-white">{label}</p>
      {desc && <p className="text-xs text-gray-500 mt-0.5">{desc}</p>}
    </div>
  </div>
);

const SEVERITY_COLOR = {
  CRITICAL: 'border-red-500/50 bg-red-500/10',
  HIGH:     'border-orange-500/50 bg-orange-500/10',
  MEDIUM:   'border-yellow-500/50 bg-yellow-500/10',
  LOW:      'border-blue-500/50 bg-blue-500/10',
  NONE:     'border-white/10 bg-[#1A1F37]',
};

const SEVERITY_BADGE = {
  CRITICAL: 'bg-red-500/20 text-red-400',
  HIGH:     'bg-orange-500/20 text-orange-400',
  MEDIUM:   'bg-yellow-500/20 text-yellow-400',
  LOW:      'bg-blue-500/20 text-blue-400',
  NONE:     'bg-green-500/20 text-green-400',
};

const RISK_COLORS = {
  HIGH:   { border: 'border-red-500/50',    bg: 'from-red-500/20 to-red-600/10',    text: 'text-red-400' },
  MEDIUM: { border: 'border-yellow-500/50', bg: 'from-yellow-500/20 to-yellow-600/10', text: 'text-yellow-400' },
  LOW:    { border: 'border-green-500/50',  bg: 'from-green-500/20 to-green-600/10', text: 'text-green-400' },
};

const rupee = (v) => v != null ? `\u20B9${Number(v).toLocaleString('en-IN')}` : '\u20B90';

// ─── Trap Card ────────────────────────────────────────────────────────────────
const TrapCard = ({ trap }) => {
  const [open, setOpen] = useState(false);
  return (
    <div className={`rounded-xl border p-4 ${SEVERITY_COLOR[trap.severity] || SEVERITY_COLOR.NONE}`}>
      <div className="flex items-center justify-between cursor-pointer" onClick={() => setOpen(o => !o)}>
        <div className="flex items-center gap-3">
          <span className="text-lg">{trap.detected ? '⚠️' : '✅'}</span>
          <span className="font-semibold text-white text-sm">{trap.trapName}</span>
        </div>
        <div className="flex items-center gap-2">
          <span className={`text-xs px-2 py-0.5 rounded-full font-bold ${SEVERITY_BADGE[trap.severity] || SEVERITY_BADGE.NONE}`}>
            {trap.detected ? trap.severity : 'SAFE'}
          </span>
          {open ? <ChevronUp className="w-4 h-4 text-gray-400" /> : <ChevronDown className="w-4 h-4 text-gray-400" />}
        </div>
      </div>

      {open && (
        <div className="mt-3 space-y-2 text-sm border-t border-white/10 pt-3">
          <p className="text-gray-300">{trap.explanation}</p>
          {trap.detected && (
            <>
              <div className="bg-black/20 rounded-lg p-3">
                <p className="text-xs text-gray-500 mb-1">Consequence</p>
                <p className="text-red-300 text-xs">{trap.consequence}</p>
              </div>
              <div className="bg-black/20 rounded-lg p-3">
                <p className="text-xs text-gray-500 mb-1">Recommendation</p>
                <p className="text-green-300 text-xs">{trap.recommendation}</p>
              </div>
              {trap.estimatedMonthlyCost > 0 && (
                <div className="flex justify-between text-xs">
                  <span className="text-gray-500">Est. Monthly Cost</span>
                  <span className="text-red-400 font-bold">{rupee(trap.estimatedMonthlyCost)}</span>
                </div>
              )}
              {trap.potentialSaving > 0 && (
                <div className="flex justify-between text-xs">
                  <span className="text-gray-500">Annual Saving Potential</span>
                  <span className="text-green-400 font-bold">{rupee(trap.potentialSaving)}</span>
                </div>
              )}
            </>
          )}
        </div>
      )}
    </div>
  );
};

// ─── Main Component ───────────────────────────────────────────────────────────
const CreditCard = () => {
  const { user } = useAuth();
  const [mode, setMode] = useState('quick');
  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState(null);
  const [error, setError] = useState('');
  const [showAI, setShowAI] = useState(false);

  const [form, setForm] = useState({
    monthlyIncome: '', monthlyExpenses: '',
    totalCreditLimit: '', totalOutstandingBalance: '',
    numberOfCards: '1', creditScore: '700',
    paysMinimumOnly: false,
    latePaymentsLastYear: '0', missedPaymentsLastYear: '0',
    totalEmiPerMonth: '0', numberOfActiveEmis: '0',
    cashAdvanceAmount: '0', cashAdvanceFrequency: '0',
    annualInterestRate: '36', latePamentFee: '0',
    otherLoanEmi: '0',
  });

  const set = (k) => (v) => setForm(f => ({ ...f, [k]: v }));
  const n = (v) => parseFloat(v) || 0;

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(''); setResult(null); setLoading(true); setShowAI(false);
    try {
      const payload = {
        userId: user?.userId || user?.username,
        monthlyIncome: n(form.monthlyIncome),
        monthlyExpenses: n(form.monthlyExpenses),
        totalCreditLimit: n(form.totalCreditLimit),
        totalOutstandingBalance: n(form.totalOutstandingBalance),
        numberOfCards: n(form.numberOfCards),
        creditScore: n(form.creditScore),
        paysMinimumOnly: form.paysMinimumOnly,
        latePaymentsLastYear: n(form.latePaymentsLastYear),
        missedPaymentsLastYear: n(form.missedPaymentsLastYear),
        totalEmiPerMonth: n(form.totalEmiPerMonth),
        numberOfActiveEmis: n(form.numberOfActiveEmis),
        cashAdvanceAmount: n(form.cashAdvanceAmount),
        cashAdvanceFrequency: n(form.cashAdvanceFrequency),
        annualInterestRate: n(form.annualInterestRate),
        latePamentFee: n(form.latePamentFee),
        otherLoanEmi: n(form.otherLoanEmi),
      };

      let res;
      if (mode === 'quick')  res = await creditAPI.quickTrapCheck(payload);
      else if (mode === 'risk') res = await creditAPI.riskOnly(payload);
      else                   res = await creditAPI.analyze(payload);

      setResult({ mode, data: res.data });
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Something went wrong');
    } finally {
      setLoading(false);
    }
  };

  const traps = result?.data?.traps || result?.data?.allTrapResults || [];
  const detected = traps.filter(t => t.detected);
  const risk = result?.data;
  const health = result?.data?.healthMetrics;

  return (
    <div className="min-h-screen bg-[#0A0E27]">
      <Navbar />
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">

        <Link to="/dashboard" className="inline-flex items-center gap-2 text-gray-400 hover:text-yellow-400 transition mb-6 font-medium">
          <ArrowLeft className="w-5 h-5" /> Back to Dashboard
        </Link>

        {/* Header */}
        <div className="mb-8 flex items-center gap-4">
          <div className="w-14 h-14 rounded-xl bg-gradient-to-br from-yellow-400 to-yellow-600 flex items-center justify-center shadow-lg shadow-yellow-500/30">
            <CreditIcon className="w-7 h-7 text-black" />
          </div>
          <div>
            <h1 className="text-4xl font-bold text-white">Credit Card Trap Analyser</h1>
            <p className="text-gray-400">6 Trap Detection &bull; ML Risk Classification &bull; AI Explainability</p>
          </div>
        </div>

        {/* Mode Toggle */}
        <div className="flex gap-3 mb-8 flex-wrap">
          {[
            { id: 'quick', label: '⚡ Quick Trap Check' },
            { id: 'risk',  label: '🎯 Risk Only (ML)' },
            { id: 'full',  label: '✨ Full AI Analysis' },
          ].map(m => (
            <button key={m.id}
              onClick={() => { setMode(m.id); setResult(null); setError(''); }}
              className={`px-6 py-2.5 rounded-lg font-semibold text-sm transition-all ${
                mode === m.id
                  ? 'bg-gradient-to-r from-yellow-400 to-yellow-600 text-black shadow-lg shadow-yellow-500/30'
                  : 'bg-white/5 text-gray-300 border border-white/10 hover:bg-white/10'
              }`}
            >
              {m.label}
            </button>
          ))}
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">

          {/* ── LEFT: Form ──────────────────────────────────────────────── */}
          <div className="bg-[#141824] rounded-2xl border border-white/10 p-8 shadow-2xl">
            <h2 className="text-xl font-bold text-white mb-6 flex items-center gap-2">
              <TrendingDown className="w-5 h-5 text-yellow-400" /> Credit Profile Details
            </h2>

            {error && (
              <div className="bg-red-500/10 border border-red-500/30 text-red-400 p-4 rounded-lg mb-6 text-sm flex items-center gap-2">
                <AlertTriangle className="w-5 h-5 flex-shrink-0" /> {error}
              </div>
            )}

            <form onSubmit={handleSubmit}>
              <div className="space-y-4 mb-6">

                {/* Income Section */}
                <p className="text-xs text-yellow-400 font-bold uppercase tracking-widest">Income & Expenses</p>
                <div className="grid grid-cols-2 gap-3">
                  <Field label="Monthly Income (₹)"   value={form.monthlyIncome}   onChange={set('monthlyIncome')}   ph="80000" />
                  <Field label="Monthly Expenses (₹)" value={form.monthlyExpenses} onChange={set('monthlyExpenses')} ph="30000" />
                </div>

                {/* Credit Section */}
                <p className="text-xs text-yellow-400 font-bold uppercase tracking-widest mt-2">Credit Card Details</p>
                <div className="grid grid-cols-2 gap-3">
                  <Field label="Total Credit Limit (₹)"    value={form.totalCreditLimit}       onChange={set('totalCreditLimit')}       ph="200000" />
                  <Field label="Outstanding Balance (₹)"   value={form.totalOutstandingBalance} onChange={set('totalOutstandingBalance')} ph="80000" />
                  <Field label="Number of Cards"           value={form.numberOfCards}           onChange={set('numberOfCards')}           ph="1" />
                  <Field label="CIBIL Credit Score"        value={form.creditScore}             onChange={set('creditScore')}             ph="700" />
                  <Field label="Interest Rate (APR %)"     value={form.annualInterestRate}      onChange={set('annualInterestRate')}      ph="36" />
                  <Field label="Late Payment Fee (₹)"      value={form.latePamentFee}           onChange={set('latePamentFee')}           ph="1200" />
                </div>

                {/* Payment Behavior */}
                <p className="text-xs text-yellow-400 font-bold uppercase tracking-widest mt-2">Payment Behavior</p>
                <Toggle
                  label="Pay Minimum Only Every Month?"
                  value={form.paysMinimumOnly}
                  onChange={set('paysMinimumOnly')}
                  desc="Paying only the minimum due traps you in perpetual debt"
                />
                <div className="grid grid-cols-2 gap-3">
                  <Field label="Late Payments (last yr)"   value={form.latePaymentsLastYear}   onChange={set('latePaymentsLastYear')}   ph="0" />
                  <Field label="Missed Payments (last yr)" value={form.missedPaymentsLastYear} onChange={set('missedPaymentsLastYear')} ph="0" />
                </div>

                {/* EMI */}
                <p className="text-xs text-yellow-400 font-bold uppercase tracking-widest mt-2">EMI & Loans</p>
                <div className="grid grid-cols-2 gap-3">
                  <Field label="Card EMI/Month (₹)"    value={form.totalEmiPerMonth}  onChange={set('totalEmiPerMonth')}  ph="0" />
                  <Field label="Active EMI Count"      value={form.numberOfActiveEmis} onChange={set('numberOfActiveEmis')} ph="0" />
                  <Field label="Other Loan EMI/Month (₹)" value={form.otherLoanEmi}   onChange={set('otherLoanEmi')}      ph="0" />
                </div>

                {/* Cash Advance */}
                <p className="text-xs text-yellow-400 font-bold uppercase tracking-widest mt-2">Cash Withdrawals</p>
                <div className="grid grid-cols-2 gap-3">
                  <Field label="Cash Advance Amount (₹)" value={form.cashAdvanceAmount}    onChange={set('cashAdvanceAmount')}    ph="0" />
                  <Field label="Times Per Month"          value={form.cashAdvanceFrequency} onChange={set('cashAdvanceFrequency')} ph="0" />
                </div>
              </div>

              <button type="submit" disabled={loading}
                className="w-full bg-gradient-to-r from-yellow-400 to-yellow-600 text-black py-4 rounded-lg font-bold text-lg hover:from-yellow-500 hover:to-yellow-700 transition-all disabled:opacity-50 shadow-lg shadow-yellow-500/30 flex items-center justify-center gap-2">
                {loading ? (
                  <><div className="w-5 h-5 border-2 border-black/30 border-t-black rounded-full animate-spin" /> Analysing...</>
                ) : (
                  <><Sparkles className="w-5 h-5" />
                    {mode === 'quick' ? 'Check for Traps' : mode === 'risk' ? 'Classify Risk' : 'Full AI Analysis'}
                  </>
                )}
              </button>
            </form>
          </div>

          {/* ── RIGHT: Results ──────────────────────────────────────────── */}
          <div className="bg-[#141824] rounded-2xl border border-white/10 p-8 shadow-2xl overflow-y-auto max-h-[85vh]">
            <h2 className="text-xl font-bold text-white mb-6 flex items-center gap-2">
              <ShieldCheck className="w-5 h-5 text-yellow-400" /> Risk & Trap Report
            </h2>

            {loading && (
              <div className="flex flex-col items-center justify-center py-20">
                <div className="relative w-20 h-20 mb-6">
                  <div className="absolute inset-0 border-4 border-yellow-500/20 rounded-full" />
                  <div className="absolute inset-0 border-4 border-yellow-400 border-t-transparent rounded-full animate-spin" />
                </div>
                <p className="text-gray-400 text-lg">Scanning your credit profile...</p>
              </div>
            )}

            {!loading && !result && (
              <div className="flex flex-col items-center justify-center py-20">
                <div className="w-24 h-24 bg-white/5 rounded-2xl flex items-center justify-center mb-4">
                  <span className="text-5xl">💳</span>
                </div>
                <p className="text-center text-gray-400 text-lg">
                  Fill in your credit details to<br />
                  <span className="text-yellow-400 font-semibold">detect hidden debt traps</span>
                </p>
              </div>
            )}

            {result && !loading && (
              <div className="space-y-4">

                {/* Risk Level Banner */}
                {risk?.riskLevel && (() => {
                  const rc = RISK_COLORS[risk.riskLevel] || RISK_COLORS.MEDIUM;
                  return (
                    <div className={`bg-gradient-to-r ${rc.bg} border ${rc.border} rounded-xl p-5`}>
                      <div className="flex items-center justify-between mb-2">
                        <span className="text-white font-bold text-lg">Overall Risk</span>
                        <span className={`text-3xl font-black ${rc.text}`}>{risk.riskLevel}</span>
                      </div>
                      {risk.riskScore != null && (
                        <>
                          <div className="w-full bg-gray-700 rounded-full h-2 mb-1">
                            <div className={`h-2 rounded-full transition-all duration-1000 ${
                              risk.riskLevel === 'HIGH' ? 'bg-red-500' : risk.riskLevel === 'MEDIUM' ? 'bg-yellow-500' : 'bg-green-500'
                            }`} style={{ width: `${risk.riskScore}%` }} />
                          </div>
                          <p className="text-xs text-gray-400">Risk Score: {risk.riskScore}/100</p>
                        </>
                      )}
                      {risk.riskCategory && <p className="text-sm text-gray-300 mt-1">{risk.riskCategory}</p>}
                      {risk.riskReasoning && <p className="text-xs text-gray-400 mt-2 leading-relaxed">{risk.riskReasoning}</p>}
                    </div>
                  );
                })()}

                {/* Traps Summary */}
                {traps.length > 0 && (
                  <div className="bg-[#1A1F37] rounded-xl border border-white/10 p-4">
                    <div className="flex items-center justify-between mb-3">
                      <p className="font-bold text-white">Trap Detection Summary</p>
                      <span className={`text-sm font-bold px-3 py-1 rounded-full ${
                        detected.length === 0 ? 'bg-green-500/20 text-green-400' : 'bg-red-500/20 text-red-400'
                      }`}>
                        {detected.length}/{traps.length} Detected
                      </span>
                    </div>
                    {result.data.summary && <p className="text-gray-400 text-sm mb-3">{result.data.summary}</p>}
                    <div className="space-y-3">
                      {traps.map((trap, i) => <TrapCard key={i} trap={trap} />)}
                    </div>
                  </div>
                )}

                {/* Financial Health Metrics */}
                {health && (
                  <div className="bg-[#1A1F37] rounded-xl border border-white/10 p-4">
                    <p className="font-bold text-white mb-3">Financial Health Metrics</p>
                    <div className="grid grid-cols-2 gap-3">
                      <Metric label="Credit Utilization"   value={`${health.creditUtilizationPct?.toFixed(1)}%`}
                        warn={health.creditUtilizationPct > 30} />
                      <Metric label="Debt-to-Income"       value={`${health.debtToIncomeRatio?.toFixed(1)}%`}
                        warn={health.debtToIncomeRatio > 40} />
                      <Metric label="EMI Burden"           value={`${health.emiBurdenRatio?.toFixed(1)}%`}
                        warn={health.emiBurdenRatio > 50} />
                      <Metric label="Monthly Interest"     value={rupee(health.estimatedMonthlyInterest)} warn />
                      <Metric label="Annual Interest Cost" value={rupee(health.estimatedAnnualInterest)} warn />
                      <Metric label="Free Cash Flow"       value={rupee(health.netFreeCashFlow)}
                        warn={health.netFreeCashFlow < 0} />
                      <Metric label="Current CIBIL"        value={health.currentCreditScore} />
                      <Metric label="Projected CIBIL"      value={health.projectedScoreIfCorrected}
                        good={health.projectedScoreIfCorrected > health.currentCreditScore} />
                    </div>
                  </div>
                )}

                {/* AI Recommendation — full mode only */}
                {mode === 'full' && result.data?.aiRecommendation && (
                  <div className="bg-[#1A1F37] rounded-xl border border-white/10 overflow-hidden">
                    <button
                      onClick={() => setShowAI(o => !o)}
                      className="w-full flex items-center justify-between p-4 text-left hover:bg-white/5 transition"
                    >
                      <span className="font-bold text-white flex items-center gap-2">
                        <Sparkles className="w-4 h-4 text-yellow-400" /> AI Recommendation
                      </span>
                      {showAI ? <ChevronUp className="w-4 h-4 text-gray-400" /> : <ChevronDown className="w-4 h-4 text-gray-400" />}
                    </button>
                    {showAI && (
                      <div className="px-4 pb-4">
                        <p className="text-gray-300 text-sm whitespace-pre-line leading-relaxed">
                          {result.data.aiRecommendation}
                        </p>
                      </div>
                    )}
                  </div>
                )}

                {/* AI Tips */}
                {result.data?.aiTips && result.data.aiTips.length > 0 && (
                  <div className="bg-[#1A1F37] rounded-xl border border-white/10 p-4">
                    <p className="font-bold text-white mb-3 text-sm">💡 Quick Tips</p>
                    <ul className="space-y-2">
                      {result.data.aiTips.map((tip, i) => (
                        <li key={i} className="text-gray-300 text-sm flex items-start gap-2">
                          <span className="text-yellow-400 mt-0.5 flex-shrink-0">•</span> {tip}
                        </li>
                      ))}
                    </ul>
                  </div>
                )}

                <p className="text-xs text-gray-600 text-center pt-2">
                  For awareness purposes only. Not financial advice.
                </p>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

const Metric = ({ label, value, warn = false, good = false }) => (
  <div className="bg-black/20 rounded-lg p-3">
    <p className="text-xs text-gray-500 mb-0.5">{label}</p>
    <p className={`font-bold text-sm ${good ? 'text-green-400' : warn ? 'text-red-400' : 'text-white'}`}>
      {value ?? 'N/A'}
    </p>
  </div>
);

export default CreditCard;