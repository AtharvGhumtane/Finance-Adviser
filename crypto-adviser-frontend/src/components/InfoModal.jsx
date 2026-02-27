import { useState } from 'react';
import { X, Brain, Calculator, CreditCard, ChevronDown, ChevronUp, Info } from 'lucide-react';

// ─── Data ─────────────────────────────────────────────────────────────────────

const TAX_INTRO = {
  title: 'Tax Optimizer — How It Works',
  description: `India's Income Tax system gives you a choice every year: Old Regime (with deductions) or New Regime (lower slab rates, fewer deductions). Our AI analyses your full financial profile and tells you which saves more money — plus gives Gemini-powered investment tips to reduce your future tax liability.`,
  regimes: [
    {
      name: 'Old Regime',
      color: 'text-blue-400',
      badge: 'bg-blue-500/10 border-blue-500/30',
      points: [
        'Standard Deduction of ₹50,000',
        'HRA Exemption (if renting)',
        'Section 80C up to ₹1.5L (PPF, ELSS, LIC, etc.)',
        'Section 80D for health insurance',
        'Home Loan Interest deduction (Sec 24b)',
        'Better if you have high deductions',
      ],
    },
    {
      name: 'New Regime',
      color: 'text-yellow-400',
      badge: 'bg-yellow-500/10 border-yellow-500/30',
      points: [
        'Standard Deduction of ₹75,000 (FY 2024-25)',
        'Lower tax slab rates across the board',
        'No HRA, 80C, 80D, home loan deductions',
        'Rebate up to ₹7L income (zero tax)',
        'Simpler — fewer things to track',
        'Better if deductions are low',
      ],
    },
  ],
  sections: [
    {
      title: '👤 Identity & Personal',
      color: 'yellow',
      params: [
        { name: 'Age', why: 'Senior citizens (60+) and super seniors (80+) get higher basic exemption limits under Old Regime.' },
        { name: 'Dependents', why: 'Helps AI suggest family floater health insurance and dependent-related deductions.' },
      ],
    },
    {
      title: '💰 Income Breakdown',
      color: 'green',
      params: [
        { name: 'Gross Salary', why: 'Total CTC before any deductions — the starting point for all tax calculations.' },
        { name: 'Basic Salary', why: 'Typically 40–50% of CTC. Used to calculate HRA exemption and PF contributions.' },
        { name: 'HRA (House Rent Allowance)', why: 'Employer-paid allowance for accommodation. Exempt up to a limit if you pay rent.' },
        { name: 'DA (Dearness Allowance)', why: 'Mostly for govt employees. Affects HRA exemption calculation.' },
        { name: 'Special Allowance', why: 'Fully taxable component. Helps determine your effective tax slab.' },
        { name: 'Other Income', why: 'FD interest, rental income, freelance, etc. Added to salary for total taxable income.' },
      ],
    },
    {
      title: '🏠 HRA & Rent',
      color: 'blue',
      params: [
        { name: 'Rent Paid (Annual)', why: 'HRA exemption = MIN(HRA received, Rent paid − 10% of Basic, 40%/50% of Basic). More rent = more exemption.' },
        { name: 'City Type (Metro / Non-Metro)', why: 'Metro cities (Delhi, Mumbai, Kolkata, Chennai) get 50% of Basic as HRA limit. Non-metro gets 40%.' },
      ],
    },
    {
      title: '📋 Deductions (Old Regime Only)',
      color: 'purple',
      params: [
        { name: 'Section 80C (max ₹1.5L)', why: 'PPF, ELSS mutual funds, LIC premium, 5-yr FD, NSC, children\'s tuition, home loan principal. Most popular deduction.' },
        { name: 'Section 80D — Self (max ₹25K)', why: 'Health insurance premium for self + family. Preventive checkup ₹5K included.' },
        { name: 'Section 80D — Parents (max ₹25K–₹50K)', why: 'Extra deduction for parents\' health insurance. ₹50K if parents are senior citizens.' },
        { name: 'Section 80CCD(1B) — NPS (max ₹50K)', why: 'Additional NPS contribution over 80C. Gives ₹50K extra deduction — effectively reduces tax by ₹15K+ at 30% slab.' },
        { name: 'Section 80EEA — Housing (max ₹1.5L)', why: 'Extra home loan interest deduction for affordable housing (stamp duty ≤ ₹45L).' },
        { name: 'Section 80G — Donations', why: 'Donations to approved funds (PM Relief, etc.) are 50–100% deductible.' },
        { name: 'Section 80TTA — Savings Interest (max ₹10K)', why: 'Interest earned on savings bank account is tax-free up to ₹10,000.' },
        { name: 'Home Loan Interest (Sec 24b, max ₹2L)', why: 'Interest paid on home loan for self-occupied property. One of the biggest deductions available.' },
        { name: 'Home Loan Principal', why: 'Counted under 80C limit. Useful if 80C isn\'t fully utilised by other investments.' },
      ],
    },
    {
      title: '⚙️ Preferences',
      color: 'orange',
      params: [
        { name: 'Risk Appetite (Low / Medium / High)', why: 'AI uses this to recommend suitable tax-saving investments — debt instruments vs ELSS vs NPS.' },
        { name: 'Liquidity Need (Low / Medium / High)', why: 'Low liquidity = AI recommends locked-in instruments (PPF). High = recommends liquid options (ELSS after 3yr).' },
      ],
    },
  ],
};

