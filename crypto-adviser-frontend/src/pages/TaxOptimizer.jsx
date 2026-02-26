import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { taxAPI } from '../services/api';
import Navbar from '../components/Navbar';
import {
  Calculator, TrendingUp, AlertTriangle, Sparkles,
  ArrowLeft, CheckCircle, ChevronDown, ChevronUp
} from 'lucide-react';

// ─── Tiny helpers ─────────────────────────────────────────────────────────────
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

const Sel = ({ label, value, onChange, opts }) => (
  <div>
    <label className="block text-sm font-semibold text-gray-300 mb-1">{label}</label>
    <select value={value} onChange={e => onChange(e.target.value)}
      className="w-full px-3 py-2.5 bg-[#1A1F37] border border-white/10 rounded-lg text-white focus:ring-2 focus:ring-yellow-500 outline-none text-sm cursor-pointer">
      {opts.map(o => <option key={o.v} value={o.v}>{o.l}</option>)}
    </select>
  </div>
);

const rupee = (v) => v != null ? `\u20B9${Number(v).toLocaleString('en-IN')}` : '\u20B90';

// ─── Main Component ───────────────────────────────────────────────────────────
const TaxOptimizer = () => {
  const { user } = useAuth();
  const [mode, setMode] = useState('compare');
  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState(null);
  const [error, setError] = useState('');
  const [showAI, setShowAI] = useState(false);

  const [form, setForm] = useState({
    age: '28', grossSalary: '', basicSalary: '', hra: '',
    specialAllowance: '', otherIncome: '0', rentPaid: '0',
    cityType: 'METRO', section80C: '0', section80D: '0',
    section80CCD1B: '0', homeLoanInterest: '0',
    riskAppetite: 'MEDIUM', liquidityNeed: 'MEDIUM', dependents: '0',
  });

  const set = (k) => (v) => setForm(f => ({ ...f, [k]: v }));
  const n = (v) => parseFloat(v) || 0;

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(''); setResult(null); setLoading(true); setShowAI(false);
    try {
      const payload = {
        userId: user?.userId || user?.username,
        age: n(form.age), grossSalary: n(form.grossSalary),
        basicSalary: n(form.basicSalary), hra: n(form.hra), da: 0,
        specialAllowance: n(form.specialAllowance), otherIncome: n(form.otherIncome),
        rentPaid: n(form.rentPaid), cityType: form.cityType,
        section80C: n(form.section80C), section80D: n(form.section80D),
        section80DParents: 0, section80CCD1B: n(form.section80CCD1B),
        homeLoanInterest: n(form.homeLoanInterest), homeLoanPrincipal: 0,
        section80EEA: 0, section80G: 0, section80TTA: 0,
        riskAppetite: form.riskAppetite, liquidityNeed: form.liquidityNeed,
        dependents: n(form.dependents),
      };
      const res = mode === 'compare'
        ? await taxAPI.compareRegimes(payload)
        : await taxAPI.optimize(payload);
      setResult(res.data);
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Something went wrong');
    } finally {
      setLoading(false);
    }
  };

  // Determine which regime is better from result
  const betterRegime = result
    ? (result.recommendedRegime || (result.oldRegimeTax < result.newRegimeTax ? 'OLD' : 'NEW'))
    : null;

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
            <Calculator className="w-7 h-7 text-black" />
          </div>
          <div>
            <h1 className="text-4xl font-bold text-white">Tax Optimizer</h1>
            <p className="text-gray-400">FY 2024-25 &bull; Old vs New Regime &bull; AI Investment Advice</p>
          </div>
        </div>

        {/* Mode Toggle */}
        <div className="flex gap-3 mb-8">
          {[
            { id: 'compare', label: '⚡ Quick Compare (No AI)' },
            { id: 'full',    label: '✨ Full AI Analysis' },
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
              <TrendingUp className="w-5 h-5 text-yellow-400" /> Your Financial Details
            </h2>

            {error && (
              <div className="bg-red-500/10 border border-red-500/30 text-red-400 p-4 rounded-lg mb-6 text-sm flex items-center gap-2">
                <AlertTriangle className="w-5 h-5 flex-shrink-0" /> {error}
              </div>
            )}

            <form onSubmit={handleSubmit}>
              <div className="grid grid-cols-2 gap-4 mb-6">
                <Field label="Age"                    value={form.age}              onChange={set('age')}              ph="28" />
                <Field label="Gross Salary (₹)"       value={form.grossSalary}      onChange={set('grossSalary')}      ph="1200000" />
                <Field label="Basic Salary (₹)"       value={form.basicSalary}      onChange={set('basicSalary')}      ph="480000" />
                <Field label="HRA Received (₹)"       value={form.hra}              onChange={set('hra')}              ph="240000" />
                <Field label="Special Allowance (₹)"  value={form.specialAllowance} onChange={set('specialAllowance')} ph="480000" />
                <Field label="Other Income (₹)"       value={form.otherIncome}      onChange={set('otherIncome')}      ph="0" />
                <Field label="Rent Paid (₹)"          value={form.rentPaid}         onChange={set('rentPaid')}         ph="180000" />
                <Sel   label="City Type"              value={form.cityType}         onChange={set('cityType')}
                  opts={[{ v: 'METRO', l: 'Metro City' }, { v: 'NON_METRO', l: 'Non-Metro' }]} />
                <Field label="80C Investments (₹)"    value={form.section80C}       onChange={set('section80C')}       ph="150000" />
                <Field label="80D Health Insurance (₹)" value={form.section80D}    onChange={set('section80D')}       ph="25000" />
                <Field label="NPS 80CCD(1B) (₹)"      value={form.section80CCD1B}   onChange={set('section80CCD1B')}   ph="50000" />
                <Field label="Home Loan Interest (₹)" value={form.homeLoanInterest} onChange={set('homeLoanInterest')} ph="0" />
                <Field label="Dependents"             value={form.dependents}       onChange={set('dependents')}       ph="0" />
                <Sel   label="Risk Appetite"          value={form.riskAppetite}     onChange={set('riskAppetite')}
                  opts={[{ v: 'LOW', l: 'Low' }, { v: 'MEDIUM', l: 'Medium' }, { v: 'HIGH', l: 'High' }]} />
              </div>

              <button type="submit" disabled={loading}
                className="w-full bg-gradient-to-r from-yellow-400 to-yellow-600 text-black py-4 rounded-lg font-bold text-lg hover:from-yellow-500 hover:to-yellow-700 transition-all disabled:opacity-50 shadow-lg shadow-yellow-500/30 flex items-center justify-center gap-2">
                {loading ? (
                  <><div className="w-5 h-5 border-2 border-black/30 border-t-black rounded-full animate-spin" /> Calculating...</>
                ) : (
                  <><Sparkles className="w-5 h-5" /> {mode === 'compare' ? 'Compare Regimes' : 'Get AI Analysis'}</>
                )}
              </button>
            </form>
          </div>

          {/* ── RIGHT: Results ──────────────────────────────────────────── */}
          <div className="bg-[#141824] rounded-2xl border border-white/10 p-8 shadow-2xl">
            <h2 className="text-xl font-bold text-white mb-6 flex items-center gap-2">
              <Calculator className="w-5 h-5 text-yellow-400" /> Analysis Result
            </h2>

            {loading && (
              <div className="flex flex-col items-center justify-center py-20">
                <div className="relative w-20 h-20 mb-6">
                  <div className="absolute inset-0 border-4 border-yellow-500/20 rounded-full" />
                  <div className="absolute inset-0 border-4 border-yellow-400 border-t-transparent rounded-full animate-spin" />
                </div>
                <p className="text-gray-400 text-lg">Analysing your tax profile...</p>
              </div>
            )}

            {!loading && !result && (
              <div className="flex flex-col items-center justify-center py-20 text-gray-500">
                <div className="w-24 h-24 bg-white/5 rounded-2xl flex items-center justify-center mb-4">
                  <span className="text-5xl">🧾</span>
                </div>
                <p className="text-center text-gray-400 text-lg">
                  Fill the form to see<br />
                  <span className="text-yellow-400 font-semibold">Old vs New Regime comparison</span>
                </p>
              </div>
            )}

            {result && !loading && (
              <div className="space-y-4">

                {/* Recommended Badge */}
                {betterRegime && (
                  <div className="bg-gradient-to-r from-yellow-500/20 to-yellow-600/10 border border-yellow-500/30 rounded-xl p-4 flex items-center gap-3">
                    <CheckCircle className="w-6 h-6 text-yellow-400 flex-shrink-0" />
                    <div>
                      <p className="text-white font-bold text-lg">
                        {betterRegime === 'OLD' ? 'Old Regime' : 'New Regime'} Recommended
                      </p>
                      {result.taxSaving != null && (
                        <p className="text-yellow-300 text-sm">
                          You save {rupee(Math.abs(result.taxSaving))} by choosing this regime
                        </p>
                      )}
                    </div>
                  </div>
                )}

                {/* Regime Comparison Cards */}
                <div className="grid grid-cols-2 gap-4">
                  {/* Old Regime */}
                  <div className={`rounded-xl p-4 border ${betterRegime === 'OLD' ? 'bg-yellow-500/10 border-yellow-500/40' : 'bg-[#1A1F37] border-white/10'}`}>
                    <p className="text-xs text-gray-400 mb-1">Old Regime Tax</p>
                    <p className="text-2xl font-bold text-white">{rupee(result.oldRegimeTax)}</p>
                    {result.oldRegimeNetTaxableIncome != null && (
                      <p className="text-xs text-gray-500 mt-1">Taxable: {rupee(result.oldRegimeNetTaxableIncome)}</p>
                    )}
                    {betterRegime === 'OLD' && <span className="text-xs text-yellow-400 font-bold">✓ BETTER</span>}
                  </div>

                  {/* New Regime */}
                  <div className={`rounded-xl p-4 border ${betterRegime === 'NEW' ? 'bg-yellow-500/10 border-yellow-500/40' : 'bg-[#1A1F37] border-white/10'}`}>
                    <p className="text-xs text-gray-400 mb-1">New Regime Tax</p>
                    <p className="text-2xl font-bold text-white">{rupee(result.newRegimeTax)}</p>
                    {result.newRegimeNetTaxableIncome != null && (
                      <p className="text-xs text-gray-500 mt-1">Taxable: {rupee(result.newRegimeNetTaxableIncome)}</p>
                    )}
                    {betterRegime === 'NEW' && <span className="text-xs text-yellow-400 font-bold">✓ BETTER</span>}
                  </div>
                </div>

                {/* Key Metrics */}
                {result.totalDeductions != null && (
                  <div className="bg-[#1A1F37] rounded-xl p-4 border border-white/10 grid grid-cols-2 gap-3">
                    <Metric label="Total Deductions" value={rupee(result.totalDeductions)} />
                    <Metric label="HRA Exemption"    value={rupee(result.hraExemption)} />
                    <Metric label="Standard Deduction" value={rupee(result.standardDeduction)} />
                    <Metric label="Effective Tax Rate" value={result.effectiveTaxRate ? `${result.effectiveTaxRate.toFixed(2)}%` : 'N/A'} />
                  </div>
                )}

                {/* AI Recommendation — only in full mode */}
                {mode === 'full' && result.aiRecommendation && (
                  <div className="bg-[#1A1F37] rounded-xl border border-white/10 overflow-hidden">
                    <button
                      onClick={() => setShowAI(!showAI)}
                      className="w-full flex items-center justify-between p-4 text-left hover:bg-white/5 transition"
                    >
                      <span className="font-bold text-white flex items-center gap-2">
                        <Sparkles className="w-4 h-4 text-yellow-400" /> AI Recommendations
                      </span>
                      {showAI ? <ChevronUp className="w-4 h-4 text-gray-400" /> : <ChevronDown className="w-4 h-4 text-gray-400" />}
                    </button>
                    {showAI && (
                      <div className="px-4 pb-4">
                        <p className="text-gray-300 text-sm whitespace-pre-line leading-relaxed">
                          {result.aiRecommendation}
                        </p>
                      </div>
                    )}
                  </div>
                )}

                {/* Investment Suggestions */}
                {result.investmentSuggestions && result.investmentSuggestions.length > 0 && (
                  <div className="bg-[#1A1F37] rounded-xl border border-white/10 p-4">
                    <p className="font-bold text-white mb-3 text-sm">📈 Investment Suggestions</p>
                    <ul className="space-y-2">
                      {result.investmentSuggestions.map((s, i) => (
                        <li key={i} className="text-gray-300 text-sm flex items-start gap-2">
                          <span className="text-yellow-400 mt-0.5">•</span> {s}
                        </li>
                      ))}
                    </ul>
                  </div>
                )}

                <p className="text-xs text-gray-600 text-center pt-2">
                  For informational purposes only. Consult a CA for professional advice.
                </p>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

const Metric = ({ label, value }) => (
  <div>
    <p className="text-xs text-gray-500 mb-0.5">{label}</p>
    <p className="text-white font-semibold text-sm">{value}</p>
  </div>
);

export default TaxOptimizer;