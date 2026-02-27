import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { TrendingUp, User, LogOut, History, LayoutDashboard, Calculator, CreditCard, Info } from 'lucide-react';
import { InfoModal } from './InfoModal';

const Navbar = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [showInfo, setShowInfo] = useState(false);

  const handleLogout = () => { navigate('/'); logout(); };

  return (
    <>
      <nav className="bg-[#0A0E27] border-b border-yellow-500/20 shadow-lg backdrop-blur-lg sticky top-0 z-50">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center h-16">

            {/* Logo */}
            <Link to="/dashboard" className="flex items-center space-x-3 group">
              <div className="w-10 h-10 rounded-lg bg-gradient-to-br from-yellow-400 to-yellow-600 flex items-center justify-center transform group-hover:scale-110 transition-transform shadow-lg shadow-yellow-500/30">
                <TrendingUp className="w-5 h-5 text-black" />
              </div>
              <div>
                <span className="text-xl font-bold bg-gradient-to-r from-yellow-400 to-yellow-200 bg-clip-text text-transparent">
                  FinAdvisor
                </span>
                <div className="text-[10px] text-gray-400 -mt-1">AI-Powered Finance</div>
              </div>
            </Link>

            {user && (
              <div className="flex items-center space-x-1">
                <NavLink to="/dashboard" icon={<LayoutDashboard className="w-4 h-4" />}>Dashboard</NavLink>
                <NavLink to="/history"   icon={<History className="w-4 h-4" />}>Crypto</NavLink>
                <NavLink to="/tax"       icon={<Calculator className="w-4 h-4" />}>Tax</NavLink>
                <NavLink to="/credit"    icon={<CreditCard className="w-4 h-4" />}>Credit</NavLink>
                <NavLink to="/profile"   icon={<User className="w-4 h-4" />}>Profile</NavLink>

                {/* ── Info Button ── */}
                <button
                  onClick={() => setShowInfo(true)}
                  className="flex items-center gap-2 px-4 py-2 text-gray-300 hover:text-yellow-400 hover:bg-white/5 rounded-lg transition-all group"
                >
                  <span className="group-hover:scale-110 transition-transform">
                    <Info className="w-4 h-4" />
                  </span>
                  <span className="text-sm font-medium">Guide</span>
                </button>

                {/* ── User + Logout ── */}
                <div className="ml-4 pl-4 border-l border-white/10 flex items-center space-x-3">
                  <div className="flex items-center space-x-2 px-3 py-2 bg-white/5 rounded-lg border border-white/10">
                    <div className="w-8 h-8 rounded-full bg-gradient-to-br from-yellow-400 to-yellow-600 flex items-center justify-center">
                      <User className="w-4 h-4 text-black" />
                    </div>
                    <span className="text-sm font-medium text-white">{user.username}</span>
                  </div>
                  <button
                    onClick={handleLogout}
                    className="px-4 py-2 bg-red-500/10 hover:bg-red-500/20 text-red-400 rounded-lg border border-red-500/30 transition-all flex items-center gap-2 font-medium"
                  >
                    <LogOut className="w-4 h-4" />
                    <span className="text-sm">Logout</span>
                  </button>
                </div>
              </div>
            )}
          </div>
        </div>
      </nav>

      {/* Info Modal */}
      {showInfo && <InfoModal onClose={() => setShowInfo(false)} />}
    </>
  );
};

const NavLink = ({ to, icon, children }) => (
  <Link
    to={to}
    className="flex items-center gap-2 px-4 py-2 text-gray-300 hover:text-yellow-400 hover:bg-white/5 rounded-lg transition-all group"
  >
    <span className="group-hover:scale-110 transition-transform">{icon}</span>
    <span className="text-sm font-medium">{children}</span>
  </Link>
);

export default Navbar;