const CREDIT_INTRO = {
  title: 'Credit Card Analyser — How It Works',
  description: `Credit cards are powerful tools — but they hide 6 common "traps" that silently drain your money. Our ML model classifies your debt risk as LOW / MEDIUM / HIGH, detects which traps you\'ve fallen into, estimates your annual loss, and gives AI-powered steps to escape them.`,
  traps: [
    { name: 'Minimum Payment Trap', desc: 'Paying only the minimum keeps you in debt for years. A ₹50K balance at 36% APR takes 8+ years to clear paying minimums.' },
    { name: 'High Utilisation Trap', desc: 'Using >30% of credit limit hurts your CIBIL score. >50% signals financial stress to lenders.' },
    { name: 'Cash Advance Trap', desc: 'ATM withdrawals from credit cards charge 2.5–3.5% fee immediately + 36–42% APR from day 1. No grace period.' },
    { name: 'Late Payment Trap', desc: 'Late fees (₹500–₹1,300) + interest reversal + CIBIL score drop. One late payment can cost ₹3,000+ annually.' },
    { name: 'EMI Overload Trap', desc: 'Too many EMIs eating into income leaves zero savings buffer. EMI burden >50% of income = financial fragility.' },
    { name: 'Debt-to-Income Trap', desc: 'Total debt obligations >40% of income means any financial shock (job loss, medical) could cause default.' },
  ],
  sections: [
    {
      title: '💵 Income & Expenses',
      color: 'green',
      params: [
        { name: 'Monthly Income', why: 'Base for calculating debt-to-income ratio and EMI burden. How much you earn vs how much you owe.' },
        { name: 'Monthly Expenses', why: 'Fixed living costs. Helps calculate your real free cash flow after all obligations.' },
      ],
    },
    {
      title: '💳 Credit Card Details',
      color: 'yellow',
      params: [
        { name: 'Total Credit Limit', why: 'Sum of all card limits. Used to calculate credit utilisation % — key CIBIL factor.' },
        { name: 'Outstanding Balance', why: 'Total unpaid balance across all cards. Core input for utilisation ratio and interest cost.' },
        { name: 'Number of Cards', why: 'Multiple cards can mean higher total limit — good. But also higher risk of overspending.' },
        { name: 'CIBIL / Credit Score', why: 'Your current score (300–900). Used to project how it changes if you fix detected traps.' },
        { name: 'Annual Interest Rate (APR)', why: 'Most Indian cards charge 36–42% annually. Even small balances compound aggressively.' },
        { name: 'Late Payment Fee', why: 'Per-instance fee charged when you miss payment due date. Often ₹500–₹1,300 per card.' },
      ],
    },
    {
      title: '📅 Payment Behaviour',
      color: 'red',
      params: [
        { name: 'Pays Minimum Only', why: 'Single biggest trap indicator. If YES, interest accrues on full balance — debt never reduces meaningfully.' },
        { name: 'Late Payments (last year)', why: 'Each late payment = fee + CIBIL damage. 3+ late payments = HIGH risk classification.' },
        { name: 'Missed Payments (last year)', why: 'Complete misses trigger 90-day overdue marking on CIBIL — severe long-term damage.' },
      ],
    },
    {
      title: '📦 EMI & Loans',
      color: 'blue',
      params: [
        { name: 'Card EMI per Month', why: 'Card purchases converted to EMI. Added to total debt obligation for burden calculation.' },
        { name: 'Active EMI Count', why: 'Number of running EMIs. High count = limited flexibility in monthly cash flow.' },
        { name: 'Other Loan EMI', why: 'Personal loan, home loan, vehicle loan EMIs. Included in total debt service ratio.' },
      ],
    },
    {
      title: '🏧 Cash Withdrawals',
      color: 'orange',
      params: [
        { name: 'Cash Advance Amount', why: 'ATM withdrawals via credit card. 3.5% fee + interest from day 1 — most expensive form of borrowing.' },
        { name: 'Times Per Month', why: 'Frequency amplifies the cost. Even ₹5K withdrawal per month = ₹2,100+ in annual fees alone.' },
      ],
    },
  ],
};

