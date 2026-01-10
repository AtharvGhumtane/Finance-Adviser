import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { TrendingUp, Lock, User } from 'lucide-react';

const Login = () => {
  const [emailOrUsername, setEmailOrUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const { login } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    console.log('🚀 Form submitted');
    console.log('📧 Email/Username:', emailOrUsername);
    console.log('🔑 Password length:', password.length);

    try {
      console.log('⏳ Calling login...');
      const result = await login(emailOrUsername, password);
      console.log('✅ Login successful, result:', result);
      
      console.log('🧭 Navigating to dashboard...');
      navigate('/dashboard');
      console.log('✅ Navigation completed');
    } catch (err) {
      console.error('❌ Login failed:', err);
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-[#0A0E27] via-[#141824] to-[#0A0E27] flex items-center justify-center py-12 px-4 relative overflow-hidden">
      {/* Animated Background Effects */}
      <div className="absolute inset-0 overflow-hidden">
        <div className="absolute top-1/4 left-1/4 w-96 h-96 bg-yellow-500/10 rounded-full blur-3xl animate-pulse"></div>
        <div className="absolute bottom-1/4 right-1/4 w-96 h-96 bg-blue-500/10 rounded-full blur-3xl animate-pulse"></div>
      </div>

      <div className="max-w-md w-full relative z-10">
        <div className="bg-[#141824] border border-white/10 rounded-2xl shadow-2xl p-8 backdrop-blur-lg">
          {/* Header */}
          <div className="text-center mb-8">
            <div className="inline-flex items-center justify-center w-16 h-16 bg-gradient-to-br from-yellow-400 to-yellow-600 rounded-2xl mb-4 shadow-lg shadow-yellow-500/20">
              <TrendingUp className="w-8 h-8 text-black" />
            </div>
            <h2 className="text-3xl font-bold text-white mb-2">Welcome Back</h2>
            <p className="text-gray-400">Sign in to your Crypto Adviser account</p>
          </div>

          {error && (
            <div className="bg-red-500/10 border border-red-500/30 text-red-400 p-3 rounded-lg mb-6 text-sm flex items-center gap-2">
              <span>⚠️</span>
              <span>{error}</span>
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-5">
            {/* Email or Username */}
            <div>
              <label className="block text-sm font-medium text-gray-300 mb-2">
                Email or Username
              </label>
              <div className="relative">
                <User className="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-gray-500" />
                <input
                  type="text"
                  value={emailOrUsername}
                  onChange={(e) => setEmailOrUsername(e.target.value)}
                  className="w-full pl-11 pr-4 py-3 bg-[#1A1F37] border border-white/10 rounded-lg text-white placeholder-gray-500 focus:ring-2 focus:ring-yellow-500 focus:border-transparent outline-none transition"
                  placeholder="Enter your email or username"
                  required
                />
              </div>
            </div>

            {/* Password */}
            <div>
              <label className="block text-sm font-medium text-gray-300 mb-2">
                Password
              </label>
              <div className="relative">
                <Lock className="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-gray-500" />
                <input
                  type="password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  className="w-full pl-11 pr-4 py-3 bg-[#1A1F37] border border-white/10 rounded-lg text-white placeholder-gray-500 focus:ring-2 focus:ring-yellow-500 focus:border-transparent outline-none transition"
                  placeholder="Enter your password"
                  required
                />
              </div>
            </div>

            {/* Submit Button */}
            <button
              type="submit"
              disabled={loading}
              className="w-full bg-gradient-to-r from-yellow-400 to-yellow-600 text-black py-3 rounded-lg font-bold text-lg hover:from-yellow-500 hover:to-yellow-700 transition-all disabled:opacity-50 disabled:cursor-not-allowed shadow-lg shadow-yellow-500/30 transform hover:scale-[1.02]"
            >
              {loading ? (
                <span className="flex items-center justify-center gap-2">
                  <div className="w-5 h-5 border-2 border-black/30 border-t-black rounded-full animate-spin"></div>
                  Signing in...
                </span>
              ) : (
                '🚀 Sign In'
              )}
            </button>
          </form>

          {/* Sign Up Link */}
          <p className="text-center mt-6 text-gray-400">
            Don't have an account?{' '}
            <Link
              to="/signup"
              className="text-yellow-400 font-semibold hover:text-yellow-300 transition"
            >
              Sign up
            </Link>
          </p>
        </div>

        {/* Footer Text */}
        <p className="text-center mt-6 text-gray-500 text-sm">
          Secure AI-powered crypto investment advice
        </p>
      </div>
    </div>
  );
};

export default Login;