const CRYPTO_INTRO = {
  title: 'Crypto Adviser — How It Works',
  description: `Our AI analyses your financial profile and risk tolerance to generate personalised cryptocurrency investment recommendations using Gemini AI. It considers market context, your investment horizon, and income to suggest allocation strategies for BTC, ETH, SOL, and more.`,
  disclaimer: '⚠️ Crypto investments are highly volatile. This is educational AI guidance, not financial advice. Never invest more than you can afford to lose.',
  sections: [
    {
      title: '💰 Financial Profile',
      color: 'yellow',
      params: [
        { name: 'Annual Income (USD)', why: 'Determines what % of income is reasonable to invest. Higher income = AI can suggest larger absolute allocations while staying within safe % limits.' },
      ],
    },
    {
      title: '📊 Risk & Strategy',
      color: 'blue',
      params: [
        { name: 'Risk Tolerance (1–10)', why: '1–3 = Conservative (BTC/ETH only, DCA strategy). 4–6 = Moderate (mix of large + mid caps). 7–10 = Aggressive (includes smaller altcoins, higher allocation %). AI tailors its recommendation to this score.' },
        { name: 'Investment Horizon', why: 'Short-term (<1yr): AI avoids illiquid assets, focuses on timing. Medium (1–3yr): Balance of growth + stability. Long-term (>3yr): AI can suggest DCA into higher-risk assets that need time to mature.' },
        { name: 'Target Cryptocurrency', why: 'The coin you\'re most interested in. AI still evaluates if it matches your profile — it may suggest diversifying or adjusting allocation based on your risk score.' },
      ],
    },
    {
      title: '📝 Context',
      color: 'green',
      params: [
        { name: 'Additional Context', why: 'Tell the AI anything relevant: "I already hold BTC", "I want to start small", "I\'m worried about regulation", etc. Gemini uses this to personalise the recommendation further.' },
      ],
    },
    {
      title: '📰 Live News Feed',
      color: 'purple',
      params: [
        { name: 'News Title & Body', why: 'Fetched from crypto news APIs. Shows you what\'s happening in the market right now — helps you make informed decisions before asking for AI advice.' },
        { name: 'Source & Source URL', why: 'Origin of the news article. Always verify big claims with the original source before acting.' },
        { name: 'Related Cryptos', why: 'Tags showing which coins the article is most relevant to (BTC, ETH, SOL, etc.).' },
        { name: 'Published At', why: 'Recency matters a lot in crypto. A 3-day-old article about a hack may already be resolved.' },
      ],
    },
  ],
};

// ─── Sub-components ───────────────────────────────────────────────────────────

const ParamRow = ({ name, why }) => {
  const [open, setOpen] = useState(false);
  return (
    <div
      className="border border-white/8 rounded-lg overflow-hidden cursor-pointer"
      onClick={() => setOpen(o => !o)}
    >
      <div className="flex items-center justify-between px-4 py-3 bg-white/3 hover:bg-white/6 transition">
        <span className="text-sm font-semibold text-white">{name}</span>
        {open
          ? <ChevronUp className="w-4 h-4 text-gray-500 flex-shrink-0" />
          : <ChevronDown className="w-4 h-4 text-gray-500 flex-shrink-0" />}
      </div>
      {open && (
        <div className="px-4 py-3 bg-[#0A0E27] border-t border-white/8">
          <p className="text-sm text-gray-400 leading-relaxed">{why}</p>
        </div>
      )}
    </div>
  );
};

const SECTION_COLORS = {
  yellow:  'text-yellow-400 bg-yellow-500/10 border-yellow-500/20',
  green:   'text-emerald-400 bg-emerald-500/10 border-emerald-500/20',
  blue:    'text-blue-400 bg-blue-500/10 border-blue-500/20',
  purple:  'text-purple-400 bg-purple-500/10 border-purple-500/20',
  orange:  'text-orange-400 bg-orange-500/10 border-orange-500/20',
  red:     'text-red-400 bg-red-500/10 border-red-500/20',
};

const Section = ({ title, color, params }) => (
  <div className="mb-6">
    <div className={`inline-flex items-center gap-2 text-xs font-bold px-3 py-1.5 rounded-full border mb-3 ${SECTION_COLORS[color] || SECTION_COLORS.yellow}`}>
      {title}
    </div>
    <div className="space-y-2">
      {params.map(p => <ParamRow key={p.name} {...p} />)}
    </div>
  </div>
);

// ─── Tab Content ──────────────────────────────────────────────────────────────

const TaxContent = () => (
  <div>
    {/* Intro */}
    <div className="bg-[#1A1F37] border border-white/10 rounded-xl p-5 mb-6">
      <p className="text-gray-300 text-sm leading-relaxed">{TAX_INTRO.description}</p>
    </div>

    {/* Regime comparison */}
    <div className="grid grid-cols-2 gap-4 mb-6">
      {TAX_INTRO.regimes.map(r => (
        <div key={r.name} className={`bg-[#1A1F37] border rounded-xl p-4 ${r.badge}`}>
          <h4 className={`font-bold text-sm mb-3 ${r.color}`}>{r.name}</h4>
          <ul className="space-y-1.5">
            {r.points.map(p => (
              <li key={p} className="text-xs text-gray-400 flex items-start gap-2">
                <span className={`mt-0.5 flex-shrink-0 ${r.color}`}>•</span> {p}
              </li>
            ))}
          </ul>
        </div>
      ))}
    </div>

    {/* Parameters */}
    <h3 className="text-white font-bold text-sm mb-4 flex items-center gap-2">
      <Info className="w-4 h-4 text-yellow-400" /> Parameter Guide
      <span className="text-xs text-gray-500 font-normal">(click any parameter to see why we need it)</span>
    </h3>
    {TAX_INTRO.sections.map(s => <Section key={s.title} {...s} />)}
  </div>
);

const CreditContent = () => (
  <div>
    <div className="bg-[#1A1F37] border border-white/10 rounded-xl p-5 mb-6">
      <p className="text-gray-300 text-sm leading-relaxed">{CREDIT_INTRO.description}</p>
    </div>

    {/* 6 Traps */}
    <h3 className="text-white font-bold text-sm mb-3 flex items-center gap-2">
      ⚠️ The 6 Credit Card Traps We Detect
    </h3>
    <div className="grid grid-cols-1 gap-2 mb-6">
      {CREDIT_INTRO.traps.map((t, i) => (
        <div key={t.name} className="flex gap-3 bg-red-500/5 border border-red-500/15 rounded-lg px-4 py-3">
          <span className="text-red-400 font-bold text-sm flex-shrink-0">{i + 1}.</span>
          <div>
            <span className="text-red-300 font-semibold text-sm">{t.name}</span>
            <p className="text-gray-500 text-xs mt-0.5 leading-relaxed">{t.desc}</p>
          </div>
        </div>
      ))}
    </div>

    <h3 className="text-white font-bold text-sm mb-4 flex items-center gap-2">
      <Info className="w-4 h-4 text-yellow-400" /> Parameter Guide
      <span className="text-xs text-gray-500 font-normal">(click any parameter to see why we need it)</span>
    </h3>
    {CREDIT_INTRO.sections.map(s => <Section key={s.title} {...s} />)}
  </div>
);

const CryptoContent = () => (
  <div>
    <div className="bg-[#1A1F37] border border-white/10 rounded-xl p-5 mb-4">
      <p className="text-gray-300 text-sm leading-relaxed">{CRYPTO_INTRO.description}</p>
    </div>
    <div className="bg-orange-500/10 border border-orange-500/20 rounded-xl px-4 py-3 mb-6">
      <p className="text-orange-300 text-xs leading-relaxed">{CRYPTO_INTRO.disclaimer}</p>
    </div>

    <h3 className="text-white font-bold text-sm mb-4 flex items-center gap-2">
      <Info className="w-4 h-4 text-yellow-400" /> Parameter Guide
      <span className="text-xs text-gray-500 font-normal">(click any parameter to see why we need it)</span>
    </h3>
    {CRYPTO_INTRO.sections.map(s => <Section key={s.title} {...s} />)}
  </div>
);

// ─── Main Modal ───────────────────────────────────────────────────────────────

const TABS = [
  { id: 'tax',    label: 'Tax Optimizer',   icon: <Calculator className="w-4 h-4" />,  content: <TaxContent /> },
  { id: 'credit', label: 'Credit Analyser', icon: <CreditCard className="w-4 h-4" />,  content: <CreditContent /> },
  { id: 'crypto', label: 'Crypto Adviser',  icon: <Brain className="w-4 h-4" />,       content: <CryptoContent /> },
];

export const InfoModal = ({ onClose }) => {
  const [active, setActive] = useState('tax');
  const tab = TABS.find(t => t.id === active);

  return (
    <div
      className="fixed inset-0 bg-black/80 backdrop-blur-sm z-[100] flex items-center justify-center p-4"
      onClick={onClose}
    >
      <div
        className="bg-[#141824] border border-white/10 rounded-2xl w-full max-w-2xl max-h-[88vh] flex flex-col shadow-2xl"
        onClick={e => e.stopPropagation()}
      >
        {/* Header */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-white/10 flex-shrink-0">
          <div className="flex items-center gap-3">
            <div className="w-8 h-8 rounded-lg bg-yellow-500/10 flex items-center justify-center">
              <Info className="w-4 h-4 text-yellow-400" />
            </div>
            <div>
              <h2 className="text-white font-bold text-base">Service Guide</h2>
              <p className="text-gray-500 text-xs">Understand every parameter before you fill the form</p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="w-8 h-8 flex items-center justify-center rounded-lg bg-white/5 hover:bg-white/10 text-gray-400 hover:text-white transition"
          >
            <X className="w-4 h-4" />
          </button>
        </div>

        {/* Tabs */}
        <div className="flex gap-2 px-6 pt-4 flex-shrink-0">
          {TABS.map(t => (
            <button
              key={t.id}
              onClick={() => setActive(t.id)}
              className={`flex items-center gap-2 px-4 py-2 rounded-lg text-sm font-semibold transition-all ${
                active === t.id
                  ? 'bg-gradient-to-r from-yellow-400 to-yellow-600 text-black shadow-lg shadow-yellow-500/20'
                  : 'bg-white/5 text-gray-400 border border-white/10 hover:bg-white/10 hover:text-white'
              }`}
            >
              {t.icon} {t.label}
            </button>
          ))}
        </div>

        {/* Scrollable content */}
        <div className="overflow-y-auto flex-1 px-6 py-5">
          {tab.content}
        </div>
      </div>
    </div>
  );